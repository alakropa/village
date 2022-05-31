package game.command;

import game.Server;

public class StartHandler implements CommandHandler{
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        server.startGame();
    }
}
