package game.Server;

import game.Characters.Bot;
import game.Characters.Character;
import game.Characters.FortuneTeller;
import game.EnumRole;
import game.Helpers;
import game.colors.Colors;

import game.colors.ColorsRef;
import game.command.Command;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {
    private ServerSocket serverSocket;
    private ExecutorService service;
    private HashMap<String, PlayerHandler> PLAYERS;
    private boolean gameInProgress;
    private boolean night;
    private List<PlayerHandler> wolvesVotes;
    private PlayerHandler victim;
    private int numOfDays;
    private boolean stopTimer;
    private ArrayList<String> colorList;

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

    public HashMap<String, PlayerHandler> getPLAYERS() {
        return PLAYERS;
    }

    public void acceptConnection() throws IOException {
        Socket playerSocket = this.serverSocket.accept();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(playerSocket.getOutputStream()));
        if (!this.gameInProgress && this.PLAYERS.size() < 12) {
            new Thread(() -> { //serve para varios jogadores poderem escrever o nome ao mesmo tempo
                try {
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
                    System.out.println("Players left");
                }
            }).start();
        } else {
            out.write("The game has already started");
            out.newLine();
            out.flush();
            playerSocket.close();
        }
    }

    /**
     * This method allows the player to enter a name and check if that name is available or not. If not, the player can insert another name until he has chosen an available one
     * @param out BufferedWriter - writes message from the server in the console of the player
     * @param in BufferedReader - reads from the console of the player
     * @return a String, the player name
     * @throws IOException
     */
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

    /**
     * This method verifies if the given name is already in use or not
     * @param playerName String, the player name
     * @return returns a boolean. If true, the name is available. If false, the name is taken.
     */
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
        giveAColorToEachPlayer();
        //add textColor to everyplayer
    }

    public void chat(String name, String message) {

//        for (PlayerHandler player : this.PLAYERS.values()) {
//            if (!player.name.equals(name) && !player.isBot)
//                player.send(name + ": " + message);
//        }

        //giveAColorToEachPlayer();
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.name.equals(name) && !player.isBot) {
                //String color = null;
                //colorList
                player.send(player.textColor + name + ": " + message + ColorsRef.RESET.getCode());
            }
        }
    }

    private void giveAColorToEachPlayer() { //onde por esta função? está no addPlayer
        putEnumColorInArrayList();
        for (PlayerHandler player : this.PLAYERS.values()) {
            for (int i = 0; i < this.PLAYERS.values().size(); i++) {
                player.textColor = colorList.get(i);
            }
        }
    }


    public void putEnumColorInArrayList() {
//        private ArrayList<String> colorList;
//        ArrayList<ColorsRef> colorEnumList = new ArrayList<>();
//        colorEnumList.add(ColorsRef.BLUE);
        //private ArrayList<String> colorList;
        //private String textColor;

        colorList = new ArrayList<>(PLAYERS.size());
        for (int i = 0; i < this.playersInGame().length(); i++) {
            switch (i) {
                case 0 -> colorList.add(ColorsRef.RED.getCode());
                case 1 -> colorList.add(ColorsRef.GREEN.getCode());
                case 2 -> colorList.add(ColorsRef.YELLOW.getCode());
                case 3 -> colorList.add(ColorsRef.BLUE.getCode());
                case 4 -> colorList.add(ColorsRef.MAGENTA.getCode());
                case 5 -> colorList.add(ColorsRef.CYAN.getCode());
                case 6 -> colorList.add(ColorsRef.WHITE.getCode());
                case 7 -> colorList.add(ColorsRef.RED_UNDERLINED.getCode());
                case 8 -> colorList.add(ColorsRef.GREEN_UNDERLINED.getCode());
                case 9 -> colorList.add(ColorsRef.YELLOW_UNDERLINED.getCode());
                case 10 -> colorList.add(ColorsRef.BLUE_UNDERLINED.getCode());
                case 11 -> colorList.add(ColorsRef.MAGENTA_UNDERLINED.getCode());
                default -> colorList.add(ColorsRef.RESET.getCode());
            }
        }
    }


    public void chat(String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) player.send(message);
        }
    }

    public void wolvesChat(String name, String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF)
                        && !x.name.equals(name) && !x.isBot)
                .forEach(x -> x.send(name + ": " + message));
    }

    public void wolvesChat(String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF) && !x.isBot)
                .forEach(x -> x.send(message));
    }

    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.name +
                        (x.isBot ? (" (bot)") : "") +
                        " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    public void sendPrivateMessage(String name, String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.name.equals(name) && !player.isBot) {
                player.send(message);
            }
        }
    }

    public void startGame() {
        this.night = false;
        //chat(displayVillageImage());
        chat(displayVillageImage2());
        //chat(displayWolfImage());

        chat("\n===== Welcome to the Spooky Village! =====\n");
        List<PlayerHandler> playersList = new ArrayList<>();
        //int playersInGame = Math.max(playersList.size(), 5);

        int nonBots = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) {
                nonBots++;
                playersList.add(player);
            }
        }
        int playersInGame = Math.max(nonBots, 5);
        System.out.println(playersInGame);

        ArrayList<EnumRole> roles = generateEnumCards(playersInGame);
        Collections.shuffle(roles);

        for (int i = 0; i < playersInGame; i++) {
            EnumRole newRole = roles.get(i);

            if (i >= playersList.size()) {
                Bot bot = new Bot();
                PlayerHandler botPlayer = new PlayerHandler(bot);
                botPlayer.character.setRole(newRole);
                playersList.add(botPlayer);
            } else {
                sendPrivateMessage(playersList.get(i).name, "You are a " + newRole.toString() + "\n");
                playersList.get(i).character = newRole.getCHARACTER();
                playersList.get(i).character.setRole(newRole);
            }
        }
        this.PLAYERS = new HashMap<>();
        for (PlayerHandler player : playersList) {
            this.PLAYERS.put(player.name, player);
        }
        setPlayersLife();
        chat(playersInGame());
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
                    Thread.sleep(20000);
                    if (!stopTimer) {
                        for (int seconds = 5; seconds > 0; seconds--) {
                            chat(seconds + " second" + (seconds == 1 ? "" : "s") + " left until the end of the night...");
                            Thread.sleep(1000);
                        }
                        chat("The night is over..." + playersInGame());
                        stopTimer = true;
                    }
                    choosePlayerWhoDies();
                    sendPrivateMessage(this.victim.name, (Colors.BLACK + "\n x.x You have been killed last night x.x") + Colors.RESET);
                    sendPrivateMessage(this.victim.name, (displaySkullImage()));
                    //chat(Colors.WHITE + "The village has woken up with the terrible news that " + victimName.name.toUpperCase() + " was killed last night");
                    //chat(Colors.YELLOW + "\nTHIS IS DAY NUMBER " + ++numOfDays);
                    //sendPrivateMessage(victimName.name, (Colors.WHITE + " x.x You have been killed last night x.x"));
                    //sendPrivateMessage(victimName.name, displaySkullImage());
                    //chat(Colors.WHITE + "The village has woken up with the terrible news that " + victimName.name.toUpperCase() + " was killed last night");
                    if (ifThereAreAliveWolves()) {
                        chat("Watch out! There are still wolves walking around. No one is safe!\nIt's time to vote and then kill the one that seems to be the wolf...");
                    }

                    //chat("The village has woken up with the terrible news that " + victimName.toUpperCase() + " was killed last night");
                    Thread.sleep(500);
                    resetUsedVision();
                    resetDefense();
                    this.night = false;
                } else {
                    chat(Colors.YELLOW + "\n===== It's day time. Chat with the other players =====");
                    Thread.sleep(20000);
                    stopTimer = false;
                    if (!stopTimer) {
                        for (int seconds = 5; seconds > 0; seconds--) {
                            chat(seconds + " second" + (seconds == 1 ? "" : "s") + " left until the end of the day...");
                            Thread.sleep(1000);
                        }
                        chat("The day is over...");
                        stopTimer = true;
                    }
                    if (numOfDays != 0) {
                        Thread.sleep(1000);
                        botsDayVotes();
                        checkVotes();
                    }
                    this.night = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resetGame();
    }

    private void botsNightVotes() {
        List<PlayerHandler> wolfBots = this.PLAYERS.values().stream()
                .filter(x -> x.isBot && x.alive
                        && x.getCharacter().getRole().equals(EnumRole.WOLF))
                .toList();

        if (wolfBots.size() > 0) {
            Optional<PlayerHandler> botVote;
            for (PlayerHandler wolfBot : wolfBots) {
                botVote = ((Bot) wolfBot.getCharacter()).getNightVote(this);
                botVote.ifPresent(x -> this.wolvesVotes.add(x));
            }
        }
    }

    private void botsDayVotes() {
        List<PlayerHandler> aliveBots = this.PLAYERS.values().stream()
                .filter(x -> x.isBot && x.alive)
                .toList();

        for (PlayerHandler aliveBot : aliveBots) {
            System.out.println(aliveBot.getCharacter().toString());
        }

        if (aliveBots.size() > 0) {
            Optional<PlayerHandler> botVote;
            for (PlayerHandler aliveBot : aliveBots) {
                botVote = ((Bot) aliveBot.getCharacter()).getDayVote(this);
                botVote.ifPresent(playerHandler -> aliveBot.vote = playerHandler);
            }
        }
    }

   /* Thread timer = new Thread(() -> {
            try {

                // Thread.sleep(2000);
                stopTimer = false;
                for (int seconds = 5; seconds > 0; seconds--) {
                    chat(seconds + " second" + (seconds == 1 ? "" : "s") + " left until the end of the night...");
                    Thread.sleep(1000);
                }
                chat("The night is over..." + playersInGame());
                notifyTimer();
            } catch (InterruptedException weCanIgnoreThisException) {
            }
    });

    public synchronized void notifyTimer(){
        stopTimer = true;
        notifyAll();
    } */

    private void printAliveWolves() {
        if (this.PLAYERS.size() >= 7) {
            String wolvesList = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .map(x -> x.name)
                    .reduce("Alive Wolves list:", (a, b) -> a + "\n" + b);
            wolvesChat(wolvesList);
        }
    }

    private void resetGame() {
        this.gameInProgress = false;
        this.numOfDays = 0;
        this.night = false;
        Bot.resetBotNumber();
    }

    //Mensagem para os lobos quando matam alguém

    private void choosePlayerWhoDies() {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF)
                        && x.alive && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList()); //List<PlayerHandler> wolvesVotes

        botsNightVotes();
        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream()
                    .filter(x -> !x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .toList();
            this.victim = players.get((int) (Math.random() * players.size()));

        } else {
            this.victim = this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size()));
        }
        if (!this.victim.getCharacter().isDefended()) {
            this.victim.alive = false;
            wolvesChat(Colors.RED + "You have decided to kill... " + this.victim.name.toUpperCase() + Colors.RESET + "\n");
            chat("\nTHIS IS DAY NUMBER " + ++numOfDays + "\n");
            chat("Unfortunately, " + this.victim.name.toUpperCase() + " was killed by hungry wolves... Rest in peace, " + this.victim.name.toUpperCase() + "\n");
        } else {
            wolvesChat(Colors.RED + "You have decided to kill " + this.victim.name.toUpperCase() + "... But he got protected by the guard! You'll stay hungry tonight!" + Colors.RESET + "\n");
            chat("\nTHIS IS DAY NUMBER " + ++numOfDays + "\n");
            chat("HURRAYYYY! The guard bravely protected the village, so everyone survived the last night!");
        }
        chat("Check out the latest update of the " + playersInGame());
    }

    private ArrayList<EnumRole> generateEnumCards(int playersInGame) {
        ArrayList<EnumRole> roles = new ArrayList<>(PLAYERS.size());
        for (int i = 0; i < playersInGame; i++) {
            switch (i) {
                case 0, 11 -> roles.add(i, EnumRole.WOLF);
                case 1, 9 -> roles.add(i, EnumRole.FORTUNE_TELLER);
                case 5 -> roles.add(i, EnumRole.GUARD);
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
                if (player.character.getRole().equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }
        /*for (Bot bot : this.bots) {
            if (bot.alive) {
                if (bot.getRole().equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }*/
        return checkWinner(wolfCount, nonWolfCount);
    }

    private boolean ifThereAreAliveWolves() {
        //se o count de lobos for maior que villagers, true, else false
        int wolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.getCharacter().getRole().equals(EnumRole.WOLF))
                    wolfCount++;
            }
        }
        return (wolfCount > 0);
    }

    private boolean checkWinner(int wolfCount, int nonWolfCount) {
        if (wolfCount >= nonWolfCount) {
            chat("\nWolves took over the village... Anyone who's letf alive, will be eaten tonight.\nWolves won!\nGAME OVER");
            resetGame();
            this.PLAYERS.values().forEach(System.out::println);
            System.out.println(wolfCount + " " + nonWolfCount);
            return false;
        } else if (wolfCount == 0) {
            chat("\nThe villagers won!\nThere are no wolves left alive.\n" + "GAME OVER");
            resetGame();
            this.PLAYERS.values().forEach(System.out::println);
            System.out.println(wolfCount + " " + nonWolfCount);
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
                .filter(player -> player.alive)
                .max(Comparator.comparing(x -> x.getCharacter().getNumberOfVotes()))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().alive = false;
            chat(highestVote.get().name + " was tragically killed by the Villagers, thinking it was a wolf");
        }
        resetNumberOfVotes();
    }

    private void checkIfAllPlayersVoted() {
        this.PLAYERS.values().stream()
                .filter(x -> x.vote == null)
                .forEach(x -> x.vote = x);
    }

    public void sendUpdateOfVotes() {
        chat("Current score", PLAYERS.values().stream()
                .filter(player -> player.alive)
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
        this.PLAYERS.values().forEach(x -> x.alive = true);
    }

    public boolean isNight() {
        return night;
    }

    public int getNumOfDays() {
        return numOfDays;
    }

    private void resetUsedVision() {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.FORTUNE_TELLER) && !x.isBot)
                .forEach(x -> ((FortuneTeller) x.getCharacter()).setUsedVision(false));
    }

    private void resetDefense() {
        this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().isDefended())
                .forEach(x -> (x.getCharacter()).setDefended(false));
    }

    public class PlayerHandler implements Runnable {
        private String name;
        private Socket playerSocket;
        private BufferedWriter out;
        private PlayerHandler vote;
        private String message;
        private Character character;
        private boolean isBot;
        private boolean alive;
        private String textColor;

        private PlayerHandler defend;

        public PlayerHandler(Socket clientSocket, String name) {
            try {
                this.playerSocket = clientSocket;
                this.name = name;
                this.out = new BufferedWriter(new OutputStreamWriter(this.playerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PlayerHandler(Bot bot) {
            this.character = bot;
            this.name = bot.getNAME();
            this.isBot = true;
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
                            case FORTUNE_TELLER, GUARD -> dealWithCommand(this.message);
                            default -> {
                                if (!this.message.split(" ")[0].equals(Command.QUIT.getCOMMAND()) ||
                                        !this.message.split(" ")[0].equals(Command.COMMAND_LIST.getCOMMAND()))
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
                    "NAME='" + name + '\'' + this.alive +
                    '}';
        }

        public boolean isAlive() {
            return alive;
        }

        public void setDefend(PlayerHandler defend) {
            this.defend = defend;
        }
    }
}


