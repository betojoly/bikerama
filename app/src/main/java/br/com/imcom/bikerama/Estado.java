package br.com.imcom.bikerama;

/**
 * Created by BETO on 15/12/2015.
 */
public class Estado {

    private String id;
    private String name;

    public Estado(String i, String n) {
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
