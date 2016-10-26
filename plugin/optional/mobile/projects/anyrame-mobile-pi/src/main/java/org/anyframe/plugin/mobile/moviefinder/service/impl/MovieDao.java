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
package org.anyframe.plugin.mobile.moviefinder.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.anyframe.pagination.Page;
import org.anyframe.plugin.mobile.domain.Movie;
import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * This MovieDao class is a DAO class to provide movie crud functionality.
 * 
 * @author Jongpil Park
 */
@Repository("mobileMovieDao")
public class MovieDao extends SqlSessionDaoSupport {

	//Velocity-Support-contextProperties-START
	@Value("#{contextProperties['pageSize'] ?: 10}")
	int pageSize;

	@Value("#{contextProperties['pageUnit'] ?: 10}")
	int pageUnit;
	//Velocity-Support-contextProperties-END

	public void create(Movie movie) {
		movie.setMovieId("MV-" + System.currentTimeMillis());
		super.getSqlSession().insert("Movie.insertMovie", movie);
	}

	public void update(Movie movie) {
		super.getSqlSession().update("Movie.updateMovie", movie);
	}

	public void remove(String movieId) {
		super.getSqlSession().delete("Movie.deleteMovie", movieId);
	}

	public Movie get(String movieId) {
		return super.getSqlSession().selectOne("Movie.getMovie", movieId);
	}

	public Page getPagingList(Movie movie, int pageIndex) {
		Movie searchArgs = new Movie();
		searchArgs.setTitle("%" + movie.getTitle() + "%");
		searchArgs.setNowPlaying(movie.getNowPlaying());

		List<Movie> list = super.getSqlSession().selectList(
				"Movie.getMovieList", searchArgs,
				new RowBounds(pageSize * (pageIndex - 1), pageSize));

		int rowCount = (Integer) super.getSqlSession().selectOne(
				"Movie.getMovieListCnt", searchArgs);

		return new Page(list, pageIndex, rowCount, pageUnit, pageSize);
	}
	
	public Page getPagingListWithDate(String startDate, String endDate, int pageIndex) throws ParseException {
		Movie searchArgs = new Movie();
		DateFormat sdFormat = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
		Date start = sdFormat.parse(startDate);
		Date end = sdFormat.parse(endDate);

		searchArgs.setStartDate(start);
		searchArgs.setEndDate(end);

		List<Movie> list = super.getSqlSession().selectList(
				"Movie.getMovieListWithDate", searchArgs,
				new RowBounds(pageSize * (pageIndex - 1), pageSize));

		int rowCount = (Integer) super.getSqlSession().selectOne(
				"Movie.getMovieListCnt", searchArgs);

		return new Page(list, pageIndex, rowCount, pageUnit, pageSize);
	}
	
}
