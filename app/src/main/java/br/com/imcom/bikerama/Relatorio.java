package br.com.imcom.bikerama;

/**
 * Created by BETO on 28/01/2016.
 */
public class Relatorio {

    private Integer id;
    private String manutencao_id;
    private String manual_id;
    private String data_criacao;
    private String status;
    private String verificacao;
    private String conjunto;

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

    public String getManutencao_id() {
        return manutencao_id;
    }
    public void setManutencao_id(String nome) {
        this.manutencao_id = manutencao_id;
    }

    public String getManual_id() {
        return manual_id;
    }
    public void setManual_id(String date) {
        this.manual_id = manual_id;
    }

    public String getData_criacao() {
        return data_criacao;
    }
    public void setData_criacao(String data_criacao) {
        this.data_criacao = data_criacao;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerificacao() {
        return verificacao;
    }
    public void setVerificacao(String verificacao) {
        this.verificacao = verificacao;
    }

    public String getConjunto() {
        return conjunto;
    }
    public void setConjunto(String conjunto) {
        this.conjunto = conjunto;
    }

    public String toString() {
        return verificacao;
    }
}
