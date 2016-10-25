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
package org.anyframe.ide.eclipse.core.model.table;

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.anyframe.ide.command.common.DefaultPluginInfoManager;
import org.anyframe.ide.command.common.plugin.PluginInfo;

/**
 * This is a PluginInfoListTestCase class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
public class PluginInfoListTestCase extends TestCase {

    private Map<String, PluginInfo> pluginList;

    /**
     * [Flow #-1] Positive Case : find all installed
     * plugin list.
     * @throws Exception
     */
    public void testGetInstalledPluginTypeList() throws Exception {

        DefaultPluginInfoManager pluginInfoManager =
            new DefaultPluginInfoManager();

        pluginList =
            pluginInfoManager
                .getInstalledPlugins("./src/test/resources/emarketplace/");

        Iterator<String> itr = pluginList.keySet().iterator();
        while (itr.hasNext()) {
            PluginInfo info = pluginList.get(itr.next());
            System.out.println("InstalledPlugin :: " + info.getName());
        }
    }
}
