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
package org.anyframe.plugin.core.moviefinder.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.anyframe.pagination.Page;
import org.anyframe.plugin.core.domain.Movie;
import org.anyframe.plugin.core.moviefinder.service.MovieFinder;
import org.springframework.stereotype.Service;

/**
 * This MovieFinderImpl class is an Implementation class to provide movie list
 * functionality.
 * 
 * @author Sooyeon Park
 */
@Service("coreMovieFinder")
public class MovieFinderImpl implements MovieFinder {

	@Inject
	@Named("coreMovieDao")
	private MovieDao movieDao;

	public Page getPagingList(Movie movie, int pageIndex) throws Exception {
		return movieDao.getPagingList(movie, pageIndex);
	}

}
