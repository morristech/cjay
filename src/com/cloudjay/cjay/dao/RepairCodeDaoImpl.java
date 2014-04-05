package com.cloudjay.cjay.dao;

import java.sql.SQLException;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.cloudjay.cjay.model.DamageCode;
import com.cloudjay.cjay.model.RepairCode;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

public class RepairCodeDaoImpl extends BaseDaoImpl<RepairCode, Integer>
		implements IRepairCodeDao {

	public RepairCodeDaoImpl(ConnectionSource connectionSource)
			throws SQLException {
		super(connectionSource, RepairCode.class);
	}

	public void bulkInsert(SQLiteDatabase db, List<RepairCode> repairCodes) {

		try {
			db.beginTransaction();

			for (RepairCode repairCode : repairCodes) {
				ContentValues values = new ContentValues();
				values.put(RepairCode.CODE, repairCode.getCode());
				values.put(RepairCode.DISPLAY_NAME, repairCode.getName());
				db.insert("repair_code", null, values);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	@Override
	public List<RepairCode> getAllRepairCodes() throws SQLException {
		return this.queryForAll();
	}

	@Override
	public void addListRepairCodes(List<RepairCode> repairCodes)
			throws SQLException {
		for (RepairCode repairCode : repairCodes) {
			this.createOrUpdate(repairCode);
		}
	}

	@Override
	public void addRepairCode(RepairCode repairCode) throws SQLException {
		this.create(repairCode);
	}

	@Override
	public void deleteAllRepairCodes() throws SQLException {
		List<RepairCode> repairCodes = getAllRepairCodes();
		for (RepairCode repairCode : repairCodes) {
			this.delete(repairCode);
		}
	}

	@Override
	public boolean isEmpty() throws SQLException {
		RepairCode repairCode = this.queryForFirst(this.queryBuilder()
				.prepare());
		if (null == repairCode)
			return true;

		return false;
	}

	public RepairCode findByCode(String repairCode) throws SQLException {
		List<RepairCode> listRepairCodes = queryForEq(RepairCode.CODE,
				repairCode);

		if (listRepairCodes.isEmpty()) {
			return null;
		} else {
			return listRepairCodes.get(0);
		}
	}
}
