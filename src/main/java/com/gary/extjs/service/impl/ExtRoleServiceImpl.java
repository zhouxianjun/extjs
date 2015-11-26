package com.gary.extjs.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gary.dao.dto.SqlWhere;
import com.gary.dao.result.Page;
import com.gary.extjs.dao.IExtRoleDao;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.entity.ExtModular;
import com.gary.extjs.entity.ExtRole;
import com.gary.extjs.service.IExtRoleService;

@Service
@Transactional("transactionManagerExt")
public class ExtRoleServiceImpl implements IExtRoleService {

	private IExtRoleDao extRoleDao;
	
	public ExtRole get(Integer id) {
		// TODO Auto-generated method stub
		return extRoleDao.get(id);
	}

	public void saveOrUpdate(ExtRole role) {
		// TODO Auto-generated method stub
		extRoleDao.saveOrUpdate(role);
	}

	public Serializable save(ExtRole role) {
		// TODO Auto-generated method stub
		return extRoleDao.save(role);
	}

	public void update(ExtRole role) {
		// TODO Auto-generated method stub
		extRoleDao.update(role);
	}

	public List<ExtModular> getModulars(Integer id) {
		// TODO Auto-generated method stub
		ExtRole role = get(id);
		if(role != null){
			List<ExtModular> modulars = role.getModulars();
			return modulars;
		}
		return null;
	}

	public List<ExtRole> list() {
		// TODO Auto-generated method stub
		return extRoleDao.list();
	}

	public Page<ExtRole> list(int pageSize, int page) {
		// TODO Auto-generated method stub
		return extRoleDao.list(pageSize, page);
	}

	public void delete(ExtRole role) {
		// TODO Auto-generated method stub
		extRoleDao.delete(role);
	}

	public ExtRole get(String name) {
		// TODO Auto-generated method stub
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		where.put(new SqlWhere("name"), name);
		return extRoleDao.get(where);
	}

	public void deleteFuction(ExtFunction function, ExtRole role) {
		// TODO Auto-generated method stub
		List<ExtFunction> functions = role.getFunctions();
		for (ExtFunction function2 : functions) {
			if(function2.getFid() == function.getFid()){
				functions.remove(function2);
				extRoleDao.update(role);
				for (ExtFunction function3 : function.getChildren()) {
					deleteFuction(function3, role);
				}
				return;
			}
		}
	}

	public void deleteModular(ExtModular modular, ExtRole role) {
		List<ExtModular> modulars = role.getModulars();
		for (ExtModular modular2 : modulars) {
			if(modular2.getId() == modular.getId()){
				modulars.remove(modular2);
				extRoleDao.update(role);
				return;
			}
		}
	}

	public void setExtRoleDao(IExtRoleDao extRoleDao) {
		this.extRoleDao = extRoleDao;
	}

}
