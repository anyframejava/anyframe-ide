package org.anyframe.plugin.core.moviefinder.service;

import java.util.List;

import org.anyframe.plugin.domain.Genre;

public interface GenreService {
	List<Genre> getList() throws Exception;
}
