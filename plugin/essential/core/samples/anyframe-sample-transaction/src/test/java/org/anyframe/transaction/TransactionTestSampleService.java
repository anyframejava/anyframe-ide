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
package org.anyframe.transaction;

import java.util.List;

/**
 * This TransactionTestSampleService class is an Interface class for transaction
 * test.
 * 
 * @author SoYon Lim
 * @author JongHoon Kim
 */
public interface TransactionTestSampleService {
	void insertData(Transaction transaction) throws Exception;

	void updateData(Transaction transaction) throws Exception;

	void removeData(Transaction transaction) throws Exception;

	List<Transaction> listData(Transaction transaction) throws Exception;

	public int getCommitCount();

	public int getRollbackCount();
}
