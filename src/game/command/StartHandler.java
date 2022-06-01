package game.command;

import game.Server.Server;

public class StartHandler implements CommandHandler{
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if(!server.isGameInProgress()) {
            server.setGameInProgress(true);
            new Thread(server::startGame).start();
            System.out.println(server.isGameInProgress());
        }
    }
}
