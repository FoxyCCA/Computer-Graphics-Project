package legogame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

public class Camera {

    // look from camera (X,Y,Z) - Position
    private double eyeX = 0;
    private double eyeY = 0;
    private double eyeZ = 0;

    // look at the origin - Direction
    private double refX = 0;
    private double refY = 0;
    private double refZ = 0;

    // position Y up vector (roll of the camera)
    private double upX = 0;
    private double upY = 0;
    private double upZ = 0;

    private double xMinRequested = -5;
    private double xMaxRequested = 5;
    private double yMinRequested = -5;
    private double yMaxRequested = 5;
    private double zMinRequested = -5;
    private double zMaxRequested = 5;

    private double xMinActual = 0;
    private double xMaxActual = 0;
    private double yMinActual = 0;
    private double yMaxActual = 0;

    // views
    private boolean orthographic = false;
    private boolean preserveAspect = true;

    // access openGL utilities libraries like volume,...
    private GLU glu;

    // sets the limit of the view scene
    private void setLimit(double xMin, double xMax,
                          double yMin, double yMax,
                          double zMin, double zMax){
        // set the limit of our viewport
        xMinRequested = xMin;
        xMaxRequested = xMax;
        yMinRequested = yMin;
        yMaxRequested = yMax;
        zMinRequested = zMin;
        zMaxRequested = zMax;
        glu = new GLU();
    }

    // a convenient method for calling setLimit()
    public void setScale(double limit){
        setLimit(limit, limit, limit, limit, limit, limit);
    }

    // return the view limit
    public double [] getLimit(){
        return new double [] {
                xMinRequested,
                xMaxRequested,
                yMinRequested,
                yMaxRequested,
                zMinRequested,
                zMaxRequested
        };
    }

    //
    public void lookAt(double eyeX, double eyeY, double eyeZ,
                       double viewCenterX, double viewCenterY, double viewCenterZ,
                       double viewUpX, double viewUpY, double viewUpZ){
        // look from camera XYZ
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.eyeZ = eyeZ;

        // look at the origin
        this.refX = viewCenterX;
        this.refY = viewCenterY;
        this.refZ = viewCenterZ;

        // Roll of the camera
        this.upX = viewUpX;
        this.upY = viewUpY;
        this.upZ = viewUpZ;
    }

    /*
        Apply the camera to an OpenGL Context to set the view once we start the game.
    */
    public void apply(GL2 gl){
        int [] viewport = new int[4];
        gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);

        xMinActual = xMinRequested;
        xMaxActual = xMinRequested;
        yMinActual = yMinRequested;
        yMaxActual = yMinRequested;

        if(preserveAspect){

            double viewWidth = viewport[2]; // grab the width
            double viewHeight = viewport[3]; // grab the height

            // compute the window width and height
            double windowWidth = xMaxActual - xMinActual;
            double windowHeight = yMaxActual - yMinActual;

            // compute the aspect ratio of the view and desired view
            double aspect = viewHeight / viewWidth;
            double desired = windowHeight / windowWidth;

            if(desired > aspect){ // expand the width
                double extra = (desired / aspect - 1.0) *
                        (xMaxActual - xMinActual) / 2.0;
                xMinActual = xMinActual - extra;
                xMaxActual = xMaxActual + extra;
            }else{ // expand the height
                double extra = (desired / aspect - 1.0) *
                        (yMaxActual - yMinActual) / 2.0;
                yMinActual = yMinActual - extra;
                yMaxActual = yMaxActual + extra;
            }
        }

        // projection from camera space to screen space
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        double viewDistance = norm(new double[] {refX - eyeX, refY - eyeY, refZ - eyeZ});

        if(orthographic){ // is orthographic view
            gl.glOrtho(xMinActual, xMaxActual,
                    yMinActual, yMaxActual,
                    viewDistance-zMaxRequested,
                    viewDistance-zMinRequested);
        }else { // is perspective view
            double near = viewDistance - zMaxRequested;
            if(near < 0.1){
                near = 0.1;
            }

            double centerX = (xMinActual + xMaxActual) / 2;
            double centerY = (yMinActual + yMaxActual) / 2;
            double newWidth = (near / viewDistance) * (xMaxActual - xMinActual);
            double newHeight = (near / viewDistance) * (yMaxActual - yMinActual);

            double x1 = centerX - newWidth / 2;
            double x2 = centerX + newWidth / 2;
            double y1 = centerY - newHeight / 2;
            double y2 = centerY + newHeight / 2;

            gl.glFrustum(x1, x2, y1, y2, near, viewDistance-zMinRequested);
        }

        // define the position orientation of the camera
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        glu.gluLookAt(eyeX, eyeY, eyeZ, // look from the camera xyz
                refX, refY, refZ, // look at the origin
                upX, upY, upZ); // positive Y up vector
    }

    private double norm(double [] v){
        double norm2 = v[0]*v[0] + v[1]*v[1] + v[2]*v[2];
        if(Double.isNaN(norm2) || Double.isInfinite(norm2) || norm2 == 0){
            throw new NumberFormatException("Vector length zero, undefined or infinite");
        }
        return norm2;
    }
}



