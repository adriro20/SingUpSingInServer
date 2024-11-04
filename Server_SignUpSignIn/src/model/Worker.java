/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import clases.Message;
import clases.Request;
import clases.Signable;
import controler.SignableSingleton;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.NoConnectionsAvailableException;
import excepciones.UserExitsException;
import excepciones.UserNotActiveException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import serversignupsignin.Aplication;

/**
 *
 * @author Adrian Rocha
 */
public class Worker extends Thread{
    private Message message;
    private Signable db;
    private Socket socket = null;
    
    private final Logger log = Logger.getLogger(Worker.class.getName());
    
    private ObjectOutputStream salida = null;
    private ObjectInputStream entrada = null;
    
    public Worker(Socket socket, ObjectOutputStream salida, 
            ObjectInputStream entrada) {
        this.socket = socket;
        this.message = message;
        this.db = SignableSingleton.getDao();
        this.entrada = entrada;
        this.salida = salida;
        
    }

    public void run() {
        try {
            message = (Message) entrada.readObject();
            if (message.getRequest() == Request.SING_UP_REQUEST) {
                message.setUser(db.signUp(message));
                salida.writeObject(message);
            } else if (message.getRequest() == Request.SING_IN_REQUEST) {
                message.setUser(db.signIn(message));
                salida.writeObject(message);
            }
        } catch (UserNotActiveException ex) {
            sendMessage(message, salida, ex, Request.USER_NOT_ACTIVE_EXCEPTION);
        } catch (LogInDataException ex) {
            sendMessage(message, salida, ex, Request.LOG_IN_EXCEPTION);
        } catch (UserExitsException ex) {
            sendMessage(message, salida, ex, Request.USER_EXISTS_EXCEPTION);
        } catch (NoConnectionsAvailableException ex) {
            sendMessage(message, salida, ex, Request.CONNECTIONS_EXCEPTION);
        } catch (ClassNotFoundException | InternalServerErrorException | 
                IOException ex) {
            sendMessage(message, salida, ex, Request.INTERNAL_EXCEPTION);
        } catch (Exception ex) {
            sendMessage(message, salida, ex, Request.INTERNAL_EXCEPTION);
        } finally {
            Aplication.releaseConn();
            try {
                if (socket != null) {
                    socket.close();
                }
                if (entrada != null) {
                    entrada.close();
                }
                if (salida != null) {
                    salida.close();
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendMessage(Message message, ObjectOutputStream salida, 
            Exception ex, Request req) {
        log.log(Level.SEVERE, null, ex);
        message.setRequest(req);
        try {
            salida.writeObject(message);
        } catch (IOException ex1) {
            log.log(Level.SEVERE, null, ex1);
        }
    }
}
