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

import org.anyframe.ide.eclipse.core.util.DatabaseUtil;

import junit.framework.TestCase;
import org.anyframe.ide.command.common.util.PropertiesIO;

/**
 * This is a DatabaseUtilTestCase class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class DatabaseUtilTestCase extends TestCase {

    private static PropertiesIO pjtProps;
    private static String SLASH = System.getProperty("file.separator");

    public void testCheckConnection() throws Exception {
        pjtProps =
            new PropertiesIO("./src/test/resources" + SLASH + "META-INF"
                + SLASH + "project.mf");

        System.out.println(DatabaseUtil.getDbUrl(pjtProps.readValue("db.type"),
            pjtProps.readValue("db.name"), pjtProps.readValue("db.server"),
            pjtProps.readValue("db.port")));

    }
}
