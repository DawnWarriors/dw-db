package dw.db.create;

import dw.db.Database;
import dw.db.DatabaseManager;

public class CreateTblDefBaseTbl
{
	/**
	 * create table dw_ref_def(
	 * refid varchar(64),
	 * tblname1 varchar(64),
	 * tblname2 varchar(64),
	 * fldnames1 varchar(64),
	 * fldnames2 varchar(64),
	 * reftype int,
	 * primary key(refid)
	 * );
	 */
	private static final String dw_ref_def_sql        = "create table dw_ref_def (refid varchar(64),tblname1 varchar(64),tblname2 varchar(64),fldnames1 varchar(64),fldnames2 varchar(64),reftype int,primary key(refid));";
	/**
	 * create table dw_ref_def_temp(
	 * refid varchar(64),
	 * tblname1 varchar(64),
	 * tblname2 varchar(64),
	 * fldnames1 varchar(64),
	 * fldnames2 varchar(64),
	 * reftype int,
	 * primary key(refid)
	 * );
	 */
	private static final String dw_ref_def_temp_sql   = "create table dw_ref_def_temp (refid varchar(64),tblname1 varchar(64),tblname2 varchar(64),fldnames1 varchar(64),fldnames2 varchar(64),reftype int,primary key(refid));";
	/**
	 * create table dw_index_def(
	 * indexid varchar(64),
	 * tblname varchar(64),
	 * fldname varchar(64),
	 * primary key(indexid)
	 * );
	 */
	private static final String dw_index_def_sql      = "create table dw_index_def (indexid varchar(64),tblname varchar(64),fldname varchar(64),primary key(indexid));";
	/**
	 * create table dw_index_def_temp(
	 * indexid varchar(64),
	 * tblname varchar(64),
	 * fldname varchar(64),
	 * primary key(indexid)
	 * );
	 */
	private static final String dw_index_def_temp_sql = "create table dw_index_def_temp (indexid varchar(64),tblname varchar(64),fldname varchar(64),primary key(indexid));";
	/**
	 * create table dw_tbl_def(
	 * tblid varchar(64),
	 * tblname varchar(64) not null,
	 * tblname_zh varchar(64) not null,
	 * cacheflags int,
	 * primary key(tblid,tblname)
	 * );
	 */
	private static final String dw_tbl_def_sql        = "create table dw_tbl_def (tblid varchar(64),tblname varchar(64) not null,tblname_zh varchar(64) not null,cacheflags int,primary key(tblid,tblname));";
	/**
	 * create table dw_tbl_def_temp(
	 * tblid varchar(64),
	 * tblname varchar(64) not null,
	 * tblname_zh varchar(64) not null,
	 * cacheflags int,
	 * primary key(tblname)
	 * );
	 */
	private static final String dw_tbl_def_temp_sql   = "create table dw_tbl_def_temp (tblid varchar(64),tblname varchar(64) not null,tblname_zh varchar(64) not null,cacheflags int,primary key(tblname));";
	/**
	 * create table dw_fld_def(
	 * tblid varchar(64),
	 * tblname varchar(64),
	 * fldid varchar(64),
	 * fldname varchar(64),
	 * fldname_zh varchar(64),
	 * fldtype varchar(16),
	 * fldlen int,
	 * flddecimal int,
	 * fldattr varchar(8),
	 * flddefault varchar(16),
	 * fldreftbl varchar(64),
	 * fldreffld varchar(64),
	 * primary key(tblname,fldname)
	 * );
	 */
	private static final String dw_fld_def_sql        = "create table dw_fld_def (tblid varchar(64),tblname varchar(64),fldid varchar(64),fldname varchar(64),fldname_zh varchar(64),fldtype varchar(16),fldlen int,flddecimal int,fldattr varchar(8),flddefault varchar(16),fldreftbl varchar(64),fldreffld varchar(64),primary key(tblname,fldname));";
	/**
	 * create table dw_fld_def_temp(
	 * tblid varchar(64),
	 * tblname varchar(64),
	 * fldid varchar(64),
	 * fldname varchar(64),
	 * fldname_zh varchar(64),
	 * fldtype varchar(16),
	 * fldlen int,
	 * flddecimal int,
	 * fldattr varchar(8),
	 * flddefault varchar(16),
	 * fldreftbl varchar(64),
	 * fldreffld varchar(64),
	 * primary key(tblname,fldid,fldname)
	 * );
	 */
	private static final String dw_fld_def_temp_sql   = "create table dw_fld_def_temp (tblid varchar(64),tblname varchar(64),fldid varchar(64),fldname varchar(64),fldname_zh varchar(64),fldtype varchar(16),fldlen int,flddecimal int,fldattr varchar(8),flddefault varchar(16),fldreftbl varchar(64),fldreffld varchar(64),primary key(tblname,fldid,fldname));";

	@SuppressWarnings("resource")
	public static void work() throws Exception
	{
		Database db = DatabaseManager.createDatabase();
		try
		{
			createBaseTbl(db,"dw_ref_def",dw_ref_def_sql);
			createBaseTbl(db,"dw_ref_def_temp",dw_ref_def_temp_sql);
			createBaseTbl(db,"dw_index_def",dw_index_def_sql);
			createBaseTbl(db,"dw_index_def_temp",dw_index_def_temp_sql);
			createBaseTbl(db,"dw_tbl_def",dw_tbl_def_sql);
			createBaseTbl(db,"dw_tbl_def_temp",dw_tbl_def_temp_sql);
			createBaseTbl(db,"dw_fld_def",dw_fld_def_sql);
			createBaseTbl(db,"dw_fld_def_temp",dw_fld_def_temp_sql);
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		} finally
		{
			DatabaseManager.closeDatabase(db);
		}
	}

	private static void createBaseTbl(Database db, String tblName, String createSql)
	{
		//判断表是否已经存在
		String checkSql = "select count(1) from " + tblName;
		try
		{
			db.execute(checkSql);
		} catch (Exception e)
		{
			//表不存在，需要新建
			try
			{
				db.execute(createSql);
			}catch (Exception e2)
			{
				throw new RuntimeException(e2);
			}
		}
	}
}
