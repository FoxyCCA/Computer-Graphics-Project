package legogame;

public class Shape {
    public int shapeId;
    public int x, y, z;
    public int colorId;
    public int rotationX, rotationY, rotationZ;
    public boolean placed;

    public Shape(int shapeId,
                 int x, int y, int z,
                 int colorId,
                 int rotationX, int rotationY, int rotationZ,
                 boolean placed) {
        this.shapeId = shapeId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.colorId = colorId;
        this.rotationX = rotationX;
        this.rotationY = rotationY;
        this.rotationZ = rotationZ;
        this.placed = placed;
    }

    public boolean equals(Shape shape){
        return  this.shapeId == shape.shapeId &&
                this.x == shape.x &&
                this.y == shape.y &&
                this.z == shape.z &&
                this.colorId == shape.colorId &&
                this.placed && shape.placed;
    }
}

