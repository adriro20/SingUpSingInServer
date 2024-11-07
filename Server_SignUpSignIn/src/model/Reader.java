/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.net.ServerSocket;
import java.util.Scanner;
import controler.Aplication;

/**
 * Esta clase permite que un usuario cierre el servidor de forma segura escribiendo "exit" en la 
 * consola. Si el usuario confirma, la aplicación llamará al método {@link Aplication#closeServer()} 
 * para finalizar el servidor.
 * <p>La clase está diseñada para ejecutarse en su propio hilo, permitiendo al servidor seguir 
 * funcionando hasta que se envíe el comando de cierre.</p>
 * 
 * @author Julen Hidalgo, Adrian Rocha
 */
public class Reader extends Thread{
    /**
     * Controla el ciclo de ejecución del hilo. Se establece en {@code true} cuando se solicita el cierre.
     */
    private boolean cerrar = false;
    
    /**
     * Escáner para leer comandos desde la consola de entrada estándar.
     */
    private Scanner sc = new Scanner(System.in);
    
    /**
     * Ejecuta el ciclo de espera y lectura de comandos desde la entrada estándar para cerrar el servidor.
     * <p>
     * Mientras el servidor esté en funcionamiento, el método monitorea la entrada del usuario.
     * Si el usuario escribe "exit", se le solicita confirmación. Al confirmar, el método
     * llama a {@link Aplication#closeServer()} y finaliza el ciclo estableciendo {@code cerrar} en {@code true}.
     * </p>
     */
    public void run(){
        while(!cerrar){
            //Recoge la entrada por consola
            System.out.println("Para cerrar el servidor, escriba exit:");
            if(sc.next().equalsIgnoreCase("exit")){
                //Hace una verificacion
                System.out.println("¿Estas seguro? SI/no");
                if(sc.next().equalsIgnoreCase("si") || sc.next().equalsIgnoreCase("")){
                    //Cierra el servidor
                    Aplication.closeServer();
                    cerrar = true;
                }
            }
        }
    }
    
}
