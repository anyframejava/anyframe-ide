<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${package}.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import ${pojo.packageName}.${pojo.shortName};
<#if c2j.isComponent(pojo.identifierProperty) >
import ${pojo.packageName}.${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)};
<#elseif !data.isPrimitive(valueType)>
  <#foreach field in pojo.getAllPropertiesIterator()>	  
    <#if field.equals(pojo.identifierProperty)>
import ${field.value.type.returnedClass.name};
    </#if>
  </#foreach>
</#if>

import anyframe.common.Page;
import anyframe.common.util.SearchVO;
import anyframe.common.util.StringUtil;
import anyframe.core.hibernate.IDynamicHibernateService;

/**
 * ${pojo.shortName}Dao implementation class with Hibernate
 *
 */
@Repository("${pojoNameLower}Dao")
public class ${pojo.shortName}Dao extends HibernateDaoSupport {

	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;

	@Inject
	private SessionFactory sessionFactory;

	@Resource
	IDynamicHibernateService dynamicHibernateService;
	
	@PostConstruct
	public void initialize() {
		super.setSessionFactory(sessionFactory);
	}
	
	/** {@inheritDoc} */
	public void create(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.getHibernateTemplate().save(${pojoNameLower});
	}
	
	/** {@inheritDoc} */
	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();        
    	${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});    
		super.getHibernateTemplate().delete(${pojoNameLower});
	}

	/** {@inheritDoc} */
	public void update(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.getHibernateTemplate().update(${pojoNameLower});
	}

	/** {@inheritDoc} */
	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		return super.getHibernateTemplate().get(${pojo.shortName}.class, ${pojo.identifierProperty.name});
	}
	
    /** {@inheritDoc} */
	public Page getPagingList(SearchVO searchVO) throws Exception {
        int pageIndex = searchVO.getPageIndex();

        String searchCondition = StringUtil.null2str(searchVO.getSearchCondition());
        String searchKeyword = StringUtil.null2str(searchVO.getSearchKeyword());
        String isNumeric = StringUtil.isNumeric(searchKeyword) ? "true" : "false";
        
        Object[] args = new Object[4];		            
        args[0] = "condition=" + searchCondition;
        args[1] = "keywordStr=%" + searchKeyword + "%";
        args[2] = "keywordNum=" + searchKeyword + "";
        args[3] = "isNumeric=" + isNumeric;

        List resultList = dynamicHibernateService.findList("find${pojo.shortName}List", args, pageIndex, pageSize);
        Long totalSize = (Long) dynamicHibernateService.find("count${pojo.shortName}List", args);

        Page resultPage = new Page(resultList, pageIndex, totalSize.intValue(), pageUnit, pageSize);
        return resultPage;
	}    
}
