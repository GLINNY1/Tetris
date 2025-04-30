package com.fallingblocks;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameBoard {
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 20;
    private static final int CELL_SIZE = 30;
    private static final int FALL_SPEED = 1000; // milliseconds
    private static final int NEXT_PIECE_X = GRID_WIDTH * CELL_SIZE + 50;
    private static final int NEXT_PIECE_Y = 50;
    private static final int SAVED_PIECE_X = GRID_WIDTH * CELL_SIZE + 50;
    private static final int SAVED_PIECE_Y = 200;
    
    // Customizable speeds (in milliseconds)
    private static final int DAS_DELAY = 133;    // Delay Auto Shift (initial delay)
    private static final int ARR_RATE = 20;      // Auto Repeat Rate (repeat rate)
    private static final int HORIZONTAL_MOVE_SPEED = 110;  // Left/Right movement speed
    private static final int ROTATION_SPEED = 150;        // Rotation speed
    private static final int SOFT_DROP_SPEED = 50;        // Down movement speed
    private static final int HARD_DROP_SPEED = 0;         // Instant drop

    private int[][] grid;
    private Block currentBlock;
    private Block nextBlock;
    private Block savedBlock;
    private boolean canSave;
    private int score;
    private Timeline gameLoop;
    private Canvas canvas;
    private GraphicsContext gc;
    private Map<KeyCode, Timeline> keyTimelines;
    private Map<KeyCode, Boolean> keyPressed;
    private Map<KeyCode, Timeline> dasTimelines;
    private boolean isGameOver;
    private Stage stage;

    public GameBoard() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        keyTimelines = new HashMap<>();
        keyPressed = new HashMap<>();
        dasTimelines = new HashMap<>();
        isGameOver = false;
        canSave = true;
        spawnNewBlock();
    }

    public void start(Stage stage) {
        this.stage = stage;
        Pane root = new Pane();
        canvas = new Canvas((GRID_WIDTH + 5) * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        setupInputHandlers(scene);
        stage.setScene(scene);
        stage.setTitle("Falling Blocks");
        stage.show();

        startGameLoop();
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(FALL_SPEED), e -> update()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    private void update() {
        if (!isGameOver) {
            if (!moveBlockDown()) {
                placeBlock();
                clearLines();
                spawnNewBlock();
                if (isGameOver()) {
                    gameOver();
                }
            }
            draw();
        }
    }

    private boolean isGameOver() {
        // Check if the new block can be placed
        return !isValidPosition(currentBlock);
    }

    private void gameOver() {
        isGameOver = true;
        gameLoop.stop();
        draw();  // Draw one final time to show game over screen
    }

    private void draw() {
        // Clear the canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw the grid lines
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        
        // Draw vertical lines
        for (int i = 0; i <= GRID_WIDTH; i++) {
            gc.strokeLine(i * CELL_SIZE, 0, i * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        }
        
        // Draw horizontal lines
        for (int i = 0; i <= GRID_HEIGHT; i++) {
            gc.strokeLine(0, i * CELL_SIZE, GRID_WIDTH * CELL_SIZE, i * CELL_SIZE);
        }

        // Draw the placed blocks
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (grid[i][j] != 0) {
                    drawCell(j, i, grid[i][j]);
                }
            }
        }

        // Draw the shadow piece
        if (currentBlock != null) {
            int[][] shape = currentBlock.getShape();
            int x = currentBlock.getX();
            int y = getDropPosition();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        drawShadowCell(x + j, y + i, currentBlock.getColor());
                    }
                }
            }
        }

        // Draw the current block
        if (currentBlock != null) {
            int[][] shape = currentBlock.getShape();
            int x = currentBlock.getX();
            int y = currentBlock.getY();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        drawCell(x + j, y + i, currentBlock.getColor());
                    }
                }
            }
        }

        // Draw the next block
        if (nextBlock != null) {
            int[][] shape = nextBlock.getShape();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        drawCell(NEXT_PIECE_X / CELL_SIZE + j, NEXT_PIECE_Y / CELL_SIZE + i, nextBlock.getColor());
                    }
                }
            }
        }

        // Draw the saved block
        if (savedBlock != null) {
            int[][] shape = savedBlock.getShape();
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] != 0) {
                        drawCell(SAVED_PIECE_X / CELL_SIZE + j, SAVED_PIECE_Y / CELL_SIZE + i, savedBlock.getColor());
                    }
                }
            }
        }

        // Draw labels
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 20));
        gc.fillText("Next:", NEXT_PIECE_X, NEXT_PIECE_Y - 20);
        gc.fillText("Saved:", SAVED_PIECE_X, SAVED_PIECE_Y - 20);
        gc.fillText("Score: " + score, NEXT_PIECE_X, NEXT_PIECE_Y + 100);

        // Draw game over screen if game is over
        if (isGameOver) {
            drawGameOver();
        }
    }

    private void drawGameOver() {
        // Draw semi-transparent overlay
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.8));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw game over text
        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial", 50));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", canvas.getWidth() / 2, canvas.getHeight() / 2 - 60);

        // Draw final score
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 30));
        gc.fillText("Final Score: " + score, canvas.getWidth() / 2, canvas.getHeight() / 2);

        // Draw restart instructions
        gc.setFont(new Font("Arial", 25));
        gc.fillText("Press R to Restart", canvas.getWidth() / 2, canvas.getHeight() / 2 + 60);
    }

    private void restart() {
        // Reset game state
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        isGameOver = false;
        keyTimelines.clear();
        keyPressed.clear();
        dasTimelines.clear();
        canSave = true;
        spawnNewBlock();
        
        // Restart game loop
        startGameLoop();
        
        // Redraw the game board
        draw();
    }

    private void spawnNewBlock() {
        if (nextBlock == null) {
            nextBlock = new Block();
        }
        currentBlock = nextBlock;
        nextBlock = new Block();
    }

    private void hardDrop() {
        while (moveBlockDown()) {
            // Keep moving down until we can't anymore
        }
        placeBlock();
        clearLines();
        spawnNewBlock();
        if (isGameOver()) {
            gameOver();
        }
    }

    private boolean moveBlockDown() {
        currentBlock.moveDown();
        if (!isValidPosition(currentBlock)) {
            currentBlock.moveUp();
            return false;
        }
        return true;
    }

    private void placeBlock() {
        int[][] shape = currentBlock.getShape();
        int x = currentBlock.getX();
        int y = currentBlock.getY();
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    grid[y + i][x + j] = currentBlock.getColor();
                }
            }
        }
        canSave = true;
    }

    private void clearLines() {
        int linesCleared = 0;
        for (int i = GRID_HEIGHT - 1; i >= 0; i--) {
            boolean isLineFull = true;
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (grid[i][j] == 0) {
                    isLineFull = false;
                    break;
                }
            }
            if (isLineFull) {
                // Remove the line and move everything down
                for (int k = i; k > 0; k--) {
                    System.arraycopy(grid[k-1], 0, grid[k], 0, GRID_WIDTH);
                }
                // Clear the top line
                for (int j = 0; j < GRID_WIDTH; j++) {
                    grid[0][j] = 0;
                }
                linesCleared++;
                // Since we moved everything down, we need to check the same line again
                i++;
            }
        }
        // Add score based on number of lines cleared
        switch (linesCleared) {
            case 1: score += 100; break;
            case 2: score += 300; break;
            case 3: score += 500; break;
            case 4: score += 800; break;
        }
    }

    private boolean isValidPosition(Block block) {
        int[][] shape = block.getShape();
        int x = block.getX();
        int y = block.getY();

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= GRID_WIDTH || newY >= GRID_HEIGHT ||
                        (newY >= 0 && grid[newY][newX] != 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int getDropPosition() {
        if (currentBlock == null) return 0;
        
        // Create a copy of the current block for shadow calculation
        Block shadowBlock = new Block();
        shadowBlock.setShape(currentBlock.getShape());
        shadowBlock.setX(currentBlock.getX());
        shadowBlock.setY(currentBlock.getY());
        shadowBlock.setColor(currentBlock.getColor());
        
        int y = shadowBlock.getY();
        while (true) {
            shadowBlock.moveDown();
            if (!isValidPosition(shadowBlock)) {
                shadowBlock.moveUp();
                break;
            }
            y++;
        }
        return y;
    }

    private void drawShadowCell(int x, int y, int color) {
        Color shadowColor = getColorForValue(color).deriveColor(0, 1, 1, 0.3);
        gc.setFill(shadowColor);
        gc.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
    }

    private void drawCell(int x, int y, int color) {
        gc.setFill(getColorForValue(color));
        gc.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, CELL_SIZE - 2, CELL_SIZE - 2);
    }

    private Color getColorForValue(int value) {
        switch (value) {
            case 1: return Color.CYAN;
            case 2: return Color.BLUE;
            case 3: return Color.ORANGE;
            case 4: return Color.YELLOW;
            case 5: return Color.GREEN;
            case 6: return Color.PURPLE;
            case 7: return Color.RED;
            default: return Color.WHITE;
        }
    }

    private void setupInputHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.R && isGameOver) {
                restart();
                return;
            }
            if (!isGameOver && !keyPressed.getOrDefault(code, false)) {
                keyPressed.put(code, true);
                handleKeyPress(code);
                
                // Start DAS timer for left/right movement
                if (code == KeyCode.LEFT || code == KeyCode.RIGHT) {
                    Timeline dasTimer = new Timeline(new KeyFrame(Duration.millis(DAS_DELAY), e -> {
                        // After DAS delay, start ARR
                        Timeline arrTimer = new Timeline(new KeyFrame(Duration.millis(ARR_RATE), e2 -> handleKeyPress(code)));
                        arrTimer.setCycleCount(Timeline.INDEFINITE);
                        arrTimer.play();
                        keyTimelines.put(code, arrTimer);
                    }));
                    dasTimer.play();
                    dasTimelines.put(code, dasTimer);
                }
                // Start soft drop timer
                else if (code == KeyCode.DOWN) {
                    Timeline softDropTimer = new Timeline(new KeyFrame(Duration.millis(SOFT_DROP_SPEED), e -> handleKeyPress(code)));
                    softDropTimer.setCycleCount(Timeline.INDEFINITE);
                    softDropTimer.play();
                    keyTimelines.put(code, softDropTimer);
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            keyPressed.put(code, false);
            
            // Stop DAS timer
            Timeline dasTimer = dasTimelines.get(code);
            if (dasTimer != null) {
                dasTimer.stop();
                dasTimelines.remove(code);
            }
            
            // Stop ARR timer
            Timeline arrTimer = keyTimelines.get(code);
            if (arrTimer != null) {
                arrTimer.stop();
                keyTimelines.remove(code);
            }
        });
    }

    private void handleKeyPress(KeyCode code) {
        switch (code) {
            case LEFT:
                currentBlock.moveLeft();
                if (!isValidPosition(currentBlock)) {
                    currentBlock.moveRight();
                }
                break;
            case RIGHT:
                currentBlock.moveRight();
                if (!isValidPosition(currentBlock)) {
                    currentBlock.moveLeft();
                }
                break;
            case DOWN:
                moveBlockDown();
                break;
            case UP:
                currentBlock.rotate();
                if (!isValidPosition(currentBlock)) {
                    currentBlock.rotateBack();
                }
                break;
            case SPACE:
                hardDrop();
                break;
            case C:
                saveBlock();
                break;
        }
        draw();
    }

    private void saveBlock() {
        if (!canSave || isGameOver) return;
        
        if (savedBlock == null) {
            savedBlock = currentBlock;
            spawnNewBlock();
        } else {
            Block temp = savedBlock;
            savedBlock = currentBlock;
            currentBlock = temp;
            currentBlock.setX(3);
            currentBlock.setY(0);
        }
        canSave = false;
    }
} 