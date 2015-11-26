package com.gary.extjs.service;

import java.io.Serializable;
import java.util.List;

import com.gary.dao.result.Page;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.entity.ExtModular;
import com.gary.extjs.entity.ExtRole;

public interface IExtRoleService {
	ExtRole get(Integer id);
	void saveOrUpdate(ExtRole role);
	Serializable save(ExtRole role);
	void update(ExtRole role);
	List<ExtModular> getModulars(Integer id); 
	List<ExtRole> list();
	Page<ExtRole> list(int pageSize, int page);
	void delete(ExtRole role);
	ExtRole get(String name);
	void deleteFuction(ExtFunction function, ExtRole role);
	void deleteModular(ExtModular modular, ExtRole role);
}
