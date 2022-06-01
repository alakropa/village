package game;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        /*
       João - TEMPORIZADOR /sorry caps

        -dizer que começa a noite (inicio do jogo)
       - os aldeoes estão a dormir
       - os lobos recebem msg sobre quem é o outro lobo
     Ala  - se houver 2 lobos, temos que fazer escolha do quem morre (não podem ser 2 killed)
     Ala  -Nas rondas que o vidente já sabe quem são todos os lobos, o vidente não acorda mais (não abre o chat dele e está tratado como um aldeão normal);


- limitar o nº de visions a noite
- limitar o número de votos do lobo
- não se pode fazer vote no 1º dia
- limitar o vito no 1º dia
- mensgem para os lobos qd matam alguém
-qg alguém


       5. começa o dia:
       -dizer que começa o dia
      - informar quem morreu
      - função da votação
       (temporizador,
       se o utilizador não votar, o voto fica nele próprio,
       contar os votos,
       fazer escolha entre os mais votados (no caso de empate))
       - no final da votação, morre quem teve mais votos (imprimir msg)

       - escrever a verificação se o jogo pode começar
     Elisa - escrever a verificação se o jogo pode continuar

      -função check winner
      -mensagem final



extras:
         - usar a voz como imput e output stream
        - criar bots (criação do bot, verificação no start do jogo se e preciso fazer bots)
        - imagens (noite, dia, lobo, aldeao, fortuneteller)
        -adicionar mais papeis (fairy/protect , procurar outros)
        - command /suicide






      DONE - lobo pode matar /kill
      DONE - vidente pode perguntar /vision



      DONE mostrar a lista dos jogadores
      DONE cada um pode fazer /vote

     DONE fazer com que não haja nomes repetidos
     DONE mostrar a lista de jogadores (com número de votos) sempre que haja o vote



     LOBOS:
     no caso de 2 lobos:
se a escolha deles não for mútua:
1 vez aparece mensagem “please choose again” + 5 sec
se a escolha deles não for mútua:
o programa escolhe aleatoriamente 1 pessoa dos votos
se 1 dos lobos não votar:
o programa escolhe o voto do outro lobo

se nenhum dos lobos votar:
O programa faz a escolha aleatória (morre qualquer um, menos os lobos)


o servidor faz a verificação de número de jogadores ativos e se o jogador que foi escolhido está vivo;
se o jogador escolhido estiver morto e o tempo dos lobos ainda não tiver acabado, o programa pede para escolherem mais 1 vez
se o jogador escolhido estiver morto e o tempo dos lobos tiver acabado o programa faz a escolha aleatória (morre qualquer um, menos os lobos);
verificar se o jogo pode continuar

no caso de 3 lobos:
se a escolha deles não for mútua:
1 vez aparece mensagem “please choose again” + 5 sec
se 1 ou 2 dos lobos não votar:
o programa escolhe o voto do outro lobo
se a escolha deles não for mútua:
o programa escolhe aleatoriamente 1 pessoa dos votos
o servidor faz a verificação de número de jogadores ativos e se o jogador está vivo
verificar se o jogo pode continuar






         */


        String[] words = "/vision".split(" ", 2);
        if (words.length >= 2) System.out.println(words[1]);

        HashMap<String, Integer> sdgv = new HashMap<>();

        System.out.println(sdgv.put("asd", 1));
        System.out.println(sdgv.put("asd", 1));
        System.out.println(sdgv.put("asd", 2));
        System.out.println(sdgv);
    }
}
