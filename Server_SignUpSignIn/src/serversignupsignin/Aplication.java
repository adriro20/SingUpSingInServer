/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Request;
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

/**
 *
 * @author Adrian Rocha
 */
public class Aplication {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int PUERTO;
        boolean finalizarServidor = false;
        int maxConn, conns = 0;
        Message message = new Message();
        Request enumReq;
        
        ServerSocket server = null;
        Socket socket = null;
        ObjectInputStream entrada = null;
        ObjectOutputStream salida = null;
        List<Thread> threads = new ArrayList<>();
        
        try {
            PUERTO = getConnInfo();
            server = new ServerSocket(PUERTO);
                    
            while (!finalizarServidor) {
                try {
                    socket = server.accept();
                    maxConn = getMaxConnections();
                    if (conns < maxConn) {
                        salida = new ObjectOutputStream(socket.getOutputStream());
                        entrada = new ObjectInputStream(socket.getInputStream());

                        message = (Message) entrada.readObject();
                        
                        enumReq = message.getRequest();
                        
                        if (enumReq == Request.CLOSE) {
                            if (conns > 0) {
                                
                            } 
                            finalizarServidor = true;
                        } else {
                            
                        }
                        conns++;
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Aplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    private static int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.totalConnections");
        String conn = fichConf.getString("TCON");
        
        return Integer.valueOf(conn);
    }

    private static int getConnInfo() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.infoServer");
        String port = fichConf.getString("PORT");
        
        return Integer.valueOf(port);
    }
    
}
