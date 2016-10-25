<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${basepackage}.web;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;

import anyframe.common.Page;
import ${pojo.packageName}.${pojo.shortName};
<#if c2j.isComponent(pojo.identifierProperty) >
import ${pojo.packageName}.${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)};
</#if>

import ${basepackage}.service.${pojo.shortName}Service;
import anyframe.common.util.SearchVO;

@RunWith(JMock.class)
public class ${pojo.shortName}ControllerTest {

    private ${pojo.shortName}Controller controller;
    private String SUCCESS_CREATEVIEW = "genView${pojo.shortName}";
    private String SUCCESS_CREATE = "redirect:/${pojoNameLower}.do?method=list";
    private String SUCCESS_GET = "genView${pojo.shortName}";
    private String SUCCESS_UPDATE = "redirect:/${pojoNameLower}.do?method=list";
    private String SUCCESS_LIST = "genList${pojo.shortName}";
    private String SUCCESS_REMOVE = "redirect:/${pojoNameLower}.do?method=list";
    private Mockery context = new JUnit4Mockery();
    private ${pojo.shortName}Service mockService = null;
	
    @Before   
    public void setUp() throws Exception {
        System.setProperty("log4j.configuration", "log4j-test.xml");

        this.mockService = context.mock(${pojo.shortName}Service.class);                
        this.controller = new ${pojo.shortName}Controller();
        this.controller.set${pojo.shortName}Service(this.mockService);       
    }
    
    public void set${pojo.shortName}Controller(${pojo.shortName}Controller controller) {
        this.controller = controller;
    }
        
    @Test
    public void testCreateView() throws Exception{
        String viewName = this.controller.createView(new ExtendedModelMap());
        
        assertEquals("returned correct view name", SUCCESS_CREATEVIEW, viewName);
   }
   
   @Test     
   public void testCreate() throws Exception{
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();
<#if c2j.isComponent(pojo.identifierProperty)>			
		${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name} = new ${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}();
</#if>		
        // add parameter for primary key and required fields
<#if c2j.isComponent(pojo.identifierProperty)>		
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>
		<#assign column = dbdata.getColumn(idfield)>
		<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>				
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));	
		<#else>
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(${data.getValueForJavaTest(column)});	
		</#if>		
    	</#foreach>
    	${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});            
<#else>
		<#assign column = dbdata.getColumn(pojo.identifierProperty)>	
		<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${data.getValueForJavaTest(column)});	
		</#if>			
</#if>           
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>
        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
			<#assign valueType = pojo.getJavaTypeName(field, jdk5)>	
			<#if c2j.isPrimitive(valueType) >
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}("${data.getValueForJavaTest(column)}");
			<#elseif valueType == "Date">		
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));	
			<#else>
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${data.getValueForJavaTest(column)});	
			</#if>
        </#if>
    </#foreach>
</#foreach>
        
        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("create");
		    }});
  		        
        String viewName = this.controller.create(${pojoNameLower}, new BeanPropertyBindingResult(${pojoNameLower},"${pojoNameLower}"),new SimpleSessionStatus());               
        assertEquals("returned correct view name", SUCCESS_CREATE, viewName);
   }
   
   @Test
   public void testGet() throws Exception{
        final ${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();
<#if c2j.isComponent(pojo.identifierProperty)>			
		${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name} = new ${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}();
</#if>        
        // add parameter for primary key
<#if c2j.isComponent(pojo.identifierProperty)>		
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>
		<#assign column = dbdata.getColumn(idfield)>
		<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}("${data.getValueForJavaTest(column)}");	
		<#elseif valueType == "Date">		
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(${data.getValueForJavaTest(column)});	
		</#if>		
        </#foreach>
        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});   
<#else>
		<#assign column = dbdata.getColumn(pojo.identifierProperty)>	
		<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${data.getValueForJavaTest(column)});	
		</#if>			
</#if>          

        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("get");
		       will(returnValue(${pojoNameLower}));
		    }});

        String viewName = this.controller.get(<#if c2j.isComponent(pojo.identifierProperty)>${pojoNameLower}<#else>${pojoNameLower}.get${pojo.getPropertyName(pojo.identifierProperty)}().toString()</#if>, new ExtendedModelMap());               
        assertEquals("returned correct view name", SUCCESS_GET, viewName);
    }
    
    @Test      
    public void testList() throws Exception{
        SearchVO searchVO = new SearchVO(); 
        
        // create ${pojo.shortName} List
        final Page page = new Page();
        List<${pojo.shortName}> ${pojoNameLower}List = new ArrayList<${pojo.shortName}>();
        ${pojoNameLower}List.add(new ${pojo.shortName}());
        ${pojoNameLower}List.add(new ${pojo.shortName}());
        page.setObjects(${pojoNameLower}List);
        page.setTotalCount(2);

        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("getPagingList");
		       will(returnValue(page));
		    }});

        String viewName = this.controller.list(searchVO, new ExtendedModelMap());        
               
        assertEquals("returned correct view name", SUCCESS_LIST, viewName);
    }

    @Test
    public void testUpdate() throws Exception{
        ${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();
<#if c2j.isComponent(pojo.identifierProperty)>			
		${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name} = new ${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}();
</#if>        
        
        // add parameter for primary key and required fields
<#if c2j.isComponent(pojo.identifierProperty)>		
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>
		<#assign column = dbdata.getColumn(idfield)>
		<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(${data.getValueForJavaTest(column)});	
		</#if>		
        </#foreach>
        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});
<#else>
		<#assign column = dbdata.getColumn(pojo.identifierProperty)>	
		<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${data.getValueForJavaTest(column)});	
		</#if>			
</#if>           
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>
        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
			<#assign valueType = pojo.getJavaTypeName(field, jdk5)>	
			<#if c2j.isPrimitive(valueType) >
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}("${data.getValueForJavaTest(column)}");
			<#elseif valueType == "Date">		
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
			<#else>
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${data.getValueForJavaTest(column)});	
			</#if>
        </#if>
    </#foreach>
</#foreach>
	       
        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("update");
		    }});
		
        String viewName = this.controller.update(${pojoNameLower}, new BeanPropertyBindingResult(${pojoNameLower},"${pojoNameLower}"),new SimpleSessionStatus());               
        assertEquals("returned correct view name", SUCCESS_UPDATE, viewName);
   }
   
    @Test
    public void testRemove() throws Exception{
        ${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();
<#if c2j.isComponent(pojo.identifierProperty)>			
		${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name} = new ${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}();
</#if>        
        
        // add parameter for primary key
<#if c2j.isComponent(pojo.identifierProperty)>		
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>
		<#assign column = dbdata.getColumn(idfield)>
		<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojo.identifierProperty.name}.set${pojo.getPropertyName(idfield)}(${data.getValueForJavaTest(column)});	
		</#if>		
        </#foreach>
        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});
<#else>
		<#assign column = dbdata.getColumn(pojo.identifierProperty)>	
		<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>		
		<#if c2j.isPrimitive(valueType) >
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}("${data.getValueForJavaTest(column)}");
		<#elseif valueType == "Date">		
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#else>
		<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${data.getValueForJavaTest(column)});	
		</#if>			
</#if>            

        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("remove");
			    }});
		
        String viewName = this.controller.remove(${pojoNameLower});               
        assertEquals("returned correct view name", SUCCESS_REMOVE, viewName); 
   }
    
}