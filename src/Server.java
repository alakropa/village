import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService service;
    private final List<ClientConnectionHandler> clients;

    public Server() {
        this.clients = new ArrayList<>();
    }

    public void start(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.service = Executors.newCachedThreadPool();

        while (true) {
            acceptConnection();
        }
    }

    public void acceptConnection() throws IOException {
        Socket clientSocket = this.serverSocket.accept();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out.write("Choose your name");
        out.newLine();
        out.flush();
        addClient(new ClientConnectionHandler(clientSocket, in.readLine()));
    }

    private void addClient(ClientConnectionHandler clientConnectionHandler) {
        this.clients.add(clientConnectionHandler);
        this.service.submit(clientConnectionHandler);
        broadcast(clientConnectionHandler.getNAME(), "joined the chat");
    }

    public void broadcast(String name, String message) {
        for (ClientConnectionHandler client : this.clients) {
            if (!client.NAME.equals(name)) {
                client.send(name + ": " + message);
            }
        }
    }

    public String listClients() {
        return this.clients.stream()
                .map(ClientConnectionHandler::getNAME)
                .reduce("", (a, b) -> a + "\n" + b);
    }

    public void removeClient(ClientConnectionHandler clientConnectionHandler) {
        try {
            clientConnectionHandler.CLIENT_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.clients.remove(clientConnectionHandler);
    }

    public Optional<ClientConnectionHandler> getClientByName(String name) {
        return this.clients.stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getNAME(), name))
                .findFirst();
    }

    public class ClientConnectionHandler implements Runnable {
        private final String NAME;
        private final Socket CLIENT_SOCKET;
        private final BufferedWriter OUT;
        private String message;

        public ClientConnectionHandler(Socket clientSocket, String name) throws IOException {
            this.CLIENT_SOCKET = clientSocket;
            this.NAME = name;
            this.OUT = new BufferedWriter(new OutputStreamWriter(this.CLIENT_SOCKET.getOutputStream()));
            System.out.println(this.NAME + ": joined the chat");
        }

        @Override
        public void run() {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(this.CLIENT_SOCKET.getInputStream()));
                while (!this.CLIENT_SOCKET.isClosed()) {
                    this.message = in.readLine();

                    if (isCommand(message.trim())) {
                        dealWithCommand(this.message);
                    } else {
                        broadcast(this.NAME, this.message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isCommand(String message) {
            return message.trim().startsWith("/");
        }

        private void dealWithCommand(String message) throws IOException {
            String commandReader = message.split(" ")[0];
            Command command = Command.getCommandFromDescription(commandReader);
            this.message = message;
            if (command != null) command.getHANDLER().command(Server.this, this);
            else broadcast(this.NAME, message);
        }

        public void send(String message) {
            try {
                this.OUT.write(message);
                this.OUT.newLine();
                this.OUT.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                this.CLIENT_SOCKET.close();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getNAME() {
            return this.NAME;
        }

        public String getMessage() {
            return this.message;
        }
    }
}

