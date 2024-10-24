/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import clases.Message;
import clases.Request;
import clases.Signable;
import controler.SignableFactory;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.NoConnectionsAvailableException;
import excepciones.UserExitsException;
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
    private int id;
    
    private ObjectOutputStream salida = null;
    private ObjectInputStream entrada = null;
    
    public Worker(Socket socket, Signable db) {
        this.socket = socket;
        this.message = message;
        this.db = db;
    }

    public void setWorkerid(int id) {
        this.id = id;
    }
    
    public int getWorkerid() {
        return id;
    }
        
    public void run() {
        try {
            this.entrada = new ObjectInputStream(socket.getInputStream());
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            
            message = (Message) entrada.readObject();
            
            if (message.getRequest() == Request.SING_UP_REQUEST) {
                message.setUser(db.signUp(message));
                Aplication.releaseConn(id);
                salida.writeObject(message);
            } else if (message.getRequest() == Request.SING_IN_REQUEST) {
                message.setUser(db.signIn(message));
                Aplication.releaseConn(id);
                salida.writeObject(message);
            }
        } catch (LogInDataException ex) {
            sendMessage(message, salida, ex, Request.LOG_IN_EXCEPTION);
        } catch (UserExitsException ex) {
            sendMessage(message, salida, ex, Request.USER_EXISTS_EXCEPTION);
        } catch (NoConnectionsAvailableException ex) {
            sendMessage(message, salida, ex, Request.CONNECTIONS_EXCEPTION);
        } catch (ClassNotFoundException | InternalServerErrorException | IOException ex) {
            sendMessage(message, salida, ex, Request.INTERNAL_EXCEPTION);
        } finally {
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
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void sendMessage(Message message, ObjectOutputStream salida, Exception ex, Request req) {
        Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(req);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
    }
}
