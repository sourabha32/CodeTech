// File: FileHandlingUtility.java
// CODTECH Internship - Task 1
// Simple file handling utility to create, read, write, and modify text files.

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class FileHandlingUtility {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== CODTECH File Handling Utility ===");

        while (true) {
            printMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1 -> createOrOverwriteFile();
                case 2 -> appendToFile();
                case 3 -> readFile();
                case 4 -> replaceTextInFile();
                case 5 -> {
                    System.out.println("Exiting. Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
            System.out.println(); // blank line between operations
        }
    }

    // -------- MENU & INPUT HELPERS --------

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Create / Overwrite a text file");
        System.out.println("2. Append text to a file");
        System.out.println("3. Read and display a file");
        System.out.println("4. Find & replace text inside a file");
        System.out.println("5. Exit");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    // -------- CORE FILE OPERATIONS --------

    /**
     * Creates a new text file or overwrites an existing one
     * with user-provided content.
     */
    private static void createOrOverwriteFile() {
        String fileName = readLine("Enter file name (e.g., notes.txt): ");
        Path path = Paths.get(fileName);

        System.out.println("Enter content for the file (end with an empty line):");
        StringBuilder contentBuilder = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break; // empty line ends input
            }
            contentBuilder.append(line).append(System.lineSeparator());
        }

        try {
            Files.writeString(path, contentBuilder.toString(), StandardCharsets.UTF_8);
            System.out.println("File saved successfully at: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    /**
     * Appends text to an existing file. If the file does not exist,
     * the user is informed and no new file is created.
     */
    private static void appendToFile() {
        String fileName = readLine("Enter existing file name to append to: ");
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + path.toAbsolutePath());
            return;
        }

        System.out.println("Enter text to append (end with an empty line):");
        StringBuilder contentBuilder = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.isEmpty()) {
                break;
            }
            contentBuilder.append(line).append(System.lineSeparator());
        }

        try {
            Files.writeString(
                    path,
                    contentBuilder.toString(),
                    StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.APPEND
            );
            System.out.println("Content appended successfully.");
        } catch (IOException e) {
            System.out.println("Error appending to file: " + e.getMessage());
        }
    }

    /**
     * Reads the content of a text file and prints it to the console.
     */
    private static void readFile() {
        String fileName = readLine("Enter file name to read: ");
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + path.toAbsolutePath());
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            System.out.println("----- File Content -----");
            if (lines.isEmpty()) {
                System.out.println("[File is empty]");
            } else {
                for (String line : lines) {
                    System.out.println(line);
                }
            }
            System.out.println("------------------------");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    /**
     * Replaces all occurrences of a target string with a replacement
     * string inside the given file.
     */
    private static void replaceTextInFile() {
        String fileName = readLine("Enter file name to modify: ");
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            System.out.println("File does not exist: " + path.toAbsolutePath());
            return;
        }

        String target = readLine("Enter text to find: ");
        if (target.isEmpty()) {
            System.out.println("Search text cannot be empty.");
            return;
        }
        String replacement = readLine("Enter replacement text: ");

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (!content.contains(target)) {
                System.out.println("The specified text was not found in the file.");
                return;
            }

            String updatedContent = content.replace(target, replacement);
            Files.writeString(path, updatedContent, StandardCharsets.UTF_8);
            System.out.println("Text replaced successfully.");
        } catch (IOException e) {
            System.out.println("Error modifying file: " + e.getMessage());
        }
    }
}

