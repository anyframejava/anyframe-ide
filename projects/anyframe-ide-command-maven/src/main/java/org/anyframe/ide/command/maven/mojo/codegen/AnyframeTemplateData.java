/*   
 * Copyright 2002-2009 the original author or authors.   
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
 * This is an AnyframeTemplateData class.
 * @author Sooyeon Park
 */

@XStreamAlias("template")
public class AnyframeTemplateData {

    private String type = "";
    private String generic = "";
    private String ftl = "";
    private String src = "";
    private String mergeSrc = "";
    private String mergeKey = "";
    private boolean share = false;
    private String dao = "";
    private String framework = "";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getGeneric() {
        return generic;
    }

    public void setGeneric(String generic) {
        this.generic = generic;
    }

    public String getFtl() {
        return ftl;
    }

    public void setFtl(String ftl) {
        this.ftl = ftl;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

	public String getMergeSrc() {
		return mergeSrc;
	}

	public void setMergeSrc(String mergeSrc) {
		this.mergeSrc = mergeSrc;
	}

	public String getMergeKey() {
		return mergeKey;
	}

	public void setMergeKey(String mergeKey) {
		this.mergeKey = mergeKey;
	}

	public boolean isShare() {
        return share;
    }

    public void setShare(boolean share) {
        this.share = share;
    }

    public String getDao() {
        return dao;
    }

    public void setDao(String dao) {
        this.dao = dao;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

}
