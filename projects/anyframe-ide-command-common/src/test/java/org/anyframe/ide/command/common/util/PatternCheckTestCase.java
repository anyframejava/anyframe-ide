package org.anyframe.ide.command.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class PatternCheckTestCase extends TestCase {
	public void testPattern() throws Exception {
		String pluginName = "ibatis2";

		Pattern pattern = Pattern.compile("(?i)(?u)(?s)\\A(?:(?:\\<\\!\\-\\-"
				+ pluginName
				+ "\\-)).*(?i)(?u)(?s).*(?:(?:\\-START\\-\\-\\>))\\z");

		String aLine = "<!--iBatis2-getMovieList-START-->";
		Matcher matcher = pattern.matcher(aLine);

		assertTrue("fail to match with ignoring case.", matcher.find());

		pattern = Pattern.compile("(?s)\\A(?:(?:\\<\\!\\-\\-" + pluginName
				+ "\\-)).*(?i)(?u)(?s).*(?:(?:\\-START\\-\\-\\>))\\z");

		matcher = pattern.matcher(aLine);

		assertFalse("fail to match with ignoring case.", matcher.find());
	}
}
