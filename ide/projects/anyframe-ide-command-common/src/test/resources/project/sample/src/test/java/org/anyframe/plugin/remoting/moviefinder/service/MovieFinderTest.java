package org.anyframe.plugin.remoting.moviefinder.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.anyframe.plugin.core.moviefinder.service.MovieFinder;
import org.anyframe.plugin.domain.Movie;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:./src/test/resources/spring/context-*.xml" })
public class MovieFinderTest {

	@Inject
	@Named("remotingMovieFinderTestClient")
	private MovieFinder movieFinder;

	@Test
	public void findMovieList() throws Exception {
		Movie movie = new Movie();
		List<Movie> movieList = movieFinder.getList(movie);

		assertNotNull("movie list is not null", movieList);
		assertEquals(3, movieList.size());
	}
}
