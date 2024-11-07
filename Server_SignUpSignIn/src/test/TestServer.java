/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import clases.Message;
import clases.Request;
import clases.User;
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
import controler.DbAccess;

/**
 *
 * @author Adrian Rocha
 */
public class TestServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int puerto = 55557;
        String ip = "127.0.0.1";
        Socket socket = null;
        ObjectInputStream entrada = null;
        ObjectOutputStream salida = null;

        try {
            User user = new User();
            user.setName("erlantz rey");
            user.setEmail("xbxcvbncxvnxnxc@gmail.com");
            user.setCity("bilbao");
            user.setStreet("calle");
            user.setZip("48610");
            user.setActive(true);
            user.setPassword("ErlantZ9");
            Message mensaje = new Message();
            mensaje.setUser(user);
            mensaje.setRequest(Request.SING_IN_REQUEST);
            controler.DbAccess db = new DbAccess();
            //Establece la conexión con el servidor con la IP y el puerto.
            socket = new Socket(ip, puerto);
            //Inicializa los flujos de entrada y salida para enviar y recoger datos.
            entrada = new ObjectInputStream(socket.getInputStream());
            salida = new ObjectOutputStream(socket.getOutputStream());
            //Envia el mensaje al servidor.
            salida.writeObject(mensaje);
            //Espera la entrada del mensaje de vuelta desde el servidor.
            mensaje = (Message) entrada.readObject();
            //Lee el mensaje enviado por el servidor y comprueba si ha devuelto
            //algun mensaje de error, dependiendo del mensaje lanza una excepción u otra.
            switch (mensaje.getRequest()) {
                case INTERNAL_EXCEPTION:
                    throw new InternalServerErrorException();
                case LOG_IN_EXCEPTION:
                    throw new LogInDataException();
                case CONNECTIONS_EXCEPTION:
                    throw new NoConnectionsAvailableException();
                case USER_EXISTS_EXCEPTION:
                    throw new UserExitsException();
                case USER_NOT_ACTIVE_EXCEPTION:
                    throw new UserNotActiveException();
            }
            
            System.out.println(mensaje.getRequest());
            //Si sucede algún otro error se lanza la excepción InternalServerErrorException.
            
        } catch (IOException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InternalServerErrorException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LogInDataException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoConnectionsAvailableException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UserExitsException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UserNotActiveException ex) {
            Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                //Se cierra la conexión y los flujos de salida y entrada.
                if (socket != null) {
                    socket.close();
                }
                if (entrada != null) {
                    entrada.close();
                }
                if (salida != null) {
                    salida.close();
                }
                //Si sucede algún error al cerrar las conexiones se lanza la
                //excepción InternalServerErrorException.
            } catch (IOException e) {
                Logger.getLogger(TestServer.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
}
