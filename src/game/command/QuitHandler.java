package game.command;

import game.Server.Server;

public class QuitHandler implements CommandHandler{
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        player.playerDisconnected();
    }
}
