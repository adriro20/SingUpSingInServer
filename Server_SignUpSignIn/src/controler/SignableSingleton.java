/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import clases.Signable;

/**
 * Clase singleton para obtener una instancia de la interfaz {@link Signable}.
 * Esta clase garantiza que solo haya una instancia de {@link Signable} durante
 * el ciclo de vida de la aplicación.
 * 
 * @author Adrian Rocha
 */
public class SignableSingleton {
    /** 
     * Instancia única de {@link Signable} para el acceso a la base de datos.
     * Se inicializa solo la primera vez que se invoca {@link #getDao()}.
     */
    private static Signable db = null;
    
    /**
     * Constructor privado para evitar la instanciación directa de esta clase.
     * El patrón singleton garantiza que solo haya una instancia de {@link Signable}.
     */
    private SignableSingleton(){
        
    }
    
    /**
     * Devuelve la instancia única de {@link Signable} utilizada para el acceso a la base de datos.
     * Si la instancia no ha sido creada aún, la obtiene a través de {@link SignableFactory#getSignable()}.
     *
     * @return la instancia única de {@link Signable}
     */
    public synchronized static Signable getDao(){
        if (db == null) {
            db = SignableFactory.getSignable();
        }
        return db;
    }
}
