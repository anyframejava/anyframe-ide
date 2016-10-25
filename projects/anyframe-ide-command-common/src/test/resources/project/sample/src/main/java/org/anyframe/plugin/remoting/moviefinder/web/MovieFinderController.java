package org.anyframe.plugin.remoting.moviefinder.web;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.anyframe.plugin.core.moviefinder.service.MovieFinder;
import org.anyframe.plugin.domain.Movie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("remotingMovieFinderController")
@RequestMapping("/remotingMovieFinder.do")
public class MovieFinderController {

	@Inject
	@Named("remotingMovieFinderClient")
	private MovieFinder movieFinder;

	@RequestMapping(params = "method=list")
	public String list(Movie movie, BindingResult result, Model model)
			throws Exception {

		List<Movie> movieList = movieFinder.getList(movie);

		model.addAttribute("movie", movie);
		model.addAttribute("movies", movieList);

		return "remotingListMovie";
	}
}
