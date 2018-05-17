package com.pms.ud6_rest_con__json_matriculas_02;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.HashMap;

public class ActualizarAlumno {
    private ProgressDialog pDialog = null;
    private Activity context;
    private String URL_PHP;
    private String id;

    public ActualizarAlumno(Activity context, int ids){
        this.context = context;
        new WebService_actualizar().execute();
        this.URL_PHP =MainActivity.URL_PHP;
        id = String.valueOf(ids);
    }

    class WebService_actualizar extends AsyncTask <Void,Void,String>{
        ProgressDialog loading;

        // antes de lanzar la tarea en 2º plano (doInBackground) define que hacer:
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // barra de progreso circular
            loading = ProgressDialog.show(context,"Actualizando...","Wait...",false,false);
        }

        // después de terminar la tarea en 2º plano define que hacer:
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // ocultar barra de progreso
            loading.dismiss();
            // muestra una tostada con la cadena "s" que le ha pasado
            // con el return res => doInBackground()
            Toast.makeText(context,s,Toast.LENGTH_LONG).show();
        }

        // proceso que se ejecuta en 2º plano
        @Override
        protected String doInBackground(Void... params) {
            // Crea un objeto HashMap
            HashMap<String,String> hashMap = new HashMap<>();

            // formando las parejas clave/valor que necesita para la solicitud POST de actualización
            hashMap.put("id",id);     // id => único valor que no cambiará
            hashMap.put("nombre",MainActivity.alumno.getText().toString().trim());
            hashMap.put("telefono",MainActivity.telefono.getText().toString().trim());
            hashMap.put("matricula",MainActivity.matricula.getText().toString().trim());
            hashMap.put("email",MainActivity.email.getText().toString().trim());

            // crea una instancia de la clase RequestHandler
            // que se necesita para realizar la petición POST
            RequestHandler rh = new RequestHandler();

            // mandar la petición POST al script updateEmp.php y obtener su resultado
            String s = rh.sendPostRequest(URL_PHP+"updateAlumno.php?id=",hashMap);

            return s;
        }
    }
}
