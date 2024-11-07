/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import clases.Message;
import clases.Signable;
import clases.User;
import controler.Closable;
import controler.ConnectionPool;
import controler.ConnectionPoolSingleton;
import excepciones.InternalServerErrorException;
import excepciones.LogInDataException;
import excepciones.NoConnectionsAvailableException;
import excepciones.ServerClosedException;
import excepciones.UserExitsException;
import excepciones.UserNotActiveException;
import java.sql.Connection;
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
    private Connection con = null;
    /**Objeto} utilizado para preparar y ejecutar consultas SQL con parametros.*/
    private PreparedStatement stmt;
    /**Objeto que almacena el resultado de una consulta SQL.*/
    private ResultSet rs;
    /**Pool de conexiones que administra múltiples conexiones reutilizables a la base de datos.*/
    private Closable connectionPool;
    /**insert para meter los datos que nos pasan en la tabla res_partner*/
    private final String INSERT_PARTNER = "insert into res_partner (company_id, name, email, street, city, zip) values (1, ?, ?, ?, ?, ?)";
    /**insert para meter los datos que nos pasan en la tabla res_users*/
    private final String INSERT_USERS = "insert into res_users (company_id, partner_id, login, password, active, notification_type) values (1, ?, ?, ?, ?, 'email')";
    /**select para comprobar que el email y contraseña existen y coincidenr*/
    private final String SELECT_SINGIN_RES_USER = "select company_id, partner_id, login, password, active, notification_type from res_users where login=? and password=?";
    /**Select recoger el resto de datos del usuario en el inicio de sesion*/
    private final String SELECT_SINGIN_RES_PARTNER = "select company_id, partner_id, login, password, active, notification_type from res_users where login=? and password=?";
    /**select para saber si el email ya existe a la hora de registrar*/
    private final String SELECT_EMAIL = "select email from res_partner where email=?";
    /**select para coger el partner id para el insert de res_users*/
    private final String SELECT_PARTNER_ID = "select id from res_partner order by id desc limit 1";
    
    
    /**
     * abrir la conexion con la base de datos utilizando el pool
     * 
     * @throws InternalServerErrorException
     * @throws NoConnectionsAvailableException
     */
    private synchronized void getConnection() throws InternalServerErrorException, NoConnectionsAvailableException {
        Closable pool = ConnectionPoolSingleton.getPool();
        this.connectionPool = pool;
        try {
            this.con = ((ConnectionPool) connectionPool).getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
            if (con != null) {
                releaseConnection();
            }
            throw new InternalServerErrorException();
        }
    }

     /**
     * Este metodo cierra la conexion con la base de datos
     *
     * @throws InternalServerErrorException
     */
    private synchronized void releaseConnection() throws InternalServerErrorException {
        try {
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                ((ConnectionPool) connectionPool).returnConnection(con);
            }

        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
            throw new InternalServerErrorException();
        }
    }
    
    /**
     * sirve para iniciar sesion para eso comprueba que los datos que 
     * le dan (message) estan en la base de datos  
     * @param user Usuario necesario para el inicio de sesion.
     * 
     * @throws InternalServerErrorException Cualquier excepcion fuera de la libreria.
     * @throws LogInDataException Excepcion de los datos de inicio de sesion.
     * @throws NoConnectionsAvailableException Excepcion que salta cuando se supera el numero maximo de conexiones.
     * @throws UserNotActiveException Excepcion de usuario inactivo.
     * @throws ServerClosedException Excepcion de servidor apagado.
     * 
     * @return Los datos de login del usuario
     */
    @Override
    public synchronized User signIn(User user) throws 
            InternalServerErrorException, LogInDataException, 
            NoConnectionsAvailableException, UserNotActiveException, 
            ServerClosedException{
        try {
            this.getConnection();
            
            //El select que recoge los datos si el email y contraseña coincide 
            stmt = con.prepareStatement(SELECT_SINGIN_RES_USER);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPassword());
            ResultSet dbResult = stmt.executeQuery();
            
            if(!dbResult.next()){
                releaseConnection();
                throw new LogInDataException();
            } else {
                user.setEmail(dbResult.getString("login"));
                user.setCompany_id(dbResult.getInt("company_id"));
                user.setActive(dbResult.getBoolean("active"));
                user.setPassword(dbResult.getString("password"));
                if(!user.isActive()) {
                    releaseConnection();
                    throw new UserNotActiveException();
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
            releaseConnection();
            throw new InternalServerErrorException();
        }
        releaseConnection();
        return user;
    }
    
    /**
     * Registra el usuario, para ello hace un insert con todos los parametros 
     * que le pasan que le pasan
     * @param user Usuario necesario para el inicio de sesion.
     * 
     * @throws InternalServerErrorException Cualquier excepcion fuera de la libreria.
     * @throws UserExitsException Excepcion provocada cuando se intenta registrar usuario ya existente.
     * @throws NoConnectionsAvailableException Excepcion que salta cuando se supera el numero maximo de conexiones.
     * @throws ServerClosedException Excepcion de servidor apagado.
     * 
     * @return El usuario introducido
     */
    @Override
    public synchronized User signUp(User user) throws 
            InternalServerErrorException, UserExitsException, 
            NoConnectionsAvailableException, ServerClosedException{
        try {
            // Iniciamos la conexión
            this.getConnection();
            
            // Desactivar autocommit para manejar manualmente la transacción
            con.setAutoCommit(false);

            // Verificar si el usuario ya existe
            stmt = con.prepareStatement(SELECT_EMAIL);
            stmt.setString(1, user.getEmail());
            ResultSet login = stmt.executeQuery();
            if(login.next()){
                releaseConnection();
                throw new UserExitsException();
            } else {
                // Insertar el nuevo partner
                stmt = con.prepareStatement(INSERT_PARTNER);
                stmt.setString(1, user.getName());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getStreet());
                stmt.setString(4, user.getCity());
                stmt.setString(5, user.getZip());
                stmt.executeUpdate();

                // Obtener el ID del partner recién creado
                stmt = con.prepareStatement(SELECT_PARTNER_ID);
                ResultSet partner = stmt.executeQuery();
                int partnerId =0;
                if (partner.next()) 
                     partnerId = partner.getInt("id");

                // Insertar en la tabla de usuarios con el ID del partner
                stmt = con.prepareStatement(INSERT_USERS);
                stmt.setInt(1, partnerId);
                stmt.setString(2, user.getEmail());
                stmt.setString(3, user.getPassword());
                stmt.setBoolean(4, user.isActive());
                stmt.executeUpdate();
                
                // Confirmamos la transacción si todas las operaciones fueron 
                //exitosas
                con.commit();
            }

        } catch (SQLException ex) {
            // Si ocurre un error, hacemos rollback para deshacer los cambios
            if (con != null) {
                try {
                    con.rollback();
                    System.err.println("Transacción revertida debido a un error.");
                } catch (SQLException rollbackEx) {
                    Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, rollbackEx);
                }
            }
            Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
            releaseConnection();
            throw new InternalServerErrorException();
        } finally {
            // Restaurar el autocommit a su estado original y liberar conexión
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ex) {
                    Logger.getLogger(DbAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        releaseConnection();
        return user;
    }
    
}