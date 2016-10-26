package org.anyframe.plugin.core.moviefinder.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.anyframe.plugin.core.moviefinder.service.MovieFinder;
import org.anyframe.plugin.core.moviefinder.service.MovieService;
import org.anyframe.plugin.domain.Genre;
import org.anyframe.plugin.domain.Movie;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:./src/main/resources/spring/context-*.xml" })
public class MovieServiceTest {
	@Inject
	@Named("coreMovieService")
	private MovieService movieService;

	@Inject
	@Named("coreMovieFinder")
	private MovieFinder movieFinder;

	@Test
	@Rollback(value = true)
	public void manageMovie() throws Exception {
		// 1. create a new movie
		Movie movie = getMovie();
		movieService.create(movie);

		// 2. assert - create
		movie = movieService.get(movie.getMovieId());
		assertNotNull("fail to fetch a movie", movie);
		assertEquals("fail to compare a movie title", "Shrek (2001)", movie
				.getTitle());

		// 3. update a title of movie
		String title = "Shrek 2 " + System.currentTimeMillis();
		movie.setTitle(title);
		movieService.update(movie);

		// 4. assert - update
		movie = movieService.get(movie.getMovieId());
		assertNotNull("fail to fetch a movie", movie);
		assertEquals("fail to compare a updated title", title, movie.getTitle());

		// 5. remove a movie
		movieService.remove(movie.getMovieId());

		// 6. assert - remove
		movie = movieService.get(movie.getMovieId());
		assertNull("fail to remove a movie", movie);
	}
	
	@Test
	public void findMovie() throws Exception {
		Movie movie = movieService.get("MV-00001");
		assertNotNull("fail to fetch a movie", movie);
	}	

	@Test
	public void findMovieList() throws Exception {
		Movie movie = new Movie();
		List<Movie> movieList = movieFinder.getList(movie);

		assertNotNull("movie list is not null", movieList);
		assertEquals(3, movieList.size());
	}

	private Movie getMovie() throws Exception {
		Genre genre = new Genre();
		genre.setGenreId("GR-03");

		Movie movie = new Movie();
		movie.setTitle("Shrek (2001)");
		movie.setActors("Shrek");
		movie.setDirector("Andrew Adamson");
		movie.setGenre(genre);
		movie.setReleaseDate(new Date());
		movie.setRuntime(new Long(90));
		movie.setTicketPrice(new Float(8000));
		movie.setNowPlaying("N");

		return movie;
	}
}
