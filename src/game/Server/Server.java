package game.Server;

import game.Characters.Bot;
import game.Characters.Character;
import game.Characters.FortuneTeller;
import game.EnumRole;
import game.Helpers;
import game.Images;
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
    private HashMap<String, PlayerHandler> PLAYERS;
    private boolean gameInProgress;
    private boolean night;
    private List<PlayerHandler> wolvesVotes;
    private PlayerHandler victim;
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

    private String verifyIfNameIsAvailable(PlayerHandler newPlayer, BufferedReader in) throws IOException {
        String playerName = in.readLine();
        while (!checkIfNameIsAvailable(playerName) || playerName.startsWith("/") || playerName.equals("")) {
            newPlayer.send("You can't choose this name. Try another one");
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

    public void chat(String name, String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.name.equals(name) && !player.isBot)
                player.send(name + ": " + message);
        }
    }

    public void chat(String message) {
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) player.send(message);
        }
    }

    private void getRoles() {
        List<PlayerHandler> playersList = new ArrayList<>();
        int playersInGame = checkNumberOfPlayers(playersList);

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

    public String playersInGame() {
        return this.PLAYERS.values().stream()
                .map(x -> x.name +
                        (x.isBot ? (" (bot)") : "") +
                        " - " + (x.alive ? "Alive" : "Dead"))
                .reduce("Players list:", (a, b) -> a + "\n" + b);
    }

    private void play() {
        while (verifyIfGameContinues()) {
            try {
                if (this.night) {
                    nightShift();
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
        Thread.sleep(1200);

        wolvesChat(Colors.RED + "===== Wolves chat is open! =====\n");
        Thread.sleep(1000);
        printAliveWolves();
        new Thread(this::botsNightVotes);
        Thread.sleep(10000);

        chat("10 seconds left until the end of the night...");
        Thread.sleep(10000);

        chat("The night is over...");
        Thread.sleep(1800);

        choosePlayerWhoDies();

        if (ifThereAreAliveWolves()) {
            chat("\n===== Watch out! There are still wolves walking around. No one is safe! =====");
            Thread.sleep(2400);
        }

        resetUsedVision();
        this.night = false;
    }

    private void dayShift() throws InterruptedException {
        chat(Colors.YELLOW + "\n===== It's day time. Chat with the other players! =====\n");
        Thread.sleep(10000);


        chat("10 seconds left until the end of the night...");
        Thread.sleep(10000);
        chat("The day is over...");

        if (numOfDays != 0) {
            botsDayVotes();
            checkVotes();
        }
        Thread.sleep(1500);
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

    private int checkNumberOfPlayers(List<PlayerHandler> playersList) {
        int nonBots = 0;
        for (PlayerHandler player : this.PLAYERS.values()) {
            if (!player.isBot) {
                nonBots++;
                playersList.add(player);
            }
        }
        return Math.max(nonBots, 5);
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
        wolvesChat(Colors.RED + "You have decided to kill... " + this.victim.name + Colors.RESET + "\n");
        Thread.sleep(1500);
        this.victim.send(Images.displaySkullImage());
        this.victim.send((Colors.BLACK + "\n x.x You have been killed last night x.x") + Colors.RESET);
        Thread.sleep(1600);
        chat("\n===== THIS IS DAY NUMBER " + ++numOfDays + " =====\n");
        Thread.sleep(1600);
        chat("Unfortunately, " + this.victim.name + " was killed by hungry wolves... Rest in peace, " + this.victim.name);
        Thread.sleep(2400);
        chat("(Type /list to check out the latest update)");
        Thread.sleep(1800);
    }

    private boolean verifyIfGameContinues() {
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

    private boolean ifThereAreAliveWolves() {
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
            chat("\nThe wolves won!\nGame over");
            resetGame();
            this.PLAYERS.values().forEach(System.out::println);
            System.out.println(wolfCount + " " + nonWolfCount);
            return false;
        } else if (wolfCount == 0) {
            chat("\nThe villagers won!\nThere are no wolves left alive\n" + "GAME OVER");
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

    private void checkVotes() throws InterruptedException {
        checkIfAllPlayersVoted();
        Optional<PlayerHandler> highestVote = PLAYERS.values().stream()
                .filter(player -> player.alive)
                .max(Comparator.comparing(x -> x.getCharacter().getNumberOfVotes()))
                .stream().findAny();

        if (highestVote.isPresent() && this.numOfDays != 0) {
            highestVote.get().alive = false;
            chat(highestVote.get().name + " was tragically killed by the Villagers, thinking it was a wolf");
            Thread.sleep(2500);
        }
        resetNumberOfVotes();
        this.PLAYERS.values().stream().toList().forEach(System.out::println);
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

    public HashMap<String, PlayerHandler> getPLAYERS() {
        return PLAYERS;
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
                    System.out.println(name + ": " + this.message); //imprime no server as msg q recebe dos clients

                    if (Server.this.night) {
                        switch (this.character.getRole()) {
                            case WOLF -> {
                                if (isCommand(this.message)) dealWithCommand(this.message);
                                else wolvesChat(this.name, this.message);
                            }
                            case FORTUNE_TELLER -> dealWithCommand(this.message);
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

        @Override
        public String toString() {
            return "PlayerHandler{" +
                    "NAME='" + name + '\'' + this.alive +
                    '}';
        }

        public boolean isAlive() {
            return alive;
        }
    }
}
