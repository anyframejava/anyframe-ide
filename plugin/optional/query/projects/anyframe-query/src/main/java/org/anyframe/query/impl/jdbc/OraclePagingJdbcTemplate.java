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
package org.anyframe.query.impl.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.anyframe.query.impl.jdbc.setter.BatchCallableStatementSetter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.InterruptibleBatchPreparedStatementSetter;
import org.springframework.jdbc.core.ParameterDisposer;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.util.ReflectionUtils;

/**
 * extend from PagingJdbcTemplate for batchUpdate based of Oracle.
 * 
 * @author SoYon Lim
 * @author JongHoon Kim
 */
public class OraclePagingJdbcTemplate extends PagingJdbcTemplate {

	private NativeJdbcExtractor nativeJdbcExtractor;
	private boolean log4jdbc = true;

	public void setNativeJdbcExtractor(NativeJdbcExtractor nativeJdbcExtractor) {
		this.nativeJdbcExtractor = nativeJdbcExtractor;

		try {
			this.getClass().getClassLoader().loadClass(
					"net.sf.log4jdbc.PreparedStatementSpy");
			this.getClass().getClassLoader().loadClass(
					"net.sf.log4jdbc.CallableStatementSpy");
		} catch (ClassNotFoundException e) {
			log4jdbc = false;
		}
	}

	private PreparedStatement getPreparedStatement(PreparedStatement ps) {
		if (ps instanceof net.sf.log4jdbc.PreparedStatementSpy) {
			return (PreparedStatement) ((net.sf.log4jdbc.PreparedStatementSpy) ps)
					.getRealPreparedStatement();
		}

		return ps;
	}

	private CallableStatement getCallableStatement(CallableStatement cs) {
		if (cs instanceof net.sf.log4jdbc.CallableStatementSpy) {
			return (CallableStatement) ((net.sf.log4jdbc.CallableStatementSpy) cs)
					.getRealCallableStatement();
		}

		return cs;
	}

	public int[] batchUpdate(String sql, final BatchCallableStatementSetter css) {
		logger.debug("Executing SQL batch update [" + sql + "]");

		return execute(sql, new CallableStatementCallback<int[]>() {
			public int[] doInCallableStatement(CallableStatement cs)
					throws SQLException {
				int batchSize = css.getBatchSize();

				if (JdbcUtils.supportsBatchUpdates(cs.getConnection())) {

					if (nativeJdbcExtractor != null) {
						cs = nativeJdbcExtractor.getNativeCallableStatement(cs);
					}
					if (log4jdbc) {
						cs = getCallableStatement(cs);
					}

					try {
						Method setExecuteBatchMethod = cs.getClass().getMethod(
								"setExecuteBatch", new Class[] { int.class });
						// 2011.08.29 - for ojdbc6 - makeAccessible
						ReflectionUtils.makeAccessible(setExecuteBatchMethod);
						setExecuteBatchMethod.invoke(cs,
								new Object[] { new Integer(batchSize) });
					} catch (Exception e) {
						if (e instanceof NoSuchMethodException) {
							throw new SQLException(
									"Query Service : Not supported a implementation of DataSource service. Fail to find a method from current callableStatement ["
											+ cs + "]");
						}
						if (e instanceof InvocationTargetException) {
							Throwable ex = (((InvocationTargetException) e)
									.getCause());
							throw new SQLException(ex.getMessage());
						}

						throw new SQLException(e.getMessage());
					}

					int[] updateCountArray = new int[batchSize];
					for (int i = 0; i < batchSize; i++) {
						css.setValues(cs, i);
						int updateCount = cs.executeUpdate();
						updateCountArray[i] = updateCount;
					}
					return updateCountArray;
				} else {
					List<Integer> rowsAffected = new ArrayList<Integer>();

					for (int i = 0; i < batchSize; i++) {
						css.setValues(cs, i);
						rowsAffected.add(new Integer(cs.executeUpdate()));
					}
					int[] rowsAffectedArray = new int[rowsAffected.size()];
					for (int i = 0; i < rowsAffectedArray.length; i++) {
						rowsAffectedArray[i] = rowsAffected.get(i).intValue();
					}
					return rowsAffectedArray;
				}
			}
		});
	}

	public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss)
			throws DataAccessException {
		logger.debug("Executing SQL batch update [" + sql + "]");

		return execute(sql, new PreparedStatementCallback<int[]>() {
			public int[] doInPreparedStatement(PreparedStatement ps)
					throws SQLException {
				try {
					int batchSize = pss.getBatchSize();

					if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
						// Native PreparedStatement extraction accordin go
						// DataSource implementation
						if (nativeJdbcExtractor != null) {
							ps = nativeJdbcExtractor
									.getNativePreparedStatement(ps);
						}
						if (log4jdbc) {
							ps = getPreparedStatement(ps);
						}

						try {
							Method setExecuteBatchMethod = ps.getClass()
									.getMethod("setExecuteBatch",
											new Class[] { int.class });
							// 2011.08.29 - for ojdbc6 - makeAccessible
							ReflectionUtils
									.makeAccessible(setExecuteBatchMethod);
							setExecuteBatchMethod.invoke(ps,
									new Object[] { new Integer(batchSize) });
						} catch (Exception e) {
							if (e instanceof NoSuchMethodException) {
								throw new SQLException(
										"Query Service : Not supported a implementation of DataSource service. Fail to find a method from current preparedStatement ["
												+ ps + "]");
							}
							if (e instanceof InvocationTargetException) {
								Throwable ex = (((InvocationTargetException) e)
										.getCause());
								throw new SQLException(ex.getMessage());
							}

							throw new SQLException(e.getMessage());
						}

						int[] updateCountArray = new int[batchSize];
						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							int updateCount = ps.executeUpdate();
							updateCountArray[i] = updateCount;
						}
						return updateCountArray;
					} else {
						List<Integer> rowsAffected = new ArrayList<Integer>();

						InterruptibleBatchPreparedStatementSetter ipss = (pss instanceof InterruptibleBatchPreparedStatementSetter ? (InterruptibleBatchPreparedStatementSetter) pss
								: null);

						for (int i = 0; i < batchSize; i++) {
							pss.setValues(ps, i);
							if (ipss != null && ipss.isBatchExhausted(i)) {
								break;
							}
							rowsAffected.add(new Integer(ps.executeUpdate()));
						}
						int[] rowsAffectedArray = new int[rowsAffected.size()];
						for (int i = 0; i < rowsAffectedArray.length; i++) {
							rowsAffectedArray[i] = rowsAffected.get(i)
									.intValue();
						}
						return rowsAffectedArray;
					}
				} finally {
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		});
	}
}
