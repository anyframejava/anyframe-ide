<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.web;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.support.SessionStatus;

import ${package}.service.${pojo.shortName}Service;
import org.anyframe.datatype.HashMapModel;
import org.anyframe.pagination.Page;
import org.anyframe.util.StringUtil;

/**
 * Controller for ${pojo.shortName}
 */
@Controller
@RequestMapping("/${pojoNameLower}.do")
public class ${pojo.shortName}Controller {

	private final String PARAM_PAGE_INDEX = "pageIndex";

	@Inject
	@Named("${pojoNameLower}Service")
    private ${pojo.shortName}Service ${pojoNameLower}Service;

    public void set${pojo.shortName}Service(${pojo.shortName}Service ${pojoNameLower}Service) {
        this.${pojoNameLower}Service = ${pojoNameLower}Service;
    }

    @RequestMapping(params = "method=createView") 
    public String createView(@ModelAttribute("${pojoNameLower}") HashMapModel hashMapModel) throws Exception {   
        return "generation/${pojoNameLower}/form";                   
    }

    @RequestMapping(params = "method=create")  
    public String create(@ModelAttribute("${pojoNameLower}") HashMapModel hashMapModel, BindingResult results, SessionStatus status) throws Exception {
    	
    	if (results.hasErrors()) {
			return "generation/${pojoNameLower}/form";
		}
		
        ${pojoNameLower}Service.create(hashMapModel.getMap());    
        status.setComplete();
            
        return "redirect:/${pojoNameLower}.do?method=list";        
    }

    @RequestMapping(params = "method=get")    
    public String get(@ModelAttribute("${pojoNameLower}") HashMapModel hashMapModel) throws Exception { 	
        Map resultMap = ${pojoNameLower}Service.get(hashMapModel.getMap());
       	hashMapModel.setMap(resultMap);   	               	                	

       return "generation/${pojoNameLower}/form";
    }
 
    @RequestMapping(params ="method=update") 
    public String update(@ModelAttribute("${pojoNameLower}") HashMapModel hashMapModel, BindingResult results, SessionStatus status) throws Exception {
    
    	if (results.hasErrors()) {
			return "generation/${pojoNameLower}/form";
		}
		    	
        ${pojoNameLower}Service.update(hashMapModel.getMap()); 
        status.setComplete();
                   	
        return "redirect:/${pojoNameLower}.do?method=list";
    }

    @RequestMapping(params = "method=remove") 
    public String remove(@ModelAttribute("${pojoNameLower}") HashMapModel hashMapModel) throws Exception {           
        ${pojoNameLower}Service.remove(hashMapModel.getMap());
		
        return "redirect:/${pojoNameLower}.do?method=list";
    }      

    @RequestMapping(params = "method=list")  
    public String list(@ModelAttribute("search") HashMapModel hashMapModel, Model model, HttpServletRequest request) throws Exception {
    	Map map = hashMapModel.getMap();
    	map.put(PARAM_PAGE_INDEX, getPageIndex(request));
    	
        Page resultPage = ${pojoNameLower}Service.getPagingList(map);
        model.addAttribute("${pojoNameLower}List", resultPage.getList());
        model.addAttribute("resultPage", resultPage);
		        
        return "generation/${pojoNameLower}/list"; 
    }
    
    protected String getPageIndex(HttpServletRequest request) {
		if (StringUtil.isNotEmpty(request.getParameter(PARAM_PAGE_INDEX)))
			return request.getParameter(PARAM_PAGE_INDEX);
		return "1";
	}
}
