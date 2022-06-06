package game.Server;

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
    private int delay;
    private static int botNumber;

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

    /**
     * This method allows the player to connect to the server if the game hasn't already begun.
     * If so, the player socket is turn off.
     *
     * @throws IOException
     */
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
     * This method allows the player to enter a name and check if that name is available or not.
     * If not, the player can insert another name until he has chosen an available one.
     *
     * @param newPlayer PlayerHandler - contains a PlayerHandler instance.
     * @param in        BufferedReader - reads from the console of the player.
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
     * This method verifies if the given name is already in use or not.
     *
     * @param playerName a String, the player name.
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
     * This method adds the player to the XXXXX, and submits it into the Thread Pool.
     * It also sends a message to all the other players informing them that this particular player joined the game.
     *
     * @param playerHandler receives a PlayerHander, an inner class from the Server, that deals with the players, and its attributes.
     */
    private void addPlayer(PlayerHandler playerHandler) {
        this.PLAYERS.put(playerHandler.name, playerHandler);
        this.service.submit(playerHandler);
        chat(playerHandler.name, "joined the chat");
    }

    /**
     * This method allows the game to start. It initializes night and numOfDays sends a message to the payers,
     * deals the game roles and calls the play method.
     *
     * @throws InterruptedException
     */
    public void startGame() throws InterruptedException {
        this.night = false;
        this.numOfDays = 0;
        this.delay = PLAYERS.size() * 5000;
        chat(Images.welcomeTo());
        chat(Images.displaySpookyVillage());
        chat("\n===== Welcome to the Spooky Village! =====\n");
        Thread.sleep(1200);

        getRoles();
        setPlayersLife();
        Thread.sleep(150);
        chat(playersInGame());
        Thread.sleep(200);
        giveAColorToEachPlayer();
        play();
    }

    /**
     * This method sends a private message to every player, except the one sending it.
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
     * This method sends a private message to every player, except the one sending it.
     *
     * @param message a String, the message you want to send to the other players
     */
    public void chat(String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) player.send(message);
        }
    }

    /**
     * Iterates throu the players HashMap, counting how many bots it's needed to play
     * and dealing each player a role to play.
     */
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
                PlayerHandler botPlayer = new PlayerHandler();
                botPlayer.setRole(newRole);
                playersList.add(botPlayer);
            } else {
                playersList.get(i).send("You are a " + newRole.toString() + "!\n");
                playersList.get(i).setRole(newRole);
            }
        }

        this.PLAYERS = new HashMap<>();
        for (PlayerHandler player : playersList) {
            this.PLAYERS.put(player.name, player);
        }
    }

    /**
     * This method assigns one of the 12 chat colors available to each player.
     */
    private void giveAColorToEachPlayer() {
        putEnumColorInArrayList();
        List<PlayerHandler> playersList = new ArrayList<>();
        playersList.addAll(this.PLAYERS.values());
        for (int i = 0; i < this.PLAYERS.values().size(); i++) {
            playersList.get(i).textColor = colorList.get(i);
        }
    }

    /**
     * This method puts all 12 available chat colors, inside an Enum, into an ArrayList.
     */
    public void putEnumColorInArrayList() {
        colorList = new ArrayList<>();
        for (int i = 0; i < this.PLAYERS.size(); i++) {
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

    /**
     * Iterates throu the players HashMap to get the players name and if
     * their dead or alive.
     *
     * @return String, the list of players
     */
    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.name +
                        (x.isBot ? (" (bot)") : "") +
                        " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    /**
     * This method allows the whole game to work. It resumes the night and day
     * turns, where the player votes and kills are chosen. It also checks the bot
     * votes if necessary and checks when the game should end at each iteration.
     * At the end of the game, it resets some variables.
     *
     * @throws InterruptedException
     */
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

    /**
     * Prints the night stages of the game.
     *
     * @throws InterruptedException
     */

    private void nightShift() throws InterruptedException {
        chat(Colors.BLUE + "\n===== It's dark already. Time to sleep... =====" + ColorsRef.RESET.getCode());
        Thread.sleep(180);
        chat(Images.displayWolfImage());
        Thread.sleep(100);

        wolvesChat(Colors.RED + "===== Wolves chat is open! =====\n" + ColorsRef.RESET.getCode());
        Thread.sleep(100);
        printAliveWolves();
        Thread.sleep(this.delay);

        chat(Colors.BLUE + "10 seconds left until the end of the night..." + ColorsRef.RESET.getCode());
        Thread.sleep(10000);

        chat(Colors.BLUE + "The night is over..." + ColorsRef.RESET.getCode());
        Thread.sleep(100);
    }

    /**
     * Prints the day stage of the game, checks the players votes and
     * increases the numOfDays.
     *
     * @throws InterruptedException
     */
    private void dayShift() throws InterruptedException {
        if (this.numOfDays != 0) printBeginingOfTheDay();
        if (this.numOfDays == 0)
            chat(Colors.YELLOW + "\n===== It's day time. Chat with the other players! =====\n" + ColorsRef.RESET.getCode());
        Thread.sleep(this.delay);
        chat(Colors.YELLOW + "10 seconds left until the end of the day..." + ColorsRef.RESET.getCode());
        Thread.sleep(10000);
        chat(Colors.YELLOW + "The day is over..." + ColorsRef.RESET.getCode());
        Thread.sleep(160);

        if (numOfDays != 0) {
            checkVotes();
        }
        this.night = true;
        this.numOfDays++;
    }

    /**
     * Prints the begging of the day after the day 0.
     *
     * @throws InterruptedException
     */
    private void printBeginingOfTheDay() throws InterruptedException {
        chat(Colors.YELLOW + "\n===== THIS IS DAY NUMBER " + numOfDays + " =====\n" + ColorsRef.RESET.getCode());
        Thread.sleep(220);
        chat("Unfortunately, " + this.victim.textColor + this.victim.name + ColorsRef.RESET.getCode() +
                " was killed by hungry wolves... Rest in peace, " + this.victim.textColor + this.victim.name + ColorsRef.RESET.getCode());
        Thread.sleep(300);
        chat("(Type " + Colors.RED + "/list " + ColorsRef.RESET.getCode() + "to check out the latest update)" + ColorsRef.RESET.getCode());
        Thread.sleep(220);
        chat(Colors.RED + "\n===== Watch out! There are still wolves walking around. No one is safe! =====" + ColorsRef.RESET.getCode());
        Thread.sleep(240);
        chat(Colors.YELLOW + "\n===== Time to vote and kill the wolf! =====" + ColorsRef.RESET.getCode());
        Thread.sleep(200);
        chat("\n(Type " + Colors.RED + "/vote name " + ColorsRef.RESET.getCode() + "to choose a player)");
        Thread.sleep(220);
    }

    /**
     * This method allows sending messages to the wolves only.
     *
     * @param name    a String, which contains the name of the player sending the message.
     * @param message a String, which contains the message to be sent.
     */
    public void wolvesChat(String name, String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.getRole().equals(EnumRole.WOLF)
                        && !x.name.equals(name) && !x.isBot)
                .forEach(x -> x.send(name + ": " + message));
    }

    /**
     * This method allows sending messages to the wolves only.
     *
     * @param message a String, which contains the message to be sent.
     */
    public void wolvesChat(String message) {
        this.PLAYERS.values().stream()
                .filter(x -> x.getRole().equals(EnumRole.WOLF) && !x.isBot)
                .forEach(x -> x.send(message));
    }

    /**
     * Prints the wolves that are still alive.
     */
    private void printAliveWolves() {
        if (this.PLAYERS.size() == 12) {
            String wolvesList = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && x.getRole().equals(EnumRole.WOLF))
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
        botNumber = 0;
    }

    /**
     * Iterates throu the wolvesVotes
     *
     * @throws InterruptedException
     */
    private void choosePlayerWhoDies() throws InterruptedException {
        this.wolvesVotes = this.PLAYERS.values().stream()
                .filter(x -> x.getRole().equals(EnumRole.WOLF)
                        && x.alive && x.vote != null)
                .map(x -> x.vote)
                .collect(Collectors.toList());

        if (this.wolvesVotes.size() == 0) {
            List<PlayerHandler> players = this.PLAYERS.values().stream()
                    .filter(x -> x.alive && !x.isDefended()
                            && !x.getRole().equals(EnumRole.WOLF))
                    .toList();
            this.victim = players.get((int) (Math.random() * players.size()));

        } else {
            this.victim = this.wolvesVotes.stream()
                    .filter(x -> !x.isDefended())
                    .findAny()
                    .orElse(null);
        }
        if (this.victim != null) {
            this.victim.alive = false;
            wolvesChat(Colors.RED + "You have decided to kill... " + this.victim.name + Colors.RESET);
            Thread.sleep(180);
        }
        printNightDayTransition();
    }

    private void printNightDayTransition() throws InterruptedException {
        if (!this.victim.isDefended()) {
            this.victim.alive = false;
            this.victim.send(Images.displaySkullImage());
            Thread.sleep(500);
            this.victim.send((Colors.RED + "\n x.x You have been killed last night x.x") + Colors.RESET);
            Thread.sleep(220);
        } else {
            wolvesChat("... But he got protected by the guard! You'll stay hungry tonight!" + Colors.RESET + "\n");
            Thread.sleep(180);
            chat("\n===== THIS IS DAY NUMBER " + ++numOfDays + " =====\n");
            Thread.sleep(200);
            chat("===== HURRAYYYY! The guard bravely protected the village, so everyone survived the last night! =====");
            Thread.sleep(220);
            chat("\n===== Watch out! There are still wolves walking around. No one is safe! =====");
            Thread.sleep(200);
        }
    }

    private boolean verifyIfGameContinues() throws InterruptedException {
        int wolfCount = 0;
        int nonWolfCount = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (player.alive) {
                if (player.getRole().equals(EnumRole.WOLF)) wolfCount++;
                else nonWolfCount++;
            }
        }
        return checkWinner(wolfCount, nonWolfCount);
    }

    private boolean checkWinner(int wolfCount, int nonWolfCount) throws InterruptedException {
        if (wolfCount >= nonWolfCount) {
            chat(Colors.RED + "\nWolves took over the village... Anyone who's letf alive, will be eaten tonight... MUAHAHAHAHAHAH\n" + Colors.RESET);
            Thread.sleep(2400);
            chat(Images.displayGameOver(Colors.RED));
            Thread.sleep(500);
            chat("===== GAME OVER =====");
            Thread.sleep(500);
            resetGame();
            return false;
        } else if (wolfCount == 0) {
            chat(Colors.GREEN + "\nThere are no wolves left alive! Villagers can now sleep deeply...\n" + Colors.RESET);
            Thread.sleep(2400);
            chat(Images.displayGameOver(Colors.GREEN));
            Thread.sleep(500);
            chat("===== GAME OVER =====");
            Thread.sleep(500);
            resetGame();
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
        this.PLAYERS.values().forEach(x -> x.setNumberOfVotes(0));
        this.PLAYERS.values().forEach(x -> x.vote = null);
    }

    private void checkVotes() throws InterruptedException {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.alive)
                .max(Comparator.comparing(x -> x.getNumberOfVotes()))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().alive = false;
            chat(highestVote.get().textColor + "\n" + highestVote.get().name + ColorsRef.RESET.getCode() +
                    " was tragically killed by the Villagers as they thought it was a wolf...");
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
                .map(player -> player.name + ": " + player.getNumberOfVotes())
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
                .filter(x -> x.getRole().equals(EnumRole.FORTUNE_TELLER) && !x.isBot)
                .forEach(x -> x.setUsedVision(false));
    }

    public HashMap<String, PlayerHandler> getPLAYERS() {
        return PLAYERS;
    }

    private void resetDefense() {
        this.PLAYERS.values().stream()
                .filter(PlayerHandler::isDefended)
                .forEach(x -> x.setDefended(false));
    }

    public class PlayerHandler implements Runnable {
        private String name;
        private Socket playerSocket;
        private BufferedWriter out;
        private PlayerHandler vote;
        private PlayerHandler previousVote;
        private String message;
        private boolean isBot;
        private boolean alive;
        private String textColor = ColorsRef.RESET.getCode();
        private EnumRole role;
        private int numberOfVotes;
        private boolean defended;
        private PlayerHandler previousDefend;
        private HashMap<String, Boolean> visions;
        private boolean usedVision;

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
                this.visions = new HashMap<>();
                this.out = new BufferedWriter(new OutputStreamWriter(this.playerSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public PlayerHandler() {
            this.name = "Bot" + botNumber++;
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
                        switch (this.getRole()) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.name, this.message);
                            }
                            case FORTUNE_TELLER, GUARD -> dealWithCommand(this.message);
                            default -> {
                                if (this.message.split(" ")[0].equals(Command.QUIT.getCOMMAND()) ||
                                        this.message.split(" ")[0].equals(Command.ROLE.getCOMMAND()) ||
                                        this.message.split(" ")[0].equals(Command.COMMAND_LIST.getCOMMAND()))
                                    dealWithCommand(this.message);
                                else send("You are sleeping");
                            }
                        }
                    } else {
                        if (isCommand(this.message.trim())) {
                            dealWithCommand(this.message);
                        } else if (gameInProgress && this.alive)
                            chat(this.textColor + this.name, this.message+Colors.RESET);
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

        public PlayerHandler getPreviousVote() {
            return previousVote;
        }

        public void setPreviousVote(PlayerHandler previousVote) {
            this.previousVote = previousVote;
        }

        public void increaseNumberOfVotes() {
            this.numberOfVotes++;
        }

        public EnumRole getRole() {
            return role;
        }

        public void decreaseNumberOfVotes() {
            this.numberOfVotes--;
        }

        public int getNumberOfVotes() {
            return numberOfVotes;
        }

        public void setNumberOfVotes(int numberOfVotes) {
            this.numberOfVotes = numberOfVotes;
        }


        public void setRole(EnumRole role) {
            this.role = role;
        }

        public boolean isDefended() {
            return defended;
        }

        public void setDefended(boolean defended) {
            this.defended = defended;
        }

        public PlayerHandler getPreviousDefend() {
            return previousDefend;
        }

        public void setPreviousDefend(PlayerHandler previousDefend) {
            this.previousDefend = previousDefend;
        }

        public HashMap<String, Boolean> getVisions() {
            return visions;
        }

        public void addVisions(String playerName, Boolean isWolf) {
            this.visions.put(playerName, isWolf);
        }

        public boolean hasUsedVision() {
            return usedVision;
        }

        public void setUsedVision(boolean usedVision) {
            this.usedVision = usedVision;
        }
    }
}
