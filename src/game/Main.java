package game;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        /*
    DONE TEMPORIZADOR /sorry caps
    DONE dizer que começa a noite (inicio do jogo)
    DONE os aldeoes estão a dormir
    DONE os lobos recebem msg sobre quem é o outro lobo
    DONE se houver 2 lobos, temos que fazer escolha do quem morre (não podem ser 2 killed)
    DONE limitar o número de votos do lobo
    DONE não se pode fazer vote no 1º dia
    DONE qd ninguem votar dele haver uma escolha random dos votos para matar alguém (noite)
    DONE dizer que começa o dia
    DONE função da votação
    DONE fazer escolha entre os mais votados (no caso de empate))


 Nas rondas que o vidente já sabe quem são todos os lobos, o vidente não acorda mais (não abre o chat dele e está tratado como um aldeão normal);
- limitar o nº de visions a noite
- mensgem para os lobos qd matam alguém
-mensagem para aldeia quem morre
 - escrever a verificação se o jogo pode continuar (loop)
  -função check winner
    -mensagem final

extras:
         - usar a voz como imput e output stream
        - criar bots (criação do bot, verificação no start do jogo se e preciso fazer bots)
        - imagens (noite, dia, lobo, aldeao, fortuneteller)
        -adicionar mais papeis (fairy/protect , procurar outros)
        - command /suicide


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
        System.out.println(image);
        System.out.println(image2);

        //Start tem de reiniciar as variáveis alive
        //



    }
}
