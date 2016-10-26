/*
 * Copyright 2002-2013 the original author or authors.
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
package org.anyframe.ide.common.usage;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

/**
 * This is UsageCheckAdapter class.
 * 
 * @author Dongin Jung
 */
public class UsageCheckAdapter implements SelectionListener,
		IDoubleClickListener {

	private String eventSourceId;

	private SelectionAdapter selectionAdapter;
	private IDoubleClickListener doubleClickListener;

	public UsageCheckAdapter(String eventSourceId,
			SelectionAdapter selectionAdapter) {
		this.eventSourceId = eventSourceId;
		this.selectionAdapter = selectionAdapter;
	}

	public UsageCheckAdapter(String eventSourceId,
			IDoubleClickListener doubleClickListener) {
		this.eventSourceId = eventSourceId;
		this.doubleClickListener = doubleClickListener;
	}

	public UsageCheckAdapter(String eventSourceId) {
		this.eventSourceId = eventSourceId;
		writeLog();
	}

	public void widgetSelected(SelectionEvent e) {
		writeLog();
		selectionAdapter.widgetSelected(e);
	}

	public void widgetDefaultSelected(SelectionEvent e) {
		writeLog();
		selectionAdapter.widgetDefaultSelected(e);
	}

	public void doubleClick(DoubleClickEvent event) {
		writeLog();
		doubleClickListener.doubleClick(event);
	}

	private void writeLog() { 
		UsageLogger logger = new UsageLogger();
		logger.write(eventSourceId);
	}

}
