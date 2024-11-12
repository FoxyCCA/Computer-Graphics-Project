package legogame;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class ShapeFile {
    public static Path saveFolderPath = Paths.get("./src/main/java/save").toAbsolutePath();
    public static Path mapFolderPath = Paths.get("./src/main/java/maps").toAbsolutePath();

    public static void exportToFile(Shape[][][] shape){
        try {
            FileWriter file = new FileWriter(saveFolderPath.toString() + "/savedata.txt");
            PrintWriter printToFile = new PrintWriter(file);
            for(int height = 0; height < shape.length; height++){
                for (int row = 0; row < shape[height].length; row++) {
                    for (int column = 0; column < shape[height][row].length; column++) {
                        if (shape[height][row][column].placed){
                            printToFile.printf("\n%d %d %d %d %d %d %d %d %d %d %d %b", height, row, column,
                                    shape[height][row][column].shapeId,
                                    shape[height][row][column].x,
                                    shape[height][row][column].y,
                                    shape[height][row][column].z,
                                    shape[height][row][column].colorId,
                                    shape[height][row][column].rotationX,
                                    shape[height][row][column].rotationY,
                                    shape[height][row][column].rotationZ,
                                    shape[height][row][column].placed
                                    );
                        }
                    }
                }
            }

            printToFile.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Shape[][][] readFromFile(String type, String fileName){
        Shape[][][] readShape = new Shape[50][21][21];

        readShape = initShapes();

        File file;

        if(Objects.equals(type, "save")){
            file = new File(saveFolderPath.toString() + "/" + fileName);
        } else {
            file = new File(mapFolderPath.toString() + "/" + fileName);
        }
        int height, row, column, shapeId, x, y, z, colorId, rX, rY, rZ;
        boolean placed;
        try {
            Scanner ls = new Scanner(file);

            ls.nextLine();
            while (ls.hasNextLine()){
                height = ls.nextInt();
                row = ls.nextInt();
                column = ls.nextInt();
                shapeId = ls.nextInt();
                x = ls.nextInt();
                y = ls.nextInt();
                z = ls.nextInt();
                colorId = ls.nextInt();
                rX = ls.nextInt();
                rY = ls.nextInt();
                rZ = ls.nextInt();
                placed= ls.nextBoolean();
                readShape[height][row][column] = new Shape(
                        shapeId,
                        x,y,z,
                        colorId,
                        rX, rY, rZ,
                        placed);
            }
            ls.close();
        } catch (FileNotFoundException ex){
            ex.printStackTrace();
        }
        return readShape;
    }

    public static Shape[][][] initShapes() {
        Shape[][][] shapes = new Shape[50][21][21];
        for (Shape[][] plane : shapes) {
            for (Shape[] row : plane) {
                Arrays.fill(row , new Shape(0,
                        0, 0, 0,
                        0,
                        0, 0, 0,
                        false));
            }
        }
        return shapes;
    }
}
