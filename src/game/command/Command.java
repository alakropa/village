package game.command;

import game.colors.Colors;

import java.util.Arrays;

public enum Command {
    START("/start", new StartHandler(), Colors.WHITE + "Starts/Resarts the game" + Colors.RESET),
    LIST("/list", new ListHandler(), Colors.WHITE + "Lists the players in game" + Colors.RESET),
    COMMAND_LIST("/cmd", new CommandListHandler(), Colors.WHITE + "Lists the game commands" + Colors.RESET),
    VOTE("/vote", new VoteHandler(), Colors.WHITE + "Vote the player that you want to eliminate" + Colors.RESET),
    KILL("/kill", new KillHandler(), Colors.WHITE + "Choose the player that you want to kill (available to Wolfs only)" + Colors.RESET),
    VISION("/vision", new VisionHandler(), Colors.WHITE + "Once per night you can choose a player to check if it's a wolf (available to Furtune Teller only)" + Colors.RESET),
    DEFEND("/defend", new DefendHandler(),Colors.WHITE +"Once per night you can choose a player to protect, so surely it won't be attacked by wolves (available to Guard only)" + Colors.RESET),
    VISIONS_LIST("/visionsList", new VisionListHandler(), Colors.WHITE + "Lists the previous visions (available to Furtune Teller only)" + Colors.RESET),
    QUIT("/quit", new QuitHandler(), Colors.WHITE + "Quit from the game" + Colors.RESET + "\n");


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

    public String getCOMMAND() {
        return COMMAND;
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
