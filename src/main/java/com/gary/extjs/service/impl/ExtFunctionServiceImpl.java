package com.gary.extjs.service.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SQLQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gary.dao.dto.SqlWhere;
import com.gary.dao.result.Page;
import com.gary.extjs.dao.IExtFunctionDao;
import com.gary.extjs.dto.FunctionTree;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.service.IExtFunctionService;

@Service
@Transactional("transactionManagerExt")
public class ExtFunctionServiceImpl implements IExtFunctionService {
	
	private IExtFunctionDao extFunctionDao;

	public ExtFunction get(Integer id) {
		// TODO Auto-generated method stub
		return extFunctionDao.get(id);
	}

	public void saveOrUpdate(ExtFunction function) {
		// TODO Auto-generated method stub
		extFunctionDao.saveOrUpdate(function);
	}

	public Serializable save(ExtFunction function) {
		// TODO Auto-generated method stub
		return extFunctionDao.save(function);
	}

	public void update(ExtFunction function) {
		// TODO Auto-generated method stub
		extFunctionDao.update(function);
	}

	public ExtFunction get(String id) {
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		where.put(new SqlWhere("id"), id);
		return extFunctionDao.get(where);
	}

	public List<FunctionTree> transform(List<ExtFunction> functions) {
		List<FunctionTree> functionsTree = new ArrayList<FunctionTree>();
		for (ExtFunction function : functions) {
			FunctionTree ft = new FunctionTree();
			ft.setCls(function.getCls());
			ft.setExpanded(function.isExpanded());
			ft.setFid(function.getFid());
			ft.setId(function.getId());
			ft.setLeaf(function.isLeaf());
			ft.setText(function.getText());
			functionsTree.add(ft);
		}
		return functionsTree;
	}

	public List<ExtFunction> list() {
		// TODO Auto-generated method stub
		return extFunctionDao.list();
	}

	public List<ExtFunction> list(Map<SqlWhere, Object> where) {
		// TODO Auto-generated method stub
		return extFunctionDao.list(where);
	}

	@SuppressWarnings("unchecked")
	public List<ExtFunction> listTop() {
		// TODO Auto-generated method stub
		SQLQuery sql = extFunctionDao.getSession().createSQLQuery("select * from `function` where parent_id is null");
		sql.addEntity(ExtFunction.class);
		return sql.list();
	}

	public List<ExtFunction> getParents(Integer fid) {
		// TODO Auto-generated method stub
		List<ExtFunction> functions = new ArrayList<ExtFunction>();
		SQLQuery sql = extFunctionDao.getSession().createSQLQuery("select Function_fid from function_function where children_fid = ?");
		sql.setParameter(0, fid);
		for (Object object : sql.list()) {
			Integer id = (Integer)object;
			functions.add(extFunctionDao.get(id));
		}
		return functions;
	}

	public List<ExtFunction> checkAuth(List<ExtFunction> functions,
			List<ExtFunction> roleFunctions) {
		List<ExtFunction> authFunctions = new ArrayList<ExtFunction>();
		for (ExtFunction function : functions) {
			for (ExtFunction roleFunction : roleFunctions) {
				if(function.getFid() == roleFunction.getFid()){
					authFunctions.add(function);
				}
			}
		}
		return authFunctions;
	}

	public List<ExtFunction> checkLocalFile(List<ExtFunction> functions, String path, String debug) {
		List<ExtFunction> rs = new ArrayList<ExtFunction>();
		for (ExtFunction function : functions) {
			String name = function.getId() + debug + ".js";
			File file = new File(path, name);
			if(file.exists() || !function.isLeaf()){
				rs.add(function);
			}
		}
		return rs;
	}

	public void delete(ExtFunction function) {
		extFunctionDao.delete(function);
		List<ExtFunction> functions = function.getChildren();
		for (ExtFunction function2 : functions) {
			delete(function2);
		}
	}

	public Page<ExtFunction> list(int pageSize, int page) {
		// TODO Auto-generated method stub
		return extFunctionDao.list(pageSize, page);
	}

	public Page<ExtFunction> list(Map<SqlWhere, Object> where, int pageSize,
			int page) {
		// TODO Auto-generated method stub
		return extFunctionDao.list(where, pageSize, page);
	}

	public void setExtFunctionDao(IExtFunctionDao extFunctionDao) {
		this.extFunctionDao = extFunctionDao;
	}
}
