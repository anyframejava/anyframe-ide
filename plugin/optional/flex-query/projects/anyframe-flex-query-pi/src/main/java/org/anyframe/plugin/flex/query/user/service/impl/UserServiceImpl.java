/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.anyframe.plugin.flex.query.user.service.impl;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.anyframe.plugin.flex.query.domain.SearchVO;
import org.anyframe.plugin.flex.query.domain.User;
import org.anyframe.plugin.flex.query.user.service.UserService;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
@RemotingDestination
@Transactional(rollbackFor = { Exception.class }, propagation = Propagation.REQUIRED)
public class UserServiceImpl implements UserService {
	
	@Inject
	@Named("userDao")
	private UserDao userDao;

	public int create(User user) throws Exception {
		return userDao.create(user);
	}

	public int update(User user) throws Exception {
		return userDao.update(user);
	}

	public int remove(User user) throws Exception {
		return userDao.remove(user);
	}

	public Map<String, Integer> saveAll(List<User> list) throws Exception {
		return userDao.saveAll(list);
	}

	public List<User> getList(SearchVO searchVO) throws Exception {
		return userDao.getList(searchVO);
	}
	
}