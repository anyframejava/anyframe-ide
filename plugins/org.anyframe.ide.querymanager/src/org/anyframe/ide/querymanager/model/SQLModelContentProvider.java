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
package org.anyframe.ide.querymanager.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.datatools.connectivity.sqm.core.containment.ContainmentService;
import org.eclipse.datatools.connectivity.sqm.core.definition.DatabaseDefinition;
import org.eclipse.datatools.connectivity.sqm.core.definition.DatabaseDefinitionRegistry;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.ColumnNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.ConstraintNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.DependencyNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.IndexNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.SequenceNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.StoredProcedureNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.TableNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.TriggerNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.UDFNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.providers.content.virtual.ViewNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IColumnNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IConstraintNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IDependencyNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IIndexNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.ISequenceNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IStoredProcedureNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.ITableNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.ITriggerNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IUDFNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IUDTNode;
import org.eclipse.datatools.connectivity.sqm.core.internal.ui.explorer.virtual.IViewNode;
import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.connectivity.sqm.core.ui.explorer.virtual.IVirtualNode;
import org.eclipse.datatools.connectivity.sqm.internal.core.RDBCorePlugin;
import org.eclipse.datatools.modelbase.sql.schema.Catalog;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.SQLObject;
import org.eclipse.datatools.modelbase.sql.schema.SQLSchemaPackage;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.BaseTable;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This is SQLModelContentProvider class.
 * 
 * @author Surindhar.Kondoor
 * @author viswa.srikant
 */
public class SQLModelContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {

	protected static final Object EMPTY_ELEMENT_ARRAY[] = new Object[0];

	private static DatabaseDefinitionRegistry registry = RDBCorePlugin
			.getDefault().getDatabaseDefinitionRegistry();

	protected static final ContainmentService containmentService = RDBCorePlugin
			.getDefault().getContainmentService();

	private final String TABLE = "Tables";
	private final String VIEW = "Views";

	// private static final String ROUTINE;
	private final String SEQUENCE = "Sequences";
	private final String UDT = "User-Defined Types";

	private final String TRIGGER = "Triggers";
	private final String INDEX = "Indexes";
	private final String CONSTRAINT = "Constraints";
	private final String COLUMN = "Columns";

	private final String DEPENDENCY = "Dependencies";
	private final String STORED_PROCEDURE = "Stored Procedures";
	private final String UDF = "User-Defined Functions";

	public Object[] getElements(Object parent) {
		// TODO Auto-generated method stub
		return getChildren(parent);
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public void inputChanged(Viewer viewer, Object obj, Object obj1) {
		// TODO Auto-generated method stub

	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof EObject)
			return getChildren((EObject) parent);
		if (parent instanceof IVirtualNode)
			return getChildren((IVirtualNode) parent);
		else
			return EMPTY_ELEMENT_ARRAY;
	}

	private Object[] getChildren(EObject parent) {
		if (parent instanceof Database) {
			return displayDatabaseChildren(parent);
		} else if (parent instanceof Catalog) {
			return displayCatalogChildren(parent);
		} else if (parent instanceof Schema) {
			return displaySchemaChildren(parent);
		} else if (parent instanceof BaseTable) {
			return displayTableChildren(parent);
		}
		return EMPTY_ELEMENT_ARRAY;
	}

	private Object[] getChildren(IVirtualNode parent) {
		if (parent instanceof ITableNode)
			return displayTableNodeChildren(parent);
		else if (parent instanceof IStoredProcedureNode)
			return displayStoredProcedureNodeChildren(parent);
		else if (parent instanceof IUDFNode)
			return displayUDFNodeChildren(parent);
		else if (parent instanceof ISequenceNode)
			return displaySequenceNodeChildren(parent);
		else if (parent instanceof IUDTNode)
			return displayUDTNodeChildren(parent);
		else if (parent instanceof IViewNode)
			return displayViewsNodeChildren(parent);
		else if (parent instanceof ITriggerNode)
			return displayTriggerNodeChildren(parent);
		else if (parent instanceof IIndexNode)
			return displayIndexNodeChildren(parent);
		else if (parent instanceof IConstraintNode)
			return displayConstraintNodeChildren(parent);
		else if (parent instanceof IColumnNode)
			return displayColumnNodeChildren(parent);
		else if (parent instanceof IDependencyNode)
			return displayDependencyNodeChildren(parent);
		else
			return EMPTY_ELEMENT_ARRAY;
	}

	public Object getParent(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren(Object parent) {
		// TODO Auto-generated method stub
		if(parent instanceof Column)
			return false;
		else	
			return true;
	}

	private Object[] displaySchemaChildren(Object parent) {
		DatabaseDefinition df = getDatabaseDefinition(parent);
		List collection = new ArrayList(7);

		collection.add(new TableNode(TABLE, TABLE, parent));
		collection.add(new ViewNode(VIEW, VIEW, parent));

		if (df.supportsStoredProcedures())
			collection.add(new StoredProcedureNode(STORED_PROCEDURE,
					STORED_PROCEDURE, parent));

		collection.add(new UDFNode(UDF, UDF, parent));

		if (df.supportsSequence())
			collection.add(new SequenceNode(SEQUENCE, SEQUENCE, parent));
		if (df.supportsUserDefinedType())
			collection.add(new UDFNode(UDF, UDF, parent));
		collection.add(new DependencyNode(DEPENDENCY, DEPENDENCY, parent));
		return getArrays(parent, collection);
	}

	private DatabaseDefinition getDatabaseDefinition(Object parent) {
		if (parent instanceof Database)
			return registry.getDefinition((Database) parent);
		if (parent instanceof ICatalogObject)
			return registry.getDefinition(((ICatalogObject) parent)
					.getCatalogDatabase());
		if (parent instanceof Schema) {
			Schema schema = (Schema) parent;
			Catalog catalog = schema.getCatalog();
			Database database;
			if (catalog == null)
				database = schema.getDatabase();
			else
				database = catalog.getDatabase();
			return registry.getDefinition(database);
		}
		if (parent instanceof Table)
			return getDatabaseDefinition(((Table) parent).getSchema());
		else
			return null;
	}

	private Object[] displayDatabaseChildren(Object parent) {
		Database database = (Database) parent;
		List catalogs = new ArrayList(database.getCatalogs());
		if (catalogs.size() == 0)
			return getArrays(database, database.getSchemas());
		for (Iterator it = catalogs.iterator(); it.hasNext();) {
			Catalog catalog = (Catalog) it.next();
			if (catalog.getName().length() == 0) {
				it.remove();
				catalogs.addAll(catalog.getSchemas());
				break;
			}
		}

		return getArrays(parent, catalogs);
	}

	private Object[] displayCatalogChildren(Object parent) {
		List collection = new ArrayList(1);
		Catalog catalog = (Catalog) parent;
		List schemas = new ArrayList(catalog.getSchemas());

		return getArrays(parent, schemas);
	}

	private Object[] displayTableChildren(Object parent) {
		DatabaseDefinition df = getDatabaseDefinition(parent);
		List collection = new ArrayList(5);
		collection.add(new ColumnNode(COLUMN, COLUMN, parent));
		if (df.supportsTriggers())
			collection.add(new TriggerNode(TRIGGER, TRIGGER, parent));
		collection.add(new IndexNode(INDEX, INDEX, parent));
		collection.add(new ConstraintNode(CONSTRAINT, CONSTRAINT, parent));
		collection.add(new DependencyNode(DEPENDENCY, DEPENDENCY, parent));
		return getArrays(parent, collection);
	}

	private Object[] displayTableNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_Tables();

		return getSchemaChildren(parent, feature);
	}

	private Object[] displayUDFNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_Routines();
		return getSchemaChildren(parent, feature);
	}

	private Object[] displaySequenceNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_Sequences();
		return getSchemaChildren(parent, feature);
	}

	private Object[] displayDependencyNodeChildren(Object parent) {
		SQLObject object = (SQLObject) ((IVirtualNode) parent).getParent();
		return getArrays(
				parent,
				getChildren(((IVirtualNode) parent).getGroupID(),
						object.getDependencies()));
	}

	private Object[] displayViewsNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_Tables();

		return getSchemaChildren(parent, feature);
	}

	private Object[] displayStoredProcedureNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_Routines();
		return getSchemaChildren(parent, feature);
	}

	protected Object[] getArrays(Object parent, Collection collection) {
		if (collection.isEmpty())
			return EMPTY_ELEMENT_ARRAY;

		return collection.toArray(new Object[collection.size()]);
	}

	protected Object[] getSchemaChildren(Object parent,
			EStructuralFeature feature) {
		Object ancestor = ((IVirtualNode) parent).getParent();
		if (ancestor instanceof Schema) {
			Schema schema = (Schema) ancestor;
			return getArrays(
					parent,
					getChildren(((IVirtualNode) parent).getGroupID(),
							(List) schema.eGet(feature)));
		}
		if (ancestor instanceof Database) {
			List schemas = ((Database) ancestor).getSchemas();
			return getSchemasChildren(parent, schemas, feature);
		}
		if (ancestor instanceof Catalog) {
			List schemas = ((Catalog) ancestor).getSchemas();
			return getSchemasChildren(parent, schemas, feature);
		} else {
			return EMPTY_ELEMENT_ARRAY;
		}
	}

	private Object[] getSchemasChildren(Object parent, List schemas,
			EStructuralFeature feature) {
		List result = new ArrayList();
		List objs;
		for (Iterator iterator = schemas.iterator(); iterator.hasNext(); result
				.addAll(objs)) {
			Schema schema = (Schema) iterator.next();
			objs = (List) schema.eGet(feature);
		}

		return getArrays(parent,
				getChildren(((IVirtualNode) parent).getGroupID(), result));
	}

	private Object[] displayUDTNodeChildren(Object parent) {
		EStructuralFeature feature = SQLSchemaPackage.eINSTANCE
				.getSchema_UserDefinedTypes();
		return getSchemaChildren(parent, feature);
	}

	private Object[] displayTriggerNodeChildren(Object parent) {
		Table table = (Table) ((IVirtualNode) parent).getParent();
		return getArrays(
				parent,
				getChildren(((IVirtualNode) parent).getGroupID(),
						table.getTriggers()));
	}

	private Object[] displayIndexNodeChildren(Object parent) {
		BaseTable table = (BaseTable) ((IVirtualNode) parent).getParent();
		return getArrays(
				parent,
				getChildren(((IVirtualNode) parent).getGroupID(),
						table.getIndex()));
	}

	private Object[] displayConstraintNodeChildren(Object parent) {
		BaseTable table = (BaseTable) ((IVirtualNode) parent).getParent();
		return getArrays(
				parent,
				getChildren(((IVirtualNode) parent).getGroupID(),
						table.getConstraints()));
	}

	private Object[] displayColumnNodeChildren(Object parent) {
		Table table = (Table) ((IVirtualNode) parent).getParent();
		return getArrays(
				parent,
				getChildren(((IVirtualNode) parent).getGroupID(),
						table.getColumns()));
	}

	private Collection getChildren(String groupID, List children) {
		List list = new ArrayList(children.size());
		for (Iterator iterator = children.iterator(); iterator.hasNext();) {
			EObject child = (EObject) iterator.next();
			if (groupID.equals(containmentService.getGroupId(child)))
				list.add(child);
		}

		return list;
	}

}
