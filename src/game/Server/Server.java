package game.Server;

import game.Characters.Bot;
import game.Characters.Character;
import game.Characters.FortuneTeller;
import game.EnumRole;
import game.Helpers;
import game.Images;
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

    public void acceptConnection() throws IOException {
        Socket playerSocket = this.serverSocket.accept();
        PlayerHandler newPlayer = new PlayerHandler(playerSocket);
        if (!this.gameInProgress && this.PLAYERS.size() < 12) {
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
                    newPlayer.send("Write your name");
                    String playerName = verifyIfNameIsAvailable(newPlayer, in);

                    if (!this.gameInProgress && this.PLAYERS.size() < 12) {
                        System.out.println(playerName + " entered the chat");
                        addPlayer(new PlayerHandler(playerSocket, playerName));
                        newPlayer.send("Welcome to our chat!\nType /cmd to open the comand list");
                    } else {
                        newPlayer.send("The game is unavailable");
                        playerSocket.close();
                    }
                } catch (IOException e) {
                    System.out.println("Players left");
                }
            }).start();
        } else {
            newPlayer.send("The game has already started");
            playerSocket.close();
        }
    }

    /**
     * This method allows the player to enter a name and check if that name is available or not. If not, the player can insert another name until he has chosen an available one
     *
     * @param newPlayer PlayerHandler - contains a PlayerHandler instance
     * @param in        BufferedReader - reads from the console of the player
     * @return a String, the player name
     * @throws IOException
     */
    private String verifyIfNameIsAvailable(PlayerHandler newPlayer, BufferedReader in) throws IOException {
        String playerName = in.readLine();
        while (!checkIfNameIsAvailable(playerName) || playerName.startsWith("/") || playerName.equals("")) {
            newPlayer.send("You can't choose this name. Try another one");
            playerName = in.readLine();
        }
        return playerName;
    }

    /**
     * This method verifies if the given name is already in use or not
     *
     * @param playerName a String, the player name
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

    /**
     * This method adds the player to the XXXXX, and submits it into the Thread Pool. It also sends a message to all the other players informing them that this particular player joined the game.
     *
     * @param playerHandler receives a PlayerHander, an inner class from the Server, that deals with the players, and its attributes
     */
    private void addPlayer(PlayerHandler playerHandler) {
        this.PLAYERS.put(playerHandler.name, playerHandler);
        this.service.submit(playerHandler);
        chat(playerHandler.name, "joined the chat");
    }

    public void startGame() throws InterruptedException {
        this.night = false;
        chat(Images.displayVillageImage2());
        chat("\n===== Welcome to the Spooky Village! =====\n");
        Thread.sleep(1200);

        getRoles();
        setPlayersLife();
        Thread.sleep(1500);
        chat(playersInGame());
        Thread.sleep(2000);

        play();
    }

    /**
     * This method sends a private message to every player, except the one sending it
     *
     * @param name    a String, the name of the person you want to send the message to
     * @param message a String, the message you want to send to the other players
     */
    public void chat(String name, String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.name.equals(name) && !player.isBot) {
                player.send(player.textColor + name + ": " + message + ColorsRef.RESET.getCode());
            }
        }
    }

    /**
     * This method sends a private message to every player, except the one sending it
     *
     * @param message a String, the message you want to send to the other players
     */
    public void chat(String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) player.send(message);
        }
    }

    private void getRoles() {
        List<PlayerHandler> playersList = new ArrayList<>();

        int nonBots = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) {
                nonBots++;
                playersList.add(player);
            }
        }
        int playersInGame = Math.max(nonBots, 5);
        System.out.println(playersInGame);

        ArrayList<EnumRole> roles = Helpers.generateEnumCards(this.PLAYERS, playersInGame);
        Collections.shuffle(roles);

        for (int i = 0; i < playersInGame; i++) {
            EnumRole newRole = roles.get(i);

            if (i >= playersList.size()) {
                Bot bot = new Bot();
                PlayerHandler botPlayer = new PlayerHandler(bot);
                botPlayer.character.setRole(newRole);
                playersList.add(botPlayer);
            } else {
                playersList.get(i).send("You are a " + newRole.toString() + "!\n");
                playersList.get(i).character = newRole.getCHARACTER();
                playersList.get(i).character.setRole(newRole);
            }
        }
        this.PLAYERS = new HashMap<>();
        for (PlayerHandler player : playersList) {
            this.PLAYERS.put(player.name, player);
        }
    }

    /**
     * This method assigns one of the 12 chat colors available to each player
     */
    private void giveAColorToEachPlayer() {
        putEnumColorInArrayList();
        for (PlayerHandler player : this.PLAYERS.values()) {
            for (int i = 0; i < this.PLAYERS.values().size(); i++) {
                player.textColor = colorList.get(i);
            }
        }
    }

    /**
     * This method puts all 12 available chat colors, inside an Enum, into an ArrayList
     */
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

    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.name +
                        (x.isBot ? (" (bot)") : "") +
                        " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    /**
     * This method allows a player to send a private message to another player
     *
     * @param name    a String, the name of the player you want to send the message to
     * @param message a String, the message you want to send to the othe player
     */
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
       // chat(displayVillageImage2());
        //chat(displayWolfImage());
        chat(displayeVillage2());
        chat(displayeVillage());

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
        putEnumColorInArrayList();
        for (int i = 0; i < this.PLAYERS.values().size(); i++) {
            playersList.get(i).textColor = colorList.get(i);
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

    /**
     * This method displays the game's welcome message in ASCII Art, in the color CYAN
     *
     * @return a String, the ASCII Art
     */
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

    /**
     * This method displays the game's night scenario in ASCII Art, in the color BLUE
     *
     * @return a String, the ASCII Art
     */
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


    private String displayeVillage2() {
        return Colors.WHITE +
                "\n" +
                "'  ██╗    █╗███████║██╗     █████═╗██████╗███╗   ██║███████╗    ████████╗██████╗ \n" +
                "'  ██║    █║███╔════██║    ██╔════██╔═══█║████╗ ███║██╔════╝    ╚══██╔══██╔═══██╗\n" +
                "'  ██║ █╗ █║██████╗ ██║    ██║    ██║   █║██╔████╔█║█████╗         ██║  ██║   ██║\n" +
                "'  ██║███╗█║███╔══╝ ██║    ██║    ██║   █║██║╚██╔╝█║██╔══╝         ██║  ██║   ██║\n" +
                "'   ╚███╔███╔███████║██████╚██████╚██████╔██║ ╚═ ██║██████╗        ██║  ╚██████╔╝\n" +
                "'    ╚══╝╚══╝╚══════╚══════╝╚═════╝╚═════╝╚═╝     ╚═╚══════╝       ╚═╝   ╚═════╝ \n" +
                Colors.RESET;
    }



    private String displayeVillage() {
        return Colors.RED +
                "'    ██████ ██▓███  ▒█████  ▒█████  ██ ▄█ ██   ██▓    ██▒   █ ██ ██▓    ██▓   ▄▄▄       ▄████ █████ \n" +
                "'  ▒██    ▒ ██░  ██ ██▒  ██ ██▒  ██ ██▄█   ██  ██▒    ██░   █ ██ ██▒   ▓██▒  ▒████▄    ██▒ ▀█ █   ▀ \n" +
                "'  ░ ▓██▄   ██░ ██▓ ██░  ██ ██░  ██ ███▄    ██ ██░     ██  █▒ ██ ██░   ▒██░  ▒██  ▀█▄  ██░▄▄▄ ███   \n" +
                "'        ██ ██▄█▓▒  ██   ██ ██   ██ ██ █▄    ▐██▓░      ██ █░ ██ ██░   ▒██░  ░██▄▄▄▄██ ▓█  ██ ▓█  ▄ \n" +
                "'  ▒██████▒ ██▒ ░  ░ ████▓▒  ████▓▒ ██  █▄   ██▒▓░       ▀█░  ██░██████░██████▓█   ▓██░▒▓███▀ ▒████▒\n" +
                "'  ▒ ▒▓▒ ▒  ▓▒░ ░  ░ ▒░▒░▒░  ▒░▒░▒░  ▒▒ ▓▒  ██▒▒▒       ░ ▐░ ░▓ ░ ▒░▓  ░ ▒░▓  ▒▒   ▓▒█░░▒   ▒ ░ ▒░ ░\n" +
                "'  ░ ░▒  ░  ▒ ░      ░ ▒ ▒░  ░ ▒ ▒░  ░▒ ▒░▓██ ░▒░       ░ ░░  ▒ ░ ░ ▒  ░ ░ ▒  ░▒   ▒▒ ░ ░   ░ ░ ░  ░\n" +
                "'  ░  ░  ░  ░      ░ ░ ░ ▒   ░ ░ ▒   ░░ ░ ▒ ▒ ░░          ░░  ▒ ░ ░ ░    ░ ░   ░   ▒  ░ ░   ░   ░   \n" +
                "'        ░             ░ ░     ░ ░    ░   ░ ░              ░  ░     ░  ░   ░  ░    ░  ░     ░   ░  ░\n" +
                "'                                         ░ ░             ░                                         \n"

                + Colors.RESET + "\n";
    }
    /**
     * This method displays the game's "you have been killed" message, in ASCII Art, in the color BLACK
     *
     * @return a String, the ASCII Art
     */
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


    private void play() throws InterruptedException {
        while (verifyIfGameContinues()) {
            try {
                if (this.night) {
                    nightShift();
                    choosePlayerWhoDies();
                    resetUsedVision();
                    resetDefense();
                    this.night = false;
                } else {
                    dayShift();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        resetGame();
    }

    private void nightShift() throws InterruptedException {
        chat("\n===== It's dark already. Time to sleep... =====");
        Thread.sleep(1800);
        chat(Images.displayWolfImage());
        Thread.sleep(1000);

        wolvesChat(Colors.RED + "===== Wolves chat is open! =====\n");
        Thread.sleep(1000);
        printAliveWolves();
        new Thread(this::botsNightVotes);
        Thread.sleep(10000);

        chat("10 seconds left until the end of the night...");
        Thread.sleep(10000);

        chat("The night is over...");
        Thread.sleep(1000);
    }

    private void dayShift() throws InterruptedException {
        chat(Colors.YELLOW + "\n===== It's day time. Chat with the other players! =====\n");
        Thread.sleep(10000);

        chat("10 seconds left until the end of the day...");
        Thread.sleep(10000);
        chat("The day is over...");
        Thread.sleep(1600);

        if (numOfDays != 0) {
            botsDayVotes();
            checkVotes();
        }
        this.night = true;
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

    private void printAliveWolves() {
        if (this.PLAYERS.size() >= 7) {
            String wolvesList = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .map(x -> x.name)
                    .reduce("Alive Wolves list:", (a, b) -> a + "\n" + b);
            wolvesChat(wolvesList);
        }
    }

    /**
     * This method resets the game, so when the game is over, players can restart it
     */
    private void resetGame() {
        this.gameInProgress = false;
        this.numOfDays = 0;
        this.night = false;
        Bot.resetBotNumber();
    }

    private void choosePlayerWhoDies() throws InterruptedException {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.getCharacter().getRole().equals(EnumRole.WOLF)
                        && x.alive && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList());

        botsNightVotes();
        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && !x.getCharacter().getRole().equals(EnumRole.WOLF))
                    .toList();
            this.victim = players.get((int) (Math.random() * players.size()));

        } else {
            this.victim = this.wolvesVotes.get((int) (Math.random() * this.wolvesVotes.size()));
        }
        this.victim.alive = false;
        wolvesChat(Colors.RED + "You have decided to kill... " + this.victim.name + Colors.RESET);
        Thread.sleep(1800);

        printNightDayTransition();
    }

    private void printNightDayTransition() throws InterruptedException {
        if (!this.victim.getCharacter().isDefended()) {
            this.victim.alive = false;
            this.victim.send(Images.displaySkullImage());
            Thread.sleep(500);
            this.victim.send((Colors.WHITE + "\n x.x You have been killed last night x.x") + Colors.RESET);
            Thread.sleep(2200);

            if (verifyIfGameContinues()) {
                chat("\n===== THIS IS DAY NUMBER " + ++numOfDays + " =====\n");
                Thread.sleep(2200);
                chat("Unfortunately, " + this.victim.name + " was killed by hungry wolves... Rest in peace, " + this.victim.name);
                Thread.sleep(3000);
                chat("(Type /list to check out the latest update)");
                Thread.sleep(2200);
                chat("\n===== Watch out! There are still wolves walking around. No one is safe! =====");
                Thread.sleep(2400);
            }
        } else {
            wolvesChat("... But he got protected by the guard! You'll stay hungry tonight!" + Colors.RESET + "\n");
            Thread.sleep(1800);
            chat("\n===== THIS IS DAY NUMBER " + ++numOfDays + " =====\n");
            Thread.sleep(2200);
            chat("===== HURRAYYYY! The guard bravely protected the village, so everyone survived the last night! =====");
            Thread.sleep(2400);
            chat("\n===== Watch out! There are still wolves walking around. No one is safe! =====");
            Thread.sleep(2400);
        }
    }

    private boolean verifyIfGameContinues() throws InterruptedException {
        int wolfCount = 0;
        int nonWolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.character.getRole().equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }
        return checkWinner(wolfCount, nonWolfCount);
    }

    /**
     * This method verifies whether there are sill wolves left in the game
     *
     * @return a boolean, true if there are alive wolves. False, if all the wolves are dead
     */
    private boolean ifThereAreAliveWolves() {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive && player.getCharacter().getRole().equals(EnumRole.WOLF))
                return true;
        }
        return false;
    }

    private boolean checkWinner(int wolfCount, int nonWolfCount) throws InterruptedException {
        if (wolfCount >= nonWolfCount) {
            chat("\nWolves took over the village... Anyone who's letf alive, will be eaten tonight... MUAHAHAHAHAHAH\n");
            Thread.sleep(2400);
            chat("===== GAME OVER =====");
            Thread.sleep(500);
            resetGame();
            this.PLAYERS.values().forEach(System.out::println);
            System.out.println(wolfCount + " " + nonWolfCount);
            return false;
        } else if (wolfCount == 0) {
            chat("\nThere are no wolves left alive! Villagers can now sleep deeply\n");
            Thread.sleep(2400);
            chat("===== GAME OVER =====");
            Thread.sleep(500);
            resetGame();
            this.PLAYERS.values().forEach(System.out::println);
            System.out.println(wolfCount + " " + nonWolfCount);
            return false;
        }
        return true;
    }

    /**
     * This method retrieves the PlayerHandler player, by a given name
     *
     * @param name a String, the name of the player we want to retrieve
     * @return an Optional, it either returns a PlayerHander of a null, without breaking the code
     */
    public Optional<PlayerHandler> getPlayerByName(String name) {
        return this.PLAYERS.values().stream()
                .filter(x -> Helpers.compareIfNamesMatch(x.getName(), name))
                .findFirst();
    }

    private void resetNumberOfVotes() {
        this.PLAYERS.values().forEach(x -> x.getCharacter().setNumberOfVotes(0));
        this.PLAYERS.values().forEach(x -> x.vote = null);
    }

    private void checkVotes() throws InterruptedException {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.alive)
                .max(Comparator.comparing(x -> x.getCharacter().getNumberOfVotes()))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().alive = false;
            chat("\n" + highestVote.get().name + " was tragically killed by the Villagers, thinking it was a wolf");
            Thread.sleep(2500);
        }
        resetNumberOfVotes();
    }

    /**
     * This method checks whether all the players have voted, or not. If not, the vote goes to itself. This prevents the game from stopping, while waiting for a vote from all the players
     */
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

    public HashMap<String, PlayerHandler> getPLAYERS() {
        return PLAYERS;
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

        public PlayerHandler(Socket clientSocket) {
            try {
                this.playerSocket = clientSocket;
                this.out = new BufferedWriter(new OutputStreamWriter(this.playerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                    System.out.println(name + ": " + this.message);

                    if (Server.this.night) {
                        switch (this.character.getRole()) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.name, this.message);
                            }
                            case FORTUNE_TELLER, GUARD -> dealWithCommand(this.message);
                            default -> {
                                if (this.message.split(" ")[0].equals(Command.QUIT.getCOMMAND()) ||
                                        this.message.split(" ")[0].equals(Command.COMMAND_LIST.getCOMMAND()))
                                    dealWithCommand(this.message);
                                else send("You are sleeping");
                            }
                        }
                    } else {
                        if (isCommand(this.message.trim())) {
                            dealWithCommand(this.message);
                        } else chat(this.textColor + this.name, this.message + Colors.RESET);

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
            if (this.isBot) return;
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

        public boolean isAlive() {
            return alive;
        }
    }
}
