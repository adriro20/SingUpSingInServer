/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

/**
 *
 * @author 2dam
 */
public class ConnectionPoolSingleton {
    private static ConnectionPool pool = null;
    
    private ConnectionPoolSingleton(){
        
    }
    
    public static ConnectionPool getPool(){
        if (pool == null) {
            pool = new ConnectionPool();
        }
        return pool;
    }
}
