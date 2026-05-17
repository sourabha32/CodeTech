## CODTECH Internship – Task 3: Multithreaded Chat Application

This project is a simple **multithreaded client–server chat application in Java**.
It uses **TCP sockets** and **threads** on the server to handle multiple clients
communicating in real time via the console.

### Project Structure

- **src/**
  - **com/codtech/chat/**
    - `Server.java` – Starts the chat server and accepts clients.
    - `ClientHandler.java` – Handles a single connected client in its own thread.
    - `Client.java` – Console client that connects to the server.

### Requirements

- **Java 8+ JDK** installed and available on your PATH (`java`, `javac`).

### How to Compile

Open a terminal in the `Task3` folder and run:

```bash
javac -d out src/com/codtech/chat/*.java
```

This will compile all Java files into the `out` directory (created automatically).

### How to Run

1. **Start the server** (in one terminal window):

```bash
cd out
java com.codtech.chat.Server
```

By default, the server listens on port **12345**.

2. **Start clients** (each in a separate terminal/command prompt):

```bash
cd out
java com.codtech.chat.Client
```

When prompted, enter:

- A **username**.
- The **server host** (press Enter for default `localhost`).
- The **server port** (press Enter for default `12345`).

3. Type messages in any client terminal and press Enter to send.
   All connected clients will see the broadcast messages.

4. To disconnect a client, type:

```text
/quit
```

### Notes

- This is a **console-based demo** focusing on Java sockets and multithreading.
- You can change the default port or host in `Server.java` and `Client.java`
  if needed.

