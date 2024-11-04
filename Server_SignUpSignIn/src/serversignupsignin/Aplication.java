/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Request;
import controler.ConnectionPool;
import controler.ConnectionPoolSingleton;
import excepciones.NoConnectionsAvailableException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Reader;
import model.Worker;

/**
 *
 * @author Adrian Rocha
 */
public class Aplication {
    private static int conns = 0;
    private static boolean finalizarServidor = false;
    private static ServerSocket server = null;
    private static Socket socket = null;
    private static ObjectOutputStream salida = null;
    private static ObjectInputStream entrada = null;
    private static final Logger log = Logger.getLogger(Aplication.class.getName());
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int PUERTO;
            int maxConn;
            
            Message message = new Message();
            Worker workerThread;
            
            PUERTO = getConnInfo();
            server = new ServerSocket(PUERTO);
            
            Reader readerThread = new Reader(server);
            readerThread.start();
            
            while (!finalizarServidor) {
                try {
                    socket = server.accept();
                    maxConn = getMaxConnections();
                    salida = new ObjectOutputStream(socket.getOutputStream());
                    entrada = new ObjectInputStream(socket.getInputStream());
                    if (conns < maxConn) {
                        workerThread = new Worker(socket, salida, entrada);
                        conns++;
                        workerThread.start();
                    } else {
                        throw new NoConnectionsAvailableException();
                    }
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                } catch (NoConnectionsAvailableException ex) {
                    log.log(Level.SEVERE, null, ex);
                    message.setRequest(Request.CONNECTIONS_EXCEPTION);
                    salida.writeObject(message);
                } finally {
                    
                }
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        } finally {
            closeServerConnection();
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
        }
        
    }

    public static int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.connections");
        String conn = fichConf.getString("TCON");
        
        return Integer.valueOf(conn);
    }

    public static int getConnInfo() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.infoServer");
        String port = fichConf.getString("PORT");
        
        return Integer.valueOf(port);
    }
    
    public synchronized static void releaseConn() {
        Aplication.conns -= 1;
    }
    
    public synchronized static void closeServerConnection() { 
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
    
    public synchronized static void closeServer() {
        ConnectionPool pool = ConnectionPoolSingleton.getPool();
        pool.close();
        Aplication.finalizarServidor = true;
        try {
            if (socket != null) {
                socket.close();
            }
            if (salida != null) {
                salida.close();
            }
            if (entrada != null) {
                entrada.close();
            }
            if (server != null) {
                server.close();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        while(conns != 0) {
        }
        System.exit(0);
    }    
    
    
    
    
    
    
    
    
    
    
    
    
}
