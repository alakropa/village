package game.Server;

import game.EnumRole;
import game.Helpers;
import game.command.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService service;
    private HashMap<String, PlayerHandler> players;
    private boolean gameInProgress;
    private boolean timesUp;
    private boolean night;

    public Server() {
        this.players = new HashMap<>();
        this.gameInProgress = false;
        this.timesUp = false;
        this.night = false;
    }

    public void start(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.service = Executors.newCachedThreadPool();

        while (true) {
            acceptConnection();
        }
    }

    public void acceptConnection() throws IOException {
        //Opcional: Haver dois ou mais jogos em simultâneo
        Socket playerSocket = this.serverSocket.accept();
        if (!this.gameInProgress && this.players.size() < 12) {
            new Thread(() -> { //serve para varios jogadores poderem escrever o nome ao mesmo tempo
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                    BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                    out.write("Write your name");
                    out.newLine();
                    out.flush();

                    //addPlayer(new PlayerHandler(playerSocket, playerName));
                    //System.out.println(playerName + " entered the chat"); //consola do servidor
                    //String playerName = in.readLine(); //fica à espera do nome

                    if (!this.gameInProgress && this.players.size() < 12) {
                        String playerName = verifyIsNameIsAvailable(playerSocket, out, in); //add no HashMap

                        System.out.println(playerName + " entered the chat"); //consola do servidor

                        out.write(Command.getCommandList());
                        out.newLine();
                        out.flush();
                    } else {
                        out.write("The game is unavailable");
                        out.newLine();
                        out.flush();
                        playerSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            playerSocket.close();
        }
    }

    private String verifyIsNameIsAvailable() {
        return "";
    }

    private String verifyIsNameIsAvailable(Socket playerSocket, BufferedWriter out, BufferedReader in) throws IOException {
        String playerName = in.readLine(); //fica à espera do nome
        while (!checkIfNameIsAvailable(playerName)) { //false
            out.write("This name already exists, try another name");
            out.newLine();
            out.flush();
            playerName = in.readLine();
        }
        addPlayer(new PlayerHandler(playerSocket, playerName));
        return playerName;
    }


    private boolean checkIfNameIsAvailable(String playerName) {
        // BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        // BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
        for (PlayerHandler player : this.players.values()) {
            if (player.name.equals(playerName)) {
                return false;
            }
        }
        return true;
    }

    private void addPlayer(PlayerHandler playerHandler) {
//        while (!this.players.put(playerHandler.name, playerHandler)) { //(!this.players.add(playerHandler))
//            try {
//                playerHandler.send("Player name is already taken\nWrite a new name: ");
//                BufferedReader in = new BufferedReader(new InputStreamReader(playerHandler.PLAYER_SOCKET.getInputStream()));
//                playerHandler.name = in.readLine();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        this.players.put(playerHandler.name, playerHandler);
        this.service.submit(playerHandler); //mandar para a threadpool
        chat(playerHandler.name, "joined the chat"); //msg para os outros players
    }

    public void chat(String name, String message) {
        for (PlayerHandler client : this.players.values()) {
            if (!client.name.equals(name)) {
                client.send(name + ": " + message);
            }
        }
    }

    public void wolvesChat(String name, String message) {
        this.players.values().stream()
                .filter(x -> x.role == EnumRole.WOLF && !x.name.equals(name))
                .forEach(x -> x.send(message));
    }

    public void sendPrivateMessage(String name, String message) {
        for (PlayerHandler client : this.players.values()) {
            if (client.name.equals(name)) {
                client.send(message);
            }
        }
    }

    public String playersInGame() {
        return this.players.values().stream()
                .map(x -> x.name + " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
        //Adicionar estado (alive ou dead)
    }

    public void removePlayer(PlayerHandler playerHandler) {
        try {
            playerHandler.PLAYER_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.players.remove(playerHandler.name, playerHandler);
    }


    public void startGame() {
        // Só um dos jogadores faz /start e o jogo começa
        // Adicionar bots necessários
        //lista dos jogadores
        chat("Welcome to a new game", "SPOOKY VILLAGE!");
        chat("A list of players starting the game", playersInGame());


        ArrayList<EnumRole> roles = generateEnumCards();
        Collections.shuffle(roles);

        List<PlayerHandler> playersList = new ArrayList<>(this.players.values());
        for (int i = 0; i < playersList.size(); i++) {
            EnumRole newRole = roles.get(i);
            sendPrivateMessage(playersList.get(i).name, "Your role is " + newRole.toString());
            playersList.get(i).role = newRole;
        }
        play();
    }

    private ArrayList<EnumRole> generateEnumCards() {
        ArrayList<EnumRole> roles = new ArrayList<>(players.size());
        for (int i = 0; i < this.players.size(); i++) {
            switch (i) {
                case 0, 6, 11 -> roles.add(i, EnumRole.WOLF);
                case 1, 9 -> roles.add(i, EnumRole.FORTUNE_TELLER);
                default -> roles.add(i, EnumRole.VILLAGER);
            }
        }
        return roles;
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

    public Optional<PlayerHandler> getPlayerByName(String name) {
        return this.players.values().stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getName(), name))
                .findFirst();
    }

    private void resetNumberOfVotes() {
        this.players.values().forEach(x -> x.numberOfVotes = 0);
    }

    private void timer(int setTime) {
        new Thread(() -> {
            try {
                Thread.sleep(setTime);
                this.timesUp = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendUpdateOfVotes() {
        chat("Current score", players.values().stream()
                .filter(player -> player.alive)
                .map(player -> player.name + " " + player.numberOfVotes)
                .reduce("", (a, b) -> a + "\n" + b));
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public boolean isNight() {
        return night;
    }

    private boolean gameContinues() {
        return true;
    }

    public class PlayerHandler implements Runnable {
        private String name;
        private final Socket PLAYER_SOCKET;
        private final BufferedWriter OUT;
        private final BufferedReader IN;
        private String message;
        private boolean alive;
        private EnumRole role;
        private int numberOfVotes;
        private PlayerHandler vote;
        //private HashMap<String, Boolean> visions;

        public PlayerHandler(Socket clientSocket, String name) throws IOException {
            this.PLAYER_SOCKET = clientSocket;
            this.name = name;
            this.OUT = new BufferedWriter(new OutputStreamWriter(this.PLAYER_SOCKET.getOutputStream()));
            this.IN = new BufferedReader(new InputStreamReader(this.PLAYER_SOCKET.getInputStream()));
            this.alive = true;
        }

        @Override
        public void run() {
            //play?
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(this.PLAYER_SOCKET.getInputStream()));
                while (!this.PLAYER_SOCKET.isClosed()) {
                    this.message = in.readLine();
                    System.out.println(name + ": " + this.message); //imprime no server as msg q recebe dos clients

                    if (Server.this.night) {
                        switch (this.role) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.name, this.message);
                            }
                            case FORTUNE_TELLER -> dealWithCommand(this.message);
                            default -> send("You are sleeping");
                        }
                        continue;
                    }

                    if (isCommand(message.trim())) {
                        dealWithCommand(this.message);
                    } else {
                        chat(this.name, this.message);
                    }
                }
            } catch (IOException e) {
                playerDisconnected();
            }
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

        private void dealWithCommand(String message) throws IOException {
            Command command = Command.getCommandFromDescription(message.split(" ")[0]);
            if (command == null) {
                send("Command unavailable");
                return;
            }
            command.getHANDLER().command(Server.this, this);
        }

        public String getName() {
            return this.name;
        }

        public String getMessage() {
            return this.message;
        }

        public void killPlayer() {
            this.alive = false;
        }

        public void increaseNumberOfVotes() {
            this.numberOfVotes++;
        }

        public BufferedReader getIN() {
            return IN;
        }

        public EnumRole getRole() {
            return role;
        }

        public void setVote(PlayerHandler vote) {
            this.vote = vote;
        }

        public void playerDisconnected() {
            try {
                chat(this.name, " disconnected");
                this.PLAYER_SOCKET.close();
                Server.this.players.remove(this.name, this);
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
