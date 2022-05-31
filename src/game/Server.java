package game;

import game.command.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    }

    public void removePlayer(PlayerHandler playerHandler) {
        try {
            playerHandler.CLIENT_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.players.remove(playerHandler);
    }

    public void startGame() {
        // Só um dos jogadores faz /start e o jogo começa
        // Adicionar bots necessários
        //lista dos jogadores
        chat("Welcome to a new game", "SPOOKY VILLAGE!" );
        chat("A list of players starting the game", playersInGame());

       ArrayList <EnumRole> roles = new ArrayList <> (players.size());
        for (int i = 0; i < roles.size(); i++) {
            switch (i) {
                case 0:
                case 6:
                case 11:
                    roles.add(i, EnumRole.WOLF);
                break;
                case 1:
                case 9:
                    roles.add(i, EnumRole.FORTUNE_TELLER);
                break;
                default: roles.add(i, EnumRole.VILLAGER);
            }
            }
        Collections.shuffle(roles);

        for (int i = 0; i < this.players.size(); i++) {
            chat("Here's your role", roles.get(i).toString());
        }

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

