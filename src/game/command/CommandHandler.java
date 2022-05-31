package game.command;

import game.Server;

public interface CommandHandler {
    void command(Server server, Server.PlayerHandler player);
}
