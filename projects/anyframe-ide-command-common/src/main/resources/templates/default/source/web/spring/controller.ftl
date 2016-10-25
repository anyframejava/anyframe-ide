<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.web;

import javax.inject.Inject;
import javax.inject.Named;

import ${pojo.packageName}.${pojo.shortName};
import ${package}.service.${pojo.shortName}Service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;

<#if !c2j.isComponent(pojo.identifierProperty)>
import org.anyframe.util.StringUtil;
import org.springframework.web.bind.annotation.RequestParam;
	<#if !data.isPrimitive(valueType)>
	  <#foreach field in pojo.getAllPropertiesIterator()>	  
	    <#if field.equals(pojo.identifierProperty)>
import ${field.value.type.returnedClass.name};
	    </#if>
	  </#foreach>
	</#if>
</#if>

import org.anyframe.pagination.Page;
import org.anyframe.datatype.SearchVO;

/**
 * Controller for ${pojo.shortName}
 */
@Controller
@RequestMapping("/${pojoNameLower}.do")
public class ${pojo.shortName}Controller {

	@Inject
	@Named("${pojoNameLower}Service")
    private ${pojo.shortName}Service ${pojoNameLower}Service;
    
    public void set${pojo.shortName}Service(${pojo.shortName}Service ${pojoNameLower}Service) {
        this.${pojoNameLower}Service = ${pojoNameLower}Service;
    }
        
    @RequestMapping(params = "method=createView") 
    public String createView(Model model) throws Exception {  
    	model.addAttribute(new ${pojo.shortName}());
        return "generation/${pojoNameLower}/form";                 
    }
    
    @RequestMapping(params = "method=create")  
    public String create(${pojo.shortName} ${pojoNameLower}, BindingResult results, SessionStatus status) throws Exception {
    	
    	if (results.hasErrors()) {
			return "generation/${pojoNameLower}/form";
		}
		
        ${pojoNameLower}Service.create(${pojoNameLower});   
        status.setComplete();
             
        return "redirect:/${pojoNameLower}.do?method=list";        
    }

    @RequestMapping(params = "method=get")    
    public String get(<#if c2j.isComponent(pojo.identifierProperty)> @ModelAttribute("input${pojo.shortName}")${pojo.shortName} ${pojoNameLower} 
    						<#else>@RequestParam("${pojo.identifierProperty.name}") String ${pojo.identifierProperty.name}</#if>, Model model) throws Exception { 	
 	<#if c2j.isComponent(pojo.identifierProperty)>
       ${pojoNameLower} = ${pojoNameLower}Service.get(${pojoNameLower}.${getIdMethodName}());        	
       model.addAttribute("${pojoNameLower}", ${pojoNameLower});    
	<#else>
       if (StringUtil.isNotEmpty(${pojo.identifierProperty.name})) {
       		${pojo.shortName} ${pojoNameLower} = ${pojoNameLower}Service.get(new ${identifierType}(${pojo.identifierProperty.name}));        	
        	model.addAttribute("${pojoNameLower}", ${pojoNameLower});        	                	
       }
	</#if>	
 	
       return "generation/${pojoNameLower}/form";
    }
     
    @RequestMapping(params ="method=update") 
    public String update(${pojo.shortName} ${pojoNameLower}, BindingResult results, SessionStatus status) throws Exception {
    
    	if (results.hasErrors()) {
			return "generation/${pojoNameLower}/form";
		}
		    	
        ${pojoNameLower}Service.update(${pojoNameLower});   
        status.setComplete();
                 	
        return "redirect:/${pojoNameLower}.do?method=list";
    }
    
    @RequestMapping(params = "method=list")  
    public String list(@ModelAttribute("search") SearchVO searchVO, Model model) throws Exception {
        
        Page resultPage = ${pojoNameLower}Service.getPagingList(searchVO);

		model.addAttribute("${pojoNameLower}List", resultPage.getList());
        model.addAttribute("resultPage", resultPage);
		        
        return "generation/${pojoNameLower}/list"; 
    }
    
    @RequestMapping(params = "method=remove") 
    public String remove(${pojo.shortName} ${pojoNameLower}) throws Exception {
    	                                 
        ${pojoNameLower}Service.remove(${pojoNameLower}.${getIdMethodName}());
		
        return "redirect:/${pojoNameLower}.do?method=list";
    }    
}

