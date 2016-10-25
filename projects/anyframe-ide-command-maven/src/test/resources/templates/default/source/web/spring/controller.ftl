<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${basepackage}.web;

import javax.inject.Inject;
import javax.inject.Named;

import ${pojo.packageName}.${pojo.shortName};
import ${basepackage}.service.${pojo.shortName}Service;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;

<#if !c2j.isComponent(pojo.identifierProperty)>
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
	<#if !data.isPrimitive(valueType)>
	  <#foreach field in pojo.getAllPropertiesIterator()>	  
	    <#if field.equals(pojo.identifierProperty)>
import ${field.value.type.returnedClass.name};
	    </#if>
	  </#foreach>
	</#if>
</#if>

import anyframe.common.Page;
import anyframe.common.util.SearchVO; 

/**
 * Controller for ${pojo.shortName}
 */
@Controller
@RequestMapping("/${pojoNameLower}.do")
public class ${pojo.shortName}Controller {

	/**
	 * Resource Injection on ${pojo.shortName}Service
	 */
	@Inject
	@Named("${pojoNameLower}Service")
    private ${pojo.shortName}Service ${pojoNameLower}Service;

    /**
     * Setter method only for test case.
     * @param ${pojoNameLower}Service resource injection
     */
    public void set${pojo.shortName}Service(${pojo.shortName}Service ${pojoNameLower}Service) {
        this.${pojoNameLower}Service = ${pojoNameLower}Service;
    }
    
     /**
     * Display form for adding ${pojo.shortName}.
     * @param model model containing control data
     * @return the prepared form view
     * @throws Exception in case of an invalid new form object
     */
    @RequestMapping(params = "method=createView") 
    public String createView(Model model) throws Exception {  
    	model.addAttribute(new ${pojo.shortName}());
        return "genView${pojo.shortName}" ;                   
    }

     /**
     * Add ${pojo.shortName}.
     * @param ${pojoNameLower} form object with request parameters bound onto it 
     * @return the prepared ${pojo.shortName} list view
     * @throws Exception in case of adding ${pojo.shortName}
     */
    @RequestMapping(params = "method=create")  
    public String create(${pojo.shortName} ${pojoNameLower}, BindingResult results, SessionStatus status) throws Exception {
    	
    	if (results.hasErrors()) {
			return "genView${pojo.shortName}";
		}
		
        ${pojoNameLower}Service.create(${pojoNameLower});   
        status.setComplete();
             
        return "redirect:/${pojoNameLower}.do?method=list";        
    }


    /**
     * Get a ${pojo.shortName} detail.
<#if c2j.isComponent(pojo.identifierProperty)>          
     * @param ${pojoNameLower} form object with request parameters bound onto it
<#else>
     * @param ${pojo.identifierProperty.name} the ID of the ${pojo.shortName} to display	
</#if>  
     * @param model model containing control data    
     * @return the prepared form view
     * @throws Exception in case of getting ${pojo.shortName}
     */
    @RequestMapping(params = "method=get")    
    public String get(<#if c2j.isComponent(pojo.identifierProperty)> @ModelAttribute("input${pojo.shortName}")${pojo.shortName} ${pojoNameLower} 
    						<#else>@RequestParam("${pojo.identifierProperty.name}") String ${pojo.identifierProperty.name}</#if>, Model model) throws Exception { 	
 	<#if c2j.isComponent(pojo.identifierProperty)>
       ${pojoNameLower} = ${pojoNameLower}Service.get(${pojoNameLower}.${getIdMethodName}());        	
       model.addAttribute("${pojoNameLower}", ${pojoNameLower});    
	<#else>
       if (!StringUtils.isBlank(${pojo.identifierProperty.name})) {
       		${pojo.shortName} ${pojoNameLower} = ${pojoNameLower}Service.get(new ${identifierType}(${pojo.identifierProperty.name}));        	
        	model.addAttribute("${pojoNameLower}", ${pojoNameLower});        	                	
       }
	</#if>	
 	
       return "genView${pojo.shortName}";
    }


    /**
     * Update ${pojo.shortName}.
     * @param ${pojoNameLower} form object with request parameters bound onto it
     * @return the prepared ${pojo.shortName} list view
     * @throws Exception in case of updating ${pojo.shortName}
     */ 
    @RequestMapping(params ="method=update") 
    public String update(${pojo.shortName} ${pojoNameLower}, BindingResult results, SessionStatus status) throws Exception {
    
    	if (results.hasErrors()) {
			return "genView${pojo.shortName}";
		}
		    	
        ${pojoNameLower}Service.update(${pojoNameLower});   
        status.setComplete();
                 	
        return "redirect:/${pojoNameLower}.do?method=list";
    }

    /**
     * Display ${pojo.shortName} list.
     * @param request current HTTP request
     * @param searchVO form object with request parameters bound onto it
     * @param model model containing control data
     * @return the prepared ${pojo.shortName} list view
     * @throws Exception in case of getting ${pojo.shortName} paging list
     */ 
    @RequestMapping(params = "method=list")  
    public String list(@ModelAttribute("search") SearchVO searchVO, Model model) throws Exception {
        
        Page resultPage = ${pojoNameLower}Service.getPagingList(searchVO);

		model.addAttribute("${pojoNameLower}List", resultPage.getList());
        model.addAttribute("size", resultPage.getTotalCount());
        model.addAttribute("resultPage", resultPage);
		        
        return "genList${pojo.shortName}";        
    }

    /**
     * Delete ${pojo.shortName}.
     * @param ${pojoNameLower} form object with request parameters bound onto it
     * @return the prepared ${pojo.shortName} list view
     * @throws Exception in case of deleting ${pojo.shortName}
     */ 
    @RequestMapping(params = "method=remove") 
    public String remove(${pojo.shortName} ${pojoNameLower}) throws Exception {
    	                                 
        ${pojoNameLower}Service.remove(${pojoNameLower}.${getIdMethodName}());
		
        return "redirect:/${pojoNameLower}.do?method=list";
    }    
}
