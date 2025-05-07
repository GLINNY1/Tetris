package com.fallingblocks.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {
    private static final int SERVER_PORT = 5000;
    
    private String serverIP;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String playerId;
    private Consumer<String> onGameStateUpdate;
    private Consumer<String> onPlayerJoined;
    private Consumer<String> onPlayerLeft;
    private Consumer<Runnable> onStartGame;
    private boolean connected;

    public GameClient(String serverIP,
                     Consumer<String> onGameStateUpdate, 
                     Consumer<String> onPlayerJoined,
                     Consumer<String> onPlayerLeft,
                     Consumer<Runnable> onStartGame) {
        this.serverIP = serverIP;
        this.onGameStateUpdate = onGameStateUpdate;
        this.onPlayerJoined = onPlayerJoined;
        this.onPlayerLeft = onPlayerLeft;
        this.onStartGame = onStartGame;
    }

    public boolean connect() {
        try {
            socket = new Socket(serverIP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;

            // Start listening for server messages
            new Thread(this::listenForMessages).start();
            return true;
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
            return false;
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleMessage(String message) {
        System.out.println("Received from server: " + message); // Debug log
        if (message.equals("START")) {
            System.out.println("Received START from server. Starting game loop.");
            if (onStartGame != null) {
                onStartGame.accept(() -> {});
            }
            return;
        }
        String[] parts = message.split(":");
        if (parts.length < 2) {
            System.err.println("Malformed message from server: " + message);
            return;
        }
        String type = parts[0];
        String playerId = parts[1];

        switch (type) {
            case "PLAYER_JOINED":
                onPlayerJoined.accept(playerId);
                break;
            case "PLAYER_LEFT":
                onPlayerLeft.accept(playerId);
                break;
            case "GAME_STATE":
                if (parts.length > 2) {
                    String gameState = parts[2];
                    onGameStateUpdate.accept(gameState);
                }
                break;
            default:
                System.err.println("Unknown message type: " + type);
        }
    }

    public void sendGameState(String gameState) {
        if (connected && out != null) {
            out.println(gameState);
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
} 