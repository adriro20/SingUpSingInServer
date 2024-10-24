/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import clases.Message;
import clases.User;
import excepciones.InternalServerErrorException;
import excepciones.NoConnectionsAvailableException;
import excepciones.UserExitsException;
import java.util.logging.Level;
import java.util.logging.Logger;
import serversignupsignin.DbAccess;

/**
 *
 * @author Adrian Rocha
 */
public class TestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        User user = new User();
        user.setName("erlantz rey");
        user.setEmail("erlantzzzzz@gmail.com");
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
            Logger.getLogger(TestDb.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoConnectionsAvailableException ex) {
            Logger.getLogger(TestDb.class.getName()).log(Level.SEVERE, null, ex);
        }   catch (InternalServerErrorException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
