package com.fallingblocks.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {
    private static final int PORT = 5000;
    private ServerSocket serverSocket;
    private Map<String, ClientHandler> clients;
    private ExecutorService pool;
    private boolean running;

    public GameServer() {
        clients = new ConcurrentHashMap<>();
        pool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String playerId;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.playerId = UUID.randomUUID().toString();
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Add client to the game
                clients.put(playerId, this);
                broadcastPlayerJoined(playerId);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Handle game state updates
                    broadcastGameState(playerId, inputLine);
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void broadcastPlayerJoined(String playerId) {
            String message = "PLAYER_JOINED:" + playerId;
            broadcast(message);
        }

        private void broadcastGameState(String playerId, String gameState) {
            String message = "GAME_STATE:" + playerId + ":" + gameState;
            broadcast(message);
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients.values()) {
                if (client != this) {
                    client.out.println(message);
                }
            }
        }

        private void cleanup() {
            try {
                clients.remove(playerId);
                broadcast("PLAYER_LEFT:" + playerId);
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer();
        server.start();
    }
} 