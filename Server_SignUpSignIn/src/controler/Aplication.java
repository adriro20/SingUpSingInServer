/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import clases.Message;
import clases.Request;
import controler.Closable;
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
 * Clase principal para el servidor de la aplicación de registro e inicio de sesión.
 * <p>
 * Esta clase gestiona la inicialización del servidor, el manejo de conexiones de clientes y
 * el control de las operaciones de cierre y liberación de recursos. Las solicitudes de los clientes
 * son atendidas por hilos de tipo {@link Worker}, que gestionan de forma individual cada sesión
 * de conexión.
 * </p>
 * 
 * @author Adrian Rocha
 */
public class Aplication {
    /** Número actual de conexiones activas al servidor. */
    private static int conns = 0;

    /** Indica si el servidor debe finalizar su ejecución. */
    private static boolean finalizarServidor = false;

    /** Socket del servidor que escucha conexiones entrantes. */
    private static ServerSocket server = null;

    /** Socket que representa la conexión con el cliente actual. */
    private static Socket socket = null;

    /** Flujo de salida para enviar datos al cliente. */
    private static ObjectOutputStream salida = null;

    /** Flujo de entrada para recibir datos del cliente. */
    private static ObjectInputStream entrada = null;

    /** Logger para registrar eventos y errores del servidor. */
    private static final Logger log = Logger.getLogger(Aplication.class.getName());
    
    /**
     * Método principal de la aplicación. Configura y ejecuta el servidor, 
     * y maneja las conexiones entrantes.
     * <p>
     * Este método establece el puerto de escucha del servidor, crea un hilo lector
     * para gestionar los comandos de cierre del servidor, y permite un número máximo de
     * conexiones según se especifica en el archivo de configuración.
     * </p>
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            //Se inicializan los parametros de conexion del servidor
            int PUERTO;
            int maxConn;
            
            Message message = new Message();
            Worker workerThread;
            
            PUERTO = getConnInfo();
            server = new ServerSocket(PUERTO);
            
            Reader readerThread = new Reader();
            readerThread.start();
            maxConn = getMaxConnections();
            //El servidor comienza a atender peticiones del cliente
            while (!finalizarServidor) {
                try {
                    socket = server.accept();
                    salida = new ObjectOutputStream(socket.getOutputStream());
                    entrada = new ObjectInputStream(socket.getInputStream());
                    //Se comprueba si el servidor puede atender mas peticiones,
                    //si no puede lanza una excepcion y comunica al cliente
                    if (conns < maxConn) {
                        workerThread = new Worker(socket, salida, entrada);
                        conns++;
                        workerThread.start();
                    } else {
                        throw new NoConnectionsAvailableException();
                    }
                } catch (IOException ex) {
                    log.log(Level.SEVERE, null, ex);
                    closeServerConnection();
                } catch (NoConnectionsAvailableException ex) {
                    log.log(Level.SEVERE, null, ex);
                    message.setRequest(Request.CONNECTIONS_EXCEPTION);
                    salida.writeObject(message);
                    closeServerConnection();
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

    /**
     * Obtiene el número máximo de conexiones permitidas para el servidor 
     * desde el archivo de configuración.
     * 
     * @return el número máximo de conexiones concurrentes permitidas
     */
    public static int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.connections");
        String conn = fichConf.getString("TCON");
        
        return Integer.valueOf(conn);
    }

    /**
     * Obtiene el puerto de conexión del servidor desde el archivo de 
     * configuración.
     * 
     * @return el número de puerto en el que el servidor escuchará las conexiones
     */
    public static int getConnInfo() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.infoServer");
        String port = fichConf.getString("PORT");
        
        return Integer.valueOf(port);
    }
    
    /**
     * Libera una conexión, disminuyendo el contador de conexiones activas.
     */
    public synchronized static void releaseConn() {
        Aplication.conns -= 1;
    }
    
    /**
     * Cierra la conexión del servidor con el cliente actual, 
     * liberando recursos asociados.
     * <p>
     * Este método se asegura de cerrar los flujos de entrada y salida, 
     * así como el socket de conexión.
     * </p>
     */
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
    
    /**
     * Detiene el servidor y cierra todos los recursos, 
     * incluyendo el pool de conexiones.
     * <p>
     * Este método cierra las conexiones activas, libera los recursos 
     * del pool de conexiones mediante {@link Closable}, y establece el estado 
     * para finalizar la ejecución del servidor.
     * </p>
     */
    public synchronized static void closeServer() {
        Closable pool = ConnectionPoolSingleton.getPool();
        //Cambia el estado del servidor para que se prepare para finalizarse
        Aplication.finalizarServidor = true;
        //Espera a que finalizen los hilos trabajadores
        while(conns != 0) {
        }
        //Cierra las connexiones del pool
        pool.close();
        //Cierra los flujos de entrada y salida y el socket de conexion con el
        //cliente
        closeServerConnection();
        //Cierra el servidor
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        //Finaliza el servidor
        System.exit(0);
    }    
}
