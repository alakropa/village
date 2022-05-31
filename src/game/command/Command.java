package game.command;

public enum Command {
    LIST("/list", new ListHandler()),
    START ("/start", new StartHandler()),
    KILL("/kill", new KillHandler()),
    VISION("/vision", new VisionHandler()),
    VOTE("/vote", new VoteHandler());

    private final String DESCRIPTION;
    private final StartHandler.CommandHandler HANDLER;

    Command(String description, StartHandler.CommandHandler handler) {
        this.DESCRIPTION = description;
        this.HANDLER = handler;
    }

    public static Command getCommandFromDescription(String description) {
        for (Command command : Command.values()) {
            if (command.DESCRIPTION.equals(description)) {
                return command;
            }
        }
        return null;
    }



    public String getDESCRIPTION() {
        return this.DESCRIPTION;
    }

    public StartHandler.CommandHandler getHANDLER() {
        return this.HANDLER;
    }
}
