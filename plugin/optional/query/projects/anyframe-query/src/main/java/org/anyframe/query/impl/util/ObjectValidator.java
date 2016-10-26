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
package org.anyframe.query.impl.util;

/**
 * Define the function of an object validator.<br />
 * This will be used when an object is constructed with the available data to
 * check that the object is correct - for example 2 target classes may be
 * specified, both those classes will be constructed from the available data and
 * an implementation of this interface will determine which of the result
 * objects will be included in the return result.
 * 
 * 
 * @author Warren Mayocchi
 * 
 * @author modified by SoYon Lim
 * @author modified by JongHoon Kim
 */
public interface ObjectValidator {

	/**
	 * Returns whether the constructed object is valid.
	 * 
	 * @param object
	 *            The Object to validate.
	 * @return true = valid object; false = invalid object.
	 */
	boolean isValid(Object object);
}
