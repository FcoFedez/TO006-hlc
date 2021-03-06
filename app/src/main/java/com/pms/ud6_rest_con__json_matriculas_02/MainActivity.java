package com.pms.ud6_rest_con__json_matriculas_02;

// código adaptado del código original de la página web:
// http://picarcodigo.blogspot.com.es/2014/05/webservice-conexion-base-de-datos-mysql.html

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

// recuerda que debes añadir el permiso de internet al manifiesto para poder acceder a
// los scripts de php y mandar información por POST mediante HTTP
// <uses-permission android:name="android.permission.INTERNET" />
// y gestionar la petición de permisos, si fuese necesario, para API >= 23

public class MainActivity extends Activity {

    // atributos
    public static EditText matricula;
    public static EditText alumno;
    public static EditText telefono;
    public static  EditText email;
    private Button insertar;
    private Button borrar;
    private Button buscar;
    private Button actualizar;
    private Button nuevo;


    private Button mostrar;
    private ImageButton mas;
    private ImageButton menos;
    private int posicion = 0;  // posición del alumno a mostrar de la lista de alumnos
    private List<Alumno> listaAlumnos = null; // Lista de alumnos obtenidos de la BD

    private ProgressDialog pDialog = null; // barra de progreso (mostrada mientras se conecta a la BD)

    // dirección IP o URL del servidor
    //private final static String URL_SERVIDOR ="192.168.1.128"; // comprobar IP local que puede cambiar
    //private final static String URL_SERVIDOR = "isabelmcg.esy.es"; // hosting en hostinger
    private final static String URL_SERVIDOR = "xtremsport.esy.es"; // hosting en hostinger


    // URL del directorio de los scripts php del servidor
    protected final static String URL_PHP = "http://" + URL_SERVIDOR + "/rest_con_json/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // acceder a los cuadros de texto
        matricula = (EditText) findViewById(R.id.matricula);
        alumno = (EditText) findViewById(R.id.nombre);
        telefono = (EditText) findViewById(R.id.telefono);
        email = (EditText) findViewById(R.id.email);

        // crear la lista de alumnos
        listaAlumnos = new ArrayList<Alumno>();

        // acceder al botón insertar
        insertar = (Button) findViewById(R.id.insertar);
        //Define la acción del botón Insertar => Insertamos los datos del alumno
        insertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //controla que la información no esté en blanco
                if (!matricula.getText().toString().trim().equalsIgnoreCase("") ||
                        !alumno.getText().toString().trim().equalsIgnoreCase("") ||
                        !telefono.getText().toString().trim().equalsIgnoreCase("") ||
                        !email.getText().toString().trim().equalsIgnoreCase(""))

                    // intenta insertar los datos del alumno con el servicio web
                    new WebService_insertar(MainActivity.this).execute();
                else
                    tostada("Hay información por rellenar");
            }
        });
        nuevo = findViewById(R.id.btnNuevo);
        nuevo.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                limpiar();
            }
        });

        borrar = findViewById(R.id.btnBorrar);
        borrar.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                confirmDeleteAlumno();

            }
        });

        buscar = findViewById(R.id.btnBuscar);
        buscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!alumno.getText().toString().trim().equalsIgnoreCase("")){
                    int i= idPorNombre(alumno.getText().toString().trim());
                    System.out.println(i);
                    BuscarAlumno ba = new BuscarAlumno(MainActivity.this,i);
                    actulizaPosicion(alumno.getText().toString().trim());
                }else tostada("El campo nombre debe estar relleno");
            }
        });

        actualizar = findViewById(R.id.btnModificar);
        actualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActualizarAlumno aa = new ActualizarAlumno(MainActivity.this,idAlumno(posicion));
                listaAlumnos.get(posicion).setAlumno(alumno.getText().toString().trim());
                listaAlumnos.get(posicion).setEmail(email.getText().toString().trim());
                listaAlumnos.get(posicion).setMatricula(Integer.parseInt(matricula.getText().toString().trim()));
                listaAlumnos.get(posicion).setTelefono(Integer.parseInt(telefono.getText().toString().trim()));
            }
        });


        // acceder al botón mostrar
        mostrar = (Button) findViewById(R.id.mostrar);
        //Define la acción del botón Mostrar =>Mostramos los datos del alumno por pantalla
        mostrar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // mostrar datos del alumno mediante el servicio web
                new WebService_mostrar(MainActivity.this).execute();
            }
        });

        // acceder al botón mas => +
        mas = (ImageButton) findViewById(R.id.mas);
        // Define la acción del botón + => Se mueve por el ArrayList mostrando el alumno siguiente
        mas.setOnClickListener(new View.OnClickListener() {

            // la lista de alumnos va desde la posición 0 hasta el tamaño-1 => size()-1

            @Override
            public void onClick(View v) {
                // Comprobar si la lista de alumnos no está vacía
                if (!listaAlumnos.isEmpty()) {

                    if (posicion >= listaAlumnos.size() - 1)
                        // se ha alcanzando o superado el final de lista
                        // posición debe valer el final de la lista por si se ha superado el valor
                        posicion = listaAlumnos.size() - 1;
                    else
                        // no se ha alcanzando o superado el final de lista => avanzar
                        posicion++;

                    // mostrar el alumno de la lista situado en posición
                    mostrarAlumno(posicion);
                }
            }

        });

        // acceder al botón menos => -
        menos = (ImageButton) findViewById(R.id.menos);
        // Definir la acción del botón - => Se mueve por el ArrayList mostrando el alumno anterior
        menos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Comprobar si la lista de alumnos no está vacía
                if (!listaAlumnos.isEmpty()) {
                    if (posicion <= 0)
                        // se ha alcanzando el principio de lista o posición tiene valor negativo
                        // posición debe valer el principio de la lista por si tiene valor negativo
                        posicion = 0;
                    else
                        // no se ha alcanzando el principio de lista => retroceder
                        posicion--;

                    // mostrar el alumno de la lista situado en posición
                    mostrarAlumno(posicion);

                }
            }
        });


    } // fin onCreate()

    public static void limpiar(){
        alumno.setText("");
        matricula.setText("");
        telefono.setText("");
        email.setText("");
    }

    /**
     * Método que muestra un cuadro de diálogo y pregunta la usuario si quiere borrar o no
     * al alumno actual. En caso de que confirmación 'Yes' => lo borra de la BD
     */
    private void confirmDeleteAlumno(){
        // Definir el cuadro de diálogo
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Desea eliminar el usuario "+alumno.getText().toString().trim()+"?");

        // Si el usuario pulsa el botón del "Yes"
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // borrar al empleado actual
                        BorrarAlumno b = new BorrarAlumno(MainActivity.this,idAlumno(posicion));
                        listaAlumnos.remove(posicion);
                    }
                });

        // Si el usuario pulsa el botón del "NO" => no hace nada
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Método que intenta insertar los datos de las Personas en el servidor
     * a través del script => insert.php
     *
     * @return: true/false
     * devuelve true => si la inserción es correcta
     * devuelve false => si hubo un error en la inserción
     */
    private boolean insertar() {
        boolean resul = false;

        // interfaz para un cliente HTTP
        HttpClient httpclient;
        // define una lista de parámetros ("clave" "valor") que serán enviados por POST al script php
        List<NameValuePair> parametros_POST;
        // define un objeto para realizar una solicitud POST a través de HTTP
        HttpPost httppost;
        // crea el cliente HTTP
        httpclient = new DefaultHttpClient();
        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        httppost = new HttpPost(URL_PHP + "insert.php"); // Url del Servidor

        // Cuando estamos trabajando de manera local la ida y vuelta será casi inmediata
        //para darle un poco de realismo decimos que el proceso se pare por unos segundos para poder
        // observar el progressdialog, la podemos eliminar si queremos

        // SystemClock.sleep(950); // dormir el proceso actual 950 milisegundos

        //Añadimos los datos que vamos a enviar por POST al script insert.php

        //************** A tener en cuenta en la Tarea********************************************
        //***** para poder modificar  y eliminar, debe añadirse el id del alumno a actualizar
        // debe coincidir la clave con índice del $_POST[] indicado en el script insert.php
        parametros_POST = new ArrayList<NameValuePair>(4);
        parametros_POST.add(new BasicNameValuePair("alumno", alumno.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("matricula", matricula.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("telefono", telefono.getText().toString().trim()));
        parametros_POST.add(new BasicNameValuePair("email", email.getText().toString().trim()));

        //***** *****************************************************************
        try {
            // establece la entidad => como una lista de pares URL codificada.
            // Esto suele ser útil al enviar una solicitud HTTP POST
            httppost.setEntity(new UrlEncodedFormEntity(parametros_POST));
            // intentamos ejecutar la solicitud HTTP POST
            httpclient.execute(httppost);
            resul = true;
            new WebService_mostrar(MainActivity.this).execute();
        } catch (UnsupportedEncodingException e) {
            // La codificación de caracteres no es compatible
            resul = false;
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            // Señala un error en el protocolo HTTP
            resul = false;
            e.printStackTrace();
        } catch (IOException e) {
            // Error de Entrada / Salida
            resul = false;
            e.printStackTrace();
        }

        // devuelve el resultado de la inserción
        return resul;

    } // fin insertar()


    /**
     * Método que realiza una consulta a la BD de todas las matriculas de los alumnos
     * a través del script => selectAll.php
     *
     * @return: Devuelve los datos del servidor en forma de String
     */
    private String mostrar() {

        // almacenará la respuesta del servidor BD
        String resultado = "";

        // crea el cliente HTTP por defecto
        HttpClient httpclient = new DefaultHttpClient();

        // creamos el objeto httpost para realizar una solicitud POST al script insert.php
        HttpPost httppost = new HttpPost(URL_PHP + "selectAll.php"); // Url del Servidor

        HttpResponse response;
        try {
            //ejecuto petición enviando datos por POST
            response = httpclient.execute(httppost);
            // obtiene la entidad del mensaje de respuesta HTTP
            HttpEntity entity = response.getEntity();
            // crea un nuevo flujo de entrada tipo InputStream => instream => con la entidad HTTP => entity
            InputStream instream = entity.getContent();
            // convierte la respuesta del servidor => instream => a formato cadena (String) => resultado
            resultado = convertStreamToString(instream);
        } catch (ClientProtocolException e) {
            // error en el protocolo HTTP
            e.printStackTrace();
        } catch (IOException e) {
            // error de E/S
            e.printStackTrace();
        }

        return resultado;

    } // fin mostrar()

    /**
     * Método que convierte la respuesta del servidor => is
     * => a formato cadena (String) => y la devuelve
     *
     * @param is: respuesta del servidor
     * @throws IOException
     * @return: respuesta en formato String
     */
    private String convertStreamToString(InputStream is) throws IOException {

        String resul = ""; // resultado a devolver
        BufferedReader reader = null;

        //Convierte respuesta a String
        try {
            // crear un flujo de entrada de tipo BufferedReader en base
            // a un flujo de entrada InputStreamReader con un juego de caracteres de tipo "UTF-8"
            // el tamaño del buffer es de 8 caracteres
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);

            // quién no sepa que es un StringBuilder =>
            // http://picandocodigo.net/2010/java-stringbuilder-stringbuffer/

            // crea una cadena de caracteres modificable => StringBuilder
            StringBuilder sb = new StringBuilder();

            // lee todas las líneas del fichero a través del flujo de entrada reader
            String line = null;
            while ((line = reader.readLine()) != null)
                // añade cada línea leída del fichero con un salto de línea => "\n"
                sb.append(line + "\n");

            // guardamos el resultado de la respuesta en el String => result
            resul = sb.toString();

            Log.e("getpostresponse", " result= " + sb.toString());

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("log_tag", "Error E/S al convertir el resultado " + e.toString());

        } finally {
            // la clausula finally siempre se ejecuta => salten excepciones o no
            // por eso es conveniente intentar cerrar aquí los flujos => por si hay un error
            // en la lectura del flujo por ejemplo de tipo E/S => IOException
            try {
                if (is != null)
                    is.close(); // cerrar el flujo de entrada is
                if (reader != null)
                    reader.close(); // cerrar el flujo de entrada reader
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("log_tag", "Error E/S al cerrar los flujos de entrada " + e.toString());
            }

        }

        return resul;

    } // fin convertStreamToString()


    /**
     * Método que descompone, crea un objeto con los datos descompuestos y lo almacena en
     * nuestro ArrayList
     *
     * @return: true /false
     * devuelve true => si hay algún alumno que mostrar
     * false => en caso contrario
     */
    private boolean filtrarDatos() {
        boolean resul = false;
        listaAlumnos.clear();
        Alumno alumno = null;

        String respuesta = mostrar();

        // Compara respuesta ignorando mayúsculas y minúsculas con la cadena ""
        if (!respuesta.equalsIgnoreCase("")) {

            JSONObject json; // define un objeto JSON

            boolean error_json = false; // para detectar un error al transformar a JSON

            try {
                // crea el objeto JSON en base al String respuesta
                json = new JSONObject(respuesta);

                //***************  A tener en cuenta en la Tarea***********************
                // devuelve un array json si existe el índice de nombre "alumnos"**
                // ***($json['alumnos'][]=$row; -- => archivo.php)
                JSONArray jsonArray = json.optJSONArray("alumnos");
                for (int i = 0; i < jsonArray.length(); i++) {
                    alumno = new Alumno();
                    // obtener el objeto JSON de la posición i
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);

                    //********** A tener en cuenta en la Tarea  ********************
                    // guardar el alumno jsonArrayChild en el objeto alumno
                    //****** indexando en el nombre de la columna de la tabla de la BD
                    //****** debes guardar el id del alumno en el atributo del objeto Alumno **
                    alumno.setAlumno(jsonArrayChild.optString("nombre"));
                    alumno.setMatricula(jsonArrayChild.optInt("matricula"));
                    alumno.setTelefono(jsonArrayChild.optInt("telefono"));
                    alumno.setEmail(jsonArrayChild.optString("email"));
                    alumno.setId(jsonArrayChild.optInt("id"));

                    /****** ******************* *************************/

                    // añadir el alumno a la lista de alumnos
                    listaAlumnos.add(alumno);
                }

            } catch (JSONException e) {
                // Error al convertir a JSON
                // => esto sucede porque no hay alumnos en la consulta y el arrya json está vacío
                // o por cualquie otro motivo
                e.printStackTrace();
                error_json = true;
            }

            if (error_json)
                resul = false;
            else
                resul = true;
        } else
            resul = false;

        return resul;
    } // fin filtrarDatos()

    private int idAlumno(int posicion){
        Alumno aux = listaAlumnos.get(posicion);
        return aux.getId();
    }

    private int idPorNombre(String nombre){
        int id = 0;
        for(int i = 0;i < listaAlumnos.size();i++){
            Alumno aux = listaAlumnos.get(i);
            if(aux.getAlumno().equalsIgnoreCase(nombre)){
                id = aux.getId();
                break;
            }

        }
        return id;
    }

    private void actulizaPosicion(String nombre){
        for(int i = 0; i<listaAlumnos.size();i++){
            Alumno aux = listaAlumnos.get(i);
            if(aux.getAlumno().equals(nombre)){
                posicion = i;
            }
        }
    }

    /**
     * Método que muestra el alumno almacenado en el ArrayList listaAlumnos en la posición pasada
     * como parámetro
     * @param posicion: posición de del alumno a mostrar
     */
    private void mostrarAlumno(int posicion) {
        // recoger en alumno2 la información del alumno ubicado en la posición ("posicion") de listaAlumnnos
        Alumno alumno2 = listaAlumnos.get(posicion);
        // poner la información del alumno en los cuadros de texto

        //************** A tener en cuenta para la Tarea *********************************
        //***** obtener id del alumno para saber al que modifico y elimino ********
        alumno.setText(alumno2.getAlumno());
        matricula.setText("" + alumno2.getMatricula());
        telefono.setText("" + alumno2.getTelefono());
        email.setText(alumno2.getEmail());

    } // fin mostrarAlumno()

    /**
     * Método que muestra una mensaje en una tostada
     * @param mensaje: cadena a mostrar en tostada
     */
    public void tostada(String mensaje) {
        Toast toast1 = Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT);
        toast1.show();
    }

    /**		CLASE ASYNCTASK
     *
     * usaremos ésta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
     * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado,
     * lo que podria ocurrir si la conexion es lenta o el servidor tarda en responder,
     * la aplicacion será inestable. Ademas observariamos el mensaje de que la app no responde.(ANR)
     *
     * Este Web Service permitirá insertar un alumno en la BD
     */

    class WebService_insertar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_insertar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
		   En este caso, muestra un diálogo de progreso
		 */
        protected void onPreExecute() {
            // Crea el diálogo de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (insertar())
                // la inserción del alumno ha sido exitosa
                resultado = "OK";
            else
                // ha habido un error al insertar el alumno y no se pudo insertar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
		   intentamos mostrar la tostada de que se pudo o no insertar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // inserción correcta
                tostada("Alumno insertado con éxito");
                alumno.setText("");
                matricula.setText("");
                telefono.setText("");
                email.setText("");
            } else
                tostada("ERROR, no se pudo insertar el alumno");
        } // fin onPostExecute()

    } // fin clase WebService_insertar


    /* Este Web Service permitirá mostrar un alumno de la BD
     */
    class WebService_mostrar extends AsyncTask<String, String, String> {

        private Activity context;

        WebService_mostrar(Activity context) {
            this.context = context;
        }

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
		   En este caso, muestra una barra de progreso
		 */
        protected void onPreExecute() {
            // Crea la barra de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Conectando a la Base de Datos....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Tarea a realizar en segundo plano (con otro hilo que no está en el Interfaz de Usuario)
        // por lo tanto esta tarea no puede interaccionar con el usuario
        @Override
        protected String doInBackground(String... params) {
            String resultado = "ERROR";

            if (filtrarDatos()) {
                // hay un alumno que mostrar
                resultado = "OK";
            } else
                // no hay alumno que mostrar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
		   intentamos mostrar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // se puede mostrar el alumno
                mostrarAlumno(posicion);
                //tostada("muestra alumno");
            } else
                tostada("ERROR, no hay más alumnos que mostrar");
        } // fin onPostExecute()

    } // fin clase WebService_mostrar

}
