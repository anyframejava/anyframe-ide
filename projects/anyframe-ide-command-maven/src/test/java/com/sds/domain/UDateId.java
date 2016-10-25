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
package com.sds.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * This is an UDateId class.
 * @author Sooyeon Park
 */
@Embeddable
public class UDateId implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long uId1;
    private Long uId2;

    @Column(name = "U_ID1", nullable = false)
    public Long getUId1() {
        return this.uId1;
    }

    public void setUId1(Long uId1) {
        this.uId1 = uId1;
    }

    @Column(name = "U_ID2", nullable = false)
    public Long getUId2() {
        return this.uId2;
    }

    public void setUId2(Long uId2) {
        this.uId2 = uId2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        UDateId pojo = (UDateId) o;

        if ((uId1 != null) ? (!uId1.equals(pojo.uId1)) : (pojo.uId1 != null)) {
            return false;
        }

        if ((uId2 != null) ? (!uId2.equals(pojo.uId2)) : (pojo.uId2 != null)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 0;
        result = (31 * result) + ((uId1 != null) ? uId1.hashCode() : 0);
        result = ((uId2 != null) ? uId2.hashCode() : 0);

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());

        sb.append(" [");
        sb.append("uId1").append("='").append(getUId1()).append("', ");
        sb.append("uId2").append("='").append(getUId2()).append("'");
        sb.append("]");

        return sb.toString();
    }
}
