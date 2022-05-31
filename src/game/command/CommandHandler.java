package game.command;

import game.Server;

public interface CommandHandler {
    void command(game.Server server, game.Server.PlayerHandler player);
}
