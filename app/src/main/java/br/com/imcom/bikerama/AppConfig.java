package br.com.imcom.bikerama;

/**
 * Created by BETO on 12/12/2015.
 */
public class AppConfig {

    // Server user login url
    public static String URL_LOGIN = "http://app.bikerama.com.br/API/login.php";

    // Server user register url
    public static String URL_REGISTER = "http://app.bikerama.com.br/API/register.php";

    // Server Consulta Usuario url
    public static String URL_CONSULTA = "http://app.bikerama.com.br/API/consulta.php";

    // Server Consulta Bike url
    public static String URL_CONSULTA_BIKE = "http://app.bikerama.com.br/API/consulta_bike.php";

    // Server Exporta Dados Percurso
    public static String URL_EXPORTA_PERCURSO = "http://app.bikerama.com.br/API/register_percurso.php";

    // Cadastro de Usuario parte 2
    public static String MAP_API_URL_ESTADO = "http://app.bikerama.com.br/API/get_estados.php";
    public static String MAP_API_URL_CIDADE = "http://app.bikerama.com.br/API/get_cidades.php";
    public static String MAP_API_URL_IDADE  = "http://app.bikerama.com.br/API/get_idade.php";
    public static String MAP_API_URL_SEXO   = "http://app.bikerama.com.br/API/get_sexo.php";
    public static String URL_SAVE_USER      = "http://app.bikerama.com.br/API/register2.php";

    // Cadastro de Bike parte 1
    public static String MAP_API_URL_TIPO   = "http://app.bikerama.com.br/API/get_tipos.php";
    public static String MAP_API_URL_PERFIL = "http://app.bikerama.com.br/API/get_perfis.php";
    public static String URL_SAVE_BIKE      = "http://app.bikerama.com.br/API/register_bike.php";
    public static String URL_SAVE_COMP_BIKE = "http://app.bikerama.com.br/API/register_componentes.php";
}
