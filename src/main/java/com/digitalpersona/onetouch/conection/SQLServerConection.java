package com.digitalpersona.onetouch.conection;

import com.digitalpersona.onetouch.views.FormSercurityAuth;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author janez - jamserv@gmail.com 319 - 588.5982
 */
public class SQLServerConection {

    public static SQLServerConection instance = new SQLServerConection();
    private static JFrame win;

    public static SQLServerConection getInstance(JFrame win) {
        SQLServerConection.win = win;
        if (instance == null) {
            instance = new SQLServerConection();
        }
        return instance;
    }

    private HashMap<String, String> hashProperties = new HashMap<String, String>();

    public SQLServerConection() {
    }

    /**
     *
     * @return
     */
    public Connection getSQLConection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(""
                    + "jdbc:sqlserver://" + hashProperties.get("host") + ":"
                    + "" + hashProperties.get("port") + ";"
                    + "user=" + hashProperties.get("username") + ";"
                    + "password=" + hashProperties.get("password") + ";"
                    + "database=" + hashProperties.get("database") + ";");
            return connection;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLServerConection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SQLException ex) {
            FormSercurityAuth auth = (FormSercurityAuth) win;
            auth.makeReportVerify(ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param database
     * @return
     */
    public Connection getSQLConectionWithParams(String host, int port, String username, String password, String database) {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection connection = DriverManager.getConnection(""
                    + "jdbc:sqlserver://" + host + ":"
                    + "" + port + ";"
                    + "user=" + username + ";"
                    + "password=" + password + ";"
                    + "database=" + database + ";");
            return connection;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SQLServerConection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (SQLException ex) {
            FormSercurityAuth auth = (FormSercurityAuth) win;
            auth.makeReportVerify(ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     *
     * @param host
     * @param port
     * @param username
     * @param password
     * @param database
     */
    public void loadPropertiesWithParams(String host, String port, String username, String password, String database) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            String filename = "config.properties";
            input = new FileInputStream(filename);
            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return;
            }
            prop.load(input);

            hashProperties.put("hostname", host);
            hashProperties.put("port", port);
            hashProperties.put("username", username);
            hashProperties.put("password", password);
            hashProperties.put("database", database);
        } catch (IOException ex) {
            FormSercurityAuth auth = (FormSercurityAuth) win;
            auth.makeReportVerify(ex.getLocalizedMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadProperties() {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            String filename = "config.properties";
            input = new FileInputStream(filename);
            if (input == null) {
                System.out.println("Sorry, unable to find " + filename);
                return;
            }
            prop.load(input);

            hashProperties.put("host", prop.getProperty("host"));
            hashProperties.put("port", prop.getProperty("port"));
            hashProperties.put("username", prop.getProperty("username"));
            hashProperties.put("password", prop.getProperty("password"));
            hashProperties.put("database", prop.getProperty("database"));
        } catch (IOException ex) {
            FormSercurityAuth auth = (FormSercurityAuth) win;
            auth.makeReportVerify(ex.getLocalizedMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
