package com.gary.extjs.dao.impl;

import org.springframework.stereotype.Repository;

import com.gary.dao.hibernate.impl.GenericDAOImpl;
import com.gary.extjs.dao.IExtModularDao;
import com.gary.extjs.entity.ExtModular;

@Repository
public class ExtModularDaoImpl extends GenericDAOImpl<ExtModular> implements IExtModularDao {

	@Override
	protected String getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<ExtModular> getEntityClass() {
		// TODO Auto-generated method stub
		return ExtModular.class;
	}

}
