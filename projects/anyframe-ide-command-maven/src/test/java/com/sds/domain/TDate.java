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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * This is a TDate class.
 * 
 * @author Sooyeon Park
 */
@Entity
@Table(name = "T_DATE", schema = "PUBLIC", uniqueConstraints = @UniqueConstraint(columnNames = {
		"U_ID1", "U_ID2" }))
public class TDate implements Serializable {

	private static final long serialVersionUID = 1L;
	private TDateId id;
	private UDate uDate;
	private Set<SDate> sDates = new HashSet<SDate>(0);

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "TId1", column = @Column(name = "T_ID1", nullable = false)),
			@AttributeOverride(name = "TId2", column = @Column(name = "T_ID2", nullable = false)) })
	public TDateId getId() {
		return this.id;
	}

	public void setId(TDateId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "U_ID1", referencedColumnName = "U_ID1", nullable = false),
			@JoinColumn(name = "U_ID2", referencedColumnName = "U_ID2", nullable = false) })
	public UDate getUDate() {
		return this.uDate;
	}

	public void setUDate(UDate uDate) {
		this.uDate = uDate;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "TDate")
	public Set<SDate> getSDates() {
		return this.sDates;
	}

	public void setSDates(Set<SDate> sDates) {
		this.sDates = sDates;
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
