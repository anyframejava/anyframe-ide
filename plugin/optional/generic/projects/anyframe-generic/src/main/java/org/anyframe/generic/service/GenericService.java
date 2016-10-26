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
package org.anyframe.generic.service;

import java.io.Serializable;
import java.util.List;

import org.anyframe.datatype.SearchVO;
import org.anyframe.generic.dao.GenericDao;
import org.anyframe.pagination.Page;

/**
 * Generic Service that talks to GenericDao to CRUD POJOs.
 * <p>
 * Extend this interface if you want typesafe (no casting necessary) services
 * for your domain objects. The original code of this class comes from Appfuse
 * framework.
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 * @author modified by SooYeon Park
 * @param <T>
 *            a type variable
 * @param <PK>
 *            the primary key for that type
 */
public interface GenericService<T, PK extends Serializable> {

	/**
	 * Setter method to set GenericDao class
	 * 
	 * @param genericDao
	 *            genericDao object to set
	 */
	public void setGenericDao(GenericDao<T, PK> genericDao);

	/**
	 * Getter method to get GenericDao class
	 * 
	 */
	public GenericDao<T, PK> getGenericDao();

	/**
	 * Generic method to get an object based on class and identifier.
	 * 
	 * @param id
	 *            the identifier (primary key) of the object to get
	 * @return a populated object
	 * @throws Exception
	 *             if there is any problem getting object
	 */
	T get(PK id) throws Exception;

	/**
	 * Checks for existence of an object of type T using the id argument.
	 * 
	 * @param id
	 *            the identifier (primary key) of the object to get
	 * @return - true if it exists, false if it doesn't
	 * @throws Exception
	 *             if there is any problem checking for existence of an object
	 */
	boolean exists(PK id) throws Exception;

	/**
	 * Generic method to create an object - handles insert
	 * 
	 * @param object
	 *            the object to create
	 * @throws Exception
	 *             if there is any problem creating an object
	 */
	void create(T object) throws Exception;

	/**
	 * Generic method to update an object - handles update
	 * 
	 * @param object
	 *            the object to save
	 * @throws Exception
	 *             if there is any problem updating an object
	 */
	void update(T object) throws Exception;

	/**
	 * Generic method to delete an object based on class and id
	 * 
	 * @param id
	 *            the identifier (primary key) of the object to remove
	 * @throws Exception
	 *             if there is any problem remove an object
	 */
	void remove(PK id) throws Exception;

	/**
	 * Generic method to get object list based on search condition and search
	 * keyword
	 * 
	 * @param searchVO
	 *            search condition and search keyword
	 * @return result page object with total count
	 * @throws Exception
	 *             if there is any problem getting object list
	 */
	Page getPagingList(SearchVO searchVO) throws Exception;

	/**
	 * Generic method to get object list based on object
	 * 
	 * @param object
	 *            domain object
	 * @param pageIndex
	 *            page number which expected to be displayed (pageIndex > 0)
	 * @return result page object with total count
	 * @throws Exception
	 *             if there is any problem getting object list
	 */
	Page getPagingList(T object, int pageIndex) throws Exception;

	/**
	 * Generic method to get object list based
	 * 
	 * @param searchVO
	 *            search condition and search keyword
	 * @return List
	 * @throws Exception
	 *             if there is any problem getting object list
	 */
	List<T> getList(SearchVO searchVO) throws Exception;

	/**
	 * Generic method to get object list based object
	 * 
	 * @param object
	 *            domain object
	 * @return List
	 * @throws Exception
	 *             if there is any problem getting object list
	 */
	List<T> getList(T object) throws Exception;
}
