package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import com.cloudjay.cjay.model.UserLog;
import com.j256.ormlite.dao.Dao;

/**
 * @author tieubao
 */

public interface IUserLogDao extends Dao<UserLog, Integer> {
	void addListLogs(List<UserLog> userLogs) throws SQLException;

	void addLog(UserLog userLog) throws SQLException;

	void deleteAllLogs() throws SQLException;

	List<UserLog> getAllLogs() throws SQLException;

	boolean isEmpty() throws SQLException;
}
