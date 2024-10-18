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
 *Gestiona la base de datos cogiendo e insertando datos en ella 
 * @author 2dam
 */
public class DbAccess implements Signable{
     
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    private ConnectionPool connectionPool;
            
    /**insert para meter los datos que nos pasan en la tabla ves_parther*/
    private final String INSERT_PARTNER = "insert into res_partner (company_id, name, email, street, city, zip) values (1, ?, ?, ?, ?, ?)";
    /**insert para meter los datos que nos pasan en la tabla ves_users*/
    private final String INSERT_USERS = "insert into res_users (company_id, partner_id, login, password, active, notification_type) values (1, ?, ?, ?, ?, 'email')";
    /**select para comprobar que el email y contraseña existen y coincidenr*/
    private final String SELECT_SINGIN = "select * from res_users where login=? and passsword=?";
    /**select para saber si el email ya existe a la hora de registrar*/
    private final String SELECT_EMAIL = "select * from res_partner where email=?";
    /**select para coger el pather_id para el insert de ves_users*/
    private final String SELECT_PARTNER_ID = "select id from res_partner order by id desc limit 1";
    
    
    /**
     * abrir la conexion con la base de datos
     */
    private void getConnection() {
        ConnectionPool pool = new ConnectionPool("jdbc:postgresql://192.168.20.231:5432/odoodes", "odoo", "abcd*1234");
       this.connectionPool=pool;
        try {
            this.con = connectionPool.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Este metodo cierra la conexion con la base de datos
     *
     * @throws SQLException
     */
    private void releaseConnection() throws SQLException {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();
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
    public User signIn(User user) throws InternalServerErrorException, LogInDataException{
        this.getConnection();
        
        try {
            
            stmt = con.prepareStatement(SELECT_SINGIN);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            ResultSet rs = stmt.executeQuery();
            releaseConnection();
            
            if(!rs.next()){
                throw new LogInDataException();
                
            }
          
        }catch(Exception e){
            
        }
 
         return user;
        
   
    }
    
    /**
     * Registra el usuario, para ello hace un insert con todos los parametros que le pasan que le pasan
     * @param mensaje
     * @throws InternalServerErrorException
     * @throws UserExitsException
     * @throws NoConnectionsAvailableException 
     */
    @Override
    public User signUp(User user) throws InternalServerErrorException, UserExitsException{
        this.getConnection();
		try {
                    stmt = con.prepareStatement(SELECT_EMAIL);
                    stmt.setString(1, user.getEmail());
                    ResultSet login = stmt.executeQuery();
                    if(login.next()){
                        throw new UserExitsException();
                    } else {
                        stmt = con.prepareStatement(INSERT_PARTNER);
                        stmt.setString(1, user.getName());
                        stmt.setString(2, user.getEmail());
                        stmt.setString(3, user.getStreet());
                        stmt.setString(4, user.getCity());
                        stmt.setString(5, user.getZip());
                        stmt.executeUpdate();
                        
                        stmt = con.prepareStatement(SELECT_PARTNER_ID);
                        ResultSet partner = stmt.executeQuery();
                        int partnerId =0;
                        if (partner.next()) 
                             partnerId = partner.getInt("id");
                        
                        stmt = con.prepareStatement(INSERT_USERS);
                        stmt.setInt(1, partnerId);
                        stmt.setString(2, user.getEmail());
                        stmt.setString(3, user.getPassword());
                        stmt.setBoolean(4, user.isActive());
                        stmt.executeUpdate();
                        
                        releaseConnection();
                    }
                    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
                return user;
    }
    
}
