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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import org.anyframe.ide.command.common.util.CommonConstants;

/**
 * This is an FileUtil class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class FileUtil {
    public static String SLASH = ProjectUtil.SLASH;

    public static File[] dirListByAscAlphabet(File folder) {

        if (!folder.isDirectory()) {
            return null;
        }

        File[] files = folder.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return !name.startsWith(".");
            }
        });

        Arrays.sort(files, new Comparator() {
            public int compare(final Object o1, final Object o2) {
                return ((File) o1).getName().toLowerCase()
                    .compareTo(((File) o2).getName().toLowerCase());
            }
        });

        return files;
    }

    public static boolean validateTemplatePath(File templateHome) {
        File path = new File(templateHome.getAbsolutePath() + SLASH + "source");
        File templateConfigPath =
            new File(path + SLASH + CommonConstants.TEMPLATE_CONFIG_FILE);

        if (path.exists()) {
            if (templateConfigPath.exists())
                return true;
        }
        return false;
    }
}
