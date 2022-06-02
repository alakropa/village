package game.command;

import game.Server.Server;

public class StartHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (server.isGameInProgress()) {
            player.send("Game has already started");
            return;
        }
        System.out.println("Aqui?");
        server.setGameInProgress(true);
        System.out.println("Aqui?2");
        new Thread(server::startGame).start();
        System.out.println("Aqui?3");
        System.out.println(server.isGameInProgress());
    }
}
