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
 * Clase que implementa un pool de conexiones a una base de datos.
 * Administra un conjunto de conexiones disponibles y ocupadas,
 * permitiendo reutilizar conexiones en lugar de abrir una nueva cada vez.
 * Esta clase implementa la interfaz {@code AutoCloseable}, lo que permite cerrar
 * todas las conexiones activas cuando el pool se cierra.
 * 
 * @author Adrian Rocha
 */
public class ConnectionPool implements AutoCloseable {

    /** URL de la base de datos para establecer la conexión. */
    private String databaseUrl;

    /** Nombre de usuario para autenticarse en la base de datos. */
    private String userName;

    /** Contraseña para autenticarse en la base de datos. */
    private String password;

    /** Número máximo de conexiones permitidas en el pool. */
    private int maxPoolSize = getMaxConnections();

    /** Número de conexiones actuales activas en el pool. */
    private int connNum = 0;

    /** Consulta SQL para verificar si una conexión es válida. */
    private static final String SQL_VERIFYCONN = "SELECT 1";

    /** Lista de conexiones disponibles en el pool. */
    private LinkedList<Connection> freePool = new LinkedList<>();

    /** Conjunto de conexiones actualmente en uso. */
    private Set<Connection> occupiedPool = new HashSet<>();

    /**
     * Constructor vacío de la clase {@code ConnectionPool}.
     * No inicializa los parámetros de conexión.
     */
    public ConnectionPool() {
    }

    /**
     * Constructor de la clase {@code ConnectionPool} que inicializa con los parámetros de conexión.
     * 
     * @param databaseUrl URL de la base de datos.
     * @param userName Nombre de usuario de la base de datos.
     * @param password Contraseña del usuario de la base de datos.
     */
    public ConnectionPool(String databaseUrl, String userName, String password) {
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
    }

    /**
     * Cierra todas las conexiones del pool. Cierra tanto las conexiones libres como las ocupadas.
     * Este método es sincronizado para garantizar la consistencia en un entorno multi-hilo.
     */
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

    /**
     * Obtiene una conexión del pool. Si no hay conexiones libres y no se ha alcanzado el límite máximo
     * de conexiones, se crea una nueva conexión. Si no hay conexiones disponibles y se ha alcanzado
     * el límite, se lanza una excepción.
     * 
     * @return Una conexión disponible.
     * @throws SQLException Si no hay conexiones disponibles o hay un error al crear la conexión.
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection conn = null;

        if (isFull()) {
            throw new SQLException("Conexiones completas");
        }

        conn = getConnectionFromPool();

        // Si no hay conexiones libres, crea una nueva.
        if (conn == null) {
            conn = createNewConnectionForPool();
        }

        conn = makeAvailable(conn);
        return conn;
    }

    /**
     * Devuelve una conexión al pool, marcándola como disponible para ser reutilizada.
     * 
     * @param conn La conexión a devolver.
     * @throws SQLException Si la conexión no pertenece al pool o ya ha sido devuelta.
     */
    public synchronized void returnConnection(Connection conn) throws SQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        if (!occupiedPool.remove(conn)) {
            throw new SQLException("La conexión ya ha sido devuelta o no pertenece a este pool.");
        }
        freePool.push(conn);
    }

    /**
     * Verifica si el pool ha alcanzado el límite máximo de conexiones.
     * 
     * @return {@code true} si el pool está lleno, {@code false} en caso contrario.
     */
    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (connNum >= maxPoolSize));
    }

    /**
     * Crea una nueva conexión y la agrega al pool de conexiones ocupadas.
     * 
     * @return La nueva conexión creada.
     * @throws SQLException Si ocurre un error al crear la conexión.
     */
    private Connection createNewConnectionForPool() throws SQLException {
        Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    /**
     * Obtiene una conexión libre del pool. Si no hay conexiones disponibles, devuelve {@code null}.
     * 
     * @return Una conexión libre o {@code null} si no hay disponibles.
     */
    private Connection getConnectionFromPool() {
        Connection conn = null;
        if (freePool.size() > 0) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    /**
     * Crea una nueva conexión a la base de datos utilizando los parámetros de conexión.
     * 
     * @return Una nueva conexión a la base de datos.
     * @throws SQLException Si ocurre un error al crear la conexión.
     */
    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl, userName, password);
    }

    /**
     * Verifica si una conexión es válida y está disponible para ser reutilizada.
     * Si la conexión no es válida, se cierra y se reemplaza por una nueva.
     * 
     * @param conn La conexión a verificar.
     * @return La conexión disponible o una nueva conexión si la anterior no era válida.
     * @throws SQLException Si ocurre un error al verificar o crear la conexión.
     */
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

    /**
     * Verifica si una conexión es válida ejecutando una consulta de prueba.
     * 
     * @param conn La conexión a verificar.
     * @return {@code true} si la conexión es válida, {@code false} en caso contrario.
     */
    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERIFYCONN);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Obtiene el número máximo de conexiones permitidas desde un archivo de configuración.
     * 
     * @return El número máximo de conexiones configurado.
     */
    private int getMaxConnections() {
        ResourceBundle fichConf = ResourceBundle.getBundle("model.totalConnections");
        String conn = fichConf.getString("TCON");
        return Integer.valueOf(conn);
    }
}