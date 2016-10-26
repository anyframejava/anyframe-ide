package org.anyframe.ide.command.maven.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.anyframe.ide.command.common.util.CommonConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * This is an CreateVersionFileMojo class. This mojo is for creating
 * Version.java.
 * 
 * @goal create-versionfile
 * @execute phase="generate-sources"
 * @author Soyon Lim
 */
public class CreateVersionFileMojo extends AbstractPluginMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	private VelocityEngine velocity;

	/**
	 * @parameter default-value="yyyy/MM/dd"
	 * @readonly
	 */
	private String datePattern;

	/**
	 * main method for executing CreateVersionFileMojo. This mojo is executed
	 * when you input 'mvn anyframe:create-version-file'
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			if (!project.getPackaging().equals("pom")) {
				initializeVelocity();

				File srcFolder = new File(baseDir
						+ CommonConstants.SRC_MAIN_JAVA);

				if (!srcFolder.isDirectory()) {
					srcFolder.mkdirs();
				}

				File versionFile = new File(srcFolder, "Version.java");
				versionFile.createNewFile();

				Writer writer = new OutputStreamWriter(new FileOutputStream(
						versionFile), getEncoding());

				VelocityContext context = new VelocityContext();
				context.put("releaseVersion", project.getVersion());

				DateTime currentDateTime = new DateTime();
				DateTimeFormatter dateTimeFormatter = DateTimeFormat
						.forPattern(datePattern);
				context.put("releaseDate",
						dateTimeFormatter.print(currentDateTime));

				velocity.mergeTemplate("Version.vm", getEncoding(), context,
						writer);
				writer.flush();
			}
		} catch (Exception ex) {
			getLog().error(
					"Fail to execute CreateVersionFileMojo. The reason is '"
							+ ex.getMessage() + "'.");
			throw new MojoFailureException(null);
		}
	}

	/**
	 * initialize velocity runtime configuration
	 */
	private void initializeVelocity() throws Exception {
		// 1. initialize velocity engine
		velocity = new VelocityEngine();
		velocity.setProperty("runtime.log.logsystem.log4j.logger.level",
				"WARNING");
		velocity.setProperty("velocimacro.library", "");
		velocity.setProperty("resource.loader", "classpath");
		velocity.setProperty("classpath.resource.loader.class",
				"org.codehaus.plexus.velocity.ContextClassLoaderResourceLoader");
		velocity.setProperty("runtime.log.logsystem.class",
				"org.apache.velocity.runtime.log.NullLogSystem");
		velocity.init();
	}

	/**
	 * get encoding of file content
	 * 
	 * @param encoding
	 *            file encoding style
	 * @return encoding style, if entered argument is null, return "UTF-8"
	 */
	private String getEncoding() {
		return ((null == encoding) || "".equals(encoding)) ? "UTF-8" : encoding;
	}
}
