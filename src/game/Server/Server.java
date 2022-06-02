package game.Server;

import game.EnumRole;
import game.Helpers;
import game.colors.Colors;
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
    private String victimName;
    private int numOfDays;

    public Server() {
        this.PLAYERS = new HashMap<>();
        this.gameInProgress = false;
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
                    String playerName = verifyIfNameIsAvailable(out, in);

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

    private String verifyIfNameIsAvailable(BufferedWriter out, BufferedReader in) throws IOException {
        String playerName = in.readLine(); //fica à espera do nome
        while (!checkIfNameIsAvailable(playerName) || playerName.startsWith("/")) { //false
            out.write("You can't choose this name. Try another one");
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
            if (player.NAME.equals(playerName)) {
                return false;
            }
        }
        return true;
    }

    private void addPlayer(PlayerHandler playerHandler) {
        this.PLAYERS.put(playerHandler.NAME, playerHandler);
        this.service.submit(playerHandler); //mandar para a threadpool
        chat(playerHandler.NAME, "joined the chat"); //msg para os outros players
    }

    public void chat(String name, String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            if (!client.NAME.equals(name)) {
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
                .filter(x -> x.role == EnumRole.WOLF && !x.NAME.equals(name))
                .forEach(x -> x.send(name + ": " + message));
    }

    public void wolvesChat(String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.role == EnumRole.WOLF)
                .forEach(x -> x.send(message));
    }

    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.NAME + " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    public void removePlayer(PlayerHandler playerHandler) {
        try {
            playerHandler.PLAYER_SOCKET.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.PLAYERS.remove(playerHandler.NAME, playerHandler);
    }

    public void sendPrivateMessage(String name, String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            if (client.NAME.equals(name)) {
                client.send(message);
            }
        }
    }

    public void startGame() {
        // Adicionar bots necessários
        this.night = false;
        //chat(displayVillageImage());
        chat(displayVillageImage2());
        chat(displayVillageImage3());
        chat("A list of players starting the game", playersInGame());

        ArrayList<EnumRole> roles = generateEnumCards();
        Collections.shuffle(roles);

        chat("\n===== Welcome to the Spooky Village! =====\n");
        List<PlayerHandler> playersList = new ArrayList<>(this.PLAYERS.values());
        for (int i = 0; i < playersList.size(); i++) {
            EnumRole newRole = roles.get(i);
            sendPrivateMessage(playersList.get(i).NAME, "You are a " + newRole.toString());
            playersList.get(i).role = newRole;
        }
        play();
    }

    private String displayVillageImage() {
        return "" +
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
    }

    private String displayVillageImage2() {
        return "                                                               \n" +
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
    }

    private String displayVillageImage3() {
        return "                                                                                                       \n" +
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
    }

    private String displaySkullImage() {
        return Colors.BLACK_BOLD +
                "        _;~)                    (~;_   \n" +
                        "        (   |                  |   )   \n" +
                        "         ~', ',   ,''~'',   ,' ,'~     \n" +
                        "            ', ','       ',' ,'        \n" +
                        "              ',: {'} {'} :,'          \n" +
                        "                ;   /^\\   ;            \n" +
                        "                 ~\\  ~  /~             \n" +
                        "               ,' ,~~~~~, ',           \n" +
                        "             ,' ,' ;~~~; ', ',         \n" +
                        "           ,' ,'    '''    ', ',       \n" +
                        "         (~  ;               ;  ~)     \n" +
                        "          -;_)               (_;-      \n";
    }


    private void play() {
        while (verifyIfGameContinues()) {
            try {
                if (this.night) {
                    chat(Colors.BLUE + "\n===== It's dark already. Time to sleep =====\n");
                    Thread.sleep(500);
                    wolvesChat(Colors.RED + "===== Wolves chat is open! =====\n");
                    Thread.sleep(500);
                    printAliveWolves();
                    Thread.sleep(30000);
                    choosePlayerWhoDies();
                    this.night = false;
                    chat(Colors.YELLOW + "\nTHIS IS DAY NUMBER " + ++numOfDays);
                    sendPrivateMessage(victimName, (Colors.WHITE + " x.x You have been killed last night x.x"));
                    sendPrivateMessage(victimName, displaySkullImage());
                    chat(Colors.WHITE + "The village has woken up with the terrible news that " + victimName.toUpperCase() + " was killed last night");
                    if(ifThereAreAliveWolves()){
                        chat(Colors.WHITE + "Unfortunately, there are still wolves walking around. No one is safe");
                    }
                    Thread.sleep(500);
                    resetUsedVision();
                } else {
                    chat(Colors.YELLOW + "\n===== It's day time. Chat with the other players =====");
                    Thread.sleep(30000);
                    checkVotes();
                    this.night = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resetGame();
    }

    private void printAliveWolves() {
        if (this.PLAYERS.size() >= 7) {
            String wolvesList = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && x.role.equals(EnumRole.WOLF))
                    .map(x -> x.NAME)
                    .reduce("Alive Wolves list:", (a, b) -> a + "\n" + b);
            wolvesChat(wolvesList);
        }
    }

    private void resetGame() {
        this.gameInProgress = false;
        this.numOfDays = 0;
        this.night = false;
    }

    //Limitar número de visions por noite
    //
    //Mensagem para os lobos quando matam alguém

    private void choosePlayerWhoDies() {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.role.equals(EnumRole.WOLF) && x.alive && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList()); //List<PlayerHandler> wolvesVotes
        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream().toList(); //se ninguém votar
            players.get((int) (Math.random() * players.size())).killPlayer(); //alive=false
        } else {
            PlayerHandler victim = this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size()));
            victimName = victim.NAME;
            victim.killPlayer();
            //this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size())).killPlayer(); //random dos votados, mesmo q seja repetido
            wolvesChat("You have decided to kill... " + victimName.toUpperCase());
        }
    }

    //Responsável pelo desenrolar de to_do o jogo. OBRA DE ARTE!!!
    //Chama as funções todas (como startGame, removePlayer, etc.)


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

    private boolean verifyIfGameContinues() {
        //O número de lobos não pode ser superior ou igual ao número dos jogadores não-lobos
        int wolfCount = 0;
        int nonWolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.role.equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }
        return checkWinner(wolfCount, nonWolfCount);
    }

    private boolean ifThereAreAliveWolves() {
        //se o count de lobos for maior que villagers, true, else false
        int wolfCount = 0;
        int nonWolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.role.equals(EnumRole.WOLF)){
                    wolfCount++;
                } else {
                    nonWolfCount++;
                }
            }
        }
        if(wolfCount > 0) {
            return true;
        }
        return false;
    }

    private boolean checkWinner(int wolfCount, int nonWolfCount) {
        if (wolfCount >= nonWolfCount) {
            chat("The wolves won!\nGame over");
            resetGame();
            return false;
        } else if (wolfCount == 0) {
            chat("The villagers won!\nThere are no wolves left alive\n" + Colors.RED + "GAME OVER");
            resetGame();
            return false;
        }
        return true;
    }

    public Optional<PlayerHandler> getPlayerByName(String name) {
        return this.PLAYERS.values().stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getNAME(), name))
                .findFirst();
    }

    private void resetNumberOfVotes() {
        this.PLAYERS.values().forEach(x -> x.numberOfVotes = 0);
        this.PLAYERS.values().forEach(x -> x.vote = null);
    }

    private void checkVotes() {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.alive)
                .max(Comparator.comparing(PlayerHandler::getNumberOfVotes))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().killPlayer();
            chat(Colors.WHITE + highestVote.get().NAME + " was tragically killed");
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
                .map(player -> player.NAME + ": " + player.numberOfVotes)
                .reduce("", (a, b) -> a + "\n" + b));
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public void setPlayersLife() {
        this.PLAYERS.values().forEach(x -> x.alive = true);
    }

    public boolean isNight() {
        return night;
    }

    public int getNumOfDays() {
        return numOfDays;
    }

    private void resetUsedVision() {
        this.PLAYERS.values().forEach(x -> x.usedVision = false);
    }

    public class PlayerHandler implements Runnable {
        private final String NAME;
        private final Socket PLAYER_SOCKET;
        private final BufferedWriter OUT;
        private String message;
        private boolean alive;
        private EnumRole role;
        private int numberOfVotes;
        private PlayerHandler vote;
        private PlayerHandler previousVote;
        private final HashMap<String, Boolean> VISIONS;
        private boolean usedVision;

        public PlayerHandler(Socket clientSocket, String name) throws IOException {
            this.PLAYER_SOCKET = clientSocket;
            this.NAME = name;
            this.OUT = new BufferedWriter(new OutputStreamWriter(this.PLAYER_SOCKET.getOutputStream()));
            this.alive = true;
            this.VISIONS = new HashMap<>();
        }

        @Override
        public void run() {
            //play?
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(this.PLAYER_SOCKET.getInputStream()));
                while (!this.PLAYER_SOCKET.isClosed()) {
                    this.message = in.readLine();
                    System.out.println(NAME + ": " + this.message); //imprime no server as msg q recebe dos clients

                    if (Server.this.night) {
                        switch (this.role) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.NAME, this.message);
                            }
                            case FORTUNE_TELLER -> {
                                dealWithCommand(this.message);
                                this.usedVision = true;
                            }
                            default -> send("You are sleeping");
                        }
                    } else {
                        if (isCommand(this.message.trim())) {
                            dealWithCommand(this.message);
                        } else chat(this.NAME, this.message);

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

        public String getNAME() {
            return this.NAME;
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

        public EnumRole getRole() {
            return role;
        }

        public void setVote(PlayerHandler vote) {
            this.vote = vote;
        }

        public void playerDisconnected() {
            try {
                chat(this.NAME, " disconnected");
                Server.this.PLAYERS.remove(this.NAME, this);
                this.PLAYER_SOCKET.close();
                Thread.currentThread().interrupt();
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

        public HashMap<String, Boolean> getVISIONS() {
            return VISIONS;
        }

        public void addVisions(String playerName, Boolean isWolf) {
            this.VISIONS.put(playerName, isWolf);
        }

        public boolean hasUsedVision() {
            return usedVision;
        }

        public void setUsedVision(boolean usedVision) {
            this.usedVision = usedVision;
        }
    }
}
