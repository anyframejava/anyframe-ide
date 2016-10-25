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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * This is an Forums class.
 * @author Sooyeon Park
 */
@Entity
@Table(name = "FORUMS", schema = "PUBLIC")
public class Forums implements Serializable {

    private static final long serialVersionUID = -6997800295867457285L;
    private ForumsId id;
    private SDate sDate;
    private Categories categories;
    private String forumName;
    private String forumDesc;
    private Long forumOrder;
    private Long forumTopics;
    private Long forumLastPostId;
    private Long moderated;

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "forumId", column = @Column(name = "FORUM_ID", nullable = false)),
        @AttributeOverride(name = "categoriesId1", column = @Column(name = "CATEGORIES_ID1", nullable = false)),
        @AttributeOverride(name = "s1Id", column = @Column(name = "S1_ID", nullable = false)),
        @AttributeOverride(name = "s2Id", column = @Column(name = "S2_ID", nullable = false)),
        @AttributeOverride(name = "TId1", column = @Column(name = "T_ID1", nullable = false)),
        @AttributeOverride(name = "TId2", column = @Column(name = "T_ID2", nullable = false)) })
    public ForumsId getId() {
        return this.id;
    }

    public void setId(ForumsId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "S1_ID", referencedColumnName = "S_ID1", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "S2_ID", referencedColumnName = "S_ID2", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "T_ID1", referencedColumnName = "T_ID1", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "T_ID2", referencedColumnName = "T_ID2", nullable = false, insertable = false, updatable = false) })
    public SDate getSDate() {
        return this.sDate;
    }

    public void setSDate(SDate sDate) {
        this.sDate = sDate;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORIES_ID1", nullable = false, insertable = false, updatable = false)
    public Categories getCategories() {
        return this.categories;
    }

    public void setCategories(Categories categories) {
        this.categories = categories;
    }

    @Column(name = "FORUM_NAME", nullable = false, length = 150)
    public String getForumName() {
        return this.forumName;
    }

    public void setForumName(String forumName) {
        this.forumName = forumName;
    }

    @Column(name = "FORUM_DESC", length = 0)
    public String getForumDesc() {
        return this.forumDesc;
    }

    public void setForumDesc(String forumDesc) {
        this.forumDesc = forumDesc;
    }

    @Column(name = "FORUM_ORDER")
    public Long getForumOrder() {
        return this.forumOrder;
    }

    public void setForumOrder(Long forumOrder) {
        this.forumOrder = forumOrder;
    }

    @Column(name = "FORUM_TOPICS", nullable = false)
    public Long getForumTopics() {
        return this.forumTopics;
    }

    public void setForumTopics(Long forumTopics) {
        this.forumTopics = forumTopics;
    }

    @Column(name = "FORUM_LAST_POST_ID", nullable = false)
    public Long getForumLastPostId() {
        return this.forumLastPostId;
    }

    public void setForumLastPostId(Long forumLastPostId) {
        this.forumLastPostId = forumLastPostId;
    }

    @Column(name = "MODERATED")
    public Long getModerated() {
        return this.moderated;
    }

    public void setModerated(Long moderated) {
        this.moderated = moderated;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        Forums pojo = (Forums) o;

        if ((forumName != null)
            ? (!forumName.equals(pojo.forumName)) : (pojo.forumName != null)) {
            return false;
        }

        if ((forumDesc != null)
            ? (!forumDesc.equals(pojo.forumDesc)) : (pojo.forumDesc != null)) {
            return false;
        }

        if ((forumOrder != null)
            ? (!forumOrder.equals(pojo.forumOrder)) : (pojo.forumOrder != null)) {
            return false;
        }

        if ((forumTopics != null)
            ? (!forumTopics.equals(pojo.forumTopics))
            : (pojo.forumTopics != null)) {
            return false;
        }

        if ((forumLastPostId != null) ? (!forumLastPostId
            .equals(pojo.forumLastPostId)) : (pojo.forumLastPostId != null)) {
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
        result =
            (31 * result) + ((forumName != null) ? forumName.hashCode() : 0);
        result =
            (31 * result) + ((forumDesc != null) ? forumDesc.hashCode() : 0);
        result =
            (31 * result) + ((forumOrder != null) ? forumOrder.hashCode() : 0);
        result =
            (31 * result)
                + ((forumTopics != null) ? forumTopics.hashCode() : 0);
        result =
            (31 * result)
                + ((forumLastPostId != null) ? forumLastPostId.hashCode() : 0);
        result =
            (31 * result) + ((moderated != null) ? moderated.hashCode() : 0);

        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(getClass().getSimpleName());

        sb.append(" [");
        sb.append("id").append("='").append(getId()).append("', ");

        sb.append("forumName").append("='").append(getForumName())
            .append("', ");
        sb.append("forumDesc").append("='").append(getForumDesc())
            .append("', ");
        sb.append("forumOrder").append("='").append(getForumOrder()).append(
            "', ");
        sb.append("forumTopics").append("='").append(getForumTopics()).append(
            "', ");
        sb.append("forumLastPostId").append("='").append(getForumLastPostId())
            .append("', ");
        sb.append("moderated").append("='").append(getModerated()).append("'");
        sb.append("]");

        return sb.toString();
    }
}
