package game;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        /*   4. começa a noite:
        - TEMPORIZADOR /sorry caps
        -dizer que começa a noite
       - os aldeoes estão a dormir
       - se houver 2 lobos, temos que fazer escolha do quem morre (não podem ser 2 killed)


       5. começa o dia:
       -dizer que começa a noite
       informar quem morreu
       - no final da votação, morre quem teve mais votos

       - escrever a verivfifcação se o jogo pode começar
      - escrever a verivfifcação se o jogo pode continuar





      DONE - lobo pode matar /kill
      DONE - vidente pode perguntar /vision



      DONE mostrar a lista dos jogadores
      DONE cada um pode fazer /vote

     DONE fazer com que não haja nomes repetidos
     DONE mostrar a lista de jogadores (com número de votos) sempre que haja o vote



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
