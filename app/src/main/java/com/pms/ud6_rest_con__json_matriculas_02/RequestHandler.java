package com.pms.ud6_rest_con__json_matriculas_02;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Belal, comentado y adaptado.
 */

/* Código de la página web:
https://www.simplifiedcoding.net/android-mysql-tutorial-to-perform-basic-crud-operation/
y adaptado y comentado */

public class RequestHandler {


    /**
     * Método para enviar una httpPostRequest
     * @param requestURL => URL del script PHP que será enviado como solicitud
     * @param postDataParams => un HashMap, es una colección de objetos, (como los Arrays), que son
     *                       accedidos  mediante una función Hash con las parejas de clave /valor
     *                       que seran enviadas en la solicitud POST al script PHP
     * @return: cadena devuelta
     */
    public String sendPostRequest(String requestURL, HashMap<String, String> postDataParams) {
        // Creando una URL
        URL url;

        //StringBuilder => objeto para guardar el mensaje obtenido desde el servidor
        StringBuilder sb = new StringBuilder();

        try {
            //Inicializando la Url
            url = new URL(requestURL);

            // Creando la conexión con HttpURLConnection
            // => La  clase HttpURLConnection se puede utilizar para enviar
            // y recibir datos de streaming cuya longitud no se conoce de antemano.
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configurando las propiedades de la conexión
            conn.setReadTimeout(15000); // establecer el tiempo máximo de espera para leer en 15000 milisegundos => 15 segundos
            conn.setConnectTimeout(15000); // establecer el tiempo máximo de conexión en 15 segundos
            conn.setRequestMethod("POST"); // método para realizar una petición HTTP => con POST
            conn.setDoInput(true); // Establecer la conexión para lectura
            conn.setDoOutput(true); // Establecer la conexión para escritura

            //Creando un flujo de Salida
            OutputStream os = conn.getOutputStream();

            // Montar sobre el flujo de salida anterior el flujo writer
            // => que escribe caracteres codificados en UTF-8
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            // escribir la cadena de la solicitud POST sobre el flujo de salida writer
            // transformando las parejas clave-valor contenidas en el HashMap
            // en una cadena o String UTF-8, separando la clave valor con &
            writer.write(getPostDataString(postDataParams));

            writer.flush(); // vaciar los buffers antes de cerrar por si queda algo por escribir
            writer.close();
            os.close();

            // obtener el código de respuesta HTTP a la petición POST
            /*  Un int que representa los tres dígitos del código de estado HTTP.
            1xx: Informativo  2xx: Éxito  3xx: redirección  4xx: Error de cliente 5xx: Error del servidor*/
            int responseCode = conn.getResponseCode();

            // comprobar código de respuesta
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // este es el código de respuesta 200 => OK => se ha realizado bien la petición POST

                // Establecer un flujo de entrada sobre la conexión POST establecida => conn
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                sb = new StringBuilder();
                String response;
                // Leyendo la respuesta del servidor HTTP línea a línea
                while ((response = br.readLine()) != null) {
                    sb.append(response); // añadir toda la respuesta en el StringBuilder sb
                }
            }

        } catch (MalformedURLException e) {
            Log.e("log_tag", "Error, URL incorrecta: " + e.toString());
            e.printStackTrace();

        } catch (UnsupportedEncodingException e) {
            Log.e("log_tag", "Error, codificación de caracteres no soportada: " + e.toString());
            e.printStackTrace();

        } catch (ProtocolException e) {
            Log.e("log_tag", "Error en el protocolo: " + e.toString());
            //  hay un error en el protocolo subyacente, tal como un error de TCP.
            e.printStackTrace();

        } catch (IOException e) {
            Log.e("log_tag", "Error de E/S: " + e.toString());
            e.printStackTrace();
        }

        // devuelve la respuesta del servidor HTTP a la petición POST
        return sb.toString();
    }

    // Método que envía una petición GET sin parámetros
    // Las peticiones GET incluyen todos los datos requeridos en la URL
    // (pero en este caso no se necesitan) => por eso va sin parámetros
    // y devuelve la respuesta del servidor HTTP
    // por ejemplo: "http://www.ramonjmc.honor.es/CRUD/getAllEmp.php"
    //               el script getAllEmp.php no necesita mandarle información (parámetros)
    //               ya que obtiene todos los empleados

    /**
     * Método que envía una petición GET sin parámetros
     * Las peticiones GET incluyen todos los datos requeridos en la URL
     * (pero en este caso no se necesitan) => por eso va sin parámetros
     * y devuelve la respuesta del servidor HTTP
     * por ejemplo: "http://isabelmcg.esy.es/CRUD/getAllEmp.php"
     * el script getAllEmp.php no necesita mandarle información (parámetros)
     * ya que obtiene todos los empleados
     * @param requestURL: URL para la petición GET
     * @return: respuesta del servidor
     */
    public String sendGetRequest(String requestURL) {
        StringBuilder sb = new StringBuilder();

        try {
            // crear la URL que mandará la petición GET
            URL url = new URL(requestURL);

            // Creando la conexión "con" a través de la clase HttpURLConnection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // Establecer un flujo de entrada sobre la conexión GET establecida => "con"
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            // // Leyendo la respuesta del servidor HTTP línea a línea
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s + "\n"); // añadir toda la respuesta en el StringBuilder sb
            }

            // cerrar el flujo de entrada => no es bueno aunque parezca lo contrario
            //bufferedReader.close();
            // cerrar la conexión => no se debe hacer
            //con.disconnect();
            // https://translate.google.es/translate?hl=es&sl=en&u=http://kingori.co/minutae/2013/04/httpurlconnection-disconnect/&prev=search
            // http://kingori.co/minutae/2013/04/httpurlconnection-disconnect/

        } catch (MalformedURLException e) {
            Log.e("log_tag", "Error, URL incorrecta: " + e.toString());
            e.printStackTrace();

        } catch (IOException e) {
            Log.e("log_tag", "Error de E/S: " + e.toString());
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * Método que envía una petición GET con parámetros
     * las peticiones GET incluyen todos los datos requeridos en la URL
     * en este caso las forma añadiendo a la URL "requestURL" el "id"
     * y devuelve la respuesta del servidor HTTP
     * por ejemplo: "http://isabelmcg.esy.es/CRUD/getEmp.php?id=75"
     * realiza la consulta en el script PHP getEmp.php del empleado con el id=75
     * @param requestURL: URL para la petición GET
     * @param id: paráemtro para petición GET
     * @return: respuesta del servidor
     */
    public String sendGetRequestParam(String requestURL, String id) {
        StringBuilder sb = new StringBuilder();

        try {
            // crea la URL que mandará la petición GET
            URL url = new URL(requestURL + id);
            // Crea la conexión "con" a través de la clase HttpURLConnection
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // Establece un flujo de entrada sobre la conexión GET establecida => "con"
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String s;
            // Leyendo la respuesta del servidor HTTP línea a línea
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s + "\n"); // añadir toda la respuesta en el StringBuilder sb
            }

        } catch (MalformedURLException e) {
            Log.e("log_tag", "Error, URL incorrecta: " + e.toString());
            e.printStackTrace();

        } catch (IOException e) {
            Log.e("log_tag", "Error de E/S: " + e.toString());
            e.printStackTrace();
        }

        return sb.toString();
    }


    /**
     * Método que transforma las parejas clave-valor contenidas en el HashMap
     * en una cadena o String UTF-8, separando la pareja clave-valor con &
     * y cada clave es separada de su valor con el caracter "="
     * => Esto es lo que hay que hacer para mandar información por POST desde un formulario
     * => https://en.wikipedia.org/wiki/POST_%28HTTP%29#Use_for_submitting_web_forms
     * @param params: parejas claves-valor contenidas en HashMap
     * @return: String UTF8, separando las parejas clave-valor por & y la clave y su valor por =
     * @throws UnsupportedEncodingException
     */
    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
         //=> sobre StringBuilder
        //http://www.edu4java.com/es/java/string-stringbuilder-stringBuffer-java.html
        // result es un StringBuilder o string variable
        StringBuilder result = new StringBuilder();
        boolean first = true;

        // recorrer todos las parejas clave/valor del HashMap params
        for (Map.Entry<String, String> entry : params.entrySet()) {
            // comprobar si estamos en la primera pareja/valor y así no se le añade "&"
            if (first)
                first = false;
            else
                // cuando mandamos información por el protocolo POST (por un formulario)
                // hay que separar las parejas clave /valor con &
                result.append("&");

            // añade a result la llave o clave codificada en UTF-8
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            // cada clave es separada de su valor con el caracter "="
            result.append("=");
            // añade a result el valor codificado en UTF-8
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
