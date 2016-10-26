/*
 * Copyright 2002-2012 the original author or authors.
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
package org.anyframe.query.impl;

import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.anyframe.pagination.Page;
import org.anyframe.query.QueryService;
import org.anyframe.query.dao.UserSupportDaoWithResultClass;
import org.anyframe.query.vo.UsersVO;
import org.anyframe.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * TestCase Name : DaoWithResultTest <br>
 * <br>
 * [Description] : This TestCase is to test AbstractDao class provided by
 * QueryService. In the case where input/output factor belongs to VO type, query
 * statements on INSERT, UPDATE, DELETE and SELECT are executed and the result
 * value is verified. However, the output value type should be based on
 * <result/> information defined at mapping XML. <br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : Called for is method provided into implemented
 * UserDaoWithResultClass class inheriting AbstractDao which is offered by
 * QueryService and its result value is verified. In the case of execution of
 * INSERT, UPDATE and DELETE, input value is delivered in the form of Map and
 * SELECT execution result is set based on mapping information defined at <
 * result/> and delivered at UserDaoWithResultClass class.</li>
 * </ul>
 * 
 * @author SoYon Lim
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"classpath:spring/daosupport/context-common.xml",
		"classpath:spring/daosupport/context-userdao.xml",
		"classpath:spring/daosupport/context-query.xml",
		"classpath:spring/daosupport/context-query-sqlloader-result.xml" })
public class SupportDaoWithResultTest {
	@Inject
	UserSupportDaoWithResultClass userSupportDaoWithResultClass;

	@Inject
	QueryService queryService;

	/**
	 * Table USERS are created for test.
	 */
	@Before
	public void onSetUp() {
		// Try to drop the table. It may not
		// exist and throw an exception.
		System.out.println("Attempting to drop old table");
		try {
			queryService.updateBySQL("DROP TABLE USERS", new String[] {},
					new Object[] {});
		} catch (Exception e) {
			// ignore
		}

		queryService
				.updateBySQL(
						"CREATE TABLE USERS (USER_ID VARCHAR(20) NOT NULL,USER_NAME VARCHAR(50) NOT NULL,PASSWORD VARCHAR(10) NOT NULL,SSN VARCHAR(13),SL_YN CHAR(1),BIRTH_DAY VARCHAR(8),AGE NUMERIC(3),CELL_PHONE VARCHAR(14),ADDR VARCHAR(100),EMAIL VARCHAR(50),EMAIL_YN CHAR(1),IMAGE_FILE VARCHAR(100),REG_DATE DATE,CONSTRAINT PK_USERS PRIMARY KEY(USER_ID))",
						new String[] {}, new Object[] {});
	}

	/**
	 * [Flow #-1] Positive Case : Called for is method provided into implemented
	 * UserDaoWithResultClass class inheriting AbstractDao which is offered by
	 * QueryService and its result value is verified. In the case of execution
	 * of INSERT, UPDATE and DELETE, input value is delivered in the form of Map
	 * and SELECT execution result is set based on mapping information defined
	 * at < result/> and delivered at UserDaoWithResultClass class.
	 * 
	 * @throws Exception
	 *             throws exception which is from QueryService
	 */
	@Test
	public void testUserDaoWithResult() {
		// 1. insert a new user
		UsersVO usersVO1 = new UsersVO();
		usersVO1.setUserId("admin");
		usersVO1.setUserName("ADMIN");
		usersVO1.setPassword("admin123");
		usersVO1.setRegDate(DateUtil.stringToDate("2012-11-22 11:11:11", "yyyy-MM-dd HH:mm:ss"));
		userSupportDaoWithResultClass.createUsers(usersVO1);

		// 2. insert another new user
		UsersVO usersVO2 = new UsersVO();
		usersVO2.setUserId("test");
		usersVO2.setUserName("TEST");
		usersVO2.setPassword("test123");
		userSupportDaoWithResultClass.createUsers(usersVO2);

		// 3. check for inserting
		UsersVO result = userSupportDaoWithResultClass.findUsers(usersVO1);
		Assert.assertEquals("2012-11-22 11:11:11", DateUtil.dateToString(result.getRegDate(), "yyyy-MM-dd HH:mm:ss"));
		Assert.assertEquals(usersVO1.getUserName(), result.getUserName());

		// 4. check for inserting
		result = userSupportDaoWithResultClass.findUsers(usersVO2);
		Assert.assertEquals(usersVO2.getUserName(), result.getUserName());

		// 5. update a user information
		usersVO2.setUserName("TESTUPD");
		userSupportDaoWithResultClass.updateUsers(usersVO2);

		// 6. check for updating
		result = userSupportDaoWithResultClass.findUsers(usersVO2);
		Assert.assertEquals(usersVO2.getUserName(), result.getUserName());

		// 7. select user list
		UsersVO searchVO = new UsersVO();
		Page page = userSupportDaoWithResultClass.findUsersList(searchVO, 1, 1, 10);
		Assert.assertEquals(2, page.getTotalCount());

		// 8. assert in detail
		List<?> list = page.getList();
		Assert.assertEquals(1, list.size());
		result = (UsersVO) list.iterator().next();
		Assert.assertEquals(usersVO1.getUserName(), result.getUserName());
	}

}
