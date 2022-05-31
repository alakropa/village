package game.command;

import game.EnumRole;
import game.Server.Server;

public class ListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (!server.isNight() || (server.isNight() && player.getRole() == EnumRole.WOLF))
            player.send(server.playersInGame());
    }
}
