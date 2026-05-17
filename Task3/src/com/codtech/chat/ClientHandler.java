package com.codtech.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

/**
 * Handles communication with a single client in its own thread.
 * Receives messages from this client and broadcasts them to others.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<ClientHandler> clients;
    private PrintWriter out;
    private String username = "Anonymous";

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            this.out = writer;

            // Ask for username
            writer.println("Welcome to CODTECH Chat!");
            writer.print("Enter your username: ");
            writer.flush();
            String nameInput = in.readLine();
            if (nameInput != null && !nameInput.trim().isEmpty()) {
                username = nameInput.trim();
            }

            broadcast("** " + username + " has joined the chat **");

            writer.println("Type your messages and press Enter to send.");
            writer.println("Type /quit to leave the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                if ("/quit".equalsIgnoreCase(message.trim())) {
                    break;
                }

                if (!message.trim().isEmpty()) {
                    broadcast("[" + username + "]: " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error with client " + username + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * Sends a message to this client.
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     */
    private void broadcast(String message) {
        System.out.println(message);
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    /**
     * Clean up resources and notify others when a client leaves.
     */
    private void cleanup() {
        try {
            clients.remove(this);
            broadcast("** " + username + " has left the chat **");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }
}

