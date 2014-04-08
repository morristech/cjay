package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.Operator;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IOperatorDao extends Dao<Operator, Integer> {
	void addListOperators(List<Operator> operators) throws SQLException;

	void addOperator(Operator operator) throws SQLException;

	void deleteAllOperators() throws SQLException;

	List<Operator> getAllOperators() throws SQLException;

	boolean isEmpty() throws SQLException;
}
