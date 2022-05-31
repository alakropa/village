package game;

import game.command.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService service;
    private HashSet<PlayerHandler> players;

    public Server() {
        this.players = new HashSet<>();
    }

    public void start(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.service = Executors.newCachedThreadPool();

        int number = 0;
        while (true) {
            acceptConnection(number);
            number++;
        }
    }

    public void acceptConnection(int number) throws IOException {
        //O método .accept() só pode ser utilizado se:
        //  - Se houver 12 jogadores
        //  - Se o jogo já estiver a decorrer
        //Opcional: Haver dois ou mais jogos em simultâneo
        Socket playerSocket = this.serverSocket.accept();
        addPlayer(new PlayerHandler(playerSocket, "CLIENT " + number));
    }

    private void addPlayer(PlayerHandler playerHandler) {
        this.players.add(playerHandler);
        this.service.submit(playerHandler);
        chat(playerHandler.NAME, "joined the chat");
    }

    public void chat(String name, String message) {
        for (PlayerHandler client : this.players) {
            if (!client.NAME.equals(name)) {
                client.send(name + ": " + message);
            }
        }
    }

    public String playersInGame() {
        return this.players.stream()
                .map(x -> x.NAME + " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("", (a, b) -> a + "\n" + b);
        //Adicionar estado (alive ou dead)
    }

    public void removePlayer(PlayerHandler playerHandler) {
        try {
            playerHandler.CLIENT_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.players.remove(playerHandler);
    }

    private void startGame() {
        EnumRole[] roles = EnumRole.values();
        // Só um dos jogadores faz /start e o jogo começa
        // Adicionar bots necessários
    }

    private boolean verifyIfGameCanStart() {
        return false;
    }

    private void verifyConnectedPlayers() {

    }

    private void verifyIfGameContinues() {

    }

    private void play() {
        //Responsável pelo desenrolar de to_do o jogo. OBRA DE ARTE!!!
        //Chama as funções todas (como startGame, removePlayer, etc.)
    }

    /*
        public Optional<ClientConnectionHandler> getClientByName(String name) {
            return this.clients.stream()
                    .filter(x -> Helpers.compareIfNamesMatch(x.getNAME(), name))
                    .findFirst();
        }
    */






    public class PlayerHandler implements Runnable {
        private final String NAME;
        private final Socket CLIENT_SOCKET;
        private final BufferedWriter OUT;
        private String message;
        private boolean alive;
        private EnumRole role;

        public PlayerHandler(Socket clientSocket, String name) throws IOException {
            this.CLIENT_SOCKET = clientSocket;
            this.NAME = name;
            this.OUT = new BufferedWriter(new OutputStreamWriter(this.CLIENT_SOCKET.getOutputStream()));
            this.alive = true;
        }

        @Override
        public void run() {
            //play?
            /*
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(this.CLIENT_SOCKET.getInputStream()));
                while (!this.CLIENT_SOCKET.isClosed()) {
                    this.message = in.readLine();

                    if (isCommand(message.trim())) {
                        dealWithCommand(this.message);
                    } else {
                        chat(this.NAME, this.message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }

        private boolean isCommand(String message) {
            return message.trim().startsWith("/");
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

        private void dealWithCommand(String message) throws IOException {
            Command command = Command.getCommandFromDescription(message.split(" ", 2)[0]);
            if (command == null) return;
            command.getHANDLER().command(Server.this, this);

        }

        public String getNAME() {
            return this.NAME;
        }

        public String getMessage() {
            return this.message;
        }

        private void killPlayer() {
            this.alive = false;
        }
    }
}

