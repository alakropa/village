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

        System.out.println(Images.displayVillager());
        System.out.println(Images.displayGuard());
        System.out.println(Images.displayFortuneTeller());
        System.out.println(Images.displayWolf());
        System.out.println(Images.displayDeath());
        System.out.println(Images.displayDay());
    }
}
