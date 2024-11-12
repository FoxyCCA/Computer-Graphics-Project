package legogame;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Project extends GLCanvas implements GLEventListener, KeyListener, MouseListener {

    private GLCanvas canvas;
    private JFrame frame;
    private FPSAnimator animator;
    private int WINDOW_WIDTH = 1800;
    private int WINDOW_HEIGHT = 1013;

    private static final String TITLE = "Lego Game";
    private static final int FPS = 60;

    private GLU glu;

    private TextRenderer textRenderer;

    private final String [] textureFileNames = {
            "palette.png",
            "red.jpg",
            "orange.jpg",
            "yellow.jpg",
            "green.jpg",
            "cyan.jpg",
            "light_blue.jpg",
            "blue.jpg",
            "purple.jpg",
            "pink.jpg",
            "white.jpg",
            "light_gray.jpg",
            "gray.jpg",
            "dark_gray.jpg",
            "black.jpg",
            "brown.jpg",
            "beige.jpg",
            "colorPalette.png",
            "taj_mahal.jpeg",
            "stonehenge.jpg"
    };

    Texture[] textures = new Texture[textureFileNames.length];

    private final HashMap<String, Integer> mapNames = new HashMap<>();

    private ArrayList mapList;

    private int currentMapImage = 0;
    private String currentMap;

    private int mapIterator = 0;

    private int totalNumberOfBlocks = 0;
    private int currentTotal = 0;

    private static final String SOUTH_SIDE = "SOUTH";
    private static final String WEST_SIDE = "WEST";
    private static final String NORTH_SIDE = "NORTH";
    private static final String EAST_SIDE = "EAST";

    private String CURRENT_SIDE = "SOUTH";


    private int currentSelectedObjectID = 1;

    private int currentSelectedColorID = 1;


    // the current angle of the blueprint
    private int currentAngleOfRotationX = 0;
    private int currentAngleOfRotationY = 0;
    private int currentAngleOfVisibleField = 80; // camera


    // translate the blueprint
    private float translateX = 0f;
    private float translateY = 0f;
    private float translateZ = 0f;

    private int selectionBoxX = 0;
    private int selectionBoxY = 0;
    private int selectionBoxZ = 0;

    private int selectionRotationX = 0;
    private int selectionRotationY = 0;
    private int selectionRotationZ = 0;


    // scale the shape added into the blueprint
    private float scale = 1f;

    private float scaleConstant = 0.2f;

//    private boolean loadedShapeWireframe = false;
    private boolean showWireframe = true;

    private boolean lightOnOff= true;
    private boolean ambientLight = false;
    private boolean diffuseLight = true;
    private boolean specularLight = true;
    private boolean globalAmbientLight = true;

    private int currentLayer = 50;
    private boolean gameStarted = false;
    private boolean gameFinished = false;
    private Camera camera;

    Shape[][][] currentShapes = new Shape[50][21][21];
    Shape[][][] loadShapes = new Shape[50][21][21];

    Project(){

        GLProfile profile = GLProfile.getDefault();

        GLCapabilities caps = new GLCapabilities(profile);
        caps.setAlphaBits(8);
        caps.setDepthBits(24);
        caps.setDoubleBuffered(true);
        caps.setStencilBits(8);


        SwingUtilities.invokeLater(() -> {
            canvas = new GLCanvas();

            canvas.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

            canvas.addGLEventListener(this);
            canvas.addKeyListener(this);
            canvas.addMouseListener(this);
            canvas.setFocusable(true);
            canvas.requestFocus();
            canvas.requestFocusInWindow();

            animator = new FPSAnimator(canvas, FPS,true);

            frame = new JFrame();

            frame.getContentPane().add(canvas);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    new Thread(() -> {
                        if(animator.isStarted()){
                            animator.stop();
                            System.exit(0);
                        }
                    }).start();
                }
            });

            camera = new Camera();
            camera.lookAt(0, 0, 1,
                    0,0,0,
                    0,1,0);

            camera.setScale(15);

            frame.pack();
            frame.setTitle(TITLE);
            frame.setVisible(true);
            animator.start();
        });

    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glClearColor(0.774f, 0.902f, 1f, 0); // RGBA

        // enable the depth buffer to allow us represent depth information in 3d space
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING); // enable lighting calculation
        gl.glEnable(GL2.GL_LIGHT0); // initial value for light (1,1,1,1) -> RGBA
        gl.glEnable(GL2.GL_NORMALIZE);

        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, 1);
        gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 100);

        // initialize different light sources
        float [] ambient = {0.1f, 0.1f, 0.9f, 1.0f};
        float [] diffuse = {0.7f, 0.9f, 1.0f, 1.0f};
        float [] specular = {0.7f, 0.9f, 1.0f, 1.0f};

        // configure different light sources
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT2, GL2.GL_SPECULAR, specular, 0);

        gl.glClearDepth(1.0f); // set clear depth value to farthest
        gl.glEnable(GL2.GL_DEPTH_TEST); // enable depth testing
        gl.glDepthFunc(GL2.GL_LEQUAL); // the type of depth test to do
        // perspective correction
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
        gl.glShadeModel(GL2.GL_SMOOTH); // blend colors nicely & have smooth lighting

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

        mapNames.put("taj_mahal.txt", 18);
        mapNames.put("stonehenge.txt", 19);
        mapNames.put("savedata.txt", 6);

        mapList = new ArrayList(Arrays.asList(mapNames.keySet().toArray()));

        Collections.shuffle(mapList);
        currentMapImage = mapNames.get((String) mapList.get(0));
        currentMap = (String) mapList.get(0);


        loadShapes = ShapeFile.readFromFile("map", currentMap);


        // initialize the textures to use
        glu = GLU.createGLU(gl); // get Gl utilties

        Path texturePath = Paths.get("src/main/java/textures/").toAbsolutePath();
        for (int i=0; i<textureFileNames.length; i++) {
            try {

                String url = texturePath.toString() +"/"+ textureFileNames[i];

                BufferedImage image = ImageIO.read(new File(url));
                ImageUtil.flipImageVertically(image);

                textures[i] = AWTTextureIO.newTexture(GLProfile.getDefault(),
                        image,
                        true);

                textures[i].setTexParameteri(gl,
                        GL2.GL_TEXTURE_WRAP_S,
                        GL2.GL_REPEAT);

                textures[i].setTexParameteri(gl,
                        GL2.GL_TEXTURE_WRAP_T,
                        GL2.GL_REPEAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentShapes = ShapeFile.initShapes();

        textures[0].enable(gl);

        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 20));
    }


    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        // clears both the color and depth buffer before rendering
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        gl.glDepthRange(0, 0.2);
        drawImagesOnScreen(glAutoDrawable);
        text();

        gl.glDepthRange(0.2, 1);

        // define the point of view of the blueprint
        gl.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(currentAngleOfVisibleField,
                (float)WINDOW_WIDTH/WINDOW_HEIGHT, 1, 100);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        setObserver();


        // change the orientation of the blueprint
        gl.glTranslated(translateX, translateY, translateZ);
        gl.glScalef(scale, scale, scale);
        gl.glRotated(currentAngleOfRotationX, 1, 0, 0);
        gl.glRotated(currentAngleOfRotationY, 0, 1, 0);


        gl.glPushMatrix();
        drawPlatform(glAutoDrawable);

        // Wireframe
        if(showWireframe){
            gl.glDisable(GL2.GL_LIGHTING);
            drawShapes(glAutoDrawable, loadShapes, GL2.GL_LINE);
            gl.glEnable(GL2.GL_LIGHTING);
            placedBlocksChecker();
        }

        drawShapes(glAutoDrawable, currentShapes, GL2.GL_FILL);

        // Selection box
        gl.glPushMatrix();
        gl.glColor3f(1,0,0);
        gl.glTranslated(selectionBoxX, selectionBoxY, selectionBoxZ);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        gl.glLineWidth(4);
        gl.glDisable(GL2.GL_LIGHTING);
        Draw.cube(gl);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopMatrix();

        gl.glPopMatrix();

        camera.apply(gl); // add the camera

        if(currentTotal == totalNumberOfBlocks){
            gameFinished = true;
        }

        //Lights
        float [] zero = {0, 0, 0, 1};
        lights(gl, zero); // add the lights


        if(globalAmbientLight){ // if it's checked
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT,
                    new float[] {0.4f, 0.4f, 0.4f, 1},
                    0);
        }else{
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT,
                    zero,
                    0);
        }

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK,
                GL2.GL_SPECULAR,
                new float[] {0.1f, 0.1f, 0.1f, 1}, 0);

    }

    private void text() {
        writeTextOnScreen(20, WINDOW_HEIGHT - 30,
                "Press [F1] to see help menu!");

        writeTextOnScreen(WINDOW_WIDTH - 450, WINDOW_HEIGHT /2 + 100,
                "Layers: " + currentLayer);

        writeTextOnScreen(WINDOW_WIDTH - 280, WINDOW_HEIGHT /2 + 100,
                "X: " + selectionBoxX + " Y: " +selectionBoxY + " Z: " +selectionBoxZ);

        writeTextOnScreen(WINDOW_WIDTH - 103, WINDOW_HEIGHT /2 + 100,
                "G: " + globalAmbientLight);
        writeTextOnScreen(WINDOW_WIDTH - 100, WINDOW_HEIGHT /2 + 70,
                "L: " + lightOnOff);
        writeTextOnScreen(WINDOW_WIDTH - 100, WINDOW_HEIGHT /2 + 40,
                "A: " + ambientLight);
        writeTextOnScreen(WINDOW_WIDTH - 100, WINDOW_HEIGHT /2 + 10,
                "D: " + diffuseLight);
        writeTextOnScreen(WINDOW_WIDTH - 100, WINDOW_HEIGHT /2 - 20,
                "S: " + specularLight);

        if(gameFinished){
            writeTextOnScreen(WINDOW_WIDTH/2 - 130, WINDOW_HEIGHT - 70,
                    "GAME FINISHED! CONGRATS");

        } else{
            writeTextOnScreen(WINDOW_WIDTH/2 - 30, WINDOW_HEIGHT - 70,
                    " " + currentTotal + "/" +totalNumberOfBlocks);
        }

        // SOUTH SIDE
        if (currentAngleOfRotationY <= 45 || currentAngleOfRotationY >= 315) {
            CURRENT_SIDE = SOUTH_SIDE;
        }
        // WEST SIDE
        else if (currentAngleOfRotationY < 135) {
            CURRENT_SIDE = WEST_SIDE;
        }
        // NORTH SIDE
        else if (currentAngleOfRotationY <= 225) {
            CURRENT_SIDE = NORTH_SIDE;
        }
        // EAST SIDE
        else {
            CURRENT_SIDE = EAST_SIDE;
        }

        writeTextOnScreen(WINDOW_WIDTH/2- 30, WINDOW_HEIGHT - 40,
                CURRENT_SIDE);
    }

    private void drawImagesOnScreen(GLAutoDrawable glAutoDrawable){

        GL2 gl = glAutoDrawable.getGL().getGL2();


        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        gl.glOrtho(
                (float) 0, // left vertical clipping plane
                (float) 1920/2, // right vertical clipping plane
                (float) 1080/2, // bottom horizontal clipping plane
                (float) 0, // top horizontal clipping plane
                -5, // near depth clipping plane
                30 // near farther clipping plane
        );

        gl.glMatrixMode(GL2.GL_MODELVIEW);

//        System.out.println(currentMapImage);
        // Backgrounds
        gl.glLoadIdentity();
        gl.glDisable(GL2.GL_LIGHTING);
        drawImageBackground(glAutoDrawable, 25f, 1080/4f, -30f, 50f, 400f, 0, 0);
        drawImageBackground(glAutoDrawable, 1920/4f, 1080/2f - 30f, -30f, 500f, 31.25f, 0, 17);
        drawImageBackground(glAutoDrawable, 1920/2f-125f, 100f, -29f, 250f, 200f, 180, currentMapImage);

        drawImageBackground(glAutoDrawable, 1920/2f-125f, 100f, -30f, 260f, 250f, 0, 10);
        drawImageBackground(glAutoDrawable, 1920/2f-20f, 100f, -30f, 75f, 370f, 0, 10);
        drawImageBackground(glAutoDrawable, 1920/4f, 20f, -30f, 220f, 50f, 0, 10);
        drawImageBackground(glAutoDrawable, 25f, 15f, -30f, 320f, 30f, 0, 10);
        gl.glEnable(GL2.GL_LIGHTING);

        glu.gluLookAt(0, 0, 30.0, // look from camera XYZ
                0.0, 0.0, 0.0, // look at the origin
                0.0, 1.0, 0.0); // positive Y up vector

        // PALETTE OBJECTS
        drawObjectOnPalette(glAutoDrawable, 1, 25f, 95f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 2, 25f, 145f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 3, 25f, 195f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 4, 25f, 245f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 5, 25f, 295f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 6, 25f, 345f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 7, 25f, 395f, 23f, 32f);
        drawObjectOnPalette(glAutoDrawable, 8, 25f, 460f, 23f, 32f);
    }

    private void drawImageBackground(GLAutoDrawable glAutoDrawable,
                                     float tX, float tY, float tZ,
                                     float sX, float sY,
                                     int rotationAngle,
                                     int textureId){
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glPushMatrix();

        // enable the server-side GL capabilities for texture
        gl.glEnable(GL2.GL_TEXTURE_2D);

        // set different texture parameters
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        // set the texture to use
        textures[textureId].bind(gl);
        gl.glTranslated(tX, tY, tZ);
        gl.glScalef(sX, sY, 0f);
        gl.glRotatef(rotationAngle, 0, 0, 1);
        gl.glColor3f(1f, 1f, 1f);

        Draw.square(gl, 1, true);

        gl.glDisable(GL2.GL_TEXTURE_2D);

        gl.glPopMatrix();
    }

    private void drawObjectOnPalette(GLAutoDrawable glAutoDrawable, int id, float x, float y, float z, float objectScale) {
        GL2 gl = glAutoDrawable.getGL().getGL2();

        gl.glPushMatrix();
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        gl.glColor3f(1f,1f,1f);
        gl.glTranslated(x,y,z);

        gl.glPushMatrix();
        gl.glScalef(objectScale, objectScale, objectScale);

        gl.glEnable(GL2.GL_TEXTURE_2D);
        textures[currentSelectedColorID].bind(gl);
        switch (id){
            case 1:
                Draw.cube(gl);
                break;
            case 2:
                Draw.slab(gl);
                break;
            case 3:
                gl.glRotatef(90, 0, 0, 1);
                gl.glRotatef(40, 1, 0, 0);
                Draw.ramp(gl);
                break;
            case 4:
                gl.glRotatef(90, 0, 0, 1);
                Draw.rampCorner(gl);
                break;
            case 5:
                gl.glRotatef(180, 0, 0, 1);
                Draw.rectangularPyramid(gl);
                break;
            case 6:
                gl.glRotatef(15, 1, 0, 0);
                Draw.cylinder(gl);
                break;
            case 7:
                Draw.pole(gl);
                break;
            case 8:
                Draw.carpet(gl);
                break;
        }
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glPopMatrix();
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopMatrix();
    }

    // DRAW PLACED SHAPES ON THE BOARD
    private void drawShapes(GLAutoDrawable glAutoDrawable, Shape[][][] shapes, int fillMode) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        int total = 0;
        for (int shapePlane = 0; shapePlane < currentLayer; shapePlane ++) {
            for (Shape[] shapeAxis : shapes[shapePlane]) {
                for (Shape shape : shapeAxis) {
                    if (shape.placed) {
                        if(fillMode == GL2.GL_LINE){
                            total += 1;
                        }
                        gl.glPushMatrix();
                        gl.glColor3f(1f, 1f,1f);

                        gl.glTranslated(shape.x,
                                        shape.y,
                                        shape.z);


                        gl.glRotatef(shape.rotationX, 1, 0 ,0);
                        gl.glRotatef(shape.rotationY,0,1,0);
                        gl.glRotatef(shape.rotationZ,0,0,1);

                        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, fillMode);
                        textures[shape.colorId].bind(gl);
                        gl.glEnable(GL2.GL_TEXTURE_2D);

                        if(shape.shapeId == 1){
                            Draw.cube(gl);
                        }
                        else if (shape.shapeId == 2){
                            gl.glTranslated(0, -0.25, 0);
                            Draw.slab(gl);
                        }
                        else if (shape.shapeId == 3){
                            Draw.ramp(gl);
                        }
                        else if (shape.shapeId == 4){
                            Draw.rampCorner(gl);
                        }
                        else if (shape.shapeId == 5){
                            Draw.rectangularPyramid(gl);
                        }
                        else if (shape.shapeId == 6){
                            Draw.cylinder(gl);
                        }
                        else if (shape.shapeId == 7){
                            Draw.pole(gl);
                        }
                        else if (shape.shapeId == 8){
                            Draw.carpet(gl);
                        }
                        else {
                            Draw.cube(gl);
                        }
                        gl.glDisable(GL2.GL_TEXTURE_2D);
                        gl.glPopMatrix();
                    }
                }
            }
        }
        if(fillMode == GL2.GL_LINE && !gameStarted){
            gameStarted = true;
            totalNumberOfBlocks = total;
        }
    }

    private void placedBlocksChecker() {
        int total = 0;
        for(int height = 0; height < currentShapes.length; height++) {
            for (int row = 0; row < currentShapes[height].length; row++) {
                for (int column = 0; column < currentShapes[height][row].length; column++) {
                    if(currentShapes[height][row][column].equals(loadShapes[height][row][column])){
                        total += 1;
                    }
                }
            }
        }
        currentTotal = total;
    }

    private void drawPlatform(GLAutoDrawable glAutoDrawable) {

        GL2 gl = glAutoDrawable.getGL().getGL2();

        // draw the blueprint
        gl.glPushMatrix();

        gl.glColor3f(0,1,0);
        gl.glTranslated(0, -0.61, 0);

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        textures[2].bind(gl);

        gl.glEnable(GL.GL_TEXTURE_2D);
        Draw.platform(gl);
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glPopMatrix();
    }

    // CAMERA
    private void setObserver() {
        glu.gluLookAt(0, 0, 10.0, // look from camera XYZ
                0.0, 0.0, 0.0, // look at the origin
                0.0, 1.0, 0.0); // positive Y up vector
    }

    private void lights(GL2 gl, float [] zero) {
        gl.glColor3d(0.5, 0.5, 0.5);
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, zero, 0);

        if(lightOnOff){
            gl.glDisable(GL2.GL_LIGHTING);
        }else{
            gl.glEnable(GL2.GL_LIGHTING);
        }

        float [] ambient = {0.4f, 0.4f, 0.4f, 1.0f};
        float [] diffuse = {1.0f, 1.0f, 1.0f, 1.0f};
        float [] specular = {0.1f, 0.1f, 0.1f, 1.0f};
        float [] shininess = {0.1f, 0.1f, 0.1f, 0.01f};

        // ambient light
        if(ambientLight){
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, ambient, 0);
            gl.glEnable(GL2.GL_LIGHT0);
        }else{
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);
            gl.glDisable(GL2.GL_LIGHT0);
        }

        // diffuse light
        if(diffuseLight){
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, diffuse, 0);
            gl.glEnable(GL2.GL_LIGHT1);
        }else{
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);
            gl.glDisable(GL2.GL_LIGHT1);
        }

        // specular light
        if(specularLight){
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, specular, 0);
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess, 0);
            gl.glEnable(GL2.GL_LIGHT2);
        }else{
            gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);
            gl.glDisable(GL2.GL_LIGHT2);
        }

        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, zero, 0);
    }

    private void writeTextOnScreen(int x, int y, String message){
        textRenderer.beginRendering(WINDOW_WIDTH, WINDOW_HEIGHT);
        textRenderer.setColor(0.3f, 0.3f, 0.5f, 1);
        textRenderer.draw3D(message, x, y, -1, 1);
        textRenderer.endRendering();
    }
    
    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h) {
        WINDOW_WIDTH = w;
        WINDOW_HEIGHT = h;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        int arrX = selectionBoxX + 10;
        int arrZ = selectionBoxZ + 10;
        int arrY = selectionBoxY;

        switch (key){
            // ZOOM CONTROLS
            case KeyEvent.VK_Z:
                if(scale > 0.3){
                    scale -= scaleConstant;
                }
                break;
            case KeyEvent.VK_X:
                if(scale < 6){
                    scale += scaleConstant;
                }
                break;

                // ROTATION CONTROLS
            case KeyEvent.VK_W:
                selectionRotationX += 90;
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].rotationX = selectionRotationX;
                }
                break;
            case KeyEvent.VK_S:
                if (e.isControlDown()){
                    ShapeFile.exportToFile(currentShapes);
                } else {
                    selectionRotationX -= -90;
                    if(currentShapes[arrY][arrX][arrZ].placed) {
                        currentShapes[arrY][arrX][arrZ].rotationX = selectionRotationX;
                    }
                }
                break;
            case KeyEvent.VK_Q:
                selectionRotationZ += 90;
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].rotationZ = selectionRotationZ;
                }
                break;
            case KeyEvent.VK_E:
                selectionRotationZ -= 90;
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].rotationZ = selectionRotationZ;
                }
                break;
            case KeyEvent.VK_A:
                selectionRotationY += 90;
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].rotationY = selectionRotationY;
                }
                break;
            case KeyEvent.VK_D:
                selectionRotationY -= 90;
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].rotationY = selectionRotationY;
                }
                break;

                // SELECTION CUBE MOVEMENT CONTROLS
            case KeyEvent.VK_LEFT:
                if(e.isControlDown()){
                    currentAngleOfRotationY+= 4;
                    if (currentAngleOfRotationY >= 360) {
                        currentAngleOfRotationY = currentAngleOfRotationY- 360;
                    }
                } else {
                    if(Objects.equals(CURRENT_SIDE, SOUTH_SIDE)){
                        if(selectionBoxX > -10) {selectionBoxX--;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, WEST_SIDE)) {
                        if(selectionBoxZ > -10) {selectionBoxZ--;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, NORTH_SIDE)){
                        if(selectionBoxX < 10) {selectionBoxX++;}
                    }
                    else {
                        if(selectionBoxZ < 10) {selectionBoxZ++;}
                    }
                }
                break;
            case KeyEvent.VK_RIGHT:
                if(e.isControlDown()) {
                    currentAngleOfRotationY -= 4;
                    if (currentAngleOfRotationY < 0) {
                        currentAngleOfRotationY = currentAngleOfRotationY + 360;
                    }
                } else {
                    if(Objects.equals(CURRENT_SIDE, SOUTH_SIDE)){
                        if (selectionBoxX < 10) {selectionBoxX++;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, WEST_SIDE)) {
                        if (selectionBoxZ < 10) {selectionBoxZ++;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, NORTH_SIDE)){
                        if (selectionBoxX > -10) {selectionBoxX--;}
                    }
                    else {
                        if (selectionBoxZ > -10) {selectionBoxZ--;}
                    }
                }
                break;
            case KeyEvent.VK_UP:
                if(e.isControlDown()) {
                    if(currentAngleOfRotationX > 0){
                        currentAngleOfRotationX -= 4;
                    }
                    if (currentAngleOfRotationX < 0) {
                        currentAngleOfRotationX = currentAngleOfRotationX + 360;
                    }
                } else {
                    if(Objects.equals(CURRENT_SIDE, SOUTH_SIDE)){
                        if (selectionBoxZ > -10) {selectionBoxZ--;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, WEST_SIDE)) {
                        if (selectionBoxX < 10) {selectionBoxX++;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, NORTH_SIDE)){
                        if (selectionBoxZ < 10) {selectionBoxZ++;}
                    }
                    else {
                        if (selectionBoxX > -10) {selectionBoxX--;}
                    }
                }
                break;
            case KeyEvent.VK_DOWN:
                if(e.isControlDown()) {
                    if(currentAngleOfRotationX < 75){
                        currentAngleOfRotationX += 4;
                    }
                    if (currentAngleOfRotationX >= 360) {
                        currentAngleOfRotationX = currentAngleOfRotationX - 360;
                    }
                } else {
                    if(Objects.equals(CURRENT_SIDE, SOUTH_SIDE)){
                        if (selectionBoxZ < 10) {selectionBoxZ++;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, WEST_SIDE)) {
                        if (selectionBoxX > -10) {selectionBoxX--;}
                    }
                    else if (Objects.equals(CURRENT_SIDE, NORTH_SIDE)){
                        if (selectionBoxZ > -10) {selectionBoxZ--;}
                    }
                    else {
                        if (selectionBoxX < 10) {selectionBoxX++;}
                    }
                }
                break;

            case KeyEvent.VK_SHIFT:
                if(selectionBoxY > 0) {selectionBoxY--;}
                break;
            case KeyEvent.VK_SPACE:
                if(selectionBoxY < 49) {selectionBoxY++;}
                break;

                // BLOCK PLACEMENT AND DELETION CONTROLS
            case KeyEvent.VK_ENTER:
                if(!currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ] = new Shape(
                            currentSelectedObjectID,
                            selectionBoxX, selectionBoxY, selectionBoxZ,
                            currentSelectedColorID,
                            selectionRotationX, selectionRotationY, selectionRotationZ,
                            true);
                }
                break;
            case KeyEvent.VK_DELETE:
            case KeyEvent.VK_BACK_SPACE:
                if(currentShapes[arrY][arrX][arrZ].placed) {
                    currentShapes[arrY][arrX][arrZ].placed = false;
                }
                break;


                // CLEARING, SAVING and LOADING CONTROLS
            case KeyEvent.VK_C:
                if (e.isControlDown()){
                    currentShapes = ShapeFile.initShapes();
                }
                break;
            case KeyEvent.VK_I:
                if (e.isControlDown()){
                    currentShapes = ShapeFile.readFromFile("save", "savedata.txt");
                }
                break;
//            case KeyEvent.VK_P:
//                if (e.isControlDown()){
//                    currentShapes = ShapeFile.initShapes();
//                    loadShapes = ShapeFile.readFromFile("save", "savedata.txt");
//                    loadedShapeWireframe = true;
//                }
////                currentShapes = clearShapes();
////                loadShapes = saveShapes.clone();
////                loadedShapeWireframe = true;
//                break;

            // MESH HIDING CONTROLS
            case KeyEvent.VK_H:
                showWireframe = !showWireframe;
                break;

                //OBJECT SELECTION CONTROLS
            case KeyEvent.VK_1:
                currentSelectedObjectID = 1;
                break;
            case KeyEvent.VK_2:
                currentSelectedObjectID = 2;
                break;
            case KeyEvent.VK_3:
                currentSelectedObjectID = 3;
                break;
            case KeyEvent.VK_4:
                currentSelectedObjectID = 4;
                break;
            case KeyEvent.VK_5:
                currentSelectedObjectID = 5;
                break;
            case KeyEvent.VK_6:
                currentSelectedObjectID = 6;
                break;
            case KeyEvent.VK_7:
                currentSelectedObjectID = 7;
                break;
            case KeyEvent.VK_8:
                currentSelectedObjectID = 8;
                break;

                // MAP SELECTION CONTROLS
            case KeyEvent.VK_EQUALS:
                if(e.isControlDown()){
                    mapIterator++;
                    if(mapIterator == mapList.size()){
                        mapIterator = 0;
                    }
                    translateY = 0;
                    gameStarted = false;
                    gameFinished = false;
                    currentLayer = 50;
                    currentShapes = ShapeFile.initShapes();
                    currentMapImage = mapNames.get((String) mapList.get(mapIterator));
                    currentMap = (String) mapList.get(mapIterator);
                    loadShapes = ShapeFile.readFromFile("map", currentMap);
                    currentAngleOfRotationY = 0;
                }
                break;
            case KeyEvent.VK_MINUS:
                if(e.isControlDown()){
                    mapIterator--;
                    if(mapIterator < 0){
                        mapIterator = mapList.size()-1;
                    }
                    translateY = 0;
                    gameStarted = false;
                    gameFinished = false;
                    currentLayer = 50;
                    currentShapes = ShapeFile.initShapes();
                    currentMapImage = mapNames.get((String) mapList.get(mapIterator));
                    currentMap = (String) mapList.get(mapIterator);
                    loadShapes = ShapeFile.readFromFile("map", currentMap);
                    currentAngleOfRotationY = 0;
                }
                break;

                //LIGHT CONTROLS
            case KeyEvent.VK_G:
                globalAmbientLight = !globalAmbientLight;
                break;
            case KeyEvent.VK_J:
                lightOnOff = !lightOnOff;
                break;
            case KeyEvent.VK_K:
                ambientLight = !ambientLight;
                break;
            case KeyEvent.VK_L:
                diffuseLight = !diffuseLight;
                break;
            case KeyEvent.VK_SEMICOLON:
                specularLight = !specularLight;
                break;

                //LAYER CONTROLS
            case KeyEvent.VK_COMMA:
                currentLayer--;
                if(currentLayer < 1){
                    currentLayer = 50;
                }
                break;
            case KeyEvent.VK_PERIOD:
                currentLayer++;
                if(currentLayer > 50){
                    currentLayer = 1;
                }
                break;
            case KeyEvent.VK_SLASH:
                currentLayer = 50;
                break;

                //TRANSLATION CONTROLS
            case KeyEvent.VK_N:
                if (translateY <= 0){
                    translateY += 0.5f;
                }
                break;
            case KeyEvent.VK_M:
                translateY -= 0.5f;
                break;

                // HELP CONTROLS
            case KeyEvent.VK_F1:
                JOptionPane.showMessageDialog(frame, "Instructions: \n" +
                                "Arrows [UP DOWN LEFT RIGHT] are used for moving your cursor block around \n" +
                                "[SPACE and SHIFT] are used to to move the block cursor up or down \n\n" +

                                "[CTRL + LEFT] or [CTRL + RIGHT] are used to move the camera left or right \n" +
                                "[CTRL + UP] or [CTRL + DOWN] are used to move the camera up or down \n" +
                                "[X] or [Z] to zoom in or zoom out \n\n" +

                                "[N] to move view down\n"+
                                "[M] to move view up\n\n"+

                                "[1 2 3 4 5 6 7 8] numbers to select what kind of block you want to place, or you can click on one on the palette to the left \n" +
                                "To select a color you need to click on one of the colors at the bottom, the palette to the left displays the currently selected color \n\n" +

                                "[ENTER] to place a block inside the cursor block \n" +
                                "[BACKSPACE] or [DEL] to remove a block that is located inside the block cursor \n\n" +

                                "[A] or [D] to rotate object inside the cursor block around Y axis \n" +
                                "[Q] or [E] to rotate object inside the cursor block around Z axis \n" +
                                "[W] or [S] to rotate object inside the cursor block around X axis \n\n" +

                                "[H] to hide wireframe \n\n" +

                                "[CTRL + S] to save your build progress (not map specific) and (overwrites your previous save file) \n" +
                                "[CTRL + I] to load your save file (this will erase your current progress, be careful) \n" +
                                "[CTRL + C] to clear all the blocks you've placed (this will erase your current progress, be careful) \n\n" +

                                "[CTRL + -] to go to previous map (clears your progress, be careful) \n" +
                                "[CTRL + =] to go to next map (clears your progress, be careful) \n\n" +

                                "[,] to reduce wireframe layer by 1, if it's at the first layer, it will overflow back to the highest layer\n"+
                                "[.] to increase wireframe layer by 1, if it's at last layer (50), it will overflow back to the lowest layer\n"+
                                "[/] resets wireframe layers to highest layer (50)\n\n"+

                                "Lights: \n"+
                                "[CTRL + G] - global ambient illumination \n"+
                                "[CTRL + J] - lights On or Off \n"+
                                "[CTRL + K] - ambient light \n"+
                                "[CTRL + L] - diffuse light \n"+
                                "[CTRL + ;] - specular light \n"
                        , "Help", JOptionPane.INFORMATION_MESSAGE);
                break;
        }

        if(selectionRotationX >= 360){
            selectionRotationX = 0;
        }
        if(selectionRotationX < 0){
            selectionRotationX += 360;
        }
        if(selectionRotationY >= 360){
            selectionRotationY = 0;
        }
        if(selectionRotationY < 0){
            selectionRotationY += 360;
        }
        if(selectionRotationZ >= 360){
            selectionRotationZ = 0;
        }
        if(selectionRotationZ < 0){
            selectionRotationZ += 360;
        }

//        System.out.println(selectionRotationX);
//        System.out.println(selectionRotationY);
//        System.out.println(selectionRotationZ);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        //OBJECT PALETTE
        float paletteTopPixel = WINDOW_HEIGHT * 0.125f;
        float paletteBottomPixel = WINDOW_HEIGHT * 0.875f;
        float paletteSquareConst = (paletteBottomPixel - paletteTopPixel) / 8f;
        float x = e.getX();
        float y = e.getY();


//        if(x < WINDOW_WIDTH * 0.05){
//            if(y > paletteTopPixel && e.getY() < paletteBottomPixel){
//                System.out.println("In the ZONE");
//            }
//        }

        if(x < WINDOW_WIDTH * 0.05){
            if(y > paletteTopPixel && y < paletteTopPixel + paletteSquareConst){
                currentSelectedObjectID = 1;
            }
            else if(y > paletteTopPixel + paletteSquareConst && y < paletteTopPixel + paletteSquareConst * 2){
                currentSelectedObjectID = 2;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 2 && y < paletteTopPixel + paletteSquareConst * 3){
                currentSelectedObjectID = 3;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 3 && y < paletteTopPixel + paletteSquareConst * 4){
                currentSelectedObjectID = 4;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 4 && y < paletteTopPixel + paletteSquareConst * 5){
                currentSelectedObjectID = 5;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 5 && y < paletteTopPixel + paletteSquareConst * 6){
                currentSelectedObjectID = 6;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 6 && y < paletteTopPixel + paletteSquareConst * 7){
                currentSelectedObjectID = 7;
            }
            else if(y > paletteTopPixel + paletteSquareConst * 7 && y < paletteTopPixel + paletteSquareConst * 8){
                currentSelectedObjectID = 8;
            }
        }


        // COLOR PALETTE
        float paletteLeftPixel = WINDOW_WIDTH * 0.240f;
        float paletteRightPixel= WINDOW_WIDTH * 0.760f;
        float paletteColorSquareConst = (paletteRightPixel - paletteLeftPixel) / 16f;

//        if(y > WINDOW_HEIGHT * 0.918 && y < WINDOW_HEIGHT * 0.972){
//            if(x > paletteLeftPixel){
//                System.out.println("IN THE ZONE");
//            }
//        }

        if(y > WINDOW_HEIGHT * 0.918 && y < WINDOW_HEIGHT * 0.972){
            if(x > paletteLeftPixel && x < paletteLeftPixel + paletteColorSquareConst){
                currentSelectedColorID = 1;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst && x < paletteLeftPixel + paletteColorSquareConst * 2){
                currentSelectedColorID = 2;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 2 && x < paletteLeftPixel + paletteColorSquareConst * 3){
                currentSelectedColorID = 3;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 3 && x < paletteLeftPixel + paletteColorSquareConst * 4){
                currentSelectedColorID = 4;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 4 && x < paletteLeftPixel + paletteColorSquareConst * 5){
                currentSelectedColorID = 5;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 5 && x < paletteLeftPixel + paletteColorSquareConst * 6){
                currentSelectedColorID = 6;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 6 && x < paletteLeftPixel + paletteColorSquareConst * 7){
                currentSelectedColorID = 7;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 7 && x < paletteLeftPixel + paletteColorSquareConst * 8){
                currentSelectedColorID = 8;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 8 && x < paletteLeftPixel + paletteColorSquareConst * 9){
                currentSelectedColorID = 9;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 9 && x < paletteLeftPixel + paletteColorSquareConst * 10){
                currentSelectedColorID = 10;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 10 && x < paletteLeftPixel + paletteColorSquareConst * 11){
                currentSelectedColorID = 11;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 11 && x < paletteLeftPixel + paletteColorSquareConst * 12){
                currentSelectedColorID = 12;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 12 && x < paletteLeftPixel + paletteColorSquareConst * 13){
                currentSelectedColorID = 13;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 13 && x < paletteLeftPixel + paletteColorSquareConst * 14){
                currentSelectedColorID = 14;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 14 && x < paletteLeftPixel + paletteColorSquareConst * 15){
                currentSelectedColorID = 15;
            }
            else if(x > paletteLeftPixel + paletteColorSquareConst * 15 && x < paletteRightPixel){
                currentSelectedColorID = 16;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public static void main(String[] args) {
        new Project();
    }
}
