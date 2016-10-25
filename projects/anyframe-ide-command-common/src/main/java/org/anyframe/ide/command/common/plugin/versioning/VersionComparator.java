package org.anyframe.ide.command.common.plugin.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is an VersionComparator class. This class is for comparing to plugin
 * version.
 * 
 * @author SoYon Lim
 * 
 */
public class VersionComparator {
	// regular expression for defining delimiters
	private static final String DELIMITER_TYPES = "[<>=,]+";

	private static final String[] DELIMITERS = { "<", "<=", ">", ">=" };

	private static final List<String> _DELIMITERS = Arrays.asList(DELIMITERS);

	/**
	 * check if a specific version belongs to defined version range
	 * 
	 * @param sourcePluginVersionRange
	 *            version range includes defined delimiters
	 * @param targetPluginVersion
	 *            version
	 * @return true if a specific version belongs to version range, else false
	 */
	public static boolean isMatched(String sourcePluginVersionRange,
			String targetPluginVersion) {
		// 1. extract tokens
		sourcePluginVersionRange = sourcePluginVersionRange.replace(" ", "");
		String[] splittedVersions = sourcePluginVersionRange
				.split(DELIMITER_TYPES);

		// 2. extract delimiters
		List<Integer> delimiters = findDelimiters(sourcePluginVersionRange,
				splittedVersions);

		return isMatched(delimiters, splittedVersions, targetPluginVersion);
	}

	/**
	 * get latest version under defined version range
	 * 
	 * @param versionRange
	 *            version range includes defined delimiters
	 * @param releaseVersions
	 *            released versions
	 * @return latest version
	 */
	public static String getLatest(String versionRange,
			List<String> releaseVersions) {
		// 1. extract tokens
		versionRange = versionRange.replace(" ", "");
		String[] splittedVersions = versionRange.split(DELIMITER_TYPES);

		// 2. extract delimiters
		List<Integer> delimiters = findDelimiters(versionRange,
				splittedVersions);

		// 3. check version range
		List<String> versions = new ArrayList<String>();
		for (String releaseVersion : releaseVersions) {
			if (isMatched(delimiters, splittedVersions, releaseVersion)) {
				versions.add(releaseVersion);
			}
		}

		return getLatest(versions);
	}

	/**
	 * get latest version under defined version range
	 * 
	 * @param versions
	 *            versions to be compared
	 * @return latest version
	 */
	private static String getLatest(List<String> versions) {
		String latestVersion = null;

		for (String version : versions) {
			if (latestVersion == null) {
				latestVersion = version;
				continue;
			}

			if (new ComparableVersion(version).compareTo(new ComparableVersion(
					latestVersion)) > 0) {
				latestVersion = version;
			}
		}

		return latestVersion;
	}

	/**
	 * find delimiters in version range
	 * 
	 * @param versionRange
	 *            version range consist of delimiters
	 * @param splittedVersions
	 *            versions
	 * @return delimiter numbers
	 */
	private static List<Integer> findDelimiters(String versionRange,
			String[] splittedVersions) {
		// 2. extract delimiters
		List<Integer> delimiters = new ArrayList<Integer>();
		for (int i = 0; i < splittedVersions.length - 1; i++) {
			int beginIndex = versionRange.indexOf(splittedVersions[i])
					+ splittedVersions[i].length();
			int endIndex = versionRange.indexOf(splittedVersions[i + 1]);

			delimiters.add(_DELIMITERS.indexOf(versionRange.substring(
					beginIndex, endIndex)));
		}

		return delimiters;
	}

	/**
	 * check if a specific version belongs to versions
	 * 
	 * @param delimiters
	 *            defined delimiters
	 * @param versions
	 *            versions to check
	 * @param targetVersion
	 *            version
	 * @return true if a specific version belongs to versions, else false
	 */
	private static boolean isMatched(List<Integer> delimiters,
			String[] versions, String targetVersion) {
		int i = 0;
		String leftVersion = null;
		String rightVersion = null;
		boolean isMatched = true;

		for (int delimiter : delimiters) {
			leftVersion = versions[i];
			rightVersion = versions[++i];

			switch (delimiter) {
			case 0:/* in case of < */
				if (leftVersion.equals("*")) {
					if (!lessThan(targetVersion, rightVersion)) {
						isMatched = false;
					}
				} else {
					if (!greaterThan(targetVersion, leftVersion)) {
						isMatched = false;
					}
				}
				break;
			case 1:/* in case of <= */
				if (leftVersion.equals("*")) {
					if (!lessThanOrEqualTo(targetVersion, rightVersion)) {
						isMatched = false;
					}
				} else {
					if (!greaterThanOrEqualTo(targetVersion, leftVersion)) {
						isMatched = false;
					}
				}
				break;
			case 2:/* in case of > */
				if (leftVersion.equals("*")) {
					if (!greaterThan(targetVersion, rightVersion)) {
						isMatched = false;
					}
				} else {
					if (!lessThan(targetVersion, leftVersion)) {
						isMatched = false;
					}
				}
				break;
			case 3:/* in case of >= */
				if (leftVersion.equals("*")) {
					if (!greaterThanOrEqualTo(targetVersion, rightVersion)) {
						isMatched = false;
					}
				} else {
					if (!lessThanOrEqualTo(targetVersion, leftVersion)) {
						isMatched = false;
					}
				}
				break;
			default:/* in case of , */
				if (!equalsTo(targetVersion, leftVersion)
						&& !equalsTo(targetVersion, rightVersion)) {
					isMatched = false;
				}
			}

			if (!isMatched) {
				break;
			}
		}

		return isMatched;
	}

	/**
	 * check if source is greater than target
	 * 
	 * @param source
	 *            version
	 * @param target
	 *            version
	 * @return true if source>target, else false
	 */
	public static boolean greaterThan(String source, String target) {
		if (new ComparableVersion(source).compareTo(new ComparableVersion(
				target)) > 0) {
			return true;
		}
		return false;
	}

	/**
	 * check if source is greater than or equal to target
	 * 
	 * @param source
	 *            version
	 * @param target
	 *            version
	 * @return true if source>=target, else false
	 */
	public static boolean greaterThanOrEqualTo(String source, String target) {
		if (new ComparableVersion(source).compareTo(new ComparableVersion(
				target)) >= 0) {
			return true;
		}
		return false;
	}

	/**
	 * check if source is less than target
	 * 
	 * @param source
	 *            version
	 * @param target
	 *            version
	 * @return true if source<target, else false
	 */
	public static boolean lessThan(String source, String target) {
		if (new ComparableVersion(source).compareTo(new ComparableVersion(
				target)) < 0) {
			return true;
		}
		return false;
	}

	/**
	 * check if source is less than or equal to target
	 * 
	 * @param source
	 *            version
	 * @param target
	 *            version
	 * @return true if source<=target, else false
	 */
	public static boolean lessThanOrEqualTo(String source, String target) {
		if (new ComparableVersion(source).compareTo(new ComparableVersion(
				target)) <= 0) {
			return true;
		}
		return false;
	}

	/**
	 * check if source is equal to target
	 * 
	 * @param source
	 *            version
	 * @param target
	 *            version
	 * @return true if source=target, else false
	 */
	public static boolean equalsTo(String source, String target) {
		if (new ComparableVersion(source).compareTo(new ComparableVersion(
				target)) == 0) {
			return true;
		}
		return false;
	}
}
