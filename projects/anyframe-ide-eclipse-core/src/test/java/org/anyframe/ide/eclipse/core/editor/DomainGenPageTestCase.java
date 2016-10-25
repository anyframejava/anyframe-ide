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
package org.anyframe.ide.eclipse.core.editor;

import java.io.File;

import junit.framework.TestCase;

import org.anyframe.ide.eclipse.core.model.tree.ITreeModel;
import org.anyframe.ide.eclipse.core.model.tree.SimpleTreeNode;
import org.anyframe.ide.eclipse.core.util.ExceptionUtil;
import org.anyframe.ide.eclipse.core.util.MessageUtil;
import org.eclipse.core.runtime.IStatus;

import org.anyframe.ide.command.common.util.PropertiesIO;

/**
 * This is a DomainGenPageTestCase class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class DomainGenPageTestCase extends TestCase {

    private String sourcePath = "./src/test/resources/emarketplace/sample/";
    private static PropertiesIO pjtProps;
    private static String SLASH = System.getProperty("file.separator");

    public void testDomainGenPage() throws Exception {
        pjtProps =
            new PropertiesIO("./src/test/resources" + SLASH + "META-INF"
                + SLASH + "project.mf");

        // make source package
        getSourcePackageModel(sourcePath, true);
        // make table model
        getTableModel();
    }

    private ITreeModel getSourcePackageModel(String sourceFolder,
            boolean topNode) {
        if (sourceFolder == null) {
            return new SimpleTreeNode("It needs more than one source package.");
        }

        File sourceFolderFile = new File(sourceFolder);
        SimpleTreeNode model =
            new SimpleTreeNode(sourceFolderFile.getName(), sourceFolderFile);

        File[] childFiles = sourceFolderFile.listFiles();
        for (int i = 0; i < childFiles.length; i++) {
            File childFile = childFiles[i];
            if (childFile.isDirectory() && !childFile.getName().startsWith(".")) {
                model.addChild(getSourcePackageModel(childFile
                    .getAbsolutePath(), false));
                System.out.println(childFile.getName());
            }
        }
        return model;
    }

    private ITreeModel getTableModel() {
        String schemaName = pjtProps.readValue("db.schema");
        SimpleTreeNode model = new SimpleTreeNode("catalog", schemaName);
        if (schemaName != null) {
            try {
                String rootName =
                    schemaName.length() == 0 ? "Tables(No Schema)" : schemaName;
                SimpleTreeNode schemaModel =
                    new SimpleTreeNode(rootName, schemaName);
                model.addChild(schemaModel);
                System.out.println(schemaModel.getName());
            } catch (Exception e) {
                ExceptionUtil.showException(MessageUtil
                    .getMessage("editor.exception.loadconfig"), IStatus.ERROR,
                    e);
            }
        }
        return model;
    }
}
