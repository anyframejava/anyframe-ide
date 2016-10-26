/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.querymanager.model;

/**
 * The class QueryInputAttribute contains complete information abt Input
 * Parameters of query
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QueryInputAttribute {

	// Serial Number of Input Parameter table in wizard
	// page
	private int no;

	// Parameter Name
	private String name;

	// Parameter Data Type
	private String type;

	// Binding Value
	private String binding;

	// Test data for parameter
	private String test;

	/**
	 * getter
	 * 
	 * @return test
	 */
	public String getTest() {
		return test;
	}

	/**
	 * getter
	 * 
	 * @return binding
	 */
	public String getBinding() {
		return binding;
	}

	/**
	 * getter
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * getter
	 * 
	 * @return no
	 */
	public int getNo() {
		return no;
	}

	/**
	 * getter
	 * 
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * setter
	 * 
	 * @param test
	 *            string
	 */
	public void setTest(String test) {
		this.test = test;
	}

	/**
	 * setter
	 * 
	 * @param binding
	 *            string
	 */
	public void setBinding(String binding) {
		this.binding = binding;
	}

	/**
	 * setter
	 * 
	 * @param name
	 *            string
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * setter
	 * 
	 * @param no
	 *            integer
	 */
	public void setNo(int no) {
		this.no = no;
	}

	/**
	 * setter
	 * 
	 * @param type
	 *            string
	 */
	public void setType(String type) {
		this.type = type;
	}

}
