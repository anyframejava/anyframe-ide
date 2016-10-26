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
package org.anyframe.ide.codegenerator.config;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is an AnyframeConfig class.
 * 
 * @author Changje Kim
 * @author Sooyeon Park
 */
@XStreamAlias("config")
public class AnyframeConfig implements Cloneable {

	private String pjtName = "";
	private List<String> ctipUrlList = new ArrayList<String>();

	public AnyframeConfig() {
		ctipUrlList.add("localhost - http://localhost:9090");
	}

	public List<String> getCtipUrlList() {
		return ctipUrlList;
	}

	public void setCtipUrlList(List<String> ctipUrlList) {
		this.ctipUrlList = ctipUrlList;
	}

	public String getPjtName() {
		return pjtName;
	}

	public void setPjtName(String pjtName) {
		this.pjtName = pjtName;
	}
}
