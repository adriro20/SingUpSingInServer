/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Scanner;
import serversignupsignin.Aplication;

/**
 *
 * @author 2dam
 */
public class Reader extends Thread{
    
    private boolean cerrar = false;
    private Scanner sc = new Scanner(System.in);
    
    public void run(){
        System.out.println("Para cerrar el servidor, pulse la tecla Esc");
        
        while(!cerrar){
            if(sc.next().equals("exit")){
                Aplication.closeServer();
                cerrar = true;
            }
        }
    }
    
}
