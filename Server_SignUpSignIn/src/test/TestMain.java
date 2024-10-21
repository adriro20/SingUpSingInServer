/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import clases.Message;
import clases.User;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.UserExitsException;
import java.util.logging.Level;
import java.util.logging.Logger;
import serversignupsignin.DbAccess;

/**
 *
 * @author 2dam
 */
public class TestMain {
    public static void main(String[] args) throws InternalServerErrorException {
        
            User user = new User();
            user.setName("erlantz rey");
            user.setEmail("erlantz@gmail.com");
            user.setCity("bilbao");
            user.setStreet("calle");
            user.setZip("48610");
            user.setActive(true);
            user.setPassword("ErlantZ9");
            Message mensaje = new Message();
            mensaje.setUser(user);
            serversignupsignin.DbAccess db = new DbAccess();
           // db.signIn(user);
            try {
                db.signUp(mensaje);
            } catch (UserExitsException ex) {
                Logger.getLogger(TestMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        
    } 
}
