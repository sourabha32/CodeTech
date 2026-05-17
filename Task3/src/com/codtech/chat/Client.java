package com.codtech.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Simple console-based chat client that connects to the Server.
 */
public class Client {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 12345;

    public static void main(String[] args) {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        try {
            System.out.print("Enter username: ");
            String username = consoleReader.readLine();
            if (username == null || username.trim().isEmpty()) {
                username = "Anonymous";
            }

            System.out.print("Enter server host (default: " + DEFAULT_HOST + "): ");
            String hostInput = consoleReader.readLine();
            String host = (hostInput == null || hostInput.trim().isEmpty()) ? DEFAULT_HOST : hostInput.trim();

            System.out.print("Enter server port (default: " + DEFAULT_PORT + "): ");
            String portInput = consoleReader.readLine();
            int port = DEFAULT_PORT;
            if (portInput != null && !portInput.trim().isEmpty()) {
                try {
                    port = Integer.parseInt(portInput.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port, using default: " + DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            }

            System.out.println("Connecting to chat server " + host + ":" + port + "...");

            Socket socket = new Socket(host, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Thread to read messages from server
            Thread readerThread = new Thread(() -> {
                String serverMsg;
                try {
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            readerThread.setDaemon(true);
            readerThread.start();

            // Send username first as expected by server
            out.println(username);

            // Main loop: read from console and send to server
            String userInput;
            while ((userInput = consoleReader.readLine()) != null) {
                out.println(userInput);
                if ("/quit".equalsIgnoreCase(userInput.trim())) {
                    break;
                }
            }

            System.out.println("Closing connection...");
            socket.close();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }
}

