/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

/**
 * Clase Singleton para gestionar un pool de conexiones a la base de datos. 
 * La clase implementa un método estático que devuelve solo una instancia de 
 * un objeto {@link Closable}.
 * 
 * @author Adrian Rocha
 */
public class ConnectionPoolSingleton {
    /**
     * Objeto {@link Closable} para crear la instancia.
     */
    private static Closable pool = null;
    
    /**
     * Constructor privado para evitar la creación de instancias adicionales.
     */
    private ConnectionPoolSingleton(){
        
    }
    
    /**
     * Obtiene la instancia única del pool de conexiones.
     * <p>
     * Este método crea una nueva instancia de {@link Closable} si no 
     * ha sido creada previamente. El método está sincronizado para garantizar
     * que la creación de la instancia sea segura en un entorno multi-hilo.
     * </p>
     * 
     * @return instancia única del {@link Closable}
     */
    public synchronized static Closable getPool(){
        if (pool == null) {
            pool = ClosableFactory.getClosable();
        }
        return pool;
    }
}
