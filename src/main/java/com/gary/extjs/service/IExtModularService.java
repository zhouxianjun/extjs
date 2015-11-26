package com.gary.extjs.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.gary.dao.dto.SqlWhere;
import com.gary.dao.result.Page;
import com.gary.extjs.dto.Config;
import com.gary.extjs.dto.FunctionTree;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.entity.ExtModular;

public interface IExtModularService {
	ExtModular get(Integer id);
	void saveOrUpdate(ExtModular modular);
	Serializable save(ExtModular modular);
	void update(ExtModular modular);
	List<ExtFunction> getFuncitons(String modularIdentifer);
	List<FunctionTree> getFuncitonsTree(String modularIdentifer);
	List<ExtModular> list();
	ExtModular get(Map<SqlWhere, Object> where);
	ExtModular get(String id);
	List<ExtModular> checkLocalFile(List<ExtModular> modulars, String path, String com);
	List<ExtModular> checkLocal(List<ExtModular> modulars, String path, String com, String iconPath, Config config);
	void deleteFuction(ExtFunction function, ExtModular modular);
	void delete(ExtModular modular);
	Page<ExtModular> list(int pageSize, int page);
	boolean checkLocal(ExtModular modular, String path, String com);
}
