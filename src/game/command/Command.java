package game.command;

import java.util.Arrays;

public enum Command {
    START("/start", new StartHandler(), "Starts the game"),
    LIST("/list", new ListHandler(), "Lists the players in game"),
    VOTE("/vote", new VoteHandler(), "Vote the player that you want to eliminate"),
    KILL("/kill", new KillHandler(), "Choose the player that you want to kill (available to Wolfs only)"),
    VISION("/vision", new VisionHandler(), "Choose a player to check it's role (available to Furtune Teller only)");

    private final String COMMAND;
    private final CommandHandler HANDLER;
    private final String DESCRIPTION;

    Command(String command, CommandHandler handler, String description) {
        this.COMMAND = command;
        this.HANDLER = handler;
        this.DESCRIPTION = description;
    }

    public static Command getCommandFromDescription(String description) {
        for (Command command : Command.values()) {
            if (command.COMMAND.equals(description)) {
                return command;
            }
        }
        return null;
    }

    public String getCOMMAND() {
        return this.COMMAND;
    }

    public CommandHandler getHANDLER() {
        return this.HANDLER;
    }

    public static String getCommandList() {
        return "Commands list:" + Arrays.stream(Command.values())
                .map(x -> x.COMMAND + " - " + x.DESCRIPTION)
                .reduce("", (a, b) -> a + "\n" + b);
    }
}
