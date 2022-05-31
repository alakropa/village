public enum Command {
    LIST("/list", new ListHandler()),
    HELP("/help", new HelpHandler()),
    WHISPER("/whisper", new WhisperHandler()),
    QUIT("/quit", new QuitHandler()),
    SHOUT("/shout", new ShoutHandler());

    private final String DESCRIPTION;
    private final CommandHandler HANDLER;

    Command(String description, CommandHandler handler) {
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

    public CommandHandler getHANDLER() {
        return this.HANDLER;
    }
}
