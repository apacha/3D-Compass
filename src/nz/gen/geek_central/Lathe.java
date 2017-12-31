package nz.gen.geek_central;

import nz.gen.geek_central.GLUseful.GLUseful;


public class Lathe
  {
    public interface VertexFunc
      {
        public Vec3f Get
          (
            int PointIndex
          );
      }

    public interface VectorFunc
      {
        public Vec3f Get
          (
            int PointIndex,
            int SectorIndex, /* 0 .. NrSectors - 1 */
            boolean Upper
              /* indicates which of two calls for each point (except for
                start and end points, which only get one call each) to allow
                for discontiguous shading */
          );
      } /*VectorFunc*/

    public interface ColorFunc
      {
        public GLUseful.Color Get
          (
            int PointIndex,
            int SectorIndex, /* 0 .. NrSectors - 1 */
            boolean Upper
              /* indicates which of two calls for each point (except for
                start and end points, which only get one call each) to allow
                for discontiguous shading */
          );
      } /*ColorFunc*/

    public static GeomBuilder.Obj Make
      (
        boolean Shaded, /* false for wireframe */
        VertexFunc Point,
          /* returns outline of object, must be at least 3 points, all Z coords must be
            zero, all X coords non-negative, and X coord of first and last point
            must be zero. Order the points by decreasing Y coord if you want
            anticlockwise face vertex ordering, or increasing if you want clockwise. */
        int NrPoints, /* valid values for arg to Point.Get are [0 .. NrPoints - 1] */
        VectorFunc Normal, /* optional to compute normal vector at each point */
        VectorFunc TexCoord, /* optional to compute texture coordinate at each point */
        ColorFunc VertexColor, /* optional to compute colour at each point */
        int NrSectors, /* must be at least 3 */
        GLUseful.ShaderVarDef[] Uniforms,
          /* optional additional uniform variable definitions for vertex shader */
        String VertexColorCalc,
          /* optional, compiled as part of vertex shader to implement lighting etc, must
            assign value to "frag_color" variable */
        boolean BindNow
          /* true to do GL calls now, false to defer to later call to Bind or Draw */
      )
      /* rotates Points around Y axis with NrSectors steps, invoking the
        specified callbacks to obtain normal vectors, texture coordinates
        and vertex colours as appropriate, and returns the constructed
        geometry object. */
      {
        final GeomBuilder Geom = new GeomBuilder
          (
            /*Shaded =*/ Shaded,
            /*GotNormals =*/ Normal != null,
            /*GotTexCoords =*/ TexCoord != null,
            /*GotColors =*/ VertexColor != null
          );
        final int[] PrevInds = new int[NrPoints * 2 - 2];
        final int[] FirstInds = new int[NrPoints * 2 - 2];
        final int[] TheseInds = new int[NrPoints * 2 - 2];
        for (int i = 0;;)
          {
            if (i < NrSectors)
              {
                final float Angle = (float)(2.0 * Math.PI * i / NrSectors);
                final float Cos = (float)Math.cos(Angle);
                final float Sin = (float)Math.sin(Angle);
                for (int j = 0; j < NrPoints; ++j)
                  {
                    final Vec3f Vertex = Point.Get(j);
                    final Vec3f ThisPoint = new Vec3f
                      (
                        Vertex.x * Cos,
                        Vertex.y,
                        Vertex.x * Sin
                      );
                    if (j < NrPoints - 1)
                      {
                        final Vec3f ThisNormal =
                            Normal != null ?
                                Normal.Get(j, i, true)
                            :
                                null;
                        final Vec3f ThisTexCoord =
                            TexCoord != null ?
                                TexCoord.Get(j, i, true)
                            :
                                null;
                        final GLUseful.Color ThisColor =
                            VertexColor != null ?
                                VertexColor.Get(j, i, true)
                            :
                                null;
                        TheseInds[j * 2] =
                            Geom.Add(ThisPoint, ThisNormal, ThisTexCoord, ThisColor);
                      } /*if*/
                    if (j > 0)
                      {
                        final Vec3f ThisNormal =
                            Normal != null ?
                                Normal.Get(j, i, false)
                            :
                                null;
                        final Vec3f ThisTexCoord =
                            TexCoord != null ?
                                TexCoord.Get(j, i, false)
                            :
                                null;
                        final GLUseful.Color ThisColor =
                            VertexColor != null ?
                                VertexColor.Get(j, i, false)
                            :
                                null;
                        TheseInds[j * 2 - 1] =
                            Geom.Add(ThisPoint, ThisNormal, ThisTexCoord, ThisColor);
                      } /*if*/
                  } /*for*/
              }
            else
              {
                for (int j = 0; j < TheseInds.length; ++j)
                  {
                    TheseInds[j] = FirstInds[j];
                  } /*for*/
              } /*if*/
            if (i != 0)
              {
                Geom.AddTri(PrevInds[1], TheseInds[0], TheseInds[1]);
                for (int j = 1; j < NrPoints - 1; ++j)
                  {
                    Geom.AddQuad
                      (
                        PrevInds[j * 2 + 1],
                        PrevInds[j * 2],
                        TheseInds[j * 2],
                        TheseInds[j * 2 + 1]
                      );
                  } /*for*/
                Geom.AddTri
                  (
                    PrevInds[TheseInds.length - 2],
                    TheseInds[TheseInds.length - 2],
                    TheseInds[TheseInds.length - 1]
                  );
              }
            else
              {
                for (int j = 0; j < TheseInds.length; ++j)
                  {
                    FirstInds[j] = TheseInds[j];
                  } /*for*/
              } /*if*/
            for (int j = 0; j < TheseInds.length; ++j)
              {
                PrevInds[j] = TheseInds[j];
              } /*for*/
            if (i == NrSectors)
                break;
            ++i;
          } /*for*/
        return 
            Geom.MakeObj(Uniforms, VertexColorCalc, BindNow);
      } /*Make*/

  } /*Lathe*/;
