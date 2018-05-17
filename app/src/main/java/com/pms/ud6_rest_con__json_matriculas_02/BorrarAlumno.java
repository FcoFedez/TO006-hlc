package com.pms.ud6_rest_con__json_matriculas_02;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BorrarAlumno {
    private ProgressDialog pDialog = null;
    private Activity context;
    private String URL_PHP;
    private String id;

    public BorrarAlumno(Activity context, int ids){
        this.context = context;
        new WebService_borrar().execute();
        this.URL_PHP =MainActivity.URL_PHP;
        id = String.valueOf(ids);
    }

    private boolean borrar(){
        RequestHandler rh = new RequestHandler();
        // manda a borrar al empleado actual "id" mediante GET con el script "deleteEmp.php"
        String s = rh.sendGetRequestParam(URL_PHP+"deletealumno.php?id=", id);
        if (s.contains("ok")){
            return true;
        }else return false;
    }


    class WebService_borrar extends AsyncTask<String, String, String> {

        /* Proceso Invocado en la Interfaz de Usuario (IU) antes de ejecutar la tarea en segundo plano.
		   En este caso, muestra un diálogo de progreso
		 */
        protected void onPreExecute() {
            // Crea el diálogo de progreso si es necesario
            if (pDialog == null)
                pDialog = new ProgressDialog(context);
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

            if (borrar())
                // el borrado del alumno ha sido exitoso
                resultado = "OK";
            else
                // ha habido un error al borrar el alumno y no se pudo borrar
                resultado = "ERROR";

            return resultado;
        }

        /* Una vez terminado doInBackground según lo que haya ocurrido
		   intentamos mostrar la tostada de que se pudo o no insertar el alumno */
        protected void onPostExecute(String result) {

            pDialog.dismiss();//ocultamos barra de progreso

            if (result.equals("OK")) {
                // inserción correcta
                Toast.makeText(context,"Alumno borrado con exito",Toast.LENGTH_LONG).show();
                MainActivity.limpiar();
            } else
                Toast.makeText(context,"ERROR, no se pudo borrar el alumno",Toast.LENGTH_LONG).show();
        } // fin onPostExecute()

    } // fin clase WebService_insertar
}
