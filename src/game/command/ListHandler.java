package game.command;

import game.EnumRole;
import game.Server.Server;

public class ListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if ((server.isNight() && player.getRole().equals(EnumRole.WOLF)) && server.isGameInProgress())
            player.send(server.playersInGame());
    }
}
