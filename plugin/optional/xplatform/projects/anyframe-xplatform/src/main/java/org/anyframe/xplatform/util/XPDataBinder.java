/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.xplatform.util;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

import com.tobesoft.xplatform.data.DataSet;
import com.tobesoft.xplatform.data.VariableList;

/**
 * The class expanding the org.springframework.validation.DataBinder
 * <P>
 * As data changing util class, when using the Tobesoft's XPLATFORM's to
 * develop the UI, it includes the method for changing the value of VariableList
 * and DataSet used as data transmitting object.
 * <p>
 * 
 * @author Jonghoon Kim
 */
public class XPDataBinder extends DataBinder {

	/**
	 * convertToCamelCase's default value is false.
	 */
	private boolean convertToCamelCase = false;

	/**
	 * XPDataBinder's constructor.
	 * 
	 * @param target
	 *            Value Object
	 */
	public XPDataBinder(Object target) {
		this(target, false);
	}

	/**
	 * XPDataBinder's constructor.
	 * 
	 * @param target
	 *            Value Object
	 * @param convertToCamenCase
	 *            When changing to CamelCase the attribute name, then true
	 */
	public XPDataBinder(Object target, boolean convertToCamelCase) {
		super(target);
		this.convertToCamelCase = convertToCamelCase;
	}

	/**
	 * Maps to VariableList the VO's value set by using the XPDataBinder
	 * constructor.
	 * 
	 * @param variableList
	 *            VariableList
	 */
	public void bind(VariableList variableList) {
		MutablePropertyValues mpvs = new VariableListPropertyValues(
				variableList, convertToCamelCase);
		doBind(mpvs);
	}

	/**
	 * Maps to DataSet the VO's value set using the XPDataBinder constructor
	 * 
	 * @param dataList
	 *            XPLATFORM DataSet
	 * @param rowNum
	 *            the row number of record
	 */
	public void bind(DataSet dataList, int rowNum) {
		MutablePropertyValues mpvs = new VariableListPropertyValues(dataList,
				rowNum, convertToCamelCase);
		doBind(mpvs);
	}

	/**
	 * Maps to the DataSet the VO's value set using the XPPDataBinder
	 * constructor If delete record, the isDeleted is true.
	 * 
	 * @param dataList
	 *            XPLATFORM DataSet
	 * @param rowNum
	 *            record's row number
	 * @param isDeleted
	 *            when delete record, then true
	 */
	public void bind(DataSet dataList, int rowNum, boolean isDeleted) {
		MutablePropertyValues mpvs = new VariableListPropertyValues(dataList,
				rowNum, isDeleted, convertToCamelCase);
		doBind(mpvs);
	}
}
