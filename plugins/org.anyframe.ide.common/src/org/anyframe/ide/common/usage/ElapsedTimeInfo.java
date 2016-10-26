package org.anyframe.ide.common.usage;

public class ElapsedTimeInfo {

	private String pluginId;
	private String souceId;
	private long startTime;
	private long elapsedTime;
	private int beforeLineOfCode;
	private int afterLineOfCode;

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getSouceId() {
		return souceId;
	}

	public void setSouceId(String souceId) {
		this.souceId = souceId;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public int getBeforeLineOfCode() {
		return beforeLineOfCode;
	}

	public void setBeforeLineOfCode(int beforeLineOfCode) {
		this.beforeLineOfCode = beforeLineOfCode;
	}

	public int getAfterLineOfCode() {
		return afterLineOfCode;
	}

	public void setAfterLineOfCode(int afterLineOfCode) {
		this.afterLineOfCode = afterLineOfCode;
	}

}
