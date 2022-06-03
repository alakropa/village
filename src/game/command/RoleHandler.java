package game.command;

import game.Server.Server;

public class RoleHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (server.isGameInProgress())
            player.send(player.getCharacter().getRole().toString());
    }
}
