package com.gary.extjs.dao.impl;

import org.springframework.stereotype.Repository;

import com.gary.dao.hibernate.impl.GenericDAOImpl;
import com.gary.extjs.dao.IExtRoleDao;
import com.gary.extjs.entity.ExtRole;

@Repository
public class ExtRoleDaoImpl extends GenericDAOImpl<ExtRole> implements IExtRoleDao {

	@Override
	protected String getDataSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<ExtRole> getEntityClass() {
		// TODO Auto-generated method stub
		return ExtRole.class;
	}

}
