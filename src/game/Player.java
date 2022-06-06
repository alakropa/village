package game;

import java.io.*;
import java.net.Socket;

public class Player {

    public static void main(String[] args) {
        Player player = new Player();
        try {
            player.start("localhost", 8080);
        } catch (IOException e) {
            System.out.println("The server is down");
        }
    }

    public void start(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        new Thread(new KeyboardHandler(socket)).start();
    }

    private static class KeyboardHandler implements Runnable {
        private final Socket SOCKET;
        BufferedReader consoleReader;
        BufferedReader in;
        BufferedWriter out;

        KeyboardHandler(Socket socket) {
            this.SOCKET = socket;
            this.consoleReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            new Thread(() -> {
                while (!this.SOCKET.isClosed()) {
                    try {
                        String message = this.in.readLine();
                        if (message == null) {
                            this.SOCKET.close();
                            break;
                        }
                        System.out.println(message);
                    } catch (IOException e) {
                        try {
                            System.out.println("You were disconnected from chat");
                            this.SOCKET.close();
                            Thread.currentThread().interrupt();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }).start();

            while (!this.SOCKET.isClosed()) {
                try {
                    String message = this.consoleReader.readLine();
                    this.out.write(message);
                    this.out.newLine();
                    this.out.flush();

                    if (message.split(" ")[0].equals("/quit")) {
                        this.SOCKET.close();
                    }
                } catch (IOException e) {
                    System.out.println("You were disconnected from chat");
                }
            }
        }
    }
}
