package game;

public class Main {
    public static void main(String[] args) {
        /*
        TO DO LIST:
            Extras:
            Usar a voz como imput e output stream do chat
            Command /suicide

        DONE LIST:
            DONE Ver ignore case nos nomes
            DONE VisionsList bug quando se reinicia o jogo
            DONE Manter o esquema de matar uma pessoa aleatória durante o dia (com 12 players, o jogo acaba em 3 rodadas caso os 3 lobos sobrevivam) ???
            DONE Mensagem para os lobos quando matam alguém
            DONE Mensagem para a aldeia sobre quem morreu durante a noite
            DONE Usar Thread.sleep() em várias zonas do código para que o código da consola seja mais intervalado
            DONE Criar bots (criação do bot, verificação, no start do jogo, se e preciso fazer bots)
            DONE Imagens (noite, dia, lobo, aldeao, fortuneteller)
            DONE Adicionar mais papeis (fairy/protect, procurar outros)
            DONE O vidente só consegue ter uma visão por noite
            DONE Wolves não conseguem matar-se uns aos outros
            DONE Fortune Tellers não conseguem usar /vision no seu próprio nome
            DONE Método checkWinner com a mensagem final
            DONE Quando um jogador sai (/quit ou terminando o terminal), deixa de aparecer na lista, sem aparentes buggs
            DONE Quando os lobos ganham, e ainda há villagers vivos, esses jogadores já não conseguem usar comandos
            DONE Remover espaço em branco quando o comando não funciona
            DONE Depois do primeiro gameover, se um lobo se desconectar, o resultado do jogo não muda
            DONE Quando o jogo acaba, ninguém pode usar comandos para além de /cmd, /start e /quit
            DONE Comando /start também permite fazer restart ao jogo
            DONE Não se pode fazer vote depois do jogo acabar
            DONE Método resetGame() reinicia as variáveis quando o jogo acaba
            DONE Start só pode ser usado quando o jogo ainda não foi iniciado
            DONE Não se pode matar ninguém no primeiro turno
            DONE Temporizador
            DONE Jogo começa de dia sem o comando /vote
            DONE Os aldeoes estão a dormir
            DONE Os lobos recebem msg sobre quem é o outro lobo
            DONE Se houver 2 lobos, temos que fazer escolha do quem morre (não podem ser 2 killed)
            DONE limitar o número de votos do lobo
            DONE não se pode fazer vote no 1º dia
            DONE qd ninguem votar dele haver uma escolha random dos votos para matar alguém (noite)
            DONE dizer que começa o dia
            DONE função da votação
            DONE fazer escolha entre os mais votados (no caso de empate))
            DONE escrever a verificação se o jogo pode continuar (loop)


         */
        String image2 = ".-.   .-.      .-.                                         .-.         .-. .-.         \n" +
                ": :.-.: :      : :                                        .' `.       .' `.: :         \n" +
                ": :: :: : .--. : :   .--.  .--. ,-.,-.,-. .--.            `. .'.--.   `. .': `-.  .--. \n" +
                ": `' `' ;' '_.': :_ '  ..'' .; :: ,. ,. :' '_.' _  _  _    : :' .; :   : : : .. :' '_.'\n" +
                " `.,`.,' `.__.'`.__;`.__.'`.__.':_;:_;:_;`.__.':_;:_;:_;   :_;`.__.'   :_; :_;:_;`.__.'\n" +
                "                                                                                       \n" +
                "                                                                                       \n" +
                " .--.                   .-.           .-..-. _ .-.  .-.                     .-.        \n" +
                ": .--'                  : :.-.        : :: ::_;: :  : :                     : :        \n" +
                "`. `. .---.  .--.  .--. : `'.'.-..-.  : :: :.-.: :  : :   .--.   .--.  .--. : :        \n" +
                " _`, :: .; `' .; :' .; :: . `.: :; :  : `' ;: :: :_ : :_ ' .; ; ' .; :' '_.':_;        \n" +
                "`.__.': ._.'`.__.'`.__.':_;:_;`._. ;   `.,' :_;`.__;`.__;`.__,_;`._. ;`.__.':_;        \n" +
                "      : :                      .-. :                             .-. :                 \n" +
                "      :_;                      `._.'                             `._.'                 ";
        String image = " _ _ _       _                                    _           _    _        \n" +
                "| | | | ___ | | ___  ___ ._ _ _  ___            _| |_ ___   _| |_ | |_  ___ \n" +
                "| | | |/ ._>| |/ | '/ . \\| ' ' |/ ._> _  _  _    | | / . \\   | |  | . |/ ._>\n" +
                "|__/_/ \\___.|_|\\_|_.\\___/|_|_|_|\\___.<_><_><_>   |_| \\___/   |_|  |_|_|\\___.\n" +
                "                                                                            \n" +
                " ___                 _          _ _  _  _  _                 _              \n" +
                "/ __> ___  ___  ___ | |__ _ _  | | |<_>| || | ___  ___  ___ | |             \n" +
                "\\__ \\| . \\/ . \\/ . \\| / /| | | | ' || || || |<_> |/ . |/ ._>|_/             \n" +
                "<___/|  _/\\___/\\___/|_\\_\\`_. | |__/ |_||_||_|<___|\\_. |\\___.<_>             \n" +
                "     |_|                 <___'                    <___'                     ";

        //System.out.println(image2);


//                switch (temp) {
//                    case 0:
//                        player.send((Colors.RED + name + ": " + message + ColorsRef.RESET));
//                    case 1:
//                        player.send(ColorsRef.GREEN + name + ": " + message + ColorsRef.RESET);
//                    case 2:
//                        player.send(ColorsRef.YELLOW + name + ": " + message + ColorsRef.RESET);
//                    case 3:
//                        player.send(ColorsRef.BLUE + name + ": " + message + ColorsRef.RESET);
//                    case 4:
//                        player.send(ColorsRef.MAGENTA + name + ": " + message + ColorsRef.RESET);
//                    case 5:
//                        player.send(ColorsRef.CYAN + name + ": " + message + ColorsRef.RESET);
//                    case 6:
//                        player.send(ColorsRef.WHITE + name + ": " + message + ColorsRef.RESET);
//                    case 7:
//                        player.send(ColorsRef.RED_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 8:
//                        player.send(ColorsRef.GREEN_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 9:
//                        player.send(ColorsRef.YELLOW_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 10:
//                        player.send(ColorsRef.BLUE_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 11:
//                        player.send(ColorsRef.MAGENTA_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                }


//
//        for (int i = 0; i < this.PLAYERS.size(); i++) {
//                switch (i) {
//                    case 0:
//                        this.PLAYERS.get(i).send(Colors.RED + name + ": " + message + ColorsRef.RESET);
//                    case 1:
//                        this.PLAYERS.get(i).send(ColorsRef.GREEN + name + ": " + message + ColorsRef.RESET);
//                    case 2:
//                        this.PLAYERS.get(i).send(ColorsRef.YELLOW + name + ": " + message + ColorsRef.RESET);
//                    case 3:
//                        this.PLAYERS.get(i).send(ColorsRef.BLUE + name + ": " + message + ColorsRef.RESET);
//                    case 4:
//                        this.PLAYERS.get(i).send(ColorsRef.MAGENTA + name + ": " + message + ColorsRef.RESET);
//                    case 5:
//                        this.PLAYERS.get(i).send(ColorsRef.CYAN + name + ": " + message + ColorsRef.RESET);
//                    case 6:
//                        this.PLAYERS.get(i).send(ColorsRef.WHITE + name + ": " + message + ColorsRef.RESET);
//                    case 7:
//                        this.PLAYERS.get(i).send(ColorsRef.RED_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 8:
//                        this.PLAYERS.get(i).send(ColorsRef.GREEN_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 9:
//                        this.PLAYERS.get(i).send(ColorsRef.YELLOW_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 10:
//                        this.PLAYERS.get(i).send(ColorsRef.BLUE_UNDERLINED + name + ": " + message + ColorsRef.RESET);
//                    case 11:
//                        this.PLAYERS.get(i).send(ColorsRef.MAGENTA_UNDERLINED + name + ": " + message + ColorsRef.RESET);
    }
}
