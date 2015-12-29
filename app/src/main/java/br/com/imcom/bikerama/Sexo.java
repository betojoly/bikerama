package br.com.imcom.bikerama;

/**
 * Created by BETO on 21/12/2015.
 */
public class Sexo {

    private static String id;
    private String sexo;

    public Sexo(String i, String n) {
        id = i;
        sexo = n;
    }

    public static String getId() {
        return id;
    }

    public String getName() {
        return sexo;
    }

    public String toString() {
        return sexo;
    }
}
