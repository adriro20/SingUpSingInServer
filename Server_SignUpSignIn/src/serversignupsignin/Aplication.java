/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Request;
import clases.Signable;
import controler.SignableFactory;
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
    private static List<Worker> threads;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ServerSocket server = null;
        Socket socket = null;
        ObjectOutputStream salida = null;
        Signable db = SignableFactory.getSignable();
        threads = new ArrayList<>();
        try {
            int PUERTO;
            boolean finalizarServidor = false;
            int maxConn;
            
            Message message = new Message();
            Worker workerThread;
            
            
            PUERTO = getConnInfo();
            server = new ServerSocket(PUERTO);
            
                    
            while (!finalizarServidor) {
                try {
                    socket = server.accept();
                    maxConn = getMaxConnections();
                    salida = new ObjectOutputStream(socket.getOutputStream());
                    if (conns < maxConn) {
                        workerThread = new Worker(socket, db);
                        conns++;
                        workerThread.setWorkerid(conns);
                        workerThread.start();
                        threads.add(workerThread);
                    } else {
                        throw new NoConnectionsAvailableException();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                    message.setRequest(Request.INTERNAL_EXCEPTION);
                } catch (NoConnectionsAvailableException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                    message.setRequest(Request.CONNECTIONS_EXCEPTION);
                    salida.writeObject(message);
                }finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                        if (salida != null) {
                            salida.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
            }
        } catch (IOException ex) {
            Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (server != null) {
                try {
                    for (Worker thread : threads) {
                        thread.join();
                    }
                    server.close();
                } catch (IOException | InterruptedException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                } 
            }
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
    
    public synchronized static void releaseConn(int id) {
        for (Worker thread : threads) {
            if (thread.getWorkerid() == id) {
                threads.remove(thread);
            }
        }
        Aplication.conns -= 1;
    }
    
}
