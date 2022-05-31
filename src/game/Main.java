package game;

import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        /*   4. começa a noite:
       - os aldeoes estão a dormir
       - lobo pode matar /kill
       - vidente pode perguntar /vision

       5. começa o dia:
       informar quem morreu
       mostrar a lista dos jogadores
       cada um pode fazer /vote

       fazer com que não haja nomes repetidos
       mostrar a lista de jogadores (com número de votos) sempre que haja o vote



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
