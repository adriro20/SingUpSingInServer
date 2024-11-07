/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

/**
 * Clase  que proporciona un método para obtener una instancia
 * de un objeto que implementa la interfaz Closable.
 * 
 * @author Adrian Rocha
 */
public class ClosableFactory {
    /**
     * Instancia única de {@link Closable} que se utiliza para gestionar el recurso.
     */
    private static Closable close;
    
    /***
     * Método que devuelve un objeto que implementa la interfaz Closable
     * 
     * @return un objeto de la interfaz {@link Closable}
     */
    public static Closable getClosable() {
        close = new ConnectionPool();
        return close;
    }
}
