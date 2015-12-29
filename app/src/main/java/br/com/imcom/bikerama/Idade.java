package br.com.imcom.bikerama;

/**
 * Created by BETO on 21/12/2015.
 */
public class Idade {

        private static String id;
        private String idade;

        public Idade(String i, String n) {
            id = i;
            idade = n;
        }

        public static String getId() {
            return id;
        }

        public String getName() {
            return idade;
        }

        public String toString() {
            return idade;
        }
}
