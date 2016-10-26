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
package org.anyframe.ide.command.ant.logger;

import org.codehaus.plexus.logging.AbstractLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an PlexusLogger class.
 * @author Sooyeon Park
 */
public class PlexusLogger extends AbstractLogger {

    private Logger logger = LoggerFactory.getLogger(PlexusLogger.class.getName());

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public PlexusLogger(int threshold, String name) {
        super(threshold, name);
    }

    public void debug(String arg0, Throwable arg1) {
        this.logger.debug(arg0, arg1);
    }

    public void error(String arg0, Throwable arg1) {
        this.logger.error(arg0, arg1);
    }

    public void fatalError(String arg0, Throwable arg1) {
        this.logger.error(arg0, arg1);
    }

    public org.codehaus.plexus.logging.Logger getChildLogger(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

    public void info(String arg0, Throwable arg1) {
        this.logger.info(arg0, arg1);
    }

    public void warn(String arg0, Throwable arg1) {
        this.logger.warn(arg0, arg1);
    }

}
