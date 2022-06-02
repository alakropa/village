package game.Server;

import game.Characters.Bot;
import game.Characters.Character;
import game.Characters.FortuneTeller;
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
    private PlayerHandler victimName;
    private int numOfDays;
    private List<Bot> bots;

    public Server() {
        this.PLAYERS = new HashMap<>();
        this.gameInProgress = false;
        this.wolvesVotes = new ArrayList<>();
        this.bots = new ArrayList<>();
    }

    public void start(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.service = Executors.newCachedThreadPool();

        while (true) {
            acceptConnection();
        }
    }

    public HashMap<String, PlayerHandler> getPLAYERS() {
        return PLAYERS;
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

                    if (!this.gameInProgress && this.PLAYERS.size() < 12) {
                        System.out.println(playerName + " entered the chat");
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
        while (!checkIfNameIsAvailable(playerName) || playerName.startsWith("/") || playerName.equals("")) { //false
            out.write("You can't choose this name. Try another one");
            out.newLine();
            out.flush();
            playerName = in.readLine();
        }
        return playerName;
    }

    private boolean checkIfNameIsAvailable(String playerName) {
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
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF) && !x.name.equals(name))
                .forEach(x -> x.send(name + ": " + message));
    }

    public void wolvesChat(String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF))
                .forEach(x -> x.send(message));
    }

    public String playersInGame() {
        String botNames = null;
        if (this.bots.size() > 0) botNames = this.bots.stream()
                .map(x -> x.getNAME() + " - " + (x.isAlive() ? "Alive" : "Dead"))
                .reduce("Bots list:", (a, b) -> a + "\n" + b);

        return this.PLAYERS.values().stream()
                .map(x -> x.name + " - " + (x.getCharacter().isAlive() ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b) + "\n" + botNames;
    }

    public void sendPrivateMessage(String name, String message) {
        for (PlayerHandler client : this.PLAYERS.values()) {
            if (client.name.equals(name)) {
                client.send(message);
            }
        }
    }

    public void startGame() {
        this.night = false;
//        if (this.PLAYERS.size() < 5) {
//            int missingBots = 5 - this.PLAYERS.size();
//            for (int i = 0; i < missingBots; i++) {
//                Bot bot = new Bot();
//                PlayerHandler botPlayer = new PlayerHandler(null, bot.getNAME());
//                this.PLAYERS.put(bot.getNAME(), botPlayer);
//            }
//        }

        //chat(displayVillageImage());
        chat(displayVillageImage2());
        //chat(displayWolfImage());


        chat("\n===== Welcome to the Spooky Village! =====\n");
        List<PlayerHandler> playersList = new ArrayList<>(this.PLAYERS.values());
        int playersInGame = Math.max(playersList.size(), 5);

        ArrayList<EnumRole> roles = generateEnumCards(playersInGame);
        Collections.shuffle(roles);

        int count = 0;
        for (int i = 0; i < playersInGame; i++) {
            EnumRole newRole = roles.get(i);
            sendPrivateMessage(playersList.get(i).name, "You are a " + newRole.toString() + "\n");
            playersList.get(i).character = newRole.getCHARACTER();
            playersList.get(i).character.setRole(newRole);
            if (i >= playersList.size()) {
                Bot bot = new Bot(newRole);
                this.bots.add(bot);
                System.out.println("Created " + ++count + " bots");
                this.PLAYERS.values().forEach(System.out::println);
                this.bots.forEach(System.out::println);
            } else {
                sendPrivateMessage(playersList.get(i).name, "You are a " + newRole.toString());
                playersList.get(i).character = newRole.getCHARACTER();
                playersList.get(i).character.setRole(newRole);
            }
        }
        setPlayersLife();
        chat("A list of players starting the game", playersInGame());
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
        return Colors.CYAN + "                                                               \n" +
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
                "                     |_|             |___|               |___|    " + Colors.RESET;
    }

    private String displayWolfImage() {
        return Colors.BLUE + "                                                                                                       \n" +
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
                "  _/\\.-'                                                                                    __/~\\/\\-.__." + Colors.RESET + "\n";
    }

    private String displaySkullImage() {
        return Colors.BLACK +
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
                "          -;_)               (_;-      \n" + Colors.RESET;
    }


    private void play() {
        while (verifyIfGameContinues()) {
            try {
                if (this.night) {
                    chat("\n===== It's dark already. Time to sleep =====");
                    chat(displayWolfImage());
                    Thread.sleep(500);
                    wolvesChat(Colors.RED + "===== Wolves chat is open! =====");
                    Thread.sleep(500);
                    printAliveWolves();

                    new Thread(this::botsNightVotes);

                    Thread.sleep(30000);
                    choosePlayerWhoDies();
                    this.night = false;

                    //chat(Colors.YELLOW + "\nTHIS IS DAY NUMBER " + ++numOfDays);
                    sendPrivateMessage(victimName.name, (Colors.BLACK + "\n x.x You have been killed last night x.x") + Colors.RESET);
                    sendPrivateMessage(victimName.name, (displaySkullImage()));
                    //chat(Colors.WHITE + "The village has woken up with the terrible news that " + victimName.name.toUpperCase() + " was killed last night");
                    if (ifThereAreAliveWolves()) {
                        chat("Watch out! There are still wolves walking around. No one is safe\n");
                    }

                    //chat(Colors.YELLOW + "THIS IS DAY NUMBER " + ++numOfDays);
                    //chat("The village has woken up with the terrible news that " + victimName.toUpperCase() + " was killed last night");
                    Thread.sleep(500);
                    resetUsedVision();
                } else {
                    chat("\n===== It's day time. Chat with the other players =====\n");
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

    private void botsNightVotes() {
        List<Bot> wolfBots = this.bots.stream()
                .filter(x -> x.getRole().equals(EnumRole.WOLF))
                .toList();
        if (wolfBots.size() > 0) {
            Optional<PlayerHandler> botVote;
            for (Bot wolfBot : wolfBots) {
                try {
                    Thread.sleep(500);
                    botVote = wolfBot.getNightVote(this);
                    botVote.ifPresent(x -> this.wolvesVotes.add(x));
                    System.out.println(botVote.get().name);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printAliveWolves() {
        if (this.PLAYERS.size() >= 7) {
            String wolvesList = this.PLAYERS.values().stream()
                    .filter(x -> x.getCharacter().isAlive() && x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .map(x -> x.name)
                    .reduce("Alive Wolves list:", (a, b) -> a + "\n" + b);
            wolvesChat(wolvesList);
        }
    }

    private void resetGame() {
        this.gameInProgress = false;
        this.numOfDays = 0;
        this.night = false;
    }

    //Mensagem para os lobos quando matam alguém

    private void choosePlayerWhoDies() {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF)
                        && x.getCharacter().isAlive() && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList()); //List<PlayerHandler> wolvesVotes
        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream()
                    .filter(x -> !x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .toList(); //se ninguém votar
            this.victimName = players.get((int) (Math.random() * players.size()));
            this.victimName.getCharacter().killPlayer(); //alive=false
        } else {
            this.victimName = this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size()));
            this.victimName.getCharacter().killPlayer();
        }
        wolvesChat(Colors.RED + "You have decided to kill... " + this.victimName.name.toUpperCase() + Colors.RESET + "\n");
        chat("\nTHIS IS DAY NUMBER " + ++numOfDays + "\n");
        chat("Unfortunately, " + this.victimName.name.toUpperCase() + " was killed by hungry wolves... Rest in peace, " + this.victimName.name);
    }

    private ArrayList<EnumRole> generateEnumCards(int playersInGame) {
        ArrayList<EnumRole> roles = new ArrayList<>(PLAYERS.size());
        for (int i = 0; i < playersInGame; i++) {
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
            if (player.getCharacter().isAlive()) {
                if (player.getCharacter().getRole().equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }
        for (Bot bot : this.bots) {
            if (bot.isAlive()) {
                if (bot.getRole().equals(EnumRole.WOLF)) wolfCount++;
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
            if (player.getCharacter().isAlive()) {
                if (player.getCharacter().getRole().equals(EnumRole.WOLF)) {
                    wolfCount++;
                } else {
                    nonWolfCount++;
                }
            }
        }
        if (wolfCount > 0) {
            return true;
        }
        return false;
    }


    private boolean checkWinner(int wolfCount, int nonWolfCount) {
        if (wolfCount >= nonWolfCount) {
            chat("\nThe wolves won!\nGame over");
            resetGame();
            return false;
        } else if (wolfCount == 0) {
            chat("\nThe villagers won!\nThere are no wolves left alive\n" + "GAME OVER");
            resetGame();
            return false;
        }
        return true;
    }

    public Optional<PlayerHandler> getPlayerByName(String name) {
        return this.PLAYERS.values().stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getName(), name))
                .findFirst();
    }

    private void resetNumberOfVotes() {
        this.PLAYERS.values().forEach(x -> x.getCharacter().setNumberOfVotes(0));
        this.PLAYERS.values().forEach(x -> x.vote = null);
    }

    private void checkVotes() {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.getCharacter().isAlive())
                .max(Comparator.comparing(x -> x.getCharacter().getNumberOfVotes()))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().getCharacter().killPlayer();
            chat(highestVote.get().name + " was tragically killed by the Villagers, thinking it was a wolf");
        }
        resetNumberOfVotes();
    }

    private void checkIfAllPlayersVoted() {
        PLAYERS.values().stream()
                .filter(x -> x.vote == null)
                .forEach(x -> x.vote = x);
    }

    public void sendUpdateOfVotes() {
        chat("Current score", PLAYERS.values().stream()
                .filter(player -> player.getCharacter().isAlive())
                .map(player -> player.name + ": " + player.getCharacter().getNumberOfVotes())
                .reduce("", (a, b) -> a + "\n" + b));
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }

    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    public void setPlayersLife() {
        for (String name : this.PLAYERS.keySet()) {
            String value = String.valueOf(this.PLAYERS.get(name).getCharacter());
            System.out.println(name + " " + value);
        }
        this.bots.forEach(Character::healPlayer);
        this.PLAYERS.values().forEach(x -> x.getCharacter().healPlayer());
    }

    public boolean isNight() {
        return night;
    }

    public int getNumOfDays() {
        return numOfDays;
    }

    private void resetUsedVision() {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.FORTUNE_TELLER))
                .forEach(x -> ((FortuneTeller) x.getCharacter()).setUsedVision(false));
    }

    public class PlayerHandler implements Runnable {
        private String name;
        private Socket playerSocket;
        private BufferedWriter out;
        private PlayerHandler vote;
        private String message;
        private Character character;

        public PlayerHandler(Socket clientSocket, String name) {
            try {
                this.playerSocket = clientSocket;
                this.name = name;
                this.out = new BufferedWriter(new OutputStreamWriter(this.playerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(this.playerSocket.getInputStream()));
                while (!this.playerSocket.isClosed()) {
                    this.message = in.readLine();
                    System.out.println(name + ": " + this.message); //imprime no server as msg q recebe dos clients

                    if (Server.this.night) {
                        switch (this.character.getRole()) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.name, this.message);
                            }
                            case FORTUNE_TELLER -> dealWithCommand(this.message);
                            default -> {
                                if (!this.message.split(" ")[0].equals(Command.QUIT.getCOMMAND()))
                                    send("You are sleeping");
                                else dealWithCommand(this.message);
                            }

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

        private void dealWithCommand(String message) throws IOException {
            Command command = Command.getCommandFromDescription(message.split(" ")[0]);
            if (command == null) {
                send("Unavailable command");
                return;
            }
            command.getHANDLER().command(Server.this, this);
        }

        private boolean isCommand(String message) {
            return message.trim().startsWith("/");
        }

        public void send(String message) {
            try {
                this.out.write(message);
                this.out.newLine();
                this.out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return this.name;
        }

        public String getMessage() {
            return this.message;
        }

        public Character getCharacter() {
            return character;
        }

        public void setVote(PlayerHandler vote) {
            this.vote = vote;
        }

        public void playerDisconnected() {
            try {
                chat(this.name, " disconnected");
                Server.this.PLAYERS.remove(this.name, this);
                this.playerSocket.close();
                Thread.currentThread().interrupt();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public String toString() {
            return "PlayerHandler{" +
                    "NAME='" + name + '\'' +
                    '}';
        }
    }
}
