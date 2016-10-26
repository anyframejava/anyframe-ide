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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.anyframe.query.QueryInfo;
import org.anyframe.query.RowMetadataCallbackHandler;
import org.anyframe.query.SqlLoader;
import org.anyframe.query.impl.LiveScrollPagination;
import org.anyframe.query.impl.Pagination;
import org.anyframe.query.impl.jdbc.generator.DefaultPagingSQLGenerator;
import org.anyframe.query.impl.jdbc.generator.PagingSQLGenerator;
import org.anyframe.query.impl.jdbc.lob.Oracle8iLobHandler;
import org.anyframe.query.impl.jdbc.mapper.MappingStyleColumnMapRowMapper;
import org.anyframe.query.impl.jdbc.setter.PreparedStatementArgSetter;
import org.anyframe.query.impl.jdbc.setter.PreparedStatementArgTypeSetter;
import org.anyframe.query.impl.util.SQLTypeTransfer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.SqlReturnUpdateCount;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

/**
 * extend from Spring's JdbcTemplate, with pagination function.
 * 
 * @author SoYon Lim
 * @author JongHoon Kim
 */
public class PagingJdbcTemplate extends JdbcTemplate { 

	// 2011.05.11
	private PagingSQLGenerator paginationSQLGetter;

	protected int maxFetchSize = -1;

	// 2009.08.24
	private static final String RETURN_RESULT_SET_PREFIX = "#result-set-";

	private static final String RETURN_UPDATE_COUNT_PREFIX = "#update-count-";

	/**
	 * If this variable is set to true then all results checking will be
	 * bypassed for any callable statement processing. This can be used to avoid
	 * a bug in some older Oracle JDBC drivers like 10.1.0.2.
	 */
	private boolean skipResultsProcessing = false;

	/**
	 * If this variable is set to true then all results from a stored procedure
	 * call that don't have a corresponding SqlOutParameter declaration will be
	 * bypassed. All other results processng will be take place unless the
	 * variable <code>skipResultsProcessing</code> is set to <code>true</code>
	 */
	private boolean skipUndeclaredResults = false;

	/**
	 * Set whether results processing should be skipped. Can be used to optimize
	 * callable statement processing when we know that no results are being
	 * passed back - the processing of out parameter will still take place. This
	 * can be used to avoid a bug in some older Oracle JDBC drivers like
	 * 10.1.0.2.
	 */
	public void setSkipResultsProcessing(boolean skipResultsProcessing) {
		this.skipResultsProcessing = skipResultsProcessing;
	}

	/**
	 * Return whether results processing should be skipped.
	 */
	public boolean isSkipResultsProcessing() {
		return this.skipResultsProcessing;
	}

	/**
	 * Set whether undelared results should be skipped.
	 */
	public void setSkipUndeclaredResults(boolean skipUndeclaredResults) {
		this.skipUndeclaredResults = skipUndeclaredResults;
	}

	/**
	 * Return whether undeclared results should be skipped.
	 */
	public boolean isSkipUndeclaredResults() {
		return this.skipUndeclaredResults;
	}

	public PagingJdbcTemplate() {
		super();
	}

	public PagingJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	public void setPaginationSQLGetter(PagingSQLGenerator paginationSQLGetter) {
		this.paginationSQLGetter = paginationSQLGetter;
	}

	// 2009.04.28
	public PagingSQLGenerator getPaginationSQLGetter() {
		return paginationSQLGetter;
	}

	public int getMaxFetchSize() {
		return maxFetchSize;
	}

	public void setMaxFetchSize(int maxFetchSize) {
		this.maxFetchSize = maxFetchSize;
	}

	// 2009.04.28
	@SuppressWarnings("unchecked")
	public List query(String sql, PreparedStatementSetter pss,
			RowMapper rowMapper, Pagination paginationVO) {
		if (pss == null) {
			return (List) query(new PagingPreparedStatementCreator(sql),
					new PagingRowMapperResultSetExtractor(rowMapper,
							paginationVO));
		}
		return (List) query(new PagingPreparedStatementCreator(sql), pss,
				new PagingRowMapperResultSetExtractor(rowMapper, paginationVO));
	}

	/**
	 * In the case where there is no defined SQLGenerator, entered SQL is
	 * executed as it is. In the case where there is defined SQLGenerator, a
	 * total case on SQL execution result and paging processed result are
	 * sought.
	 * 
	 * @param sql
	 *            query statement
	 * @param args
	 *            a set of variable values for executing query
	 * @param argTypes
	 *            is matched with input parameters. A type must belong to fields
	 *            defined java.sql.Types package. *
	 * @param rowMapper
	 *            Mapper to map one Row from search result on a specific object
	 *            format
	 * @param context
	 *            Basic information necessary for paging process
	 * @return Query execution result
	 */
	@SuppressWarnings("unchecked")
	public List queryWithPagination(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize, RowMapper rowMapper, Pagination paginationVO) {
		if (checkPagingSQLGenerator(paginationSQLGetter))
			return query(sql, args, argTypes, queryMaxFetchSize, rowMapper,
					paginationVO);

		if (paginationVO.isCountRecordSize()) {
			long recordCount = executeCountSQL(sql, args, argTypes);
			paginationVO.setRecordCount(recordCount);
			paginationVO.setPageIndexToLast();
		}

		// generate pagination sql
		Object[] paginationArgs = paginationSQLGetter.setQueryArgs(args,
				paginationVO.getPageIndex(), paginationVO.getPageSize());
		int[] paginationArgTypes = paginationSQLGetter
				.setQueryArgTypes(argTypes);

		String paginationSql = getPaginationSQL(sql, args, argTypes,
				paginationVO);

		return query(paginationSql, paginationArgs, paginationArgTypes,
				queryMaxFetchSize, rowMapper);
	}

	@SuppressWarnings("unchecked")
	public void queryWithPagination(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize, RowCallbackHandler rch,
			Pagination paginationVO) {
		if (checkPagingSQLGenerator(paginationSQLGetter)) {
			query(new PagingPreparedStatementCreator(sql),
					new PreparedStatementArgTypeSetter(args, argTypes, null),
					new PagingRowCallbackHandlerResultSetExtractor(rch,
							paginationVO, queryMaxFetchSize));
			return;
		}

		if (paginationVO.isCountRecordSize()) {
			long recordCount = queryForObject(getCountSQL(sql), args, argTypes, Long.class);
			paginationVO.setRecordCount(recordCount);
			paginationVO.setPageIndexToLast();
		}

		// generate pagination sql
		Object[] paginationArgs = paginationSQLGetter.setQueryArgs(args,
				paginationVO.getPageIndex(), paginationVO.getPageSize());
		int[] paginationArgTypes = paginationSQLGetter
				.setQueryArgTypes(argTypes);

		String paginationSql = getPaginationSQL(sql, args, argTypes,
				paginationVO);

		query(paginationSql, new PreparedStatementArgTypeSetter(paginationArgs,
				paginationArgTypes, null),
				new NonPagingRowCallbackHandlerResultSetExtractor(rch,
						queryMaxFetchSize));
	}

	/**
	 * In the case where there is no defined SQLGenerator, entered SQL is
	 * executed as it is. In the case where there is defined SQLGenerator, a
	 * total case on SQL execution result and paging processed result are
	 * sought.
	 * 
	 * @param sql
	 *            query statement
	 * @param rowMapper
	 *            Mapper to map one Row from search result on a specific object
	 *            format
	 * @param context
	 *            Basic information necessary for paging process
	 * @return Query execution result
	 */
	@SuppressWarnings("unchecked")
	public List queryWithPagination(String sql, RowMapper rowMapper,
			Pagination paginationVO) {
		if (checkPagingSQLGenerator(paginationSQLGetter))
			return query(sql, null, null, -1, rowMapper, paginationVO);
		if (paginationVO.isCountRecordSize()) {
			long recordCount = executeCountSQL(sql, null, null);
			paginationVO.setRecordCount(recordCount);
			paginationVO.setPageIndexToLast();
		}

		Object[] paginationArgs = paginationSQLGetter.setQueryArgs(
				new Object[0], paginationVO.getPageIndex(), paginationVO
						.getPageSize());
		String paginationSql = getPaginationSQL(sql, new Object[0], new int[0],
				paginationVO);

		return query(paginationSql, paginationArgs, rowMapper);
	}

	/**
	 * In the case where there is no defined SQLGenerator, entered SQL is
	 * executed as it is. In the case where there is defined SQLGenerator, a
	 * total case on SQL execution result and paging processed result are
	 * sought.
	 * 
	 * @param sql
	 *            query statement
	 * @param args
	 *            a set of variable values for executing query
	 * @param rowMapper
	 *            Mapper to map one Row from search result on a specific object
	 *            format
	 * @param context
	 *            Basic information necessary for paging process
	 * @return Query execution result
	 */
	@SuppressWarnings("unchecked")
	public List queryWithPagination(String sql, Object[] args,
			RowMapper rowMapper, Pagination paginationVO) {
		if (checkPagingSQLGenerator(paginationSQLGetter))
			return query(sql, args, null, -1, rowMapper, paginationVO);

		if (paginationVO.isCountRecordSize()) {
			long recordCount = executeCountSQL(sql, args, null);
			paginationVO.setRecordCount(recordCount);
			paginationVO.setPageIndexToLast();
		}

		// generate pagination sql
		Object[] paginationArgs = paginationSQLGetter.setQueryArgs(args,
				paginationVO.getPageIndex(), paginationVO.getPageSize());
		String paginationSql = getPaginationSQL(sql, args, new int[0],
				paginationVO);

		return query(paginationSql, paginationArgs, rowMapper);
	}

	@SuppressWarnings("unchecked")
	public List queryForListWithPagination(String sql, Object[] args,
			int[] argTypes, int queryMaxFetchSize, Pagination paginationVO) {
		return queryWithPagination(sql, args, argTypes, queryMaxFetchSize,
				getColumnMapRowMapper(), paginationVO);
	}

	/**
	 * Generic method to execute the update given arguments. All other update()
	 * methods invoke this method.
	 * 
	 * @return the number of rows affected by the update
	 */
	// 2008.05.08 - add for Handling Lob of Oracle 8i
	@SuppressWarnings("unchecked")
	public int update(String sql, Object[] values, LobHandler lobHandler,
			String lobStatement, String[] lobTypes, Object[] lobKeys,
			Object[] lobValues) {
		int updateCount = update(sql, values);
		LinkedList<SqlParameter> lobParameters = null;
		lobParameters = new LinkedList<SqlParameter>();
		for (int i = 0; i < lobTypes.length; i++) {
			int type = SQLTypeTransfer.getSQLType(lobTypes[i].toUpperCase());
			lobParameters.add(new SqlParameter(type));
		}

		PreparedStatementCreatorFactory preparedStatementFactory = new PreparedStatementCreatorFactory(
				lobStatement, lobParameters);

		query(preparedStatementFactory.newPreparedStatementCreator(lobKeys),
				new Oracle8iResultSetExtractor((Oracle8iLobHandler) lobHandler,
						lobValues));

		return updateCount;
	}

	// 2008.05.22 - query override
	// 2008.07.21 reopen - cf.) need to fix
	// ResultSetMapperSupport VO byte[] -
	// not getBytes() --> BLOB
	@SuppressWarnings("unchecked")
	public void query(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize, RowCallbackHandler rch) {
		if (checkPagingSQLGenerator(paginationSQLGetter)) {
			query(new PagingPreparedStatementCreator(sql),
					new PreparedStatementArgTypeSetter(args, argTypes, null),
					new NonPagingRowCallbackHandlerResultSetExtractor(rch,
							queryMaxFetchSize));
			return;
		}

		query(sql, new PreparedStatementArgTypeSetter(args, argTypes, null),
				new NonPagingRowCallbackHandlerResultSetExtractor(rch,
						queryMaxFetchSize));
	}

	// 2011.05.03 - maxFetchSize
	@SuppressWarnings("unchecked")
	public List query(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize) {
		return query(sql, args, argTypes, queryMaxFetchSize,
				new ColumnMapRowMapper());
	}

	// 2011.05.03 - maxFetchSize
	@SuppressWarnings("unchecked")
	public List query(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize, RowMapper rowMapper) {
		return (List) query(sql, args, argTypes,
				new NonPagingRowMapperResultSetExtractor(rowMapper,
						queryMaxFetchSize));
	}

	/** ************* PROTECTED METHODS ************** */

	protected String getPaginationSQL(String originalSql, Object[] args,
			int[] argTypes, Pagination context) {
		int pageIndex = context.getPageIndex();
		int pageSize = context.getPageSize();
		return paginationSQLGetter.getPaginationSQL(originalSql, args,
				argTypes, pageIndex, pageSize);
	}

	// 2008.05.08, 2009.04.28
	public String getCountSQL(String originalSql) {
		return paginationSQLGetter.getCountSQL(originalSql);
	}

	// 2009.08.24
	// added to pass queryInfo object (mapping style info)
	public Map<String, Object> call(final SqlLoader sqlLoader,
			CallableStatementCreator csc, List<SqlParameter> declaredParameters,
			final QueryInfo queryInfo) throws DataAccessException {
		final List<SqlParameter> updateCountParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> resultSetParameters = new ArrayList<SqlParameter>();
		final List<SqlParameter> callParameters = new ArrayList<SqlParameter>();
		for (int i = 0; i < declaredParameters.size(); i++) {
			SqlParameter parameter = declaredParameters.get(i);
			if (parameter.isResultsParameter()) {
				if (parameter instanceof SqlReturnResultSet) {
					resultSetParameters.add(parameter);
				} else {
					updateCountParameters.add(parameter);
				}
			} else {
				callParameters.add(parameter);
			}
		}
		return execute(csc,
				new CallableStatementCallback<Map<String, Object>>() {
					public Map<String, Object> doInCallableStatement(
							CallableStatement cs) throws SQLException {
						boolean retVal = cs.execute();
						int updateCount = cs.getUpdateCount();
						if (logger.isDebugEnabled()) {
							logger
									.debug("CallableStatement.execute() returned '"
											+ retVal + "'");
							logger
									.debug("CallableStatement.getUpdateCount() returned "
											+ updateCount);
						}
						Map<String, Object> returnedResults = createResultsMap();
						if (retVal || updateCount != -1) {
							returnedResults
									.putAll(extractReturnedResults(sqlLoader,
											cs, updateCountParameters,
											resultSetParameters, updateCount,
											queryInfo));
						}
						returnedResults.putAll(extractOutputParameters(cs,
								callParameters));
						return returnedResults;
					}
				});

		/*
		 * return (Map<String, Object>) execute(csc, new
		 * CallableStatementCallback() { public Object
		 * doInCallableStatement(CallableStatement cs) throws SQLException {
		 * boolean retVal = cs.execute(); int updateCount = cs.getUpdateCount();
		 * if (logger.isDebugEnabled()) {
		 * logger.debug("CallableStatement.execute() returned '" + retVal +
		 * "'"); logger.debug("CallableStatement.getUpdateCount() returned " +
		 * updateCount); } Map<String, Object> returnedResults =
		 * createResultsMap(); if (retVal || updateCount != -1) {
		 * returnedResults.putAll(extractReturnedResults(sqlLoader, cs,
		 * updateCountParameters, resultSetParameters, updateCount, queryInfo));
		 * } returnedResults.putAll(extractOutputParameters(cs,
		 * callParameters)); return returnedResults; } });
		 */
	}

	// 2009.08.24
	// added to identify mapping style
	/**
	 * Extract returned ResultSets from the completed stored procedure.
	 * 
	 * @param cs
	 *            JDBC wrapper for the stored procedure
	 * @param updateCountParameters
	 *            Parameter list of declared update count parameters for the
	 *            stored procedure
	 * @param resultSetParameters
	 *            Parameter list of declared resturn resultSet parameters for
	 *            the stored procedure
	 * @return Map that contains returned results
	 * @throws SQLException
	 */
	protected Map<String, Object> extractReturnedResults(SqlLoader sqlLoader,
			CallableStatement cs, List<SqlParameter> updateCountParameters,
			List<SqlParameter> resultSetParameters, int updateCount, QueryInfo queryInfo)
			throws SQLException {

		Map<String, Object> returnedResults = new HashMap<String, Object>();
		int rsIndex = 0;
		int updateIndex = 0;
		boolean moreResults;
		if (!skipResultsProcessing) {
			do {
				if (updateCount == -1) {
					if (resultSetParameters != null
							&& resultSetParameters.size() > rsIndex) {
						SqlReturnResultSet declaredRsParam = (SqlReturnResultSet) resultSetParameters
								.get(rsIndex);
						returnedResults.putAll(processResultSet(cs
								.getResultSet(), declaredRsParam));
						rsIndex++;
					} else {
						if (!skipUndeclaredResults) {
							String rsName = RETURN_RESULT_SET_PREFIX
									+ (rsIndex + 1);
							SqlReturnResultSet undeclaredRsParam = new SqlReturnResultSet(
									rsName, new MappingStyleColumnMapRowMapper(
											sqlLoader, queryInfo));
							logger
									.info("Added default SqlReturnResultSet parameter named "
											+ rsName);
							returnedResults.putAll(processResultSet(cs
									.getResultSet(), undeclaredRsParam));
							rsIndex++;
						}
					}
				} else {
					if (updateCountParameters != null
							&& updateCountParameters.size() > updateIndex) {
						SqlReturnUpdateCount ucParam = (SqlReturnUpdateCount) updateCountParameters
								.get(updateIndex);
						String declaredUcName = ucParam.getName();
						returnedResults.put(declaredUcName, new Integer(
								updateCount));
						updateIndex++;
					} else {
						if (!skipUndeclaredResults) {
							String undeclaredUcName = RETURN_UPDATE_COUNT_PREFIX
									+ (updateIndex + 1);
							logger
									.info("Added default SqlReturnUpdateCount parameter named "
											+ undeclaredUcName);
							returnedResults.put(undeclaredUcName, new Integer(
									updateCount));
							updateIndex++;
						}
					}
				}
				moreResults = cs.getMoreResults();
				updateCount = cs.getUpdateCount();
				if (logger.isDebugEnabled()) {
					logger.debug("CallableStatement.getUpdateCount() returned "
							+ updateCount);
				}
			} while (moreResults || updateCount != -1);
		}
		return returnedResults;
	}

	/** ************* PRIVATE METHODS ************** */

	private boolean checkPagingSQLGenerator(
			PagingSQLGenerator pagingSQLGenerator) {
		return (pagingSQLGenerator instanceof DefaultPagingSQLGenerator);
	}

	@SuppressWarnings("unchecked")
	private List query(String sql, Object[] args, int[] argTypes,
			int queryMaxFetchSize, RowMapper rowMapper, Pagination paginationVO) {
		if (args == null)
			return (List) query(new PagingPreparedStatementCreator(sql),
					new PagingRowMapperResultSetExtractor(rowMapper,
							paginationVO, queryMaxFetchSize));
		if (argTypes == null)
			return (List) query(new PagingPreparedStatementCreator(sql),
					new PreparedStatementArgSetter(args),
					new PagingRowMapperResultSetExtractor(rowMapper,
							paginationVO, queryMaxFetchSize));
		return (List) query(new PagingPreparedStatementCreator(sql),
				new PreparedStatementArgTypeSetter(args, argTypes, null),
				new PagingRowMapperResultSetExtractor(rowMapper, paginationVO,
						queryMaxFetchSize));
	}

	// 2009.04.28 : method modifier private -> public,
	// return type int -> long
	public long executeCountSQL(String sql, Object[] args, int[] argTypes) {
		if (args == null)
			return queryForObject(getCountSQL(sql), Long.class);
		if (argTypes == null)
			return queryForObject(getCountSQL(sql), args, Long.class);
		return queryForObject(getCountSQL(sql), args, argTypes, Long.class);
	}

	/** ************* INNER CLASSES ************** */

	/**
	 * Simple adapter for PreparedStatementCreator, allowing to use a plain SQL
	 * statement.
	 */
	private static class PagingPreparedStatementCreator implements
			PreparedStatementCreator {

		private final String sql;

		public PagingPreparedStatementCreator(String sql) {
			Assert.notNull(sql, "Query Service : SQL must not be null");
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection conn)
				throws SQLException {
			return conn.prepareStatement(this.sql,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		}
	}

	@SuppressWarnings("unchecked")
	class PagingRowMapperResultSetExtractor implements ResultSetExtractor {

		private final RowMapper rowMapper;

		private final Pagination paginationVO;

		private final int queryMaxFetchSize;

		public PagingRowMapperResultSetExtractor(RowMapper rowMapper,
				Pagination paginationVO) {
			Assert.notNull(rowMapper, "Query Service : RowMapper is required");
			this.rowMapper = rowMapper;
			this.paginationVO = paginationVO;
			this.queryMaxFetchSize = -1;
		}

		public PagingRowMapperResultSetExtractor(RowMapper rowMapper,
				Pagination paginationVO, int queryMaxFetchSize) {
			Assert.notNull(rowMapper, "Query Service : RowMapper is required");
			this.rowMapper = rowMapper;
			this.paginationVO = paginationVO;
			this.queryMaxFetchSize = queryMaxFetchSize;
		}

		public List<Object> extractData(ResultSet rs) throws SQLException {
			int pageIndex = paginationVO.getPageIndex();
			int pageSize = paginationVO.getPageSize();
			rs.last();
			paginationVO.setRecordCount(rs.getRow());
			if (pageIndex == 1) {
				rs.beforeFirst();
			} else if (pageIndex > 1) {
				int pageCount = paginationVO.getPageCount();
				// 2009.05.12
				if (pageCount < pageIndex) {
					rs.last();
				} else {
					rs.absolute((pageIndex - 1) * pageSize);
				}
			}
			List<Object> results = new ArrayList<Object>();
			int rowNum = 1;

			if (rowMapper instanceof RowMetadataCallbackHandler
					&& ((RowMetadataCallbackHandler) rowMapper)
							.isNeedColumnInfo()) {
				((RowMetadataCallbackHandler) rowMapper).processMetaData(rs);
			}

			// 2009.04.28 - maxFetchSize
			if (queryMaxFetchSize == -1) {
				while (rs.next() && rowNum <= pageSize) {
					results.add(this.rowMapper.mapRow(rs, rowNum++));
				}
			} else {
				while (rs.next() && rowNum <= pageSize) {
					if (rowNum > queryMaxFetchSize) {
						throw new DataRetrievalFailureException(
								"Too many data in ResultSet. maxFetchSize is "
										+ queryMaxFetchSize);
					}
					results.add(this.rowMapper.mapRow(rs, rowNum++));
				}
			}
			return results;
		}
	}
	
	@SuppressWarnings("unchecked")
	private class PagingRowCallbackHandlerResultSetExtractor implements
			ResultSetExtractor {

		private final RowCallbackHandler rch;
		private final Pagination paginationVO;
		private final int queryMaxFetchSize;

		public PagingRowCallbackHandlerResultSetExtractor(
				RowCallbackHandler rch, Pagination paginationVO,
				int queryMaxFetchSize) {
			this.rch = rch;
			this.paginationVO = paginationVO;
			this.queryMaxFetchSize = queryMaxFetchSize;
		}

		public Object extractData(ResultSet rs) throws SQLException {

			int pageIndex = paginationVO.getPageIndex();
			int pageSize = paginationVO.getPageSize();
			rs.last();
			paginationVO.setRecordCount(rs.getRow());
			if (paginationVO instanceof LiveScrollPagination) {
				int targetIndex = ((LiveScrollPagination) paginationVO)
						.getStartIndex();
				if (targetIndex == 0) {
					rs.beforeFirst();
				} else {
					rs.absolute(targetIndex);
				}
			} else {
				if (pageIndex == 1) {
					rs.beforeFirst();
				} else if (pageIndex > 1) {

					int pageCount = paginationVO.getPageCount();
					if (pageCount < pageIndex) {
						rs.next();
						// rs.absolute((pageCount - 1) * pageSize);
					} else {
						rs.absolute((pageIndex - 1) * pageSize);
					}
				}
			}

			int rowNum = 1;

			// 2008.04.11
			// Even in the case where there is no data, meta data setup can be
			// needed.
			// processMetaData handling follows implementation of each rch. As
			// of now, only it is implemented in Gauce.
			if (this.rch instanceof RowMetadataCallbackHandler
					&& ((RowMetadataCallbackHandler) rch).isNeedColumnInfo()) {
				((RowMetadataCallbackHandler) this.rch).processMetaData(rs);
			}

			// 2009.04.28 - maxFetchSize
			if (queryMaxFetchSize == -1) {
				while (rs.next() && rowNum <= pageSize) {
					this.rch.processRow(rs);
					rowNum++;
				}
			} else {
				while (rs.next() && rowNum <= pageSize) {
					if (rowNum > queryMaxFetchSize) {
						throw new DataRetrievalFailureException(
								"Too many data in ResultSet maxFetchSize is "
										+ queryMaxFetchSize);
					}
					this.rch.processRow(rs);
					rowNum++;
				}
			}

			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private class NonPagingRowCallbackHandlerResultSetExtractor implements
			ResultSetExtractor {

		private final RowCallbackHandler rch;

		private final int queryMaxFetchSize;

		public NonPagingRowCallbackHandlerResultSetExtractor(
				RowCallbackHandler rch, int queryMaxFetchSize) {
			this.rch = rch;
			this.queryMaxFetchSize = queryMaxFetchSize;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			int rowNum = 1;

			if (rch instanceof RowMetadataCallbackHandler
					&& ((RowMetadataCallbackHandler) rch).isNeedColumnInfo()) {
				((RowMetadataCallbackHandler) rch).processMetaData(rs);
			}

			// 2009.04.28 - maxFetchSize
			if (queryMaxFetchSize == -1) {
				while (rs.next()) {
					this.rch.processRow(rs);
					rowNum++;
				}
			} else {
				while (rs.next()) {
					if (rowNum > queryMaxFetchSize) {
						throw new DataRetrievalFailureException(
								"Too many data in ResultSet. maxFetchSize is "
										+ queryMaxFetchSize);
					}
					this.rch.processRow(rs);
					rowNum++;
				}
			}

			return null;
		}
	}

	// 2011.05.03 - maxFetchSize
	@SuppressWarnings("unchecked")
	private class NonPagingRowMapperResultSetExtractor implements
			ResultSetExtractor {

		private final RowMapper rowMapper;

		private final int queryMaxFetchSize;

		public NonPagingRowMapperResultSetExtractor(RowMapper rowMapper,
				int queryMaxFetchSize) {
			Assert.notNull(rowMapper, "Query Service : RowMapper is required");
			this.rowMapper = rowMapper;
			this.queryMaxFetchSize = queryMaxFetchSize;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			List<Object> results = new ArrayList<Object>();
			int rowNum = 1;

			if (rowMapper instanceof RowMetadataCallbackHandler
					&& ((RowMetadataCallbackHandler) rowMapper)
							.isNeedColumnInfo()) {
				((RowMetadataCallbackHandler) rowMapper).processMetaData(rs);
			}

			if (queryMaxFetchSize == -1) {
				while (rs.next()) {
					results.add(this.rowMapper.mapRow(rs, rowNum));
					rowNum++;
				}
			} else {
				while (rs.next()) {
					if (rowNum > queryMaxFetchSize) {
						throw new DataRetrievalFailureException(
								"Too many data in ResultSet. maxFetchSize is "
										+ queryMaxFetchSize);
					}
					results.add(this.rowMapper.mapRow(rs, rowNum));
					rowNum++;
				}
			}

			return results;
		}
	}

	// 2008.05.08 - add for Handling Lob of Oracle 8i
	@SuppressWarnings("unchecked")
	private class Oracle8iResultSetExtractor implements ResultSetExtractor {

		private final Oracle8iLobHandler lobHandler;

		private final Object[] lobValues;

		public Oracle8iResultSetExtractor(Oracle8iLobHandler lobHandler,
				Object[] lobValues) {
			this.lobHandler = lobHandler;
			this.lobValues = lobValues;
		}

		public Object extractData(ResultSet rs) throws SQLException {
			ResultSetMetaData meta = rs.getMetaData();
			while (rs.next()) {
				for (int i = 1; i < meta.getColumnCount() + 1; i++) {
					Object tObj = lobValues[i - 1];
					if (tObj instanceof String) {
						lobHandler.setClobOutputValue(rs, i, (String) tObj);
					} else if (tObj instanceof byte[]) {
						lobHandler.setBlobOutputValue(rs, i, (byte[]) tObj);
					}
				}
			}

			return null;
		}
	}
}
