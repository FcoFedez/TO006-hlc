package com.pms.ud6_rest_con__json_matriculas_02;

/**
 * Created by IMCG .
 */
public class Alumno {

        // atributos
        private String alumno;
        private int matricula;
        private int telefono;
        private String email;

        // para insertar los métodos get y set de forma automática en Android Studio
        // pulsar Alt + Insert  => escoger Getter and Setter

        public String getAlumno() {
            return alumno;
        }

        public void setAlumno(String alumno) {
            this.alumno = alumno;
        }

        public int getMatricula() {
            return matricula;
        }

        public void setMatricula(int matricula) {
            this.matricula = matricula;
        }

        public int getTelefono() {
            return telefono;
        }

        public void setTelefono(int telefono) {
            this.telefono = telefono;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }





