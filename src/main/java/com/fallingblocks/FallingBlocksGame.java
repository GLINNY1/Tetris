package com.fallingblocks;

import javafx.application.Application;
import javafx.stage.Stage;

public class FallingBlocksGame extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameBoard gameBoard = new GameBoard();
        gameBoard.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 