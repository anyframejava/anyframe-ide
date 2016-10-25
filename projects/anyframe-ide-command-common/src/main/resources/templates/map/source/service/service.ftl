package ${package}.service;

import org.anyframe.pagination.Page;

import java.util.Map;

/**
 * ${pojo.shortName}Service interface.
 */
public interface ${pojo.shortName}Service {

	public void create(Map targetMap) throws Exception;

	public void update(Map targetMap) throws Exception;

	public void remove(Map targetMap) throws Exception;

	public Map get(Map targetMap) throws Exception;

	Page getPagingList(Map targetMap) throws Exception;   
}
