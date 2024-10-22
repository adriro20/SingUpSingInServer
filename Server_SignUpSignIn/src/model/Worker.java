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
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import serversignupsignin.Aplication;

/**
 *
 * @author Adrian Rocha
 */
public class Worker implements Runnable{
    private Message message;
    private Signable db;
    private Thread thread;
    Socket socket = null;
    ObjectOutputStream salida = null;
    
    public Worker(Message message, Socket socket) {
        this.socket = socket;
        this.thread = new Thread(this);
        this.message = message;
        this.db = SignableFactory.getSignable();
        this.thread.start();
    }
    
    public void run() {
        try {
            this.salida = new ObjectOutputStream(socket.getOutputStream());
            
            if (message.getRequest() == Request.SING_UP_REQUEST) {
                message.setUser(db.signUp(message));
                Aplication.releaseConn();
                salida.writeObject(message);
            } else if (message.getRequest() == Request.SING_IN_REQUEST) {
                message.setUser(db.signIn(message));
                Aplication.releaseConn();
                salida.writeObject(message);
            }
        } catch (InternalServerErrorException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(Request.INTERNAL_EXCEPTION);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (LogInDataException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(Request.LOG_IN_EXCEPTION);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (UserExitsException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(Request.USER_EXISTS_EXCEPTION);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(Request.INTERNAL_EXCEPTION);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (NoConnectionsAvailableException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
            message.setRequest(Request.CONNECTIONS_EXCEPTION);
            try {
                salida.writeObject(message);
            } catch (IOException ex1) {
                Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
    }
}
