/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

/**
 *Clase  que proporciona un método para obtener una instancia
 * de un objeto que implementa la interfaz Closable. 
 * @author Adrian Rocha
 */
public class ClosableFactory {
    Closable close;
    
    /***
     * Método que devuelve un objeto que implementa la interfaz Closable
     * @return 
     */
    public Closable getClosable() {
        close = (Closable) new ConnectionPool();
        return close;
    }
}
