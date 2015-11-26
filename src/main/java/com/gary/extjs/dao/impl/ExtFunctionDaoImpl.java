package com.gary.extjs.dao.impl;

import org.springframework.stereotype.Repository;

import com.gary.dao.hibernate.impl.GenericDAOImpl;
import com.gary.extjs.dao.IExtFunctionDao;
import com.gary.extjs.entity.ExtFunction;

@Repository
public class ExtFunctionDaoImpl extends GenericDAOImpl<ExtFunction> implements IExtFunctionDao {

	@Override
	protected String getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<ExtFunction> getEntityClass() {
		// TODO Auto-generated method stub
		return ExtFunction.class;
	}

}
