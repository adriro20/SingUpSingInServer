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
        while(!cerrar){
            System.out.println("Para cerrar el servidor, escriba exit:");
            if(sc.next().equalsIgnoreCase("exit")){
                System.out.println("¿Estas seguro? SI/no");
                if(sc.next().equalsIgnoreCase("si") || sc.next().equalsIgnoreCase("")){
                    Aplication.closeServer();
                    cerrar = true;
                }
            }
        }
    }
    
}
