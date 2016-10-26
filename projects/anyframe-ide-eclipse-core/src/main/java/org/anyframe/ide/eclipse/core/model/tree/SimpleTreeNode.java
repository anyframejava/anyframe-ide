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
package org.anyframe.ide.eclipse.core.model.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an SimpleTreeNode class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class SimpleTreeNode implements ITreeModel {
    private ITreeModel parent = null;
    private List<ITreeModel> children;
    private String name;
    private Object element;

    public SimpleTreeNode() {
        children = new ArrayList<ITreeModel>();
    }

    public SimpleTreeNode(Object element) {
        this(null, element);
    }

    public SimpleTreeNode(String name, Object element) {
        this();
        this.name = name;
        this.element = element;
    }

    public ITreeModel getParent() {
        return parent;
    }

    public void setParent(ITreeModel parent) {
        this.parent = parent;
    }

    public void addChild(ITreeModel child) {
        children.add(child);
        child.setParent(this);
    }

    public List<ITreeModel> getChildren() {
        return children;
    }

    public String getName() {
        return this.name;
    }

    public Object getElement() {
        return element;
    }

    public int size() {
        return getChildren().size();
    }

    public String getPath() {
        StringBuffer path = new StringBuffer();
        ITreeModel node = this;
        while (true) {
            if (!path.toString().equals("")) {
                path.insert(0, ".");
            }

            path.insert(0, node.getName());

            node = (ITreeModel) node.getParent();
            if (node == null || node.getParent() == null) {
                break;
            }
        }

        return path.toString();
    }

}
