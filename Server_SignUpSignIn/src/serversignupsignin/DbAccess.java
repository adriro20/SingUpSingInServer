/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serversignupsignin;

import clases.Message;
import clases.Signable;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.NoConnectionsAvailableException;
import excepciones.UserExitsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *Gestiona la base de datos cogiendo e insertando datos en ella 
 * @author 2dam
 */
public class DbAccess implements Signable{
    
    Message mensaje;
    
    private Connection con;
    private PreparedStatement stmt;
    private ResultSet rs;
    
    /**insert para meter los datos que nos pasan en la tabla ves_parther*/
    private final String INSERT_PARTHER = "insert into ves_parther(company_id, name, zip, email, street, city) values(?,?,?,?,?,?)";
    /**insert para meter los datos que nos pasan en la tabla ves_users*/
    private final String INSERT_USERS = "insert into ves_users(company_id, parther_id, login, password) values(?,?,?,?)";
    /**select para comprobar que el email y contraseña existen y coincidenr*/
    private final String SELECT_SINGIN = "select * from ves_user where login=? and passsword=?";
    /**select para saber si el email ya existe a la hora de registrar*/
    private final String SELECT_EMAIL = "select * from ves_parther where email=?";
    /**select para coger el pather_id para el insert de ves_users*/
    private final String SELECT_PARTHER_ID = "select * from ves_parther order by parther_id desc limit 1";
    
    
    /**
     * abrir la conexion con la base de datos
     */
    private void getConnection() {
        try {
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/beatdam?serverTimezone=Europe/Madrid&useSSL=false", "root",
                    "abcd*1234");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
    public boolean signIn(Message mensaje) throws InternalServerErrorException, LogInDataException{
        this.getConnection();
        try {
            stmt = con.prepareStatement(SELECT_SINGIN);
            stmt.setString(1, mensaje.getUser().getEmail());
            stmt.setString(2, mensaje.getUser().getPassword());
            ResultSet rs = stmt.executeQuery();
            releaseConnection();
            if(rs==null){
                throw new LogInDataException();
                
            }
          
        }catch(Exception e){
            
        }
 
         return true;
        
        
    }
    
    /**
     * Registra el usuario, para ello hace un insert con todos los parametros que le pasan que le pasan
     * @param mensaje
     * @throws InternalServerErrorException
     * @throws UserExitsException
     * @throws NoConnectionsAvailableException 
     */
    @Override
    public boolean signUp(Message mensaje) throws InternalServerErrorException, UserExitsException{
        this.getConnection();
		try {
                    stmt = con.prepareStatement(SELECT_EMAIL);
                    stmt.setString(1, mensaje.getUser().getEmail());
                    ResultSet login = stmt.executeQuery();
                    if(login!=null){
                        throw new UserExitsException();
                    } else {
                        stmt = con.prepareStatement(INSERT_PARTHER);
                        stmt.setString(1, mensaje.getUser().getName());
                        stmt.setString(2, mensaje.getUser().getZip());
                        stmt.setString(3, mensaje.getUser().getEmail());
                        stmt.setString(4, mensaje.getUser().getStreet());
                        stmt.setString(4, mensaje.getUser().getCity());

                        stmt = con.prepareStatement(SELECT_PARTHER_ID);
                        ResultSet parther = stmt.executeQuery();

                        stmt = con.prepareStatement(INSERT_USERS);
                        stmt.setInt(1, mensaje.getUser().getCompany_id());
                        stmt.setString(2, String.valueOf(parther));
                        stmt.setString(3, mensaje.getUser().getEmail());
                        stmt.setString(4, mensaje.getUser().getPassword());
                        stmt.executeUpdate();
                        releaseConnection();
                    }
                    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
                return true;
    }
    
    /**
     * cierra la sesion del usuario 
     * @param mensaje
     * @throws InternalServerErrorException 
     */
    @Override
    public boolean logOut(Message mensaje) throws InternalServerErrorException{
        
        return false;
    }
}
