package game.command;

public class StartHandler {
    public static interface CommandHandler {
        void command(Server server, Server.PlayerHandler player);
    }
}
