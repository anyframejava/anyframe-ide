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
package org.anyframe.cxf.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CXFMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class CXFMap {
	@XmlElement(nillable = false, name = "entry")
	private final List<CXFEntry> entries = new ArrayList<CXFEntry>();

	public List<CXFEntry> getEntries() {
		return entries;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "IdentifiedCXF")
	static class CXFEntry {
		// Map keys cannot be null
		@XmlElement(required = true, nillable = false)
		private String key;

		private Object value;

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}
}
