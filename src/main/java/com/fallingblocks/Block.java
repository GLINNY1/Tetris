package com.fallingblocks;

// Importing the Random class from the java.util package
import java.util.Random;

// Block class for the falling blocks game
public class Block {
    private static final int[][][] SHAPES = {
        // I shape
        {
            {1, 1, 1, 1}
        },
        // J shape
        {
            {2, 0, 0},
            {2, 2, 2}
        },
        // L shape
        {
            {0, 0, 3},
            {3, 3, 3}
        },
        // O shape
        {
            {4, 4},
            {4, 4}
        },
        // S shape
        {
            {0, 5, 5},
            {5, 5, 0}
        },
        // T shape
        {
            {0, 6, 0},
            {6, 6, 6}
        },
        // Z shape
        {
            {7, 7, 0},
            {0, 7, 7}
        }
    };

    // shape variables
    private int[][] shape;
    private int x, y;
    private int color;

    // constructor
    public Block() {
        Random random = new Random();
        int index = random.nextInt(SHAPES.length);
        shape = SHAPES[index];
        color = index + 1;
        x = 3;
        y = 0;
    }

    // movement methods
    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public void moveDown() {
        y++;
    }

    public void moveUp() {
        y--;
    }

    // rotate
    public void rotate() {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                rotated[j][shape.length - 1 - i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    // rotate back
    public void rotateBack() {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                rotated[shape[i].length - 1 - j][i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    // getShape
    public int[][] getShape() {
        return shape;
    }

    // grid coordinates
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    // New setter methods for shadow piece
    public void setShape(int[][] newShape) {
        this.shape = newShape;
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void setY(int newY) {
        this.y = newY;
    }

    public void setColor(int newColor) {
        this.color = newColor;
    }
} 