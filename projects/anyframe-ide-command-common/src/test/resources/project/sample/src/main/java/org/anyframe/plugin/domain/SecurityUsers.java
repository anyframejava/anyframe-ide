package org.anyframe.plugin.domain;

import java.io.Serializable;

public class SecurityUsers implements Serializable {

	private static final long serialVersionUID = -2181114344703204906L;

	private String userId;
	private String userName;
	private String password;
	private Integer enabled;
	private Long age;
	private String cellPhone;
	private String addr;
	private String email;
	private String createDate;
	private String modifyDate;

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEnabled(Integer enabled) {
		this.enabled = enabled;
	}

	public Integer getEnabled() {
		return enabled;
	}

	public Long getAge() {
		return this.age;
	}

	public void setAge(Long age) {
		this.age = age;
	}

	public String getCellPhone() {
		return this.cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public String getAddr() {
		return this.addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getModifyDate() {
		return modifyDate;
	}

	@Override
	public String toString() {
		return "[addr=" + addr + ", age=" + age + ", cellPhone=" + cellPhone
				+ ", createDate=" + createDate + ", \nemail=" + email
				+ ", enabled=" + enabled + ", modifyDate=" + modifyDate
				+ ", password=" + password + ", \nuserId=" + userId
				+ ", userName=" + userName + "]";
	}
}
