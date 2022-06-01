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
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService service;
    private final HashMap<String, PlayerHandler> PLAYERS;
    private boolean gameInProgress;
    private boolean night;
    private List<PlayerHandler> wolvesVotes;

    private int numOfDays;

    public Server() {
        this.PLAYERS = new HashMap<>();
        this.gameInProgress = false;
        this.night = false;
        this.wolvesVotes = new ArrayList<>();
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
        if (!this.gameInProgress && this.PLAYERS.size() < 12) {
            new Thread(() -> { //serve para varios jogadores poderem escrever o nome ao mesmo tempo
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
                    BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                    out.write("Write your name");
                    out.newLine();
                    out.flush();
                    String playerName = verifyIfNameIsAvailable(playerSocket, out, in);

                    //addPlayer(new PlayerHandler(playerSocket, playerName));
                    //System.out.println(playerName + " entered the chat"); //consola do servidor
                    //String playerName = in.readLine(); //fica à espera do nome

                    if (!this.gameInProgress && this.PLAYERS.size() < 12) {
                        System.out.println(playerName + " entered the chat"); //consola do servidor
                        addPlayer(new PlayerHandler(playerSocket, playerName));
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

    private String verifyIfNameIsAvailable(Socket playerSocket, BufferedWriter out, BufferedReader in) throws IOException {
        String playerName = in.readLine(); //fica à espera do nome
        while (!checkIfNameIsAvailable(playerName)) { //false
            out.write("This name already exists, try another name");
            out.newLine();
            out.flush();
            playerName = in.readLine();
        }
        return playerName;
    }


    private boolean checkIfNameIsAvailable(String playerName) {
        // BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        // BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.name.equals(playerName)) {
                return false;
            }
        }
        return true;
    }

    private void addPlayer(PlayerHandler playerHandler) {
        this.PLAYERS.put(playerHandler.name, playerHandler);
        this.service.submit(playerHandler); //mandar para a threadpool
        chat(playerHandler.name, "joined the chat"); //msg para os outros players
    }

    public void chat(String name, String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            if (!client.name.equals(name)) {
                client.send(name + ": " + message);
            }
        }
    }

    public void chat(String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            client.send(message);
        }
    }

    public void wolvesChat(String name, String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.role == EnumRole.WOLF && !x.name.equals(name))
                .forEach(x -> x.send(name + ": " + message));
    }

    public void wolvesChat(String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.role == EnumRole.WOLF)
                .forEach(x -> x.send(message));
    }

    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.name + " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    public void removePlayer(PlayerHandler playerHandler) {
        try {
            playerHandler.PLAYER_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.PLAYERS.remove(playerHandler.name, playerHandler);
    }

    public void sendPrivateMessage(String name, String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            if (client.name.equals(name)) {
                client.send(message);
            }
        }
    }

    public void startGame() {
        // Adicionar bots necessários
        System.out.println("night: " + this.night);
        //chat(displayVillageImage());
        chat(displayVillageImage2());
        chat(displayVillageImage3());
        chat("A list of players starting the game", playersInGame());

        ArrayList<EnumRole> roles = generateEnumCards();
        Collections.shuffle(roles);

        List<PlayerHandler> playersList = new ArrayList<>(this.PLAYERS.values());
        for (int i = 0; i < playersList.size(); i++) {
            EnumRole newRole = roles.get(i);
            sendPrivateMessage(playersList.get(i).name, "Your role is " + newRole.toString());
            playersList.get(i).role = newRole;
        }
        play();
    }

    private String displayVillageImage() {
        String villageImage = "" +
                " __        __   _                                  _          _   _          \n" +
                " \\ \\      / /__| | ___ ___  _ __ ___   ___        | |_ ___   | |_| |__   ___ \n" +
                "  \\ \\ /\\ / / _ \\ |/ __/ _ \\| '_ ` _ \\ / _ \\       | __/ _ \\  | __| '_ \\ / _ \\\n" +
                "   \\ V  V /  __/ | (_| (_) | | | | | |  __/_ _ _  | || (_) | | |_| | | |  __/\n" +
                "  __\\_/\\_/ \\___|_|\\___\\___/|_| |_| |_|\\___(_|_|_)_ \\__\\___/   \\__|_| |_|\\___|\n" +
                " / ___| _ __   ___   ___ | | ___   _  \\ \\   / (_) | | __ _  __ _  ___| |     \n" +
                " \\___ \\| '_ \\ / _ \\ / _ \\| |/ / | | |  \\ \\ / /| | | |/ _` |/ _` |/ _ \\ |     \n" +
                "  ___) | |_) | (_) | (_) |   <| |_| |   \\ V / | | | | (_| | (_| |  __/_|     \n" +
                " |____/| .__/ \\___/ \\___/|_|\\_\\\\__, |    \\_/  |_|_|_|\\__,_|\\__, |\\___(_)     \n" +
                "       |_|                     |___/                       |___/             ";

        return villageImage;
    }

    private String displayVillageImage2() {
        String villageImage = "                                                               \n" +
                "                _ _ _     _                            _          _   _       \n" +
                "               | | | |___| |___ ___ _____ ___         | |_ ___   | |_| |_ ___ \n" +
                "               | | | | -_| |  _| . |     | -_|_ _ _   |  _| . |  |  _|   | -_|\n" +
                "               |_____|___|_|___|___|_|_|_|___|_|_|_|  |_| |___|  |_| |_|_|___|\n" +
                "                                                               " +
                "                                                   \n" +
                "                _____             _       _____ _ _ _             \n" +
                "               |   __|___ ___ ___| |_ _ _|  |  |_| | |___ ___ ___ \n" +
                "               |__   | . | . | . | '_| | |  |  | | | | .'| . | -_|\n" +
                "               |_____|  _|___|___|_,_|_  |\\___/|_|_|_|__,|_  |___|\n" +
                "                     |_|             |___|               |___|    ";

        return villageImage;
    }

    private String displayVillageImage3() {
        String villageImage = "                                                                                                       \n" +
                " .         _  .          .          .    +     .          .          .      .                           \n" +
                "        .          .            .            .            .       :               .           .         \n" +
                "        .   .      .    .     .     .    .      .   .      . .  .  -+-        .                        \n" +
                "                      .           .           .   .        .           .          /         :  .       \n" +
                "           .        / V\\    . .        .  .      / .   .    .    .     .      .  / .      . ' .        \n" +
                "    .             / `  /        .  +       .    /     .          .          .   /      .               \n" +
                "           *     <<   |       .             .  /         .            .        *   .         .     .    \n" +
                "                 /    |      .   .       .    *     .     .    .      .   .       .  .                 \n" +
                "       .       /      |          .           .           .           .           .         +  .        \n" +
                "    .        /        |  . .        .  .       .   .      .    .     .     .    .      .   .           \n" +
                "           /    \\  \\ /                                                                                 \n" +
                "          (      ) | | .   +      .          ___/\\_._/~~\\_...__/\\__.._._/~\\        .         .   .     \n" +
                "  ________|   _/_  | |       .         _.--'                              `--./\\          .   .        \n" +
                "<__________\\______)\\__) ._ - /~~\\/~\\ -                                        `-/~\\_            .      \n" +
                " .      .-'                                                                           `-/\\_            \n" +
                "  _/\\.-'                                                                                    __/~\\/\\-.__.";

        return villageImage;
    }

    private void play() {
        chat("===== Welcome to the Spooky Village! =====");
        chat("===== It's day time. Chat with the other players =====");
        while (verifyIfGameContinues()) {
            try {
                if (this.night) {
                    if (this.PLAYERS.size() >= 6) {
                        String wolvesList = this.PLAYERS.values().stream()
                                .filter(x -> x.alive && x.role.equals(EnumRole.WOLF))
                                .map(x -> x.name)
                                .reduce("Alive Wolves list:", (a, b) -> a + "\n" + b);
                        wolvesChat(wolvesList);
                    }
                    Thread.sleep(30000);
                    choosePlayerWhoDies();
                    chat("===== Wake up! The night is over =====");
                    this.night = false;
                    chat("THIS IS DAY NUMBER " + ++numOfDays);

                    Thread.sleep(2000);
                } else {

                    Thread.sleep(30000);
                    checkNumOfVotes();
                    chat("===== It's dark already. Time to sleep =====");
                    wolvesChat("===== Wolves chat is open! =====");
                    this.night = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Limitar número de visions por noite
    //Mensagem para os lobos quando matam alguém

    private void choosePlayerWhoDies() {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.role.equals(EnumRole.WOLF) && x.alive && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList());
        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream().toList();
            players.get((int) (Math.random() * players.size())).killPlayer();
        } else this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size())).killPlayer();
    }

    //Responsável pelo desenrolar de to_do o jogo. OBRA DE ARTE!!!
    //Chama as funções todas (como startGame, removePlayer, etc.)
    //  }


    private ArrayList<EnumRole> generateEnumCards() {
        ArrayList<EnumRole> roles = new ArrayList<>(PLAYERS.size());
        for (int i = 0; i < this.PLAYERS.size(); i++) {
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

    private boolean verifyIfGameContinues() {
        //se não houver lobos vivos: todos os jogadores recebem uma mensagem “No more wolves left. game over”
        //O número de lobos não pode ser superior ou igual ao número dos jogadores não-lobos
        int wolfCount = 0;
        int nonWolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.role.equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }

        if (wolfCount >= nonWolfCount) {
            chat("The wolves won! \n Game over");
            gameInProgress = false;
            killAll();
            return false;
        } else if (wolfCount == 0) {
            chat("The villagers won! \n There are no wolves left alive \n GAME OVER");
            gameInProgress = false;
            killAll();
            return false;
        }

        return true;
    }

    private void killAll() {
        this.PLAYERS.values().stream()
                .filter(x -> x.alive)
                .forEach(PlayerHandler::killPlayer);
    }

    public Optional<PlayerHandler> getPlayerByName(String name) {
        return this.PLAYERS.values().stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getName(), name))
                .findFirst();
    }

    private void resetNumberOfVotes() {
        this.PLAYERS.values().forEach(x -> x.numberOfVotes = 0);
        this.PLAYERS.values().forEach(x -> x.vote = null);
    }

    private void checkNumOfVotes() {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.alive)
                .max(Comparator.comparing(PlayerHandler::getNumberOfVotes))
                .stream().findAny();

        if (highestVote.isPresent()) {
            highestVote.get().killPlayer();
            chat(highestVote.get().name + " was killed");
        }
        resetNumberOfVotes();
    }

    private void checkIfAllPlayersVoted() {
        PLAYERS.values().stream()
                .filter(x -> x.vote == null)
                .forEach(x -> x.setVote(x));
    }

    public void sendUpdateOfVotes() {
        chat("Current score", PLAYERS.values().stream()
                .filter(player -> player.alive)
                .map(player -> player.name + ": " + player.numberOfVotes)
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

    public int getNumberOfPlayers() {
        return this.PLAYERS.size();
    }

    public int getNumOfDays() {
        return numOfDays;
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
        private PlayerHandler previousVote;
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
                    } else {
                        if (isCommand(this.message.trim())) {
                            dealWithCommand(this.message);
                        } else chat(this.name, this.message);

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
                Server.this.PLAYERS.remove(this.name, this);
                Thread.currentThread().interrupt();
                verifyIfGameContinues();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        public PlayerHandler getPreviousVote() {
            return previousVote;
        }

        public void setPreviousVote(PlayerHandler previousVote) {
            this.previousVote = previousVote;
        }

        public void decreaseNumberOfVotes() {
            this.numberOfVotes--;
        }

        public int getNumberOfVotes() {
            return numberOfVotes;
        }

        public boolean isAlive() {
            return alive;
        }

        public PlayerHandler getVote() {
            return vote;
        }
    }
}
