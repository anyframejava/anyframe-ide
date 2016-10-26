/*
 * Copyright 2008-2013 the original author or authors.
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
package org.anyframe.ide.querymanager.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * This is QMMarkerResolutionGenerator class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public class QMMarkerResolutionGenerator implements IMarkerResolutionGenerator {

	public IMarkerResolution[] getResolutions(IMarker marker) {
		IProject project = marker.getResource().getProject();
		ArrayList resolutions = new ArrayList();
		HashMap queryIds = BuilderHelper.getInstance()
				.collectAllQueryIdsForProject(project);

		String duplicateQueryID = "";
		try {
			String markerMsg = (String) marker.getAttribute(IMarker.MESSAGE);
			duplicateQueryID = markerMsg.substring(markerMsg.indexOf(':') + 1)
					.trim();
		} catch (CoreException e) {
			e.printStackTrace();
		}

		Location loc = null;
		Location renameLoc = null;

		if (queryIds.get(duplicateQueryID) != null) {
			loc = queryIds.get(duplicateQueryID) instanceof Location ? (Location) queryIds
					.get(duplicateQueryID) : null;
			if (loc == null) {
				Collection locCollection = (ArrayList) queryIds
						.get(duplicateQueryID);

				Iterator itr = locCollection.iterator();

				while (itr.hasNext()) {
					loc = (Location) itr.next();
					resolutions.add(new QMMarkerResolution(loc));

					if (loc.getFile().getName()
							.equals(marker.getResource().getName())) {
						renameLoc = loc;
					}

				}

			}
		}
		// resolutions.add(new QMRenameQueryIdResolution(renameLoc));

		return (IMarkerResolution[]) resolutions
				.toArray(new IMarkerResolution[resolutions.size()]);
	}

	public boolean hasResolutions(IMarker marker) {
		return true;
	}

}
