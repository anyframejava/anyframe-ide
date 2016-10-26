package org.anyframe.ide.command.aspect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.ant.util.FileUtils;
import org.codehaus.plexus.util.Os;

public aspect ReplaceInterceptor {
	private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

	pointcut renameMethod(): execution(* org.apache.tools.ant.util.FileUtils.rename(File, File));

	void around() throws IOException : renameMethod()  {
		Object[] arguments = thisJoinPoint.getArgs();

		File from = (File) arguments[0];
		File to = (File) arguments[1];

		from = FILE_UTILS.normalize(from.getAbsolutePath()).getCanonicalFile();
		to = FILE_UTILS.normalize(to.getAbsolutePath());

		if (!from.exists()) {
			System.err.println("Cannot rename nonexistent file " + from);
			return;
		}
		if (from.equals(to)) {
			System.err.println("Rename of " + from + " to " + to
					+ " is a no-op.");
			return;
		}

		if (to.exists() && !(from.equals(to.getCanonicalFile()))) {
			deleteFile(to);
		}
		File parent = to.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IOException("Failed to create directory " + parent
					+ " while trying to rename " + from);
		}
		if (!from.renameTo(to)) {
			copyFile(from, to);
			deleteFile(from);
		}
	}

	public boolean deleteFile(File file) {
		if (!file.delete()) {
			if (Os.isFamily("windows")) {
				System.gc();
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException ex) {
				// Ignore Exception
			}
			if (!file.delete()) {
				return false;
			}
		}
		return true;
	}

	public static void copyFile(File source, File destination)
			throws IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(destination);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} catch (IOException e) {
			System.out.println("[WARN] Copy a " + source.getAbsolutePath()
					+ " to " + destination.getAbsolutePath()
					+ " is skipped. The reason is a '" + e.getMessage() + "'.");
		} finally {
			in.close();
			out.close();
		}
	}
}
