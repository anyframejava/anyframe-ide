package org.anyframe.plugin.core.moviefinder.service;

import java.util.List;

import org.anyframe.plugin.domain.Movie;

public interface MovieFinder {
	List<Movie> getList(Movie movie) throws Exception;
}
