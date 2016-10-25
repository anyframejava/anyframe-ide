<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
package ${package}.service.impl;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ${package}.service.${pojo.shortName}Service;
import org.anyframe.pagination.Page;

/**
 * ${pojo.shortName}Service implementation class
 */
@Service("${pojoNameLower}Service")
@Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRED)
public class ${pojo.shortName}ServiceImpl implements ${pojo.shortName}Service {

	@Inject
	@Named("${pojoNameLower}Dao")
    ${pojo.shortName}Dao ${pojoNameLower}Dao;
    
    
    public void create(Map targetMap) throws Exception{
        this.${pojoNameLower}Dao.create(targetMap);	
    }

    public void update(Map targetMap) throws Exception{
        this.${pojoNameLower}Dao.update(targetMap);
    }

    public void remove(Map targetMap) throws Exception{
        this.${pojoNameLower}Dao.remove(targetMap);
    }

    public Map get(Map targetMap) throws Exception{
        return this.${pojoNameLower}Dao.get(targetMap);
    }

	public Page getPagingList(Map targetMap) throws Exception {
		return this.${pojoNameLower}Dao.getPagingList(targetMap);
	}      
}
