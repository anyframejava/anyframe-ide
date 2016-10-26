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
import java.util.Map;

import javax.inject.Inject;

import junit.framework.Assert;

import org.anyframe.query.QueryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * TestCase Name : QueryServiceForSpecialCharTest <br>
 * <br>
 * [Description] : Query statement including special character is executed and
 * its execution result is verified. <br>
 * [Main Flow]
 * <ul>
 * <li>#-1 Positive Case : Query statement including special character is
 * executed by calling for method and its result value is verified.</li>
 * </ul>
 * 
 * @author SoYon Lim
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/spring/context-*.xml" })
public class QueryServiceForSpecialCharTest {

	@Inject
	QueryService queryService;

	/**
	 * Table TB_USER is created for test and initial data is entered.
	 */
	@Before
	public void onSetUp() {
		try {
			queryService.updateBySQL("DROP TABLE TB_USER", new String[] {},
					new Object[] {});
		} catch (Exception e) {
			System.out.println("Fail to DROP Table.");
		}
		queryService.updateBySQL("CREATE TABLE TB_USER ( "
				+ "LOGON_ID  VARCHAR(20), " + "PASSWORD VARCHAR(20),"
				+ "NAME VARCHAR(20)," + "PRIMARY KEY (LOGON_ID))",
				new String[] {}, new Object[] {});

		queryService.createBySQL(
				"INSERT INTO TB_USER VALUES ('admin', 'admin', 'ADMIN')",
				new String[] {}, new Object[] {});
		queryService.createBySQL(
				"INSERT INTO TB_USER VALUES ('test', 'test123', 'TESTER&123')",
				new String[] {}, new Object[] {});
	}

	/**
	 * [Flow #-1] Positive Case : By calling for findBySQL()method of
	 * QueryService, query statement including special character is executed and
	 * its result value is verified.
	 * 
	 * @throws Exception
	 *             throws exception which is from QueryService
	 */
	@Test
	public void testUserUsingConditionWithSpecialChar() {
		// 1. set query statement
		String sql = "select * from TB_USER "
				+ "where NAME like '%~`!@#$^&*()+-={}|[]\\:\";''<>?,./%' ";

		// 2. execute query
		List<Map<String, Object>> results = queryService.findBySQL(sql,
				new String[] {}, new Object[] {});

		// 3. assert
		Assert.assertTrue("Fail to execute query with special character.",
				results.size() == 0);

		// 4. set another query statement
		sql = "select * from TB_USER " + "where NAME like '%&%' ";

		// 5. execute query
		results = queryService.findBySQL(sql, new String[] {}, new Object[] {});

		// 6. assert
		Assert.assertTrue("Fail to execute query with special character.",
				results.size() == 1);
		Map<String, Object> result = results.iterator().next();
		Assert.assertEquals("Fail to compare result.", "test", result
				.get("LOGON_ID"));
	}
}
