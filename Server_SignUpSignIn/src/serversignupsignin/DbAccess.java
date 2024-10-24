/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Signable;
import clases.User;
import controler.ConnectionPool;
import controler.ConnectionPoolSingleton;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.NoConnectionsAvailableException;
import excepciones.UserExitsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Gestiona la base de datos cogiendo e insertando datos en ella, haciendo
 * tanto el metodo SingIn como SIngUp.
 * Maneja las conexiones, realiza consultas SQL y gestiona excepciones relacionadas
 * con las operaciones de la base de datos
 * @author Erlantz Rey
 */
public class DbAccess implements Signable{
    /**Objeto utilizado para establecer una conexión con la base de datos.*/
    private Connection con;
    /**Objeto} utilizado para preparar y ejecutar consultas SQL con parametros.*/
    private PreparedStatement stmt;
    /**Objeto que almacena el resultado de una consulta SQL.*/
    private ResultSet rs;
    /**Pool de conexiones que administra múltiples conexiones reutilizables a la base de datos.*/
    private ConnectionPool connectionPool;
            
    /**insert para meter los datos que nos pasan en la tabla res_partner*/
    private final String INSERT_PARTNER = "insert into res_partner (company_id, name, email, street, city, zip) values (1, ?, ?, ?, ?, ?)";
    /**insert para meter los datos que nos pasan en la tabla res_users*/
    private final String INSERT_USERS = "insert into res_users (company_id, partner_id, login, password, active, notification_type) values (1, ?, ?, ?, ?, 'email')";
    /**select para comprobar que el email y contraseña existen y coincidenr*/
    private final String SELECT_SINGIN = "select company_id, partner_id, login, password, active, notification_type from res_users where login=? and passsword=?";
    /**select para saber si el email ya existe a la hora de registrar*/
    private final String SELECT_EMAIL = "select email from res_partner where email=?";
    /**select para coger el partner id para el insert de res_users*/
    private final String SELECT_PARTNER_ID = "select id from res_partner order by id desc limit 1";
    
    
    /**
     * abrir la conexion con la base de datos utilizando el pool
     */
    private synchronized void getConnection() throws NoConnectionsAvailableException, SQLException {
        ConnectionPool pool = ConnectionPoolSingleton.getPool();
       this.connectionPool=pool;
        try {
            this.con = connectionPool.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
            throw new SQLException(ex);
        }
    }

     /**
     * Este metodo cierra la conexion con la base de datos
     *
     * @throws SQLException
     */
    private synchronized void releaseConnection() throws SQLException {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                connectionPool.returnConnection(con);
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * sirve para iniciar sesion para eso comprueba que los datos que 
     * le dan (message) estan en la base de datos  
     * @param mensaje
     * @throws InternalServerErrorException
     * @throws LogInDataException
     * @throws NoConnectionsAvailableException 
     */
    @Override
    public synchronized User signIn(Message mensaje) throws InternalServerErrorException, LogInDataException, NoConnectionsAvailableException{
        try {
            this.getConnection();
            
            //el select que recoge los datos si el email y contraseña coincide 
            stmt = con.prepareStatement(SELECT_SINGIN);
            stmt.setString(1, mensaje.getUser().getEmail());
            stmt.setString(2, mensaje.getUser().getPassword());
            ResultSet rs = stmt.executeQuery();
            releaseConnection();
            
            if(!rs.next()){
                throw new LogInDataException();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception e){
            
        }
 
         return mensaje.getUser();
    }
    
    /**
     * Registra el usuario, para ello hace un insert con todos los parametros que le pasan que le pasan
     * @param mensaje
     * @throws InternalServerErrorException
     * @throws UserExitsException
     * @throws NoConnectionsAvailableException 
     */
    @Override
    public synchronized User signUp(Message mensaje) throws InternalServerErrorException, UserExitsException, NoConnectionsAvailableException{
        try {
            this.getConnection();
            stmt = con.prepareStatement(SELECT_EMAIL);
            stmt.setString(1, mensaje.getUser().getEmail());
            ResultSet login = stmt.executeQuery();
            if(login.next()){
                throw new UserExitsException();
            } else {
                stmt = con.prepareStatement(INSERT_PARTNER);
                stmt.setString(1, mensaje.getUser().getName());
                stmt.setString(2, mensaje.getUser().getEmail());
                stmt.setString(3, mensaje.getUser().getStreet());
                stmt.setString(4, mensaje.getUser().getCity());
                stmt.setString(5, mensaje.getUser().getZip());
                stmt.executeUpdate();

                stmt = con.prepareStatement(SELECT_PARTNER_ID);
                ResultSet partner = stmt.executeQuery();
                int partnerId =0;
                if (partner.next()) 
                     partnerId = partner.getInt("id");

                stmt = con.prepareStatement(INSERT_USERS);
                stmt.setInt(1, partnerId);
                stmt.setString(2, mensaje.getUser().getEmail());
                stmt.setString(3, mensaje.getUser().getPassword());
                stmt.setBoolean(4, mensaje.getUser().isActive());
                stmt.executeUpdate();

                releaseConnection();
            }

        } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
        } catch(Exception e){
            
        }

        return mensaje.getUser();
    }
    
}