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
package org.anyframe.ide.eclipse.core.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This is an JdbcType class.
 * @author Changje Kim
 * @author Sooyeon Park
 */
@XStreamAlias("jdbcType")
public class JdbcType {
    private String type;
    private String driver;
    private String[] dialect;
    private String port;
    private String driverGroupId;   
    private String driverArtifactId;
    private String driverVersion;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String[] getDialect() {
        return dialect;
    }

    public void setDialect(String[] dialect) {
        this.dialect = dialect;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    public String getDriverGroupId() {
        return driverGroupId;
    }

    public void setDriverGroupId(String driverGroupId) {
        this.driverGroupId = driverGroupId;
    }

    public String getDriverArtifactId() {
        return driverArtifactId;
    }

    public void setDriverArtifactId(String driverArtifactId) {
        this.driverArtifactId = driverArtifactId;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    public void setDriverVersion(String driverVersion) {
        this.driverVersion = driverVersion;
    }

}
