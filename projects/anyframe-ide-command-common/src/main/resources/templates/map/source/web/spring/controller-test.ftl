<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.web;

import static org.junit.Assert.assertEquals;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.support.SimpleSessionStatus;

import org.anyframe.pagination.Page;
import ${package}.service.${pojo.shortName}Service;
import org.anyframe.datatype.HashMapModel;
import ${pojo.packageName}.${pojo.shortName};

@RunWith(JMock.class)
public class ${pojo.shortName}ControllerTest {

    private ${pojo.shortName}Controller controller;
    private String SUCCESS_CREATEVIEW = "generation/${pojoNameLower}/form";
    private String SUCCESS_CREATE = "redirect:/${pojoNameLower}.do?method=list";
    private String SUCCESS_GET = "generation/${pojoNameLower}/form";
    private String SUCCESS_UPDATE = "redirect:/${pojoNameLower}.do?method=list";
    private String SUCCESS_LIST = "generation/${pojoNameLower}/list";
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
        String viewName = this.controller.createView(new HashMapModel());
        
        assertEquals("returned correct view name", SUCCESS_CREATEVIEW, viewName);
    }
   
    @Test     
    public void testCreate() throws Exception{
        MockHttpServletRequest request = new MockHttpServletRequest();

        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("create");
		    }});

        HashMapModel mapModel = new HashMapModel();
        mapModel.setMap(getParameterMap(request));
        String viewName = this.controller.create(mapModel,new BeanPropertyBindingResult(new ${pojo.shortName}(),"${pojoNameLower}"),new SimpleSessionStatus());                            
        assertEquals("returned correct view name", SUCCESS_CREATE, viewName);
    }

    @Test
    public void testGet() throws Exception{ 
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        final Map resultMap = new HashMap();     
        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("get");
		       will(returnValue(resultMap));
		    }});

        HashMapModel mapModel = new HashMapModel();
        mapModel.setMap(getParameterMap(request));
        String viewName = this.controller.get(mapModel);               
        assertEquals("returned correct view name", SUCCESS_GET, viewName);
    }

    @Test      
    public void testList() throws Exception{
        MockHttpServletRequest request = new MockHttpServletRequest();
<#if c2j.isComponent(pojo.identifierProperty)>		
	<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
	<#foreach idfield in pojoIdentifier.getPropertyIterator()>
	<#assign column = dbdata.getColumn(idfield)>
	<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>	
		request.addParameter("searchCondition", "${idfield.name}");
	<#break>	        
	</#foreach>
<#else>
	<#assign column = dbdata.getColumn(pojo.identifierProperty)>
	<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
        request.addParameter("searchCondition", "${pojo.identifierProperty.name}");
</#if>
<#if valueType == "Date">		
        request.addParameter("searchKeyword", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
<#elseif valueType == "String">		
        request.addParameter("searchKeyword", ${data.getValueForJavaTest(column)});	
<#else>
        request.addParameter("searchKeyword", ${data.getValueForJavaTest(column)}.toString());	
</#if>
        request.addParameter("pageIndex", "1");
		        
        // create ${pojo.shortName} List
        final Page page = new Page();
        context.checking(new Expectations() {{
		       allowing(any(${pojo.shortName}Service.class)).method("getPagingList");
		       will(returnValue(page));
		    }});

        HashMapModel mapModel = new HashMapModel();
        mapModel.setMap(getParameterMap(request));
        Model model = new ExtendedModelMap();
        
        String viewName = this.controller.list(mapModel, model, request);        
        assertEquals("returned correct view name", SUCCESS_LIST, viewName);
        assertEquals("contains ${pojoNameLower}List attribute in model", true, model.containsAttribute("${pojoNameLower}List"));
    }

    @Test
    public void testUpdate() throws Exception{
        MockHttpServletRequest request = new MockHttpServletRequest();	

	    context.checking(new Expectations() {{
			allowing(any(${pojo.shortName}Service.class)).method("update");
			}});

        HashMapModel mapModel = new HashMapModel();
        mapModel.setMap(getParameterMap(request));
        
        String viewName = this.controller.update(mapModel, new BeanPropertyBindingResult(new ${pojo.shortName}(),"${pojoNameLower}"),new SimpleSessionStatus());               
	    assertEquals("returned correct view name", SUCCESS_UPDATE, viewName);
    }
	
    @Test
    public void testRemove() throws Exception{
	    MockHttpServletRequest request = new MockHttpServletRequest();	
	
        context.checking(new Expectations() {{
       		allowing(any(${pojo.shortName}Service.class)).method("remove");
	    	}});

		HashMapModel mapModel = new HashMapModel();
        mapModel.setMap(getParameterMap(request));
        
        String viewName = this.controller.remove(mapModel);               
        assertEquals("returned correct view name", SUCCESS_REMOVE, viewName); 
    }	
	
	private Map getParameterMap(MockHttpServletRequest request) throws Exception {
        // add parameters for primary keys and required fields	
<#if c2j.isComponent(pojo.identifierProperty)>		
	<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
	<#foreach idfield in pojoIdentifier.getPropertyIterator()>
	<#assign column = dbdata.getColumn(idfield)>
	<#assign valueType = pojo.getJavaTypeName(idfield, jdk5)>	
	<#if valueType == "Date">		
        request.addParameter("${idfield.name}", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
	<#elseif valueType == "String">		
        request.addParameter("${idfield.name}", ${data.getValueForJavaTest(column)});	
	<#else>		
        request.addParameter("${idfield.name}", ${data.getValueForJavaTest(column)}.toString());	
	</#if>		
    </#foreach>
<#else>
	<#assign column = dbdata.getColumn(pojo.identifierProperty)>	
	<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>		
	<#if valueType == "Date">		
        request.addParameter("${pojo.identifierProperty.name}", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
	<#elseif valueType == "String">		
        request.addParameter("${pojo.identifierProperty.name}", ${data.getValueForJavaTest(column)});
	<#else>		
        request.addParameter("${pojo.identifierProperty.name}", ${data.getValueForJavaTest(column)}.toString());	
	</#if>	
</#if>           
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>
       <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		<#assign valueType = pojo.getJavaTypeName(field, jdk5)>	
		<#if valueType == "Date">		
	    request.addParameter("${field.name}", new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(${data.getValueForJavaTest(column)}));
		<#elseif valueType == "String">		
	    request.addParameter("${field.name}", ${data.getValueForJavaTest(column)});	
		<#else>		
	    request.addParameter("${field.name}", ${data.getValueForJavaTest(column)}.toString());	
		</#if>
       </#if>
    </#foreach>
</#foreach>
	
		Map<String,Object> parameterMap = new HashMap<String,Object>();
	    
	    Enumeration<String> enumeration = request.getParameterNames();
	    
	    while(enumeration.hasMoreElements())
	    {
	    	String parameterKey = (String)enumeration.nextElement();
	    	parameterMap.put(parameterKey, request.getParameterMap().get(parameterKey));	
	    }	  
	    
	    return parameterMap;
	}		
}
