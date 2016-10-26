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
package org.anyframe.ide.querymanager.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.anyframe.ide.querymanager.QueryManagerActivator;
import org.anyframe.ide.querymanager.preferences.AnyframePreferencePage;
import org.anyframe.ide.querymanager.preferences.PreferencesHelper;
import org.anyframe.ide.querymanager.util.AnyframeJarLoader;
import org.anyframe.ide.querymanager.util.BuilderUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

/**
 * This is QMProposalProcessor class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 */
public final class QMProposalProcessor implements IContentAssistProcessor {
	private String serviceType;
	private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	String classAttributes[];
	String classDataTypes[];
	boolean found = false;
	String prefixToken = null;
	String suffixToken = null;
	HashMap preferencesMap;
	HashMap mehtodMap;

	// String prefixAndSuffixesForAbstractDAO[] = null;

	public QMProposalProcessor() {
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset) {
		// There can be two cases from 1.3.0
		// 1) DAO which extends AbstractDAO.
		// 2) DAO which does not extend AbstractDAO but uses IQueryService.

		// Find the file.
		IWorkbenchPage activePage = QueryManagerActivator.getDefault()
				.getActiveWorkbenchPage();
		IEditorPart part = activePage.getActiveEditor();
		IEditorInput input = part.getEditorInput();
		IFileEditorInput ifei = (IFileEditorInput) input;
		IFile file = ifei.getFile();
		// Find project for getting queryIds.
		IProject project = file.getProject();
		PreferencesHelper preferencesHelper = PreferencesHelper
				.getPreferencesHelper();
		preferencesMap = new HashMap();
		preferencesMap = preferencesHelper
				.populateHashMapWithPreferences(preferencesMap);
		mehtodMap = preferencesHelper
				.populateHashMapWithAbstractDAOMethodNames();
		// 1.3 Find QS or EQS
		// 1.3 do not fine now, get it from
		// QMProposalComputer. (changed on 15th
		// September 2008)

		// /The returned serviceType will contain
		// "abstractDAO" or "iQueryService" or "technicalService"

		this.serviceType = findQsOrEqs(this.serviceType);

		// 2.find whether we can show content assist
		// 2.1 use methodArr for QS and EQS
		// respectively

		boolean showContentAssist = canShowContentAssist(this.serviceType,
				viewer, documentOffset, project);
		IDocument doc = viewer.getDocument();
		IRegion content = findWord(doc, documentOffset);
		String token = "";
		try {
			token = doc.get(content.getOffset(), content.getLength());
		} catch (BadLocationException e) {
			token = "";
		}
		if (showContentAssist) {
			// So, show content assist. :-)
			String queryId = "";
			String eQSqueryId = "";
			// 3.Find Queries based on serviceType AND token
			HashMap map = getQueryIdsForCompProposals(project, token,
					this.serviceType);
			ArrayList aList = new ArrayList(map.values());
			ICompletionProposal[] proposals = new ICompletionProposal[map
					.size()];
			Iterator mapIterator = map.keySet().iterator();
			ICompletionProposal testProposal = null;
			// ArrayList aList = new ArrayList(
			// map.values ());
			Iterator itr = aList.iterator();
			// 4.Create ICompletion proposals
			int k = 0;
			while (itr.hasNext()) {
				Location loc = (Location) itr.next();
				// serviceType = loc.getServiceType();
				if (serviceType.equalsIgnoreCase("iQueryService")
						|| serviceType.equalsIgnoreCase("technicalService")) {
					queryId = loc.getKey();
					testProposal = new CompletionProposal(queryId,
							content.getOffset(), token.length(),
							queryId.length());
				} else {
					// Handle all the scenarios.
					String displayId = PreferencesHelper.getPreferencesHelper()
							.getTheDisplayValueForQuery(loc.getKey());
					testProposal = new CompletionProposal(displayId,
							content.getOffset(), token.length(),
							queryId.length(), null, loc.getKey(), null,
							"Additional Proposal Info");
				}
				proposals[k++] = testProposal;
			}
			// 5 return proposals :-)
			return proposals;
		}
		return new ICompletionProposal[0];
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int documentOffset, String serviceType) {
		this.serviceType = serviceType;
		return this.computeCompletionProposals(viewer, documentOffset);
	}

	private boolean canShowContentAssist(String serviceType,
			ITextViewer viewer, int documentOffset, IProject project) {
		IDocument doc = viewer.getDocument();
		boolean isQsOrEqsOk = false;
		IRegion reg = null;
		try {
			reg = doc.getLineInformationOfOffset(documentOffset);
			String queryServiceMethodName = doc.get(reg.getOffset(),
					reg.getLength());
			if (serviceType.equalsIgnoreCase("iQueryService")) {
				if (queryServiceMethodName.indexOf('.') <= 0)
					queryServiceMethodName = queryServiceMethodName.substring(
							0, queryServiceMethodName.indexOf('(')).trim();
				else
					queryServiceMethodName = queryServiceMethodName.substring(
							queryServiceMethodName.lastIndexOf('.') + 1,
							queryServiceMethodName.indexOf('"') - 1).trim();

				HashMap methodMap = BuilderUtil.getQsMethodMap();
				Iterator methodMapItr = methodMap.keySet().iterator();
				methodMapLoop: while (methodMapItr.hasNext()) {
					String qsMethodName = methodMap.get(
							methodMapItr.next().toString()).toString();
					if (qsMethodName.equalsIgnoreCase(queryServiceMethodName)) {
						isQsOrEqsOk = true;
						break methodMapLoop;
					}
				}
				// String methodArr[] =
				// new String[] {"findWithRowCount", "findBySQL",
				// 		"findBySQLWithRowCount", "createBySQL", "updateBySQL",
				// 		"removeBySQL", "executeBySQL", "batchUpdate",
				// 		"batchUpdateBySQL", "getStatement", "getQueryParams",
				// 		"find", "remove", "create", "update", "execute" };
				// for (int i = 0; i < methodArr.length; i++) {
				// 		if (methodArr[i].equalsIgnoreCase(queryServiceMethodName)) {
				// 			isQsOrEqsOk = true;
				// 		}
				// }
			} else if (serviceType.equalsIgnoreCase("abstractDao")) {
				prefixToken = null;
				suffixToken = null;
				// prefixAndSuffixesForAbstractDAO =
				// 		PreferencesHelper.getPreferencesHelper().populateArrayWithPreferences(prefixAndSuffixesForAbstractDAO);
				String createMethod = this.mehtodMap.get(
						AnyframePreferencePage.CREATE_PREFIX_ID).toString();
				// String findMethod =
				// 		this.mehtodMap.get(AnyframePreferencePage.FIND_PREFIX_ID).toString();
				String removeMethod = this.mehtodMap.get(
						AnyframePreferencePage.REMOVE_PREFIX_ID).toString();
				String updateMethod = this.mehtodMap.get(
						AnyframePreferencePage.UPDATE_PREFIX_ID).toString();
				String findByPkMethod = this.mehtodMap.get(
						AnyframePreferencePage.FIND_BYPK_SUFFIX_ID).toString();
				String findListMethod = this.mehtodMap.get(
						AnyframePreferencePage.FIND_LIST_SUFFIX_ID).toString();

				if (queryServiceMethodName.indexOf(findByPkMethod) > 0) {
					prefixToken = preferencesMap.get(
							AnyframePreferencePage.FIND_PREFIX_ID).toString();
					suffixToken = preferencesMap.get(
							AnyframePreferencePage.FIND_BYPK_SUFFIX_ID)
							.toString();
					isQsOrEqsOk = true;
				} else if (queryServiceMethodName.indexOf(findListMethod) > 0) {
					prefixToken = preferencesMap.get(
							AnyframePreferencePage.FIND_PREFIX_ID).toString();
					suffixToken = preferencesMap.get(
							AnyframePreferencePage.FIND_LIST_SUFFIX_ID)
							.toString();
					isQsOrEqsOk = true;
				} else if (queryServiceMethodName.indexOf(createMethod) > 0) {
					prefixToken = preferencesMap.get(
							AnyframePreferencePage.CREATE_PREFIX_ID).toString();
					suffixToken = null;
					isQsOrEqsOk = true;
				}
				// else if(queryServiceMethodName.indexOf(findMethod) > 0){
				// 		prefixToken =
				// 			preferencesMap.get(AnyframePreferencePage.FIND_PREFIX_ID).toString();
				// 		suffixToken = null;
				// 		isQsOrEqsOk = true;
				// }
				else if (queryServiceMethodName.indexOf(removeMethod) > 0) {
					prefixToken = preferencesMap.get(
							AnyframePreferencePage.REMOVE_PREFIX_ID).toString();
					suffixToken = null;
					isQsOrEqsOk = true;
				} else if (queryServiceMethodName.indexOf(updateMethod) > 0) {
					prefixToken = preferencesMap.get(
							AnyframePreferencePage.UPDATE_PREFIX_ID).toString();
					suffixToken = null;
					isQsOrEqsOk = true;
				} else {
					prefixToken = null;
					suffixToken = null;
				}
				// Iterator methodItr = this.mehtodMap.keySet().iterator();
				// while(methodItr.hasNext() && !isQsOrEqsOk){
				// 		String methodName = methodItr.next().toString();
				// 		if(queryServiceMethodName.indexOf(this.mehtodMap.get(methodName).toString())
				// 			> 0) {
				// 				prefixToken = preferencesMap.get(methodName).toString();
				//				isQsOrEqsOk = true;
				// 		}
				// }

			} else {

				// Technical Service.

				AnyframeJarLoader loader = new AnyframeJarLoader();
				HashMap classMethodsMap = null;
				classMethodsMap = loader
						.getRuntimeProjectTechnicalServicesDetails(project);
				HashMap methodMap = new HashMap();
				HashMap methMapHolder = new HashMap();
				String qsVar = "";
				if (classMethodsMap != null && classMethodsMap.size() > 0) {
					Iterator classMethodMapIterator = classMethodsMap.keySet()
							.iterator();
					while (classMethodMapIterator.hasNext()) {
						// String className =
						// classMethodMapIterator.next().toString();
						final String importClassFullyQualName = classMethodMapIterator
								.next().toString();
						final String className = importClassFullyQualName
								.substring(importClassFullyQualName
										.lastIndexOf('.') + 1);
						// BuilderHelper helper = new BuilderHelper();
						qsVar = BuilderUtil.getVarName(doc.get(),
								importClassFullyQualName, className);
						if (qsVar != null) {
							Collection methodCollection = (Collection) classMethodsMap
									.get(importClassFullyQualName);
							Iterator methodCollectionItr = methodCollection
									.iterator();
							while (methodCollectionItr.hasNext()) {
								String methName = methodCollectionItr.next()
										.toString();
								methMapHolder.put(methName, methName);
							}
							// break;
						}
						methMapHolder = BuilderUtil.appendVarToMethods(
								methMapHolder, qsVar + ".");
						methMapHolder.put(className + ".", className + ".");
						methodMap.putAll(methMapHolder);
					}
					// got the class and methods.
					// methodMap = BuilderUtil.appendVarToMethods(methodMap,
					// qsVar + ".");
					// methodMap.put(className+".", className+".");
				}
				Iterator methodItr = methodMap.keySet().iterator();
				while (methodItr.hasNext() && !isQsOrEqsOk) {
					String methodName = methodItr.next().toString();
					if (queryServiceMethodName.indexOf(methodMap
							.get(methodName).toString()) > 0) {
						// prefixToken =
						// preferencesMap.get(methodName).toString();
						isQsOrEqsOk = true;
					}
				}
			}

		} catch (BadLocationException e) {

		}

		if (isQsOrEqsOk) {
			try {
				IRegion content = findWord(doc, documentOffset);
				// get first double quote index
				// get second double quote index

				// check if the word region falls
				// between this.
				// and check if the attribute is a
				// IQueryService.
				// if so , show content assist
				int firstDoubleQuoteIndex = -1;
				int secondDoubleQuoteIndex = -1;
				// String token = null;
				firstDoubleQuoteIndex = doc
						.get(reg.getOffset(), reg.getLength()).toString()
						.indexOf("\"");
				if (firstDoubleQuoteIndex > -1)
					secondDoubleQuoteIndex = doc
							.get(reg.getOffset(), reg.getLength()).toString()
							.indexOf("\"", firstDoubleQuoteIndex + 1);
				firstDoubleQuoteIndex = firstDoubleQuoteIndex > -1 ? (reg
						.getOffset() + firstDoubleQuoteIndex)
						: firstDoubleQuoteIndex;
				secondDoubleQuoteIndex = secondDoubleQuoteIndex > -1 ? (reg
						.getOffset() + secondDoubleQuoteIndex)
						: secondDoubleQuoteIndex;
				// token = doc.get(content.getOffset(), content.getLength());
				if (firstDoubleQuoteIndex > -1 && secondDoubleQuoteIndex > -1
						&& content.getOffset() > firstDoubleQuoteIndex
						&& content.getOffset() <= secondDoubleQuoteIndex) {
					return true;
				} else {
					return false;
				}
			} catch (BadLocationException badLocationException) {
				return false;
			}
		} else {
			return false;
		}
	}

	private String findQsOrEqs(String string) {
		if (string.indexOf("AbstractDAO") > -1)
			return "abstractDAO";
		else if (string.indexOf("IQueryService") > -1)
			return "iQueryService";
		else if (string.indexOf("QueryService") > -1)
			return "iQueryService";
		else
			return "technicalService";
	}

	private HashMap getQueryIdsForCompProposals(IProject project, String token,
			String serviceType) {

		// collect all query Ids from project
		HashMap map = BuilderHelper.getInstance().collectAllQueryIdsForProject(
				project);
		HashMap newMap = null;
		if (token != null && !token.equalsIgnoreCase("")) {
			// if he entered some token return query
			// ids start with that token

			// Check the serviceType, if it is extended
			// Query Service.
			// if we are invoking the Content Assist
			// for insert method,
			// only the queries starting with insert
			// should be shown.
			// and if the user enters a token, then,
			// insert<keyedInChars>*** should only be
			// shown.
			if (prefixToken != null
					&& serviceType.equalsIgnoreCase("abstractDao")) {
				token = prefixToken + token;
			}
			newMap = new HashMap();
			Iterator mapIterator = map.keySet().iterator();
			while (mapIterator.hasNext()) {
				Location location = null;
				Collection duplicateIds = null;
				Object individualQueryId = mapIterator.next();
				Object fileName = map.get(individualQueryId);
				if (fileName instanceof Location)
					location = (Location) fileName;
				else if (fileName instanceof ArrayList)
					duplicateIds = (ArrayList) fileName;
				if (duplicateIds != null) {
					Iterator itr = duplicateIds.iterator();
					while (itr.hasNext()) {
						Location obj = (Location) itr.next();
						String queryId = obj.getKey();
						// String thisServiceType = obj.getServiceType();

						// if(queryId.toUpperCase().startsWith(token.toUpperCase())
						// 		&& serviceType.equalsIgnoreCase(thisServiceType)) {

						if (suffixToken != null) {
							if (queryId.startsWith(token)
									&& queryId.endsWith(suffixToken)) {
								newMap.put(queryId, obj);
							}
						} else {
							if (queryId.startsWith(token)) {
								newMap.put(queryId, obj);
							}
						}

						// if(queryId.toUpperCase().startsWith(token.toUpperCase()))
						// {
						// 		newMap.put(queryId, obj);
						// }
					}
				} else if (location != null) {
					String queryId = location.getKey();
					if (suffixToken != null) {
						if (queryId.startsWith(token)
								&& queryId.endsWith(suffixToken)) {
							newMap.put(queryId, location);
						}
					} else {
						if (queryId.startsWith(token)) {
							newMap.put(queryId, location);
						}
					}

				}
			} // while
		} else {
			// token is null or empty string.
			newMap = new HashMap();
			Iterator mapIterator = map.keySet().iterator();
			while (mapIterator.hasNext()) {
				Location location = null;
				Collection duplicateIds = null;
				Object individualQueryId = mapIterator.next();
				Object fileName = map.get(individualQueryId);
				if (fileName instanceof Location)
					location = (Location) fileName;
				else if (fileName instanceof ArrayList)
					duplicateIds = (ArrayList) fileName;
				if (duplicateIds != null) {
					Iterator itr = duplicateIds.iterator();
					while (itr.hasNext()) {
						Location obj = (Location) itr.next();
						String queryId = obj.getKey();
						// String thisServiceType = obj.getServiceType();

						// if (serviceType.equalsIgnoreCase(thisServiceType)) {
						if (serviceType.equalsIgnoreCase("abstractDao")) {
							if (suffixToken != null) {
								if (prefixToken != null) {
									if (queryId.startsWith(prefixToken)
											&& queryId.endsWith(suffixToken)) {
										newMap.put(queryId, obj);
									}
								}
							} else {
								if (prefixToken != null) {
									if (queryId.startsWith(prefixToken)) {
										newMap.put(queryId, obj);
									}
								}
							}
							// if(queryId.toUpperCase().startsWith(prefixToken.toUpperCase()))
							// {
							// 		newMap.put(queryId, obj);
							// }
						} else
							newMap.put(queryId, obj);
						// }
					}
				} else if (location != null) {
					String queryId = location.getKey();
					// String thisServiceType = location.getServiceType();
					// if (serviceType.equalsIgnoreCase(thisServiceType)) {
					if (serviceType.equalsIgnoreCase("abstractDao")) {
						if (suffixToken != null) {
							if (prefixToken != null) {
								if (queryId.startsWith(prefixToken)
										&& queryId.endsWith(suffixToken)) {
									newMap.put(queryId, location);
								}
							}
						} else {
							if (prefixToken != null) {
								if (queryId.startsWith(prefixToken)) {
									newMap.put(queryId, location);
								}
							}
						}
						// if(queryId.toUpperCase().startsWith(prefixToken.toUpperCase()))
						// {
						// 		newMap.put(queryId, obj);
						// }
						// if(queryId.toUpperCase().startsWith(prefixToken.toUpperCase()))
						// {
						// 		newMap.put(queryId, location);
						// }
					} else
						newMap.put(queryId, location);
					// }
				}
			} // while
		}
		return newMap;
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return NO_CONTEXTS;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '\"' };
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/**
	 * Utility method to return the position of the token to consider showing
	 * the proposals.
	 * 
	 * @param document
	 *            Document object
	 * @param offset
	 *            start position, where content assist is invoked.
	 * @return IRegion object having the position information
	 */
	protected IRegion findWord(IDocument document, int offset) {
		int start = -1;
		int end = -1;

		try {
			int pos = offset - 1;
			char c;
			while (pos >= 0) {
				c = document.getChar(pos);
				if (".".equalsIgnoreCase(Character.toString(c))) {
					--pos;
					continue;
				}
				if (!Character.isJavaIdentifierPart(c)) {
					break;
				}
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (".".equalsIgnoreCase(Character.toString(c))) {
					++pos;
					continue;
				}
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
			start = -1;
			end = -1;
		}

		if (start > -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	public String getErrorMessage() {
		return null;
	}

}
