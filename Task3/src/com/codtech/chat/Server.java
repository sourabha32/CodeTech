package com.codtech.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Chat server that accepts multiple clients and broadcasts messages
 * to all connected clients.
 */
public class Server {

    // Thread-safe set of all active client handlers
    private static final Set<ClientHandler> clients =
            Collections.synchronizedSet(new HashSet<>());

    // Default port for the chat server
    public static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Allow custom port as first command-line argument
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        System.out.println("Starting chat server on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                clients.add(clientHandler);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

