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
package org.anyframe.ide.command.common.util;

import java.util.Iterator;
import java.util.Map;

import org.anyframe.ide.command.common.plugin.PluginInfo;
import org.apache.commons.collections.map.ListOrderedMap;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Converts java.util.HashMap<String, PluginInfo> to XML, specifying a nested
 * element for each item.
 * <p/>
 * <p>
 * Supports java.util.HashMap
 * </p>
 * 
 * @author Joe Walnes
 * @author modified by Soyon Lim (modify CollectionConverter of XStream)
 */
public class PluginMapConverter extends AbstractCollectionConverter {

	public PluginMapConverter(Mapper mapper) {
		super(mapper);
	}

	public boolean canConvert(Class type) {
		return type.equals(ListOrderedMap.class);
	}

	public void marshal(Object source, HierarchicalStreamWriter writer,
			MarshallingContext context) {
		Map map = (Map) source;

		Iterator keyItr = map.keySet().iterator();
		while (keyItr.hasNext()) {
			Object item = map.get(keyItr.next());
			writeItem(item, context, writer);
		}
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext context) {
		Map map = new ListOrderedMap();
		populateCollection(reader, context, map);
		return map;
	}

	protected void populateCollection(HierarchicalStreamReader reader,
			UnmarshallingContext context, Map map) {
		while (reader.hasMoreChildren()) {
			reader.moveDown();
			PluginInfo item = (PluginInfo) readItem(reader, context, map);
			map.put(item.getName(), item);
			reader.moveUp();
		}
	}
}
