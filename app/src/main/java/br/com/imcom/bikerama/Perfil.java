package br.com.imcom.bikerama;

/**
 * Created by BETO on 22/12/2015.
 */
public class Perfil {

    private static String id;
    private String perfil;

    public Perfil(String i, String n) {
        id = i;
        perfil = n;
    }

    public static String getId() {
        return id;
    }

    public String getName() {
        return perfil;
    }

    public String toString() {
        return perfil;
    }
}
