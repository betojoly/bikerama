package br.com.imcom.bikerama;

/**
 * Created by BETO on 22/12/2015.
 */
public class Tipo {

    private static String id;
    private String tipo;

    public Tipo(String i, String n) {
        id = i;
        tipo = n;
    }

    public static String getId() {
        return id;
    }

    public String getName() {
        return tipo;
    }

    public String toString() {
        return tipo;
    }
}
