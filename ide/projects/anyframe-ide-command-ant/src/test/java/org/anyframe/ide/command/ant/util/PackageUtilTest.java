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
package org.anyframe.ide.command.ant.util;

import java.io.File;

import org.anyframe.ide.command.ant.util.PackageUtil;


import junit.framework.TestCase;

/**
 * This is an PackageUtilTest class.
 * 
 * @author Sooyeon Park
 */
public class PackageUtilTest extends TestCase {

    public void testGetClassesInPackage() throws Exception {
        PackageUtil util = new PackageUtil();
        File directory = new File("src/main/java");
        System.out.println(util.getClassesInPackage("", directory));
    }
}
