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
package org.anyframe.ide.command.maven.mojo.codegen;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;

import org.apache.commons.collections.map.ListOrderedMap;
import org.appfuse.tool.DataHelper;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.annotations.reflection.EJB3ReflectionManager;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.tool.hbm2x.Cfg2HbmTool;
import org.hibernate.tool.hbm2x.Cfg2JavaTool;
import org.hibernate.tool.hbm2x.GenericExporter;
import org.hibernate.tool.hbm2x.pojo.POJOClass;
import org.hibernate.util.ReflectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an AnyframeDBData class to access pojo data(field and column
 * information) in freemarker template files.
 * 
 * @author Sooyeon Park
 */
public class AnyframeDBData {

	private static Logger log = LoggerFactory.getLogger(AnyframeDBData.class);

	private Cfg2JavaTool c2j;
	private Cfg2HbmTool c2h;
	private GenericExporter exporter;
	private DataHelper dataHelper;
	private ListOrderedMap sampleDataSet = new ListOrderedMap();
	private ListOrderedMap keyDataMap;

	public AnyframeDBData(Cfg2JavaTool c2j, Cfg2HbmTool c2h,
			GenericExporter exporter) {
		this.c2j = c2j;
		this.c2h = c2h;
		this.exporter = exporter;
		dataHelper = (DataHelper) exporter.getProperties().get("data");
	}

	public String getColumnName(Property field) {

		Iterator<?> itr = field.getColumnIterator();
		while (itr.hasNext())
			return ((Column) itr.next()).getName();

		return null;
	}

	public String getColumnJavaType(Property field) {
		return field.getType().getName();
	}

	public Column getColumn(Property field) {

		Iterator<?> itr = field.getColumnIterator();
		while (itr.hasNext())
			return ((Column) itr.next());

		return null;
	}

	private ListOrderedMap getPrimaryKeyData(ListOrderedMap pojoDataMap,
			Component keyComponent) {
		ListOrderedMap primaryDataMap = new ListOrderedMap();

		Iterator<?> itr = keyComponent.getPropertyIterator();
		while (itr.hasNext()) {
			Property p = ((Property) itr.next());
			Column column = getColumn(p);

			String value = dataHelper.getTestValueForDbUnit(column);
			pojoDataMap.put((String) column.getName(), value);
			primaryDataMap.put(column.getName(), value);
		}

		return primaryDataMap;
	}

	private ListOrderedMap getReferencedColumnNames(String clazzName,
			String index) {
		ListOrderedMap referencedColumnNames = new ListOrderedMap();

		try {
			EJB3ReflectionManager reflectionManager = new EJB3ReflectionManager();
			Class<?> loadedClass = ReflectHelper.classForName(clazzName);
			XClass persistentXClass = reflectionManager.toXClass(loadedClass);
			List<XProperty> properties = persistentXClass
					.getDeclaredProperties("property");

			for (XProperty property : properties) {
				if (property.getName().toLowerCase()
						.equals(index.toLowerCase())) {
					JoinColumn[] anns = null;
					if (property.isAnnotationPresent(JoinColumn.class)) {
						anns = new JoinColumn[] { property
								.getAnnotation(JoinColumn.class) };
						for (int i = 0; i < anns.length; i++) {
							referencedColumnNames.put(anns[i].name(),
									anns[i].referencedColumnName());
						}
					} else if (property.isAnnotationPresent(JoinColumns.class)) {
						javax.persistence.JoinColumns ann = property
								.getAnnotation(javax.persistence.JoinColumns.class);
						anns = ann.value();

						for (int i = 0; i < anns.length; i++) {
							referencedColumnNames.put(anns[i].name(),
									anns[i].referencedColumnName());
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			log.error("Property about referenced column is not found in getReferencedColumnNames(): "
					+ e.getMessage());
		}

		return referencedColumnNames;
	}

	public ListOrderedMap createDataSet(POJOClass pojo) {
		ListOrderedMap pojoDataMap = new ListOrderedMap();
		ListOrderedMap keyDataMap = new ListOrderedMap();

		PersistentClass clazz = (PersistentClass) pojo.getDecoratedObject();

		Component keyComponent = null;
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			keyComponent = (Component) pojo.getIdentifierProperty().getValue();
			// 주키가 복합키인 경우에 대한 데이터를 미리 생성하고, 이후 외부 참조키에
			// 대한 데이터는 아래 코드에서
			// Override를 한다.
			keyDataMap = getPrimaryKeyData(pojoDataMap, keyComponent);
		}

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)) {
				// ** 참조키가 필요한 경우 참조 데이블을 위한 데이터를 생성하고
				// 참조키에 사용할 데이터를 가져온다.
				ListOrderedMap refColumnNameMap = getReferencedColumnNames(
						clazz.getClassName(), field.getNodeName());

				PersistentClass pc = exporter.getConfiguration()
						.getClassMapping(field.getValue().getType().getName());

				POJOClass reference = c2j.getPOJOClass(pc);

				ListOrderedMap refColumnValueMap = createDataSet(reference);
				Iterator<?> itr = field.getColumnIterator();
				int index = 0;

				while (itr.hasNext()) {
					Column column = (Column) itr.next();
					String refColumnName = (String) refColumnNameMap.get(column
							.getName());
					String value;

					if (refColumnName == null || refColumnName.equals(""))
						value = (String) refColumnValueMap.getValue(index++);
					else
						value = (String) refColumnValueMap.get(refColumnName);

					pojoDataMap.put(column.getName(), value);
					if (keyDataMap.get(column.getName()) != null) {
						keyDataMap.put(column.getName(), value);
					}
				}
				// 주키가 단일키로 이루어진 경우데 대한 데이터를 생성한다.
			} else if (field.equals(pojo.getIdentifierProperty())) {
				if (!c2j.isComponent(field)) {
					String keyData = dataHelper
							.getTestValueForDbUnit(getColumn(field));
					pojoDataMap.put(getColumnName(field), keyData);
					keyDataMap.put(getColumnName(field), keyData);
				}
			} else { // 나머지 필드에 대한 데이터를 생성한다.
				pojoDataMap.put(getColumnName(field),
						dataHelper.getTestValueForDbUnit(getColumn(field)));
			}
		}

		sampleDataSet.put(sampleDataSet.size() + ":"
				+ clazz.getTable().getName(), pojoDataMap);

		return keyDataMap;
	}

	public ListOrderedMap getKeyList(POJOClass pojo) {
		ListOrderedMap primaryKeyList = new ListOrderedMap();
		ListOrderedMap foreignKeyList = new ListOrderedMap();
		ListOrderedMap orderedPrimaryKeyList = new ListOrderedMap();

		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();

			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				Column column = getColumn(p);
				primaryKeyList.put(column.getName(), p);
			}
		}

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)) {
				Iterator<Column> itr = field.getColumnIterator();

				while (itr.hasNext()) {
					Column column = (Column) itr.next();
					if (primaryKeyList.get(column.getName()) != null) {
						foreignKeyList.put(column.getName(),
								primaryKeyList.get(column.getName()));

						primaryKeyList.remove(column.getName());
					}
				}
			} else if (field.equals(pojo.getIdentifierProperty())) {
				if (!c2j.isComponent(field)) {
					primaryKeyList.put(getColumnName(field), field);
				}
			}
		}

		primaryKeyList.putAll(foreignKeyList);

		for (int i = 0; i < primaryKeyList.size(); i++) {
			orderedPrimaryKeyList
					.put(primaryKeyList.getValue(i),
							((primaryKeyList.size() - foreignKeyList.size()) <= i) ? "FK"
									: "");
		}

		return orderedPrimaryKeyList;
	}

	public ListOrderedMap getForeignKeyList(POJOClass pojo) {
		ListOrderedMap foreignKeyList = new ListOrderedMap();
		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)) {
				String type = field.getType().getName();
				String typeClassName = type
						.substring(type.lastIndexOf(".") + 1);
				foreignKeyList.put(getColumnName(field), typeClassName);
			}
		}
		return foreignKeyList;
	}

	public ListOrderedMap getKeyDataList(POJOClass pojo) {
		return this.keyDataMap;
	}

	public ListOrderedMap getDataList(POJOClass pojo) {
		ListOrderedMap dataMap = new ListOrderedMap();
		Iterator itr = sampleDataSet.keyList().iterator();
		while (itr.hasNext()) {
			String tablename = (String) itr.next();
			ListOrderedMap datamap = (ListOrderedMap) sampleDataSet
					.get(tablename);
			Iterator mapItr = datamap.keyList().iterator();
			while (mapItr.hasNext()) {
				String columnName = (String) mapItr.next();
				// System.out.println(" [Column]" + columnName + ":"
				// + datamap.get(columnName));
				dataMap.put(columnName, datamap.get(columnName));
			}
		}
		return dataMap;
	}

	public ListOrderedMap getSampleDataSet(POJOClass pojo) {
		sampleDataSet.clear();
		keyDataMap = createDataSet(pojo);

		/*
		 * Iterator itr = sampleDataSet.keyList().iterator();
		 * while(itr.hasNext()) { String tablename = (String)itr.next();
		 * System.out.println("[Table name] : "+ tablename); ListOrderedMap
		 * datamap = (ListOrderedMap )sampleDataSet.get(tablename); Iterator
		 * mapItr = datamap.keyList().iterator(); while(mapItr.hasNext()) {
		 * String columnName = (String) mapItr.next(); System.out.println("
		 * [Column] "+ columnName + ":" + datamap.get(columnName)); } }
		 */

		return sampleDataSet;
	}

	public ListOrderedMap getKeyAndRequiredFieldList(POJOClass pojo) {
		ListOrderedMap typePropertiesMap = new ListOrderedMap();

		// get key field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				String propertyName = pojo.getIdentifierProperty().getName()
						+ "." + p.getName();
				putTypePropertiesMap(typePropertiesMap, p, propertyName);
			}
		} else {
			Property p = pojo.getIdentifierProperty();
			putTypePropertiesMap(typePropertiesMap, p, p.getName());
		}

		// get required field
		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property p = (Property) pojoItr.next();
			if (getColumn(p) == null)
				continue;

			if (!p.equals(pojo.getIdentifierProperty())
					&& !getColumn(p).isNullable() && !c2h.isManyToOne(p)
					&& !c2h.isCollection(p) && !c2j.isComponent(p)) {
				putTypePropertiesMap(typePropertiesMap, p, p.getName());
			}
		}

		return typePropertiesMap;
	}

	@SuppressWarnings("unchecked")
	private void putTypePropertiesMap(ListOrderedMap typePropertiesMap,
			Property p, String propertyName) {
		Column column = getColumn(p);
		// p.setName(propertyName);
		if (typePropertiesMap.get("NUM") == null)
			typePropertiesMap.put("NUM", new ArrayList<Column>());
		if (typePropertiesMap.get("STR") == null)
			typePropertiesMap.put("STR", new ArrayList<Column>());

		if (((AnyframeDataHelper) dataHelper).getColumnType(column).equals(
				"NUM")) {
			List<Property> list = (List<Property>) typePropertiesMap.get("NUM");
			list.add(p);
			typePropertiesMap.put("NUM", list);
		} else if (((AnyframeDataHelper) dataHelper).getColumnType(column)
				.equals("STR")) {
			List<Property> list = (List<Property>) typePropertiesMap.get("STR");
			list.add(p);
			typePropertiesMap.put("STR", list);
		}
	}

	public String checkAndGetPropertyName(POJOClass pojo, Property property) {
		// get key field
		String propertyName = property.getName();
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				if (p.getName().equals(property.getName())) {
					propertyName = pojo.getIdentifierProperty().getName() + "."
							+ propertyName;
					break;
				}
			}
		}

		return propertyName;
	}

	// get all column and field information
	public ListOrderedMap getColumnFieldMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Class clazz = pojo.getClass();
		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)
					&& !field.equals(pojo.getIdentifierProperty())) {
				if (field.getValue() instanceof ManyToOne) {
					ManyToOne manytoone = (ManyToOne) field.getValue();
					Iterator<?> itr = manytoone.getColumnIterator();
					while (itr.hasNext()) {
						Column c = ((Column) itr.next());
						PersistentClass pc = exporter.getConfiguration()
								.getClassMapping(field.getType().getName());
						POJOClass pojoclass = c2j.getPOJOClass(pc);

						if (pojoclass.getIdentifierProperty().getValue() instanceof Component) {
							Component keyComponent = (Component) pojoclass
									.getIdentifierProperty().getValue();
							Iterator<?> keyitr = keyComponent
									.getPropertyIterator();

							while (keyitr.hasNext()) {
								Property p = ((Property) keyitr.next());
								Column column = getColumn(p);
								String propertyName = field.getName() + "."
										+ p.getName();
								columnFieldMap.put(column.getName(),
										propertyName);
							}
						} else {
							String propertyName = field.getName()
									+ "."
									+ pojoclass.getIdentifierProperty()
											.getName();
							columnFieldMap.put(c.getName(), propertyName);
						}

					}
				}
			} else {
				columnFieldMap.put(getColumn(field).getName(), field.getName());
			}
		}

		// set composite key id field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				String propertyName = pojo.getIdentifierProperty().getName()
						+ "." + p.getName();
				columnFieldMap.put(getColumn(p).getName(), propertyName);
			}
		}

		return columnFieldMap;
	}
	
	// get all column and field information (Only Mybatis)
	public ListOrderedMap getColumnFieldMapMybatis(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Class clazz = pojo.getClass();
		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			Column col = getColumn(field);
			if (col == null)
				continue;
			
			if (c2h.isManyToOne(field)
					&& !field.equals(pojo.getIdentifierProperty())) {
				if (field.getValue() instanceof ManyToOne) {
					ManyToOne manytoone = (ManyToOne) field.getValue();
					Iterator<?> itr = manytoone.getColumnIterator();
					while (itr.hasNext()) {
						Column c = ((Column) itr.next());
						PersistentClass pc = exporter.getConfiguration()
								.getClassMapping(field.getType().getName());
						POJOClass pojoclass = c2j.getPOJOClass(pc);

						if (pojoclass.getIdentifierProperty().getValue() instanceof Component) {
							Component keyComponent = (Component) pojoclass
									.getIdentifierProperty().getValue();
							Iterator<?> keyitr = keyComponent
									.getPropertyIterator();

							while (keyitr.hasNext()) {
								Property p = ((Property) keyitr.next());
								Column column = getColumn(p);
								String propertyName = field.getName() + "."
										+ p.getName();
								propertyName += ", jdbcType=" + getMybatisJdbcType(column);
								columnFieldMap.put(column.getName(),
										propertyName);
							}
						} else {
							String propertyName = field.getName()
									+ "."
									+ pojoclass.getIdentifierProperty()
										.getName();
							propertyName += ", jdbcType=" + getMybatisJdbcType(c);
							columnFieldMap.put(c.getName(), propertyName);
						}

					}
				}
			} else {
				String propertyName = field.getName();
				propertyName += ", jdbcType=" + getMybatisJdbcType(col);
				columnFieldMap.put(getColumn(field).getName(), propertyName);
			}
		}

		// set composite key id field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				Column column = getColumn(p);
				String propertyName = pojo.getIdentifierProperty().getName()
						+ "." + p.getName();
				propertyName += ", jdbcType=" + getMybatisJdbcType(column);
				columnFieldMap.put(column.getName(), propertyName);
			}
		}

		return columnFieldMap;
	}

	private String getMybatisJdbcType(Column column) {
		String className = column.getValue().getType().getReturnedClass().getName();
		if(className.equals(String.class.getName())){
			return "VARCHAR";
		}  else if(className.equals(Boolean.class.getName())){
			return "BOOLEAN";
		}else if(className.equals(Integer.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Long.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Short.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Float.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Byte.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Double.class.getName())){
			return "NUMERIC";
		} else if(className.equals(BigDecimal.class.getName())){
			return "NUMERIC";
		} else if(className.equals(Byte[].class.getName())){
			return "BLOB";
		} else if(className.equals(Date.class.getName())){
			return "DATE";
		} else if(className.equals(Timestamp.class.getName())){
			return "TIMESTAMP";
		}else{
			return "OTHER";
		}
	}
	
	public ListOrderedMap getColumnFieldWithoutIdMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null || c2h.isManyToOne(field)
					|| field.equals(pojo.getIdentifierProperty()))
				continue;

			columnFieldMap.put(getColumn(field).getName(), field.getName());
		}

		return columnFieldMap;
	}
	
	// (Only Mybatis)
	public ListOrderedMap getColumnFieldWithoutIdMapMybatis(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			Column col = getColumn(field);
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (col == null || c2h.isManyToOne(field)
					|| field.equals(pojo.getIdentifierProperty()))
				continue;
			
			String propertyName = field.getName() + ", jdbcType=" + getMybatisJdbcType(col);
			columnFieldMap.put(col.getName(), propertyName);
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldTypeWithoutIdMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null || c2h.isManyToOne(field)
					|| field.equals(pojo.getIdentifierProperty()))
				continue;

			columnFieldMap.put(getColumn(field).getName(), field.getType()
					.getReturnedClass().getName());
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnDataTypeWithoutIdMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null || c2h.isManyToOne(field)
					|| field.equals(pojo.getIdentifierProperty()))
				continue;

			columnFieldMap.put(getColumn(field).getName(), field.getType()
					.getName());
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldOnlyIdMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null)
				continue;

			if (field.equals(pojo.getIdentifierProperty())
					&& !c2j.isComponent(field)) {
				columnFieldMap.put(getColumn(field).getName(), field.getName());
			}
		}

		// set composite key id field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				String propertyName = pojo.getIdentifierProperty().getName()
						+ "." + p.getName();
				columnFieldMap.put(getColumn(p).getName(), propertyName);
			}
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldOnlyIdWithoutNestedPropertyMap(
			POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			// ** 컬럼정보가 존재하지 않는 Set Type
			// Field(OneToMany)에 대해서는 데이터를 생성하지 않는다.
			if (getColumn(field) == null)
				continue;

			if (field.equals(pojo.getIdentifierProperty())
					&& !c2j.isComponent(field)) {
				columnFieldMap.put(getColumn(field).getName(), field.getName());
			}
		}

		// set composite key id field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				String propertyName = p.getName();
				columnFieldMap.put(getColumn(p).getName(), propertyName);
			}
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldCompositeKeyIdMap(POJOClass pojo) {
		ListOrderedMap columnFieldMap = new ListOrderedMap();

		// set composite key id field
		if (pojo.getIdentifierProperty().getValue() instanceof Component) {
			Component keyComponent = (Component) pojo.getIdentifierProperty()
					.getValue();
			Iterator<?> itr = keyComponent.getPropertyIterator();
			while (itr.hasNext()) {
				Property p = ((Property) itr.next());
				String propertyName = pojo.getIdentifierProperty().getName()
						+ "." + p.getName();
				columnFieldMap.put(getColumn(p).getName(), propertyName);
			}
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldManyToOneMap(POJOClass pojo) {

		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)
					&& !field.equals(pojo.getIdentifierProperty())) {
				if (field.getValue() instanceof ManyToOne) {
					ManyToOne manytoone = (ManyToOne) field.getValue();
					Iterator<?> itr = manytoone.getColumnIterator();
					while (itr.hasNext()) {
						Column c = ((Column) itr.next());
						PersistentClass pc = exporter.getConfiguration()
								.getClassMapping(field.getType().getName());
						POJOClass pojoclass = c2j.getPOJOClass(pc);

						if (pojoclass.getIdentifierProperty().getValue() instanceof Component) {
							Component keyComponent = (Component) pojoclass
									.getIdentifierProperty().getValue();
							Iterator<?> keyitr = keyComponent
									.getPropertyIterator();

							while (keyitr.hasNext()) {
								Property p = ((Property) keyitr.next());
								Column column = getColumn(p);
								String propertyName = field.getName() + "."
										+ p.getName();
								columnFieldMap.put(column.getName(),
										propertyName);
							}
						} else {
							String propertyName = field.getName()
									+ "."
									+ pojoclass.getIdentifierProperty()
											.getName();
							columnFieldMap.put(c.getName(), propertyName);
						}

					}
				}
			}
		}

		return columnFieldMap;
	}

	public ListOrderedMap getColumnFieldManyToOneMapWithObject(POJOClass pojo) {

		ListOrderedMap columnFieldMap = new ListOrderedMap();

		Iterator<?> pojoItr = pojo.getAllPropertiesIterator();

		while (pojoItr.hasNext()) {
			Property field = (Property) pojoItr.next();
			if (getColumn(field) == null)
				continue;

			if (c2h.isManyToOne(field)
					&& !field.equals(pojo.getIdentifierProperty())) {
				if (field.getValue() instanceof ManyToOne) {
					ManyToOne manytoone = (ManyToOne) field.getValue();
					Iterator<?> itr = manytoone.getColumnIterator();
					while (itr.hasNext()) {
						Column c = ((Column) itr.next());
						PersistentClass pc = exporter.getConfiguration()
								.getClassMapping(field.getType().getName());
						POJOClass pojoclass = c2j.getPOJOClass(pc);

						if (pojoclass.getIdentifierProperty().getValue() instanceof Component) {
							Component keyComponent = (Component) pojoclass
									.getIdentifierProperty().getValue();
							Iterator<?> keyitr = keyComponent
									.getPropertyIterator();

							while (keyitr.hasNext()) {
								Property p = ((Property) keyitr.next());
								Column column = getColumn(p);
								String propertyName = p.getName();
								columnFieldMap.put(column, propertyName);
							}
						} else {
							String propertyName = pojoclass
									.getIdentifierProperty().getName();
							columnFieldMap.put(c, propertyName);
						}

					}
				}
			}
		}

		return columnFieldMap;
	}

	public String getGetterMethodName(String fieldName) {
		int seperator = fieldName.indexOf(".");
		String getterMethodName = "get";
		if (seperator == -1)
			getterMethodName += fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1) + "()";
		else
			getterMethodName += fieldName.substring(0, 1).toUpperCase()
					+ fieldName.substring(1, seperator) + "()."
					+ getGetterMethodName(fieldName.substring(seperator + 1));

		return getterMethodName;
	}

	public String getIdGetterMethodName(String fieldName) {
		int seperator = fieldName.indexOf(".");

		if (seperator != -1)
			fieldName = fieldName.substring(seperator + 1);

		return getGetterMethodName(fieldName);
	}
}
