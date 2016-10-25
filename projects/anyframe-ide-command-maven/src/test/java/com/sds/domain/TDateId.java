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
 * This is an TDateId class.
 * @author Sooyeon Park
 */
@Embeddable
public class TDateId implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long tId1;
    private Long tId2;

    @Column(name = "T_ID1", nullable = false)
    public Long getTId1() {
        return this.tId1;
    }

    public void setTId1(Long tId1) {
        this.tId1 = tId1;
    }

    @Column(name = "T_ID2", nullable = false)
    public Long getTId2() {
        return this.tId2;
    }

    public void setTId2(Long tId2) {
        this.tId2 = tId2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        TDateId pojo = (TDateId) o;

        if ((tId1 != null) ? (!tId1.equals(pojo.tId1)) : (pojo.tId1 != null)) {
            return false;
        }

        if ((tId2 != null) ? (!tId2.equals(pojo.tId2)) : (pojo.tId2 != null)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 0;
        result = (31 * result) + ((tId1 != null) ? tId1.hashCode() : 0);
        result = ((tId2 != null) ? tId2.hashCode() : 0);

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());

        sb.append(" [");
        sb.append("tId1").append("='").append(getTId1()).append("', ");
        sb.append("tId2").append("='").append(getTId2()).append("'");
        sb.append("]");

        return sb.toString();
    }
}
