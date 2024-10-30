/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import clases.Signable;
import serversignupsignin.DbAccess;

/**
 *
 * @author Adrian Rocha
 */
public class SignableSingleton {
    private static DbAccess db = null;
    
    private SignableSingleton(){
        
    }
    
    public synchronized static DbAccess getDao(){
        if (db == null) {
            db = new DbAccess();
        }
        return db;
    }
}
