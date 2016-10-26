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
package org.anyframe.ide.querymanager.parsefile;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 * the class ParseTemplateString parses the velocity template in the qury
 * statement
 * 
 * @author Junghwan Hong
 */
public class ParseTemplateString {
	private String referenceVariableToken = null;
	private String foreachListToken = null;

	/**
	 * returns a list of references in a template in the order that they are
	 * encountered
	 */
	public List referenceList(Template template) {
		SimpleNode sn = (SimpleNode) template.getData();

		// ResourceLoader resLoder = template.getResourceLoader();

		ReferenceListVisitor rlv = new ReferenceListVisitor();

		sn.jjtAccept(rlv, null);

		return rlv.getList();
	}

	/**
	 * returns a map of velocity tokens as keys..
	 */

	static VelocityEngine ve;

	static VelocityEngine getVelocityEngine() throws Exception {
		if (ve == null) {
			ve = new VelocityEngine();
			ve.setProperty("resource.loader", "string");
			ve.setProperty("string.resource.loader.description",
					"Velocity StringResource loader");
			ve.setProperty("string.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.StringResourceLoader");
			ve.setProperty("runtime.log.logsystem.class",
					"org.apache.velocity.runtime.log.NullLogSystem");
			ve.init();
		}
		return ve;
	}

	public static Template getTemplate(String query) {

		try {
			ve = getVelocityEngine();
			StringResourceRepository repo1 = StringResourceLoader
					.getRepository();
			repo1.putStringResource("query", query);
			return ve.getTemplate("query");
		} catch (Exception ignore) {
			/* ignore.printStackTrace(); */
			return null;
		}
	}

	/**
	 * Visitor to accumulate references.
	 */
	/* static */class ReferenceListVisitor extends BaseVisitor {
		List list = null;

		ReferenceListVisitor() {
			list = new ArrayList();
		}

		public List getList() {
			return list;
		}

		public Object visit(ASTReference node, Object data) {
			String lit = node.literal();

			list.add(lit);

			if (inForeach) {

				/*
				 * System.out .println("&&&&&&&&&&&&&&&&&&&&&& GOT THE VAR OF
				 * FOREACH==" + node.getRootString());
				 */
				referenceVariableToken = "$" + node.getRootString();

				inForeach = false;
				list.remove(lit);

			}

			/*
			 * feed the children...
			 */
			data = node.childrenAccept(this, data);

			return data;
		}

		boolean inForeach;

		public Object visit(ASTDirective node, Object data) {
			foreachListToken = node.jjtGetChild(2).literal();

			if (node.getDirectiveName().equals("foreach")) {

				inForeach = true;
			}

			return super.visit(node, data);
		}
	}

	public String getForeachListToken() {
		return foreachListToken;
	}

	public String getReferenceVariableToken() {
		return referenceVariableToken;
	}
}
