package game.command;

import java.util.Arrays;

public enum Command {
    START("/start", new StartHandler(), "Starts/Resarts the game"),
    LIST("/list", new ListHandler(), "Lists the players in game"),
    COMMAND_LIST("/cmd", new CommandListHandler(), "Lists the game commands"),
    VOTE("/vote", new VoteHandler(), "Vote the player that you want to eliminate"),
    KILL("/kill", new KillHandler(), "Choose the player that you want to kill (available to Wolfs only)"),
    VISION("/vision", new VisionHandler(), "Once per night you can choose a player to check if it's a wolf (available to Furtune Teller only)"),
    VISIONS_LIST("/visionsList", new VisionListHandler(), "Lists the previous visions (available to Furtune Teller only)"),
    QUIT("/quit", new QuitHandler(), "Quit from the game");
    //Fazer comando com as regras do jogo

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
            if (command.COMMAND.equalsIgnoreCase(description.trim())) {
                return command;
            }
        }
        return null;
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
