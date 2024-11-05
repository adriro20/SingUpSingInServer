/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

/**
 * Interfaz que define el método para cerrar recursos.
 * 
 * Contiene el metodo close
 * 
 * @author Adrian Rocha
 */
public interface Closable {
    /**
     * Método para cerrar recursos.
     */
    public void close();
}
