package com.core.impl;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.core.ColumnInfo;
import com.core.IDbCompartor;
import com.core.TableInfo;
import com.util.DbUtil;

/**
 * Oracle数据库比较器实现
 * @author MX
 *
 */
public class OracleDbCompartor implements IDbCompartor {
	
	private Connection conn;

	@Override
	public void initConnection(Connection conn) throws Exception {
		this.conn = conn;
	}

	@Override
	public List<TableInfo> getTables(String namePattern) throws Exception {
		StringBuilder buf = new StringBuilder();
		List<Object> params = new ArrayList<>(1);
		buf.append("SELECT TABLE_NAME FROM USER_TABLES WHERE 1=1 ");
		if(!StringUtils.isEmpty(namePattern)){
			buf.append(" AND TABLE_NAME LIKE ? ");
			params.add(namePattern + "%");
		}
		List<Map<String, Object>> tables = DbUtil.query(conn, buf.toString(), params.toArray());
		if(tables == null || tables.size() < 1){
			return null;
		}
		List<TableInfo> results = new ArrayList<>(tables.size());
		for(Map<String, Object> table : tables){
			TableInfo ti = new TableInfo();
			String tableName = (String)table.get("TABLE_NAME");
			ti.setName(tableName);
			ti.setColumns(getColumns(tableName));
			results.add(ti);
		}
		return results;
	}
	
	private List<ColumnInfo> getColumns(String tableName)throws Exception {
		List<Map<String, Object>> datas = DbUtil.query(conn, "SELECT COLUMN_NAME,DATA_TYPE ,DATA_LENGTH FROM ALL_TAB_COLUMNS "
				+ "WHERE TABLE_NAME = ?", tableName);
		if(datas == null || datas.size() < 1){
			return null;
		}
		List<ColumnInfo> results = new ArrayList<>(datas.size());
		for(Map<String, Object> data : datas){
			ColumnInfo ci = new ColumnInfo();
			ci.setName((String)data.get("COLUMN_NAME"));
			ci.setType((String)data.get("DATA_TYPE"));
			Object size = data.get("DATA_LENGTH");
			if(size != null){
				ci.setSize(Integer.parseInt(size.toString()));
			}
			results.add(ci);
		}
		return results;
	}

}
