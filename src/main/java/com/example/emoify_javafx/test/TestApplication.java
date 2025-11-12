package com.example.emoify_javafx.test;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestApplication{

    private static Process backendProcess;

    public static void startBackend() throws IOException {
        String backendPath = "C:\\Users\\rpras\\OneDrive\\Documents\\Rashmitha\\Semester_7\\project\\Desktop_app\\Emoify_javafx\\src\\main\\java\\com\\example\\emoify_javafx\\app.exe"; // or absolute path
        ProcessBuilder pb = new ProcessBuilder(backendPath);
        pb.redirectErrorStream(true);
        backendProcess = pb.start();

        System.out.println("Backend started!");
    }

    public static void stopBackend() {
        if (backendProcess != null && backendProcess.isAlive()) {
            ProcessHandle handle = backendProcess.toHandle();
            handle.descendants().forEach(ProcessHandle::destroy); // Kill all child processes
            handle.destroy();

            try {
                if (!backendProcess.waitFor(3, TimeUnit.SECONDS)) {
                    handle.descendants().forEach(ProcessHandle::destroyForcibly);
                    handle.destroyForcibly();
                }
                System.out.println("Backend stopped!");
            } catch (InterruptedException e) {
                handle.descendants().forEach(ProcessHandle::destroyForcibly);
                handle.destroyForcibly();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Testing app!");
        startBackend();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Press 'W' and hit Enter to continue...");

        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("w")) {
                stopBackend();
                break;
            } else {
                System.out.println("Not W, try again...");
            }
        }
        scanner.close();
    }
}

