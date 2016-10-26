/*   
 * Copyright 2008-2012 the original author or authors.   
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");   
 * you may not use this file except in compliance with the License.   
 * You may obtain a copy of the License at   
 *   
 *      http://www.apache.org/licenses/LICENSE-2.0   
 *   
 * Unless required by applicable law or agreed to in writing, software   
 * distributed under the License is distributed on an "AS IS" BASIS,   
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   
 * See the License for the specific language governing permissions and   
 * limitations under the License.   
 */
package org.anyframe.ide.eclipse.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;

/**
 * This is an DatabaseUtil class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class DatabaseUtil {

    protected DatabaseUtil() {
        throw new UnsupportedOperationException(); // prevents
        // calls
        // from
        // subclass
    }

    private static final String DB_HSQL_FILE_URL =
        "jdbc:hsqldb:file:<database_name>";
    private static final String DB_HSQL_SERVER_URL =
        "jdbc:hsqldb:hsql://<server>/<database_name>";
    private static final String DB_HSQL_SERVER_PORT_URL =
        "jdbc:hsqldb:hsql://<server>:<port>/<database_name>";
    private static final String DB_ORACLE_URL =
        "jdbc:oracle:thin:@<server>:<port>:<database_name>";
    private static final String DB_MYSQL_URL =
        "jdbc:mysql://<server>:<port>/<database_name>";
    private static final String DB_SYBASE_URL =
        "jdbc:sybase:Tds:<server>:<port>?ServiceName=<database_name>";
    private static final String DB_DB2_URL =
        "jdbc:db2://<server>:<port>/<database_name>";
    private static final String DB_MSSQL_URL = 
        "jdbc:sqlserver://<server>:<port>;DatabaseName=<database_name>";

    public static final String DB_CON_CHK_KEY = "DB_CON_CHK";
    public static final String DB_CON_MSG_KEY = "DB_CON_MSG";
    private static final boolean DB_CON_CHK = false;
    private static final String DB_CON_MSG = "";

    /*
     * public static Map<Object,Object>
     * checkConnection(AnyframeConfig anyframeConfig) {
     * String driverJarName =
     * anyframeConfig.getJdbcDriverJar(); String
     * dbDriver = anyframeConfig.getJdbcDriverName();
     * String userName =
     * anyframeConfig.getDbUsername(); String password
     * = anyframeConfig.getDbPassword(); String dbUrl =
     * getDbUrl(anyframeConfig); return
     * checkConnection(
     * driverJarName,dbDriver,dbUrl,userName,password);
     * }
     */
    public static Map<Object, Object> checkConnection(String projectHome, String driverJarName,
            String dbDriver, String dbUrl, String userName, String password) {
        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put(DB_CON_CHK_KEY, DB_CON_CHK);
        result.put(DB_CON_MSG_KEY, DB_CON_MSG);
        Connection connection = null;
        try {
            connection =
                getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName,
                    password);
        } catch (Exception e) {
            result.put(DB_CON_MSG_KEY, e.getMessage());
            ExceptionUtil
                .showException(MessageUtil
                    .getMessage("editor.exception.getconnection"),
                    IStatus.ERROR, e);
        } finally {
            if (connection != null)
                result.put(DB_CON_CHK_KEY, true);
            close(connection);
        }
        return result;
    }

    /*
     * private static Connection
     * getConnection(AnyframeConfig anyframeConfig)
     * throws Exception { String driverJarName =
     * anyframeConfig.getJdbcDriverJar(); String
     * dbDriver = anyframeConfig.getJdbcDriverName();
     * String userName =
     * anyframeConfig.getDbUsername(); String password
     * = anyframeConfig.getDbPassword(); String dbUrl =
     * getDbUrl(anyframeConfig); // String dbUrl //=
     * "jdbc:sybase:Tds:129.100.254.241:3000?ServiceName=cti"
     * ; return getConnection(driverJarName, dbDriver,
     * dbUrl, userName, password); }
     */
    private static Connection getConnection(String projectHome, String driverJarName,
            String dbDriver, String dbUrl, String userName, String password)
            throws Exception {
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        try {
            Driver driver = getDriverFromPath(projectHome, driverJarName, dbDriver);
            return driver.connect(dbUrl, connectionProps);
        } catch (Exception e) {
            ExceptionUtil
                .showException(MessageUtil
                    .getMessage("editor.exception.getconnection"),
                    IStatus.ERROR, e);
            throw e;
        }
    }

    public static void close(Connection connection) {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("editor.exception.closeconnection"), IStatus.ERROR,
                e);
        }
    }

    /*
     * public static String getDbUrl(AnyframeConfig
     * anyframeConfig) { String dbType =
     * anyframeConfig.getDbType(); String dbName =
     * anyframeConfig.getDbName(); String dbServer =
     * anyframeConfig.getDbServer(); String dbPort =
     * String.valueOf(anyframeConfig.getDbPort());
     * return getDbUrl(dbType, dbName, dbServer,
     * dbPort); }
     */
    public static String getDbUrl(String dbType, String dbName,
            String dbServer, String dbPort) {
        String dbUrl = "";
        if (dbType.equals("oracle"))
            dbUrl = DB_ORACLE_URL;
        else if (dbType.equals("hsqldb") && dbServer.equals("file"))
            dbUrl = DB_HSQL_FILE_URL;
        else if (dbType.equals("hsqldb") && dbPort.equals("-1"))
            dbUrl = DB_HSQL_SERVER_URL;
        else if (dbType.equals("hsqldb") && !dbPort.equals("-1"))
            dbUrl = DB_HSQL_SERVER_PORT_URL;
        else if (dbType.equals("mysql"))
            dbUrl = DB_MYSQL_URL;
        else if (dbType.equals("sybase"))
            dbUrl = DB_SYBASE_URL;
        else if (dbType.equals("db2"))
            dbUrl = DB_DB2_URL;
        else if (dbType.equals("mssql"))
            dbUrl = DB_MSSQL_URL;

        dbUrl = dbUrl.replace("<server>", dbServer);
        dbUrl = dbUrl.replace("<port>", dbPort);
        dbUrl = dbUrl.replace("<database_name>", dbName);

        return dbUrl;
    }

    public static Driver getDriverFromPath(String projectHome, String path, String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, MalformedURLException {
        File dbLibFile = new File(path);
        if (!dbLibFile.exists()) {
                dbLibFile = new File(projectHome, path);
        }
        
        URL[] url = {dbLibFile.toURL() };

        URLClassLoader loader = new URLClassLoader(url);

        Class<?> c = loader.loadClass(className);
        Object instance = c.newInstance();

        return (Driver) instance;
    }

    public static synchronized String[] getTables(String projectHome, String driverJarName,
            String dbDriver, String dbUrl, String userName, String password,
            String dbType, String schemaName) throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn =
                getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName,
                    password);
            // ".*", ".BIN"
            String tableNamePattern = null;
            if (dbType.equals("sybase"))
                schemaName = null;
            rs =
                conn.getMetaData().getTables(conn.getCatalog(), schemaName,
                    tableNamePattern, new String[] {"TABLE" });
            return getRsList(rs, "TABLE_NAME");
        } catch (Exception e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("editor.exception.gettable"), IStatus.ERROR, e);
            throw e;
        } finally {
            close(conn);
            if (rs != null)
                rs.close();
        }
    }

    public static synchronized String[] getSchemas(String projectHome, String driverJarName,
            String dbDriver, String dbUrl, String userName, String password)
            throws Exception {
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn =
                getConnection(projectHome, driverJarName, dbDriver, dbUrl, userName,
                    password);
            rs = conn.getMetaData().getSchemas();
            return getRsList(rs, "TABLE_SCHEM");
        } catch (Exception e) {
            ExceptionUtil.showException(MessageUtil
                .getMessage("editor.exception.getschema"), IStatus.ERROR, e);
            throw e;
        } finally {
            close(conn);
            if (rs != null)
                rs.close();
        }
    }

    private static synchronized String[] getRsList(ResultSet rs,
            String columnName) throws SQLException {
        final ArrayList<String> list = new ArrayList<String>();
        try {
            if (rs != null) {
                while (rs.next()) {
                    list.add((String) rs.getString(columnName));
                }
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }
        return list.toArray(new String[list.size()]);
    }

}
