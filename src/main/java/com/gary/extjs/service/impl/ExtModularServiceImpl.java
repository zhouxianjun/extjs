package com.gary.extjs.service.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.gary.dao.dto.SqlWhere;
import com.gary.dao.result.Page;
import com.gary.extjs.dao.IExtModularDao;
import com.gary.extjs.dto.Config;
import com.gary.extjs.dto.FunctionTree;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.entity.ExtModular;
import com.gary.extjs.service.IExtModularService;

@Service
@Transactional("transactionManagerExt")
public class ExtModularServiceImpl implements IExtModularService {
	
	private IExtModularDao extModularDao;

	public ExtModular get(Integer id) {
		// TODO Auto-generated method stub
		return extModularDao.get(id);
	}

	public void saveOrUpdate(ExtModular modular) {
		// TODO Auto-generated method stub
		extModularDao.saveOrUpdate(modular);
	}

	public Serializable save(ExtModular modular) {
		// TODO Auto-generated method stub
		return extModularDao.save(modular);
	}

	public void update(ExtModular modular) {
		// TODO Auto-generated method stub
		extModularDao.update(modular);
	}

	public List<ExtFunction> getFuncitons(String modularIdentifer) {
		// TODO Auto-generated method stub
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		where.put(new SqlWhere("modularIdentifer"), modularIdentifer);
		ExtModular modular = extModularDao.get(where);
		if(modular != null){
			List<ExtFunction> functions = modular.getFunctions();
			return functions;
		}
		return null;
	}

	public List<FunctionTree> getFuncitonsTree(String modularIdentifer) {
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		where.put(new SqlWhere("modularIdentifer"), modularIdentifer);
		ExtModular modular = extModularDao.get(where);
		if(modular != null){
			List<FunctionTree> functionsTree = new ArrayList<FunctionTree>();
			List<ExtFunction> functions = modular.getFunctions();
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
		return null;
	}

	public List<ExtModular> list() {
		// TODO Auto-generated method stub
		return extModularDao.list();
	}

	public ExtModular get(Map<SqlWhere, Object> where) {
		// TODO Auto-generated method stub
		return extModularDao.get(where);
	}

	public ExtModular get(String id) {
		// TODO Auto-generated method stub
		Map<SqlWhere, Object> where = new HashMap<SqlWhere, Object>();
		where.put(new SqlWhere("modularIdentifer"), id);
		return get(where);
	}

	public List<ExtModular> checkLocalFile(List<ExtModular> modulars, String path, String com) {
		List<ExtModular> rs = new ArrayList<ExtModular>();
		for (ExtModular modular : modulars) {
			String name = com + "." + modular.getModularIdentifer() + ".js";
			File file = new File(path, name);
			if(file.exists()){
				rs.add(modular);
			}
		}
		return rs;
	}

	public void deleteFuction(ExtFunction function, ExtModular modular) {
		List<ExtFunction> functions = modular.getFunctions();
		for (ExtFunction function2 : functions) {
			if(function2.getFid() == function.getFid()){
				functions.remove(function2);
				extModularDao.update(modular);
				for (ExtFunction function3 : function.getChildren()) {
					deleteFuction(function3, modular);
				}
				return;
			}
		}
	}

	public void delete(ExtModular modular) {
		// TODO Auto-generated method stub
		extModularDao.delete(modular);
	}

	public List<ExtModular> checkLocal(List<ExtModular> modulars, String path,
			String com, String iconPath, Config config) {
		for (ExtModular modular : modulars) {
			String name = com + "." + modular.getModularIdentifer() + ".js";
			File file = new File(path, name);
			if(file.exists()){
				modular.setLocal(true);
			}
			if(!StringUtils.isEmpty(modular.getIcon())){
				name = modular.getIcon().substring(0, modular.getIcon().lastIndexOf("."));
				String suffix = modular.getIcon().substring(modular.getIcon().lastIndexOf("."));
				file = new File(iconPath, name + "-" + ((int)config.getIconMaxW()) + suffix);
				if(file.exists()){
					file = new File(iconPath, name + "-" + ((int)config.getIconMinW()) + suffix);
					if(file.exists()){
						modular.setIconLocal(true);
					}
				}
			}
		}
		return modulars;
	}
	
	public boolean checkLocal(ExtModular modular, String path, String com){
		if("SysMonitoring".equals(modular.getModularIdentifer())){
			return false;
		}
		String name = com + "." + modular.getModularIdentifer() + ".js";
		File file = new File(path, name);
		return file.exists();
	};

	public Page<ExtModular> list(int pageSize, int page) {
		// TODO Auto-generated method stub
		return extModularDao.list(pageSize, page);
	}

	public void setExtModularDao(IExtModularDao extModularDao) {
		this.extModularDao = extModularDao;
	}

}
