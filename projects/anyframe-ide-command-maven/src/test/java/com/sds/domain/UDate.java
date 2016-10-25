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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * This is an UDate class.
 * @author Sooyeon Park
 */
@Entity
@Table(name = "U_DATE", schema = "PUBLIC")
public class UDate implements Serializable {

    private static final long serialVersionUID = 1L;
    private UDateId id;
    private Set<TDate> tDates = new HashSet<TDate>(0);

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "UId1", column = @Column(name = "U_ID1", nullable = false)),
        @AttributeOverride(name = "UId2", column = @Column(name = "U_ID2", nullable = false)) })
    public UDateId getId() {
        return this.id;
    }

    public void setId(UDateId id) {
        this.id = id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "UDate")
    public Set<TDate> getTDates() {
        return this.tDates;
    }

    public void setTDates(Set<TDate> tDates) {
        this.tDates = tDates;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 0;

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());

        sb.append(" [");
        sb.append("id").append("='").append(getId()).append("', ");

        sb.append("]");

        return sb.toString();
    }
}
