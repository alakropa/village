package game.command;

public class StartHandler {
    public static interface CommandHandler {
        void command(game.Server server, game.Server.PlayerHandler player);
    }
}
