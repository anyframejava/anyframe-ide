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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * This is QMProposalComputer class.
 * 
 * @author Surindhar.Kondoor
 * @author Sreejesh.Nair
 * @author Tulasi.m
 */
public class QMProposalComputer implements IJavaCompletionProposalComputer {

	private String fErrorMessage;
	private final QMProposalProcessor fProcessor = new QMProposalProcessor();
	private static final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];

	public QMProposalComputer() {
	}

	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		try {
			if (context instanceof JavaContentAssistInvocationContext) {
				JavaContentAssistInvocationContext javaContext = (JavaContentAssistInvocationContext) context;
				return internalComputeCompletionProposals(
						context.getInvocationOffset(), javaContext, monitor);
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			// ae.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	public List internalComputeCompletionProposals(int offset,
			JavaContentAssistInvocationContext context, IProgressMonitor monitor) {
		ICompilationUnit unit = ((JavaContentAssistInvocationContext) context)
				.getCompilationUnit();
		if (unit == null)
			return Collections.EMPTY_LIST;

		ITextViewer viewer = context.getViewer();
		IDocument doc = viewer.getDocument();
		IRegion reg = null;
		int beforeOpenParenthesesPosition = -1;
		try {
			reg = doc.getLineInformationOfOffset(offset);
			String lineValue = doc.get(reg.getOffset(), reg.getLength());
			beforeOpenParenthesesPosition = reg.getOffset()
					+ lineValue.lastIndexOf('(');
		} catch (BadLocationException e1) {

		}

		CompletionProposalCollector collector = createCollector((JavaContentAssistInvocationContext) context);
		collector.setInvocationContext(context);

		try {
			Point selection = viewer.getSelectedRange();
			if (selection.y > 0)
				collector.setReplacementLength(selection.y);

			unit.codeComplete(beforeOpenParenthesesPosition, collector);
		} catch (JavaModelException x) {
			Shell shell = viewer.getTextWidget().getShell();
			if (x.isDoesNotExist()
					&& !unit.getJavaProject().isOnClasspath(unit))
				MessageDialog.openInformation(shell,
						"CompletionProcessor_error_notOnBuildPath_title",
						"CompletionProcessor_error_notOnBuildPath_message");
			else
				ErrorDialog.openError(shell,
						"CompletionProcessor_error_accessing_title",
						"CompletionProcessor_error_accessing_message",
						x.getStatus());
		}

		ICompletionProposal[] javaProposals = collector
				.getJavaCompletionProposals();
		// if (javaProposals.length < 1) {
		// 		MessageDialog.openInformation(viewer.getTextWidget().getShell(),
		// 			"Required Libraries for getting Content Assist are missing.",
		// 			"Please add required libraries for IQueryService and Abstract DAO to the project classpath.");
		// 		return Collections.EMPTY_LIST;
		// }
		List list = null;
		String serviceType = "";

		// serviceType will be AbstractDAO OR IQueryService
		serviceType = getQualifiedName(javaProposals, unit);
		// if (serviceType == null
		// 		|| (!serviceType.equalsIgnoreCase("IQueryService") && !serviceType
		// 		.equalsIgnoreCase("AbstractDAO"))) {
		// 			MessageDialog.openInformation(viewer.getTextWidget().getShell(),
		// 				"Required Libraries for getting Content Assist are missing.",
		// 				"Please add required libraries for IQueryService and Abstract DAO to the project classpath.");
		//			return Collections.EMPTY_LIST;
		// }
		try {
			list = Arrays.asList(fProcessor.computeCompletionProposals(
					context.getViewer(), context.getInvocationOffset(),
					serviceType));
			if (list.size() == 0) {
				String error = collector.getErrorMessage();
				if (error.length() > 0)
					fErrorMessage = error;
			}
		} catch (Exception e) {
			// do not do anything.
		}
		return (list == null) ? Collections.EMPTY_LIST : list;
	}

	public String getQualifiedName(ICompletionProposal[] proposal,
			ICompilationUnit unit) {
		if (proposal != null) {
			for (int i = 0; i < proposal.length; i++) {
				String retVal = unit.getType(
						getSimpleName(proposal[i].getDisplayString()))
						.getElementName();
				if (retVal != null) {
					if (retVal.equalsIgnoreCase("AbstractDAO")
							|| retVal.equalsIgnoreCase("IQueryService")
							|| retVal.equalsIgnoreCase("QueryService")
							|| retVal.equalsIgnoreCase("QueryServiceDaoSupport")) {
						return retVal;
					} else {
						// ........
						// AnyframeJarLoader loader = new AnyframeJarLoader();
						// HashMap classMethodsMap = null;
						// try {
						// 		classMethodsMap = loader.getClassNamesHashMap();
						// } catch (IOException e) {
						// 		// TODO Auto-generated catch block
						// 		e.printStackTrace();
						// }
						// Iterator keysIterator = classMethodsMap.keySet().iterator();
						// while(keysIterator.hasNext()){
						// 		String key = keysIterator.next().toString();
						// }
						// .........
					}
				}
			}
			return "technicalService";
		}
		return null;
	}

	protected CompletionProposalCollector createCollector(
			JavaContentAssistInvocationContext context) {
		return new CompletionProposalCollector(context.getCompilationUnit());
	}

	protected String getSimpleName(String display) {
		String tokens[] = display.split(" ");
		return tokens[tokens.length - 1];
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return NO_CONTEXTS;
	}

	public String getErrorMessage() {
		return fErrorMessage;
	}

	public void sessionEnded() {
	}

	public void sessionStarted() {
		fErrorMessage = null;
	}

	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return Arrays.asList(fProcessor.computeContextInformation(
				context.getViewer(), context.getInvocationOffset()));
	}

	public void accept(CompletionProposal proposal) {

	}

}
