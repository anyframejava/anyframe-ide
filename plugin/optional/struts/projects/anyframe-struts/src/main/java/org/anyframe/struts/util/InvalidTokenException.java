/*
 * Copyright 2007-2012 the original author or authors.
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
package org.anyframe.struts.util;

import org.apache.struts.util.ModuleException;

/**
 * 
 * Sub Class of InvalidTokenException which is occurred when the request has not valid token.
 * 
 * @author Byunghun Woo
 */
public class InvalidTokenException extends ModuleException {

	private static final long serialVersionUID = 4744568600373321051L;

	private String exceptionKey;

    private Throwable exception;

    /**
	 * @return the exceptionKey
	 */
	public String getExceptionKey() {
		return exceptionKey;
	}

	/**
	 * @return the exception
	 */
	public Throwable getException() {
		return exception;
	}

	public InvalidTokenException(String exceptionKey) {
        super(exceptionKey);
        this.exceptionKey = exceptionKey;
    }

    public InvalidTokenException(String exceptionKey, Throwable exception) {
        this(exceptionKey);
        this.exception = exception;
    }
}