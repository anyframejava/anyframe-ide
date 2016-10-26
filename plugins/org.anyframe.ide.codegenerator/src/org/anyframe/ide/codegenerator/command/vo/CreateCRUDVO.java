/*   
 * Copyright 2002-2013 the original author or authors.   
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
package org.anyframe.ide.codegenerator.command.vo;

/**
 * This is a CreateCRUDVO class.
 * 
 * @author Sooyeon Park
 */
public class CreateCRUDVO extends CommandVO {

	private String domainClassName = "";
	private String scope = "";
	private String templateType = "";
	private String insertSampleData = "";

	public String getInsertSampleData() {
		return insertSampleData;
	}

	public void setInsertSampleData(String insertSampleData) {
		this.insertSampleData = insertSampleData;
	}

	public String getDomainClassName() {
		return domainClassName;
	}

	public void setDomainClassName(String domainClassName) {
		this.domainClassName = domainClassName;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getTemplateType() {
		return templateType;
	}

	public void setTemplateType(String templateType) {
		this.templateType = templateType;
	}
}
