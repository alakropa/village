package game.command;

import game.Server.Server;

public class StartHandler implements CommandHandler{
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if(!server.isGameInProgress()) {
            server.startGame();
            server.setGameInProgress(true);
        }
    }
}
