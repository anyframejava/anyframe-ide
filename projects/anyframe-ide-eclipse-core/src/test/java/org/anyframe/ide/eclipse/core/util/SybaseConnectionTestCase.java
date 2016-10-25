/*   
 * Copyright 2008-2011 the original author or authors.   
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
import java.util.Properties;

import junit.framework.TestCase;

/**
 * This is an SybaseConnectionTestCase class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class SybaseConnectionTestCase extends TestCase {

    public static Driver getDriverFromPath(String path, String className)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, MalformedURLException {
        URL[] url = {new File(path).toURL() };

        URLClassLoader loader = new URLClassLoader(url);

        Class<?> c = loader.loadClass(className);
        Object instance = c.newInstance();

        return (Driver) instance;
    }

    private static Connection getConnection(String driverJarName,
            String dbDriver, String dbUrl, String userName, String password)
            throws Exception {
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);

        try {
            Driver driver = getDriverFromPath(driverJarName, dbDriver);
            return driver.connect(dbUrl, connectionProps);
        } catch (Exception e) {
            throw e;
        }
    }

    private static Connection getConnection() throws Exception {

        // String driverJarName =
        // "C:\\kyobo\\sybase\\jconn3.jar";
        // String dbDriver
        // ="com.sybase.jdbc3.jdbc.SybDataSource";
        // String userName = "cti";
        // String password = "ctiuse";
        //
        // String dbUrl
        // ="jdbc:sybase:Tds:129.100.254.241:3000?ServiceName=cti";

        String driverJarName = "E:\\anyframe-step\\jdbc\\jconn3.jar";
        String dbDriver = "com.sybase.jdbc3.jdbc.SybDataSource";
        String userName = "dba";
        String password = "sql";

        String dbUrl = "jdbc:sybase:Tds:70.7.105.232:4747?ServiceName=sampledb";

        return getConnection(driverJarName, dbDriver, dbUrl, userName, password);
    }

    public void testSybaseConnection() throws Exception {
   
        // if sybase db is ready, you can test this testcase.
//        Connection con = getConnection();
//        System.out.println("con" + con + ",ct=" + con.getCatalog());
//        DatabaseMetaData md = con.getMetaData();
//        ResultSet rs =
//            md.getTables(con.getCatalog(), null, null, new String[] {"TABLE" });
//        // DatabaseUtil.getRsList(rs);
//
//        while (rs.next()) {
//            System.out.println("   " + ", " + rs.getString("TABLE_NAME"));
//        }
//        
//        assertEquals("dba", md.getUserName());
//        
//        /*
//         * ResultSet rs = md.getSchemas(); while
//         * (rs.next()) { System.out.println( " " + ",
//         * "+rs.getString("TABLE_SCHEM") ); }
//         */
//        con.close();
    }
}
