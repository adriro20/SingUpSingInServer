/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import clases.Signable;

/**
 * Clase  que proporciona un método para obtener una instancia
 * de un objeto que implementa la interfaz Signable. 
 * @author Adrian Rocha
 */
public class SignableFactory {
    /**
     * Instancia única de {@link Signable} utilizada para gestionar el acceso a la base de datos.
     */
    private static Signable sign;
    
    /***
     * Método que devuelve un objeto que implementa la interfaz Signable
     * 
     * @return un objeto de la interfaz Signable.
     */
    public static Signable getSignable() {
        sign = new DbAccess();
        return sign;
    }
}
