package com.gary.extjs.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.gary.dao.dto.SqlWhere;
import com.gary.dao.result.Page;
import com.gary.extjs.dto.FunctionTree;
import com.gary.extjs.entity.ExtFunction;

public interface IExtFunctionService {
	ExtFunction get(Integer id);
	void saveOrUpdate(ExtFunction function);
	Serializable save(ExtFunction function);
	void update(ExtFunction function);
	ExtFunction get(String id);
	List<FunctionTree> transform(List<ExtFunction> functions);
	List<ExtFunction> list();
	List<ExtFunction> list(Map<SqlWhere, Object> where);
	List<ExtFunction> listTop();
	List<ExtFunction> getParents(Integer fid);
	List<ExtFunction> checkAuth(List<ExtFunction> functions, List<ExtFunction> roleFunctions);
	List<ExtFunction> checkLocalFile(List<ExtFunction> functions, String path, String debug);
	void delete(ExtFunction function);
	Page<ExtFunction> list(int pageSize, int page);
	Page<ExtFunction> list(Map<SqlWhere, Object> where, int pageSize, int page);
}
