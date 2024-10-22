/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Request;
import excepciones.NoConnectionsAvailableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Worker;

/**
 *
 * @author Adrian Rocha
 */
public class Aplication {
    private static int conns = 0;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int PUERTO;
            boolean finalizarServidor = false;
            int maxConn;
            
            Message message = new Message();
            Worker workerThread;
            
            ServerSocket server = null;
            Socket socket = null;
            ObjectInputStream entrada = null;
            ObjectOutputStream salida = null;
            List<Worker> threads = new ArrayList<>();
            
            PUERTO = getConnInfo();
            server = new ServerSocket(PUERTO);
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
                    
            while (!finalizarServidor) {
                try {
                    socket = server.accept();
                    maxConn = getMaxConnections();
                    if (conns < maxConn) {
                        message = (Message) entrada.readObject();

                        workerThread = new Worker(message, socket);
                        threads.add(workerThread);

                        conns++;
                    } else {
                        throw new NoConnectionsAvailableException();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                    message.setRequest(Request.INTERNAL_EXCEPTION);
                    salida.writeObject(message);
                } catch (NoConnectionsAvailableException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                    message.setRequest(Request.CONNECTIONS_EXCEPTION);
                    salida.writeObject(message);
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.totalConnections");
        String conn = fichConf.getString("TCON");
        
        return Integer.valueOf(conn);
    }

    public static int getConnInfo() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.infoServer");
        String port = fichConf.getString("PORT");
        
        return Integer.valueOf(port);
    }
    
    public static void releaseConn() {
        Aplication.conns -= 1;
    }
    
}
