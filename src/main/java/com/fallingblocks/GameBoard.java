package com.fallingblocks;

import java.util.HashMap;
import java.util.Map;

import com.fallingblocks.network.GameClient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameBoard extends Pane {
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
    private GameClient gameClient;
    private Map<String, int[][]> opponentGrids;
    private boolean isMultiplayer;
    private Pane root;
    private boolean waitingForStart = false;

    public GameBoard() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        keyTimelines = new HashMap<>();
        keyPressed = new HashMap<>();
        dasTimelines = new HashMap<>();
        isGameOver = false;
        canSave = true;
        opponentGrids = new HashMap<>();
        isMultiplayer = false;
        spawnNewBlock();
    }

    public void start(Stage stage) {
        this.stage = stage;
        root = new Pane();
        canvas = new Canvas((GRID_WIDTH + 5) * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        Scene scene = new Scene(root);
        setupInputHandlers(scene);
        stage.setScene(scene);
        stage.setTitle("Falling Blocks");
        stage.show();

        if (!isMultiplayer) {
            startGameLoop();
        }
    }

    public void startMultiplayer(String serverIP) {
        isMultiplayer = true;
        waitingForStart = true;
        gameClient = new GameClient(
            serverIP,
            this::handleGameStateUpdate,
            this::handlePlayerJoined,
            this::handlePlayerLeft,
            (ignored) -> Platform.runLater(() -> {
                waitingForStart = false;
                startGameLoop();
            })
        );
        
        if (gameClient.connect()) {
            System.out.println("Connected to multiplayer server at " + serverIP);
        } else {
            System.out.println("Failed to connect to multiplayer server at " + serverIP);
            isMultiplayer = false;
        }
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
            if (isMultiplayer) {
                sendGameState();
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

        // If waiting for multiplayer start, show waiting message
        if (isMultiplayer && waitingForStart) {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 40));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Waiting for another player...", canvas.getWidth() / 2, canvas.getHeight() / 2);
            return;
        }

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

        // Draw opponent grids
        if (isMultiplayer) {
            for (Map.Entry<String, int[][]> entry : opponentGrids.entrySet()) {
                int[][] opponentGrid = entry.getValue();
                for (int i = 0; i < GRID_HEIGHT; i++) {
                    for (int j = 0; j < GRID_WIDTH; j++) {
                        if (opponentGrid[i][j] != 0) {
                            drawOpponentCell(j, i, opponentGrid[i][j]);
                        }
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
        if (isGameOver) return;
        
        switch (code) {
            case LEFT:
                moveLeft();
                break;
            case RIGHT:
                moveRight();
                break;
            case DOWN:
                moveDown();
                break;
            case UP:
                rotate();
                break;
            case SPACE:
                dropDown();
                break;
            case ESCAPE:
                showPauseMenu();
                break;
            case C:
                saveBlock();
                break;
        }
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

    private void handleGameStateUpdate(String gameState) {
        // Parse the game state update from opponent
        String[] parts = gameState.split(",");
        String playerId = parts[0];
        int[][] opponentGrid = new int[GRID_HEIGHT][GRID_WIDTH];
        
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                opponentGrid[i][j] = Integer.parseInt(parts[1 + i * GRID_WIDTH + j]);
            }
        }
        
        opponentGrids.put(playerId, opponentGrid);
        draw(); // Redraw to show opponent's grid
    }

    private void handlePlayerJoined(String playerId) {
        System.out.println("Player joined: " + playerId);
    }

    private void handlePlayerLeft(String playerId) {
        System.out.println("Player left: " + playerId);
        opponentGrids.remove(playerId);
        draw(); // Redraw to remove opponent's grid
    }

    private void sendGameState() {
        if (isMultiplayer && gameClient != null && gameClient.isConnected()) {
            StringBuilder state = new StringBuilder();
            for (int i = 0; i < GRID_HEIGHT; i++) {
                for (int j = 0; j < GRID_WIDTH; j++) {
                    state.append(grid[i][j]).append(",");
                }
            }
            gameClient.sendGameState(state.toString());
        }
    }

    private void drawOpponentCell(int x, int y, int color) {
        gc.setFill(getColorForValue(color).deriveColor(0, 1, 1, 0.5)); // Semi-transparent
        gc.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        gc.setStroke(Color.GRAY);
        gc.strokeRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
    }

    private void showPauseMenu() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
        VBox pauseMenu = new VBox(10);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20;");
        
        Label titleLabel = new Label("PAUSED");
        titleLabel.setStyle("-fx-font-size: 24; -fx-text-fill: white;");
        
        Button resumeButton = new Button("Resume");
        Button restartButton = new Button("Restart");
        Button quitButton = new Button("Quit");
        
        // Style the buttons
        String buttonStyle = "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14; -fx-padding: 10 20; -fx-min-width: 120;";
        resumeButton.setStyle(buttonStyle);
        restartButton.setStyle(buttonStyle);
        quitButton.setStyle(buttonStyle);
        
        resumeButton.setOnAction(e -> {
            root.getChildren().remove(pauseMenu);
            if (gameLoop != null) {
                gameLoop.play();
            }
        });
        
        restartButton.setOnAction(e -> {
            root.getChildren().remove(pauseMenu);
            restart();
            if (gameLoop != null) {
                gameLoop.play();
            }
        });
        
        quitButton.setOnAction(e -> {
            Platform.exit();
        });
        
        pauseMenu.getChildren().addAll(titleLabel, resumeButton, restartButton, quitButton);
        pauseMenu.setMaxWidth(200);
        
        // Center the pause menu on the screen
        pauseMenu.setLayoutX((canvas.getWidth() - pauseMenu.getMaxWidth()) / 2);
        pauseMenu.setLayoutY((canvas.getHeight() - pauseMenu.getHeight()) / 2);
        
        root.getChildren().add(pauseMenu);
    }

    private void moveLeft() {
        if (currentBlock != null) {
            currentBlock.moveLeft();
            if (!isValidPosition(currentBlock)) {
                currentBlock.moveRight();
            }
            draw();
        }
    }

    private void moveRight() {
        if (currentBlock != null) {
            currentBlock.moveRight();
            if (!isValidPosition(currentBlock)) {
                currentBlock.moveLeft();
            }
            draw();
        }
    }

    private void moveDown() {
        if (currentBlock != null) {
            moveBlockDown();
            draw();
        }
    }

    private void rotate() {
        if (currentBlock != null) {
            currentBlock.rotate();
            if (!isValidPosition(currentBlock)) {
                // Try wall kicks: shift left or right by 1, 2, or 3
                boolean kicked = false;
                for (int dx : new int[]{-1, 1, -2, 2, -3, 3}) {
                    currentBlock.setX(currentBlock.getX() + dx);
                    if (isValidPosition(currentBlock)) {
                        kicked = true;
                        break;
                    }
                    currentBlock.setX(currentBlock.getX() - dx); // revert if not valid
                }
                if (!kicked) {
                    currentBlock.rotateBack(); // revert rotation if no kick works
                }
            }
            draw();
        }
    }

    private void dropDown() {
        if (currentBlock != null) {
            hardDrop();
            draw();
        }
    }
} 