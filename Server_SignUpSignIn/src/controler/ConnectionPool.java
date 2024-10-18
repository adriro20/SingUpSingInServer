/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.LinkedList;

/**
 *
 * @author Adrian Rocha
 */
public class ConnectionPool implements AutoCloseable {
    /**URL de la base para hacer la conexion*/
    private String databaseUrl;
    /**El nombre del usuario*/
    private String userName;
    /**La contraseña del */
    private String password;
    /**Maximo de conexiones*/
    private int maxPoolSize = getMaxConnections();
    /**conexiones que tiene ahora*/
    private int connNum = 0;
    
    private static final String SQL_VERIFYCONN = "SELECT 1";
    
    /**constructor vacio*/
    public ConnectionPool() {
    }
    
    
    
    LinkedList<Connection> freePool = new LinkedList<>();
    Set<Connection> occupiedPool = new HashSet<>();
    
    private int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.totalConnections");
        String conn = fichConf.getString("TCON");
        return Integer.valueOf(conn);
    }

    public ConnectionPool(String databaseUrl, String userName, String password) {
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
    }
    
    @Override
    public synchronized void close() {
        // Cerrar todas las conexiones libres
        while (!freePool.isEmpty()) {
            try {
                freePool.pop().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
       
        // Cerrar todas las conexiones ocupadas
        for (Connection conn : occupiedPool) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        occupiedPool.clear();
        connNum = 0;
    }
    
    public synchronized Connection getConnection() throws SQLException {
        Connection conn = null;

        if (isFull()) {
            throw new SQLException("conexiones completas");
        }

        conn = getConnectionFromPool();

        // If there is no free connection, create a new one.
        if (conn == null) {
            conn = createNewConnectionForPool();
        }

        conn = makeAvailable(conn);
        return conn;
    }

    public synchronized void returnConnection(Connection conn) throws SQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        if (!occupiedPool.remove(conn)) {
            throw new SQLException("The connection is returned already or it isn't for this pool");
        }
        freePool.push(conn);
    }

    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (connNum >= maxPoolSize));
    }

    private Connection createNewConnectionForPool() throws SQLException {
        Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    private Connection getConnectionFromPool() {
        Connection conn = null;
        if (freePool.size() > 0) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    private Connection createNewConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(databaseUrl, userName, password);
        return conn;
    }

    private Connection makeAvailable(Connection conn) throws SQLException {
        if (isConnectionAvailable(conn)) {
            return conn;
        }

        occupiedPool.remove(conn);
        connNum--;
        conn.close();

        conn = createNewConnection();
        occupiedPool.add(conn);
        connNum++;
        return conn;
    }

    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERIFYCONN);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}