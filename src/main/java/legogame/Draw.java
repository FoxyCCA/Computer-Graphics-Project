package legogame;

import com.jogamp.opengl.GL2;

public class Draw {
    public static void cube(GL2 gl2){
        cube(gl2, 1, true);
    }
    public static void slab(GL2 gl2){
        slab(gl2, 1, true);
    }
    public static void ramp(GL2 gl2){
        ramp(gl2, 1, true);
    }
    public static void rampCorner(GL2 gl2){
        rampCorner(gl2, 1, true);
    }

    public static void cylinder(GL2 gl2){
        gl2.glPushMatrix();
        gl2.glTranslated(0f, 0.5f, 0f);
        gl2.glRotatef(90, 1, 0, 0);
        cylinder(gl2, 0.5, 1, 16, 2, 3, true);
        gl2.glPopMatrix();
    }

    public static void rectangularPyramid(GL2 gl){
        rectangularPyramid(gl, 1, true);
    }

    public static void pole(GL2 gl) {
        gl.glPushMatrix();
        gl.glScalef(0.2f, 1f, 0.2f);
        cube(gl, 1, true);
        gl.glPopMatrix();
    }

    public static void carpet(GL2 gl2) {
        gl2.glPushMatrix();
        gl2.glScalef(1f, 10f, 1f);
        gl2.glTranslated(0f, -0.045f, 0f);
        platform(gl2, 1, true);
        gl2.glPopMatrix();
    }

    public static void platform(GL2 gl2){
        platform(gl2, 21, true);
    }

    private static void rectangularPyramid(GL2 gl, int side, boolean makeTextureCoordinate){
        gl.glPushMatrix();

        // draw the four triangles
        // front
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glNormal3f(0, 0.5f, 1);
        gl.glVertex3f(0.0f, 0.5f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glEnd();
        gl.glPopMatrix();

        // right
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glNormal3f(1, 0.5f, 0);
        gl.glVertex3f(0.0f, 0.5f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, 0.5f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glEnd();
        gl.glPopMatrix();

        // left
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glNormal3f(-1, 0.5f, 1);
        gl.glVertex3f(0.0f, 0.5f, 0.0f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);
        gl.glVertex3f(-0.5f, -0.5f, 0.5f);
        gl.glEnd();
        gl.glPopMatrix();

        // back
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_TRIANGLES);
        gl.glNormal3f(0, 0.5f, -1);
        gl.glVertex3f(0.0f, 0.5f, 0.0f);
        gl.glVertex3f(0.5f, -0.5f, -0.5f);
        gl.glVertex3f(-0.5f, -0.5f, -0.5f);
        gl.glEnd();
        gl.glPopMatrix();


        // the bottom square
        gl.glPushMatrix();
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, (float)1/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    private static void cylinder(GL2 gl, double radius, double height, int slices, int stacks, int rings,
                                 boolean makeTextureCoordinates){
        if(radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        if(height <= 0) throw new IllegalArgumentException("Height must be positive");
        if(slices < 3) throw new IllegalArgumentException("Number of slices must be at least 3.");
        if(stacks < 2) throw new IllegalArgumentException("Number of stacks must be at least 2.");

        // body
        for(int j = 0; j < stacks; j++){
            double z1 = (height / stacks) * j;
            double z2 = (height / stacks) * (j+1);
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for(int i = 0; i <= slices; i++){
                double longitude = (2 * Math.PI / slices) * i;
                double sinLongitude = Math.sin(longitude);
                double cosineLongitude = Math.cos(longitude);
                double x = cosineLongitude;
                double y = sinLongitude;
                gl.glNormal3d(x, y, 0);
                if(makeTextureCoordinates){
                    gl.glTexCoord2d(1.0 / slices * i, 1.0 / stacks * (j+1));
                }
                gl.glVertex3d(radius*x, radius*y, z2);
                if(makeTextureCoordinates){
                    gl.glTexCoord2d(1.0 / slices * i, 1.0 / stacks * j);
                }
                gl.glVertex3d(radius * x, radius*y, z1);
            }
            gl.glEnd();
        }

        // draw the top and bottom
        if(rings > 0){
            gl.glNormal3d(0, 0, 1);
            for (int j=0; j<rings; j++){
                double d1 = (1.0 / rings) * j;
                double d2 = (1.0 / rings) * (j+1);
                gl.glBegin(GL2.GL_QUAD_STRIP);
                for (int i = 0; i <= slices; i++) {
                    double angle = (2* Math.PI / slices) * i;
                    double sin = Math.sin(angle);
                    double cosine = Math.cos(angle);
                    if(makeTextureCoordinates){
                        gl.glTexCoord2d(1 * (1 + cosine * d1), 0.5 * (1 + sin * d1));
                    }
                    gl.glVertex3d(radius * cosine * d1, radius * sin * d1, height);

                    if(makeTextureCoordinates){
                        gl.glTexCoord2d(1 * (1 + cosine * d2), 0.5 * (1 + sin * d2));
                    }
                    gl.glVertex3d(radius * cosine * d2, radius * sin * d2, height);
                }
                gl.glEnd();
            }
            gl.glNormal3d(0, 0, -1);

            for (int j=0; j<rings; j++){
                double d1 = (1.0 / rings) * j;
                double d2 = (1.0 / rings) * (j+1);
                gl.glBegin(GL2.GL_QUAD_STRIP);
                for (int i = 0; i <= slices; i++) {
                    double angle = (2* Math.PI / slices) * i;
                    double sin = Math.sin(angle);
                    double cosine = Math.cos(angle);
                    if(makeTextureCoordinates){
                        gl.glTexCoord2d(0.5 * (1 + cosine * d2), 0.5 * (1 + sin * d2));
                    }
                    gl.glVertex3d(radius * cosine * d2, radius * sin * d2, 0);

                    if(makeTextureCoordinates){
                        gl.glTexCoord2d(0.5 * (1 + cosine * d1), 0.5 * (1 + sin * d1));
                    }
                    gl.glVertex3d(radius * cosine * d1, radius * sin * d1, 0);
                }
                gl.glEnd();
            }
        }
    }

    private static void platform(GL2 gl, double side, boolean makeTextureCoordinate){
        gl.glPushMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(0, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(90, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(180, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(270, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.01f, 1f);
        gl.glRotatef(-90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    private static void ramp(GL2 gl, double side, boolean makeTextureCoordinate){
        // push the current matrix down in the stack
        gl.glPushMatrix();

        gl.glPushMatrix();
        gl.glRotatef(0, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        triangle(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(90, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glRotatef(180, 0, 1, 0);
        gl.glRotatef(-90, 0, 0, 1);
        gl.glTranslated(0, 0, side/2);
        triangle(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glNormal3f(-2, 2, 0);
        gl.glBegin(GL2.GL_QUADS);


        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,0);
        }
        gl.glVertex3f(-0.5f,-0.5f,-0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,1);
        }
        gl.glVertex3f(-0.5f,-0.5f,0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(1,1);
        }
        gl.glVertex3f(0.5f,0.5f,0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(1,0);
        }
        gl.glVertex3f(0.5f,0.5f,-0.5f);

        gl.glEnd();
        gl.glPopMatrix();


        gl.glPushMatrix();
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    private static void rampCorner(GL2 gl, double side, boolean makeTextureCoordinate){
        // push the current matrix down in the stack
        gl.glPushMatrix();

        //front
        gl.glPushMatrix();
        gl.glNormal3f(0, 1, 1);
        gl.glBegin(GL2.GL_TRIANGLES);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,0);
        }
        gl.glVertex3f(-0.5f,-0.5f,0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,1);
        }
        gl.glVertex3f(0.5f,-0.5f,0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(1,1);
        }
        gl.glVertex3f(0.5f,0.5f,-0.5f);


        gl.glEnd();
        gl.glPopMatrix();

        //right
        gl.glPushMatrix();
        gl.glRotatef(90, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        triangle(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        //back
        gl.glPushMatrix();
        gl.glRotatef(180, 0, 1, 0);
        gl.glRotatef(-90, 0, 0, 1);
        gl.glTranslated(0, 0, side/2);
        triangle(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        // left
        gl.glPushMatrix();
        gl.glNormal3f(-1, 1, 0);
        gl.glBegin(GL2.GL_TRIANGLES);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,0);
        }
        gl.glVertex3f(-0.5f,-0.5f,-0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(0,1);
        }
        gl.glVertex3f(-0.5f,-0.5f,0.5f);

        if(makeTextureCoordinate){
            gl.glTexCoord2d(1,1);
        }
        gl.glVertex3f(0.5f,0.5f,-0.5f);


        gl.glEnd();
        gl.glPopMatrix();

        //bottom
        gl.glPushMatrix();
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    private static void cube(GL2 gl, double side, boolean makeTextureCoordinate){
        // push the current matrix down in the stack
        gl.glPushMatrix();

        //front
        gl.glPushMatrix();
        gl.glRotatef(0, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        //right
        gl.glPushMatrix();
        gl.glRotatef(90, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        //back
        gl.glPushMatrix();
        gl.glRotatef(180, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        // left
        gl.glPushMatrix();
        gl.glRotatef(270, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        //bottom
        gl.glPushMatrix();
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        //top
        gl.glPushMatrix();
        gl.glRotatef(-90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    private static void slab(GL2 gl, double side, boolean makeTextureCoordinate){
        // push the current matrix down in the stack
        gl.glPushMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(0, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(90, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(180, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(270, 0, 1, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glScalef(1f, 0.5f, 1f);
        gl.glRotatef(-90, 1, 0, 0);
        gl.glTranslated(0, 0, side/2);
        square(gl, side, makeTextureCoordinate);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }

    private static void triangle(GL2 gl, double side, boolean makeTextureCoordinate) {
        double radius = side / 2;
        gl.glBegin(GL2.GL_TRIANGLES);

        // vector for lighting calculation
        gl.glNormal3f(0, 0, 1);

        // bottom left corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(0, 0);
        }
        gl.glVertex2d(-radius, -radius);

        // bottom right corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(1, 0);
        }
        gl.glVertex2d(radius, -radius);

        // top right corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(1, 1);
        }
        gl.glVertex2d(radius, radius);

        gl.glEnd();
    }// draw a square in the (x,y) plane, with given side length
    public static void square(GL2 gl, double side, boolean makeTextureCoordinate) {
        double radius = side / 2;
        gl.glBegin(GL2.GL_POLYGON);

        // vector for lighting calculation
        gl.glNormal3f(0, 0, -2);

        // top left corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(0, 1);
        }
        gl.glVertex2d(-radius, radius);

        // top right corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(1, 1);
        }
        gl.glVertex2d(radius, radius);

        // bottom right corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(1, 0);
        }
        gl.glVertex2d(radius, -radius);

        // bottom left corner of a square
        if(makeTextureCoordinate){
            gl.glTexCoord2d(0, 0);
        }
        gl.glVertex2d(-radius, -radius);


        gl.glEnd();
    }
}
