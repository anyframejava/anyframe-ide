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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * This is an Categories class.
 * @author Sooyeon Park
 */
@Entity
@Table(name = "CATEGORIES", schema = "PUBLIC")
public class Categories implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long cId1;
    private String title;
    private Long displayOrder;
    private Long moderated;
    private Set<Forums> forumses = new HashSet<Forums>(0);

    @Id
    @Column(name = "C_ID1", unique = true, nullable = false)
    public Long getCId1() {
        return this.cId1;
    }

    public void setCId1(Long cId1) {
        this.cId1 = cId1;
    }

    @Column(name = "TITLE", nullable = false, length = 100)
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "DISPLAY_ORDER", nullable = false)
    public Long getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(Long displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Column(name = "MODERATED")
    public Long getModerated() {
        return this.moderated;
    }

    public void setModerated(Long moderated) {
        this.moderated = moderated;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "categories")
    public Set<Forums> getForumses() {
        return this.forumses;
    }

    public void setForumses(Set<Forums> forumses) {
        this.forumses = forumses;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        Categories pojo = (Categories) o;

        if ((title != null)
            ? (!title.equals(pojo.title)) : (pojo.title != null)) {
            return false;
        }

        if ((displayOrder != null)
            ? (!displayOrder.equals(pojo.displayOrder))
            : (pojo.displayOrder != null)) {
            return false;
        }

        if ((moderated != null)
            ? (!moderated.equals(pojo.moderated)) : (pojo.moderated != null)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result = 0;
        result = ((title != null) ? title.hashCode() : 0);
        result =
            (31 * result)
                + ((displayOrder != null) ? displayOrder.hashCode() : 0);
        result =
            (31 * result) + ((moderated != null) ? moderated.hashCode() : 0);

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());

        sb.append(" [");
        sb.append("cId1").append("='").append(getCId1()).append("', ");
        sb.append("title").append("='").append(getTitle()).append("', ");
        sb.append("displayOrder").append("='").append(getDisplayOrder())
            .append("', ");
        sb.append("moderated").append("='").append(getModerated())
            .append("', ");

        sb.append("]");

        return sb.toString();
    }
}
