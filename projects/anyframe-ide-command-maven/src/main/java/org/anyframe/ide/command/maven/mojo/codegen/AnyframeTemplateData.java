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
package org.anyframe.ide.command.maven.mojo.codegen;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is an AnyframeTemplateData class which is a Value Object to express
 * template.config file.
 * 
 * @author Sooyeon Park
 */

@XStreamAlias("template")
public class AnyframeTemplateData {

	private String type = "";
	private String vm = "";
	private String src = "";
	private String common = "";
	private String merge = "";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVm() {
		return vm;
	}

	public void setVm(String vm) {
		this.vm = vm;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getCommon() {
		return common;
	}

	public void setCommon(String common) {
		this.common = common;
	}

	public String getMerge() {
		return merge;
	}

	public void setMerge(String merge) {
		this.merge = merge;
	}

	public String toString() {
		return "AnyframeTemplateData [type=" + type + ", vm=" + vm + ", src=" + src + ", common=" + common + ", merge=" + merge + "]";
	}

}
