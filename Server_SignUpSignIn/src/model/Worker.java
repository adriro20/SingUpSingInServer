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
import controler.Aplication;
import static controler.Aplication.closeServerConnection;

/**
 * Clase que procesa las solicitudes de inicio de sesión (sign-in) y de registro
 * (sign-up) de los usuarios, gestionando las respuestas y enviándolas de vuelta
 * al cliente.
 * 
 * <p>
 * Utiliza la instancia de {@link Signable} obtenida de 
 * {@link SignableSingleton} para manejar las operaciones de base de datos.
 * </p>
 * 
 * @author Adrian Rocha
 */
public class Worker extends Thread{
    
    /** 
     * Mensaje recibido del cliente y modificado según la solicitud del cliente.
     */
    private Message message;

    /**
     * Objeto de tipo {@link Signable} utilizado para acceder a la base de 
     * datos.
     */
    private Signable db;

    /** 
     * Socket de la conexión cliente-servidor.
     */
    private Socket socket;
    
    /**
     * Logger para registrar eventos y errores.
     */
    private final Logger log = Logger.getLogger(Worker.class.getName());

    /** 
     * Flujo de salida para enviar respuestas al cliente.
     */
    private ObjectOutputStream salida;

    /** 
     * Flujo de entrada para recibir mensajes del cliente.
     */
    private ObjectInputStream entrada;
    
    /**
     * Constructor que inicializa el trabajador con el socket, 
     * el flujo de salida y el flujo de entrada.
     * 
     * @param socket el socket de conexión con el cliente
     * @param salida el flujo de salida para enviar mensajes al cliente
     * @param entrada el flujo de entrada para recibir mensajes del cliente
     */
    public Worker(Socket socket, ObjectOutputStream salida, 
            ObjectInputStream entrada) {
        this.socket = socket;
        this.db = SignableSingleton.getDao();
        this.entrada = entrada;
        this.salida = salida;
        
    }

    /**
     * Ejecuta el proceso de comunicación entre el cliente y el servidor.
     * <p>
     * Lee un mensaje del cliente, identifica la solicitud de inicio de sesión 
     * o registro y responde con el resultado de la operación o con un mensaje
     * de error si ocurre una excepción.
     * </p>
     */
    public void run() {
        try {
            //Recoge el mensaje del cliente
            message = (Message) entrada.readObject();
            //Realiza la accion del mensaje y le notifica el resultado
            if (message.getRequest() == Request.SING_UP_REQUEST) {
                message.setUser(db.signUp(message.getUser()));
                salida.writeObject(message);
            } else if (message.getRequest() == Request.SING_IN_REQUEST) {
                message.setUser(db.signIn(message.getUser()));
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
            Aplication.closeServerConnection();
        }
    }

    /**
     * Envía un mensaje de error al cliente cuando ocurre una excepción
     * durante el procesamiento.
     * 
     * @param message el mensaje a enviar
     * @param salida el flujo de salida para enviar el mensaje
     * @param ex la excepción que ocurrió
     * @param req el tipo de solicitud que representa el error específico
     */
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
