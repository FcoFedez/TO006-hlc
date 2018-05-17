package com.pms.ud6_rest_con__json_matriculas_02;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BuscarAlumno {

    private ProgressDialog pDialog = null;
    private Activity context;
    private String URL_PHP;
    private String id;

    public BuscarAlumno(Activity context, int ids){
        this.context = context;
        new WebService_buscar().execute();
        this.URL_PHP =MainActivity.URL_PHP;
        id = String.valueOf(ids);
    }

    class WebService_buscar extends AsyncTask<Void,Void,String>{
        ProgressDialog loading; // barra de progreso

        // antes de lanzar la tarea en 2º plano (doInBackground) define que hacer:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // muestra una barra de progreso circular
            loading = ProgressDialog.show(context,"Buscando...","Wait...",false,false);
        }

        // después de terminar la tarea en 2º plano define que hacer:
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // oculta la barra de progreso
            loading.dismiss();
            // muestra los datos del empleado actualizando los valores de los EditText
            muestraAlumno(s);
        }

        // proceso que se ejecuta en 2º plano
        @Override
        protected String doInBackground(Void... params) {
            // Crear una instancia de RequestHandler para hacer una petición GET con parámetros
            RequestHandler rh = new RequestHandler();

            // realiza una petición GET que manda al script PHP getEmp.php
            // y obtiene la respuesta del servidor HTTP
            String s = rh.sendGetRequestParam(URL_PHP+"getAlumno.php?id=", id);

            return s;
        }
    }

    /**
     * metodo que recibe un string por parametro al que convierte en json
     * @param json
     */
    private void muestraAlumno(String json){

        try {
            // crea un objeto JSON con el String json
            JSONObject jsonObject = new JSONObject(json);
            // crea el Array JSON con el objeto JSON
            JSONArray result = jsonObject.getJSONArray("result");
            // recoger el primero de los elementos del vector JSON
            JSONObject c = result.getJSONObject(0);
            // Guardar el nombre, puesto y salario del trabajor con el objeto JSON
            if(!c.getString("nombre").contains("null")) {
                String nombre = c.getString("nombre");
                String matricula = c.getString("matricula");
                String telefono = c.getString("telefono");
                String email = c.getString("email");

                // Actualizar los valores de los cuadros de texto
                MainActivity.alumno.setText(nombre);
                MainActivity.email.setText(email);
                MainActivity.matricula.setText(matricula);
                MainActivity.telefono.setText(telefono);
            }else Toast.makeText(context,"No se ha encontrado el alumno",Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
