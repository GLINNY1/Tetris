package com.fallingblocks;

import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FallingBlocksGame extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Create menu scene
        VBox menuRoot = new VBox(20);
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.setPadding(new Insets(20));
        menuRoot.setStyle("-fx-background-color: black;");

        Button singlePlayerButton = new Button("Single Player");
        Button hostGameButton = new Button("Host Game");
        Button joinGameButton = new Button("Join Game");
        
        singlePlayerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 200px;");
        hostGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 200px;");
        joinGameButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 20px; -fx-min-width: 200px;");

        menuRoot.getChildren().addAll(singlePlayerButton, hostGameButton, joinGameButton);

        Scene menuScene = new Scene(menuRoot, 400, 300);
        primaryStage.setTitle("Falling Blocks");
        primaryStage.setScene(menuScene);
        primaryStage.show();

        // Handle single player button click
        singlePlayerButton.setOnAction(e -> {
            GameBoard gameBoard = new GameBoard();
            gameBoard.start(primaryStage);
        });

        // Handle host game button click
        hostGameButton.setOnAction(e -> {
            GameBoard gameBoard = new GameBoard();
            gameBoard.start(primaryStage);
            gameBoard.startMultiplayer("localhost");
        });

        // Handle join game button click
        joinGameButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("localhost");
            dialog.setTitle("Join Game");
            dialog.setHeaderText("Enter Server IP Address");
            dialog.setContentText("IP Address:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(serverIP -> {
                GameBoard gameBoard = new GameBoard();
                gameBoard.start(primaryStage);
                gameBoard.startMultiplayer(serverIP);
            });
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
} 