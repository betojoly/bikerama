package br.com.imcom.bikerama;

/**
 * Created by BETO on 11/01/2016.
 */
public class Percurso {

    private Integer id;
    private String date;
    private String nome;
    private String tipo;
    private String nivel;
    private String descricao;
    private String bikeid;

    /*public Percurso() {
        id = i;
        date = d;
        nome = no;
        tipo = t;
        nivel = ni;
        descricao = desc;
        bikeid = b;
    }*/

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getBikeid() {
        return bikeid;
    }

    public void setBikeid(String bikeid) {
        this.bikeid = bikeid;
    }

    public String toString() {
        return nome;
    }
}
