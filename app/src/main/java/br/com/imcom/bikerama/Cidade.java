package br.com.imcom.bikerama;

/**
 * Created by BETO on 16/12/2015.
 */
public class Cidade {

    private String id;
    private String name;

    public Cidade(String i, String n) {
        id = i;
        name = n;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
