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
package com.sds.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * This is a ForumsId class.
 * 
 * @author Sooyeon Park
 */

@Embeddable
public class ForumsId implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long forumId;
	private Long categoriesId1;
	private Long s1Id;
	private Long s2Id;
	private Long tId1;
	private Long tId2;

	@Column(name = "FORUM_ID", nullable = false)
	public Long getForumId() {
		return this.forumId;
	}

	public void setForumId(Long forumId) {
		this.forumId = forumId;
	}

	@Column(name = "CATEGORIES_ID1", nullable = false)
	public Long getCategoriesId1() {
		return this.categoriesId1;
	}

	public void setCategoriesId1(Long categoriesId1) {
		this.categoriesId1 = categoriesId1;
	}

	@Column(name = "S1_ID", nullable = false)
	public Long getS1Id() {
		return this.s1Id;
	}

	public void setS1Id(Long s1Id) {
		this.s1Id = s1Id;
	}

	@Column(name = "S2_ID", nullable = false)
	public Long getS2Id() {
		return this.s2Id;
	}

	public void setS2Id(Long s2Id) {
		this.s2Id = s2Id;
	}

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

		ForumsId pojo = (ForumsId) o;

		if ((forumId != null) ? (!forumId.equals(pojo.forumId))
				: (pojo.forumId != null)) {
			return false;
		}

		if ((categoriesId1 != null) ? (!categoriesId1
				.equals(pojo.categoriesId1)) : (pojo.categoriesId1 != null)) {
			return false;
		}

		if ((s1Id != null) ? (!s1Id.equals(pojo.s1Id)) : (pojo.s1Id != null)) {
			return false;
		}

		if ((s2Id != null) ? (!s2Id.equals(pojo.s2Id)) : (pojo.s2Id != null)) {
			return false;
		}

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
		result = (31 * result) + ((forumId != null) ? forumId.hashCode() : 0);
		result = ((categoriesId1 != null) ? categoriesId1.hashCode() : 0);
		result = (31 * result) + ((s1Id != null) ? s1Id.hashCode() : 0);
		result = (31 * result) + ((s2Id != null) ? s2Id.hashCode() : 0);
		result = (31 * result) + ((tId1 != null) ? tId1.hashCode() : 0);
		result = (31 * result) + ((tId2 != null) ? tId2.hashCode() : 0);

		return result;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getSimpleName());

		sb.append(" [");
		sb.append("forumId").append("='").append(getForumId()).append("', ");
		sb.append("categoriesId1").append("='").append(getCategoriesId1())
				.append("', ");
		sb.append("s1Id").append("='").append(getS1Id()).append("', ");
		sb.append("s2Id").append("='").append(getS2Id()).append("', ");
		sb.append("tId1").append("='").append(getTId1()).append("', ");
		sb.append("tId2").append("='").append(getTId2()).append("'");
		sb.append("]");

		return sb.toString();
	}
}
