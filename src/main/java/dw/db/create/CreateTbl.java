package dw.db.create;

import dw.db.Database;
import dw.db.DatabaseConstant;
import dw.db.DatabaseManager;
import dw.db.util.CreateTblUtil;
import dw.db.util.DBUtil;
import dw.db.util.SqlFilterUtil;
import dw.common.util.list.ListUtil;
import dw.common.util.map.MapUtil;
import dw.common.util.str.StrUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 规定一般顺序限制为：include，define，tbldef_temp, flddef_temp, refdef, indexdef
 * 		include顺序限制：先引入一般表结构定义，再引入define文件
 * 顺序错误可能会导致define失效，flddef_temp找不到table.id
 * 
 * 通过新表旧表的对比，决定是新增还是删除还是更新（建删表，加删改字段、约束和索引）
 * 更新：先找出修改的所有列，然后再确定是那张表的那个字段做了哪些修改
 * 
 * 1、清空新表数据（通过脚本生成先清空新表，为了直接修改表信息也能作用到数据库，一切修改都是修改新表）
 * 2、存储表结构信息至新表
 * 3、根据新旧差异操作数据库（留出接口可直接从这步开始，为了能直接修改表信息）
 * 4、清空旧表数据，将新表数据复制一份到旧表
 * @author xins_cyf
 */
public class CreateTbl
{
	private static String	tags[]			= { "#flddef", "#indexdef", "#refdef", "#tbldef", "#define", "#include" };
	//与tags对应
	private static String	tables_temp[]	= { "dw_fld_def_temp", "dw_index_def_temp", "dw_ref_def_temp", "dw_tbl_def_temp" };
	//数据库脚本入口文件
	private static final String entranceFile = "dw_tbl_base.txt";
	//表可更新列定义
	//字段可更新列定义
	public static void work() throws Exception
	{
		Map<String,String> defineMap = new HashMap<>();
		Map<String,List<Map<String,String>>> defs = new HashMap<>();
		String pathPre = CreateTbl.class.getClassLoader().getResource("").toURI().getPath() + "/dw/";
		//约定总入口只有一个
		getDBFileTree(entranceFile, defineMap, defs, pathPre);
		Database db = DatabaseManager.createDatabase();
		//先清空临时表
		for (String tbl : tables_temp)
		{
			String sql = "delete from " + tbl;
			db.execute(sql);
		}
		Map<String,String> tableNameCode = new HashMap<>();
		List<Map<String,String>> tbldef_temps = defs.get("#tbldef");
		for (Map<String,String> map : tbldef_temps)
		{
			String tblCode = map.get("tblid");
			String tblName = map.get("tblname");
			tableNameCode.put(tblName, tblCode);
			//加入固定字段
			List<Map<String,String>> defInfos = defs.get("#flddef");
			//-- 删除标志、创建日期、更新日期
			//--删除标志
			addDefaultFld(defInfos, tblName, "999", "_delete_flag", "删除标志", "int", "2", "0");
			//--创建日期
			addDefaultFld(defInfos, tblName, "998", "_create_date", "创建日期", "date", "8", "");
			//--更新日期
			addDefaultFld(defInfos, tblName, "997", "_update_date", "更新日期", "date", "8", "");
		}
		for (int i = 0; i < 4; i++)
		{
			List<Map<String,String>> defInfos = defs.get(tags[i]);
			for (Map<String,String> map : defInfos)
			{
				if (i == 0)
				{
					String len = map.get("fldlen");
					String tblname = map.get("tblname");
					if (!len.matches("[0-9]+"))
					{
						String realLen = defineMap.get(len);
						if (realLen == null)
						{
							String error = "表：" + tblname + ":" + "长度宏未定义：" + len;
							throw new RuntimeException(error);
						}
						map.put("fldlen", realLen);
					}
					map.put("tblid", tableNameCode.get(tblname));
				}
				db.insert2(tables_temp[i], map);
			}
		}
		db.beginTrans();
		boolean isNeedRollback = false;
		try
		{
			updateTable(db, true);
		} catch (Exception e)
		{
			//e.printStackTrace();
			isNeedRollback = true;
			throw new RuntimeException(e);
		} finally
		{
			db.commitTrans(isNeedRollback);
			DatabaseManager.closeDatabase(db);
		}
	}

	/**
	 * 获取一个文件所有引入的文件集合中所有的def信息
	 * @param fileName
	 * @param defineMap
	 * @param defs
	 */
	public static void getDBFileTree(String fileName, Map<String,String> defineMap, Map<String,List<Map<String,String>>> defs, String pathPre)
	{
		File file = new File(pathPre + fileName);
		InputStreamReader reader = null;
		try
		{
			reader = new InputStreamReader(new FileInputStream(file), "utf-8");
			char cbuf[] = new char[(int) file.length()];
			reader.read(cbuf);
			Map<String,String> defineMap_ = getDefineForOneFile(file.getName(), new String(cbuf));
			List<String> includes = getIncludeForOneFile(file.getName(), new String(cbuf));
			Map<String,List<Map<String,String>>> defs_ = getTagDefs(file.getName(), new String(cbuf));
			MapUtil.mergeMap(defineMap, defineMap_);
			if (defs_ != null)
			{
				Set<String> keySet = defs_.keySet();
				for (String key : keySet)
				{
					List<Map<String,String>> values = defs.get(key);
					if (values == null)
					{
						defs.put(key, defs_.get(key));
					}
					ListUtil.mergeList(values, defs_.get(key));
				}
			}
			if (includes != null)
			{
				for (String include : includes)
				{
					getDBFileTree(include, defineMap, defs, pathPre);
				}
			}
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		} finally
		{
			//e.printStackTrace();
			if (reader != null)
			{
				try
				{
					reader.close();
				} catch (IOException e1)
				{
					throw new RuntimeException(e1);
				}
			}
		}
	}

	/**
	 * 获取当前文件的include内容，一般include的内容都是文件，在文件加载后，获取其对应的所有包含文件
	 * table相关的定义，是针对所有文件的，要一次性读取完
	 * define相关的，只处理当前文件
	 * define的优先级：
	 * 		当前文件中的定义优先级高于引入文件的定义；
	 * 		后引入的文件优先级高于先引入的文件；
	 * 约定：所有的define都在指定的define文件里
	 * @return
	 */
	private static List<String> getIncludeForOneFile(String fileName, String file)
	{
		String startTag = "#include";
		String lines[] = file.split("\n");
		List<String> includeList = new ArrayList<>();
		int lineIdex = 1;
		for (String line : lines)
		{
			boolean isTagStart = false;
			line = line.trim();
			if (isTagStart == false && line.startsWith(startTag))
			{
				isTagStart = true;
			}
			if (isTagStart)
			{
				if (isTagEnd(startTag, line))
				{
					isTagStart = false;
					continue;
				}
				String includes[] = getDefInfo(startTag, line);
				if (includes.length < 1)
				{
					throwError(fileName, lineIdex);
				}
				for (String include : includes)
				{
					includeList.add(include);
				}
				lineIdex++;
			}
		}
		return includeList;
	}

	/**
	 * 获取一个文件的宏定义信息
	 * @param fileName
	 * @param file
	 * @return
	 */
	private static Map<String,String> getDefineForOneFile(String fileName, String file)
	{
		String startTag = "#define";
		String lines[] = file.split("\n");
		Map<String,String> defineMap = new HashMap<>();
		int line_index = 1;
		for (String line : lines)
		{
			boolean isTagStart = false;
			line = line.trim();
			if (isTagStart == false && line.startsWith(startTag))
			{
				isTagStart = true;
			}
			if (isTagStart)
			{
				if (isTagEnd(startTag, line))
				{
					isTagStart = false;
					continue;
				}
				line = line.replaceAll(startTag, "").trim().replaceAll("\\s+", ",");
				String defines[] = line.split(",");
				if (defines.length == 2)
				{
					defineMap.put(defines[0], defines[1]);
				} else
				{
					throwError(fileName, line_index + 1);
				}
			}
			line_index++;
		}
		return defineMap;
	}

	/**
	 * 获取file中的所有定义信息
	 * @param filenames
	 * @param file
	 * @return
	 */
	private static Map<String,List<Map<String,String>>> getTagDefs(String filenames, String file)
	{
		Map<String,List<Map<String,String>>> confInfos = new HashMap<>();
		for (int i = 0; i < 4; i++)
		{
			confInfos.put(tags[i], getTagDef(tags[i], filenames, file));
		}
		return confInfos;
	}

	/**
	 * 单个文件单个tag
	 * @param startTag
	 * @param filenames
	 * @param file
	 * @return
	 */
	private static List<Map<String,String>> getTagDef(String startTag, String filenames, String file)
	{
		List<Map<String,String>> confInfo = new ArrayList<>();
		String lines[] = file.split("\n");
		String definfo[] = null;
		boolean isTagStart = false;
		//行数记录
		int lineIndex = 0;
		for (String line : lines)
		{
			lineIndex++;
			line = line.trim();
			if (isTagStart == false && line.startsWith(startTag))
			{
				isTagStart = true;
				definfo = getDefInfo(startTag, line);
			}
			if (isTagStart)
			{
				if (isTagEnd(startTag, line))
				{
					isTagStart = false;
					continue;
				}
				if (line.startsWith("#") || line.equals("") || line.startsWith("//"))
				{
					continue;
				}
				line = line.replaceAll("\\s+", ",");
				String lineDatas[] = line.split(",");
				if (definfo.length < lineDatas.length)
				{
					//文件结构错误
					throwError(filenames, lineIndex);
				}
				//封装结果
				Map<String,String> lineDataMap = new HashMap<>();
				for (int i = 0, len = definfo.length; i < len; i++)
				{
					if (i >= lineDatas.length)
					{
						lineDataMap.put(definfo[i], null);
					} else
					{
						if (StrUtil.isStrTrimNull(lineDatas[i]))
						{
							lineDatas[i] = null;
						}
						lineDataMap.put(definfo[i], lineDatas[i]);
					}
				}
				confInfo.add(lineDataMap);
			}
		}
		return confInfo;
	}

	/**
	 * 是否是一个标签的定义结束，根据一行的其实标签是否和上一个标签相同，如不同则说明是新标签
	 * @param startTag
	 * @param line
	 * @return
	 */
	private static boolean isTagEnd(String startTag, String line)
	{
		if (line == null || "".equals(line))
		{
			return false;
		}
		for (String tag : tags)
		{
			if (!tag.equals(startTag))
			{
				if (line.startsWith(tag))
				{
					return true;
				}
			} else
			{
				//同一个标签可以连续出现多次
				return false;
			}
		}
		return false;
	}

	/**
	 * 获取定义数据，这里的定义数据指的是定义的列信息，写在#def后面的括号中的内容
	 * @param tag
	 * @param line
	 * @return
	 */
	private static String[] getDefInfo(String tag, String line)
	{
		if (line.startsWith(tag))
		{
			line = line.replaceAll("(#.*\\()(.*)(\\))", "$2");
			line = line.replaceAll("\\s+", "");
			return line.split(",");
		}
		return null;
	}

	/**
	 * 输出异常信息，指出报错位置
	 * @param fileName
	 * @param lineIndex
	 */
	private static void throwError(String fileName, int lineIndex)
	{
		throw new RuntimeException("文件" + fileName + "第" + lineIndex + "行错误：" + "定义错误!");
	}

	/**
	 * 根据定义信息去处理表结构信息
	 * 10：控制表的新建和删除
	 * 15：更新表定义信息，如表中文名、缓存标识等
	 * 20：控制字段的新加和删除
	 * 30：索引和关联的处理----TODO 暂放，可以放在2.0去做
	 * 40：把def中得数据写到old里
	 * @param db
	 * @param isCleanAble	是否可以清空字段值，因为清空属于危险操作，所以建议手动去执行SQL，留下这个参数做接口
	 * @throws Exception 
	 */
	private static void updateTable(Database db, boolean isCleanAble) throws Exception
	{
		//表的删除
		dropTable(db);
		//表的新建
		addTable(db);
		//更新表
		updateTbl(db);
		//字段定义表的主键有两个，所以需要特殊处理
		String tbldef_sql = "select tblname,fldname from dw_fld_def_temp";
		String tbldef_old_sql = "select tblname,fldname from dw_fld_def";
		List<Map<String,Object>> newFld = db.queryListMap(tbldef_sql);
		List<Map<String,Object>> oldFld = db.queryListMap(tbldef_old_sql);
		List<String> newFld_strList = new ArrayList<>();
		List<String> oldFld_strList = new ArrayList<>();
		//组装tblname和fldname，用@连接
		for (Map<String,Object> map : newFld)
		{
			String tblname = (String) map.get("tblname");
			String fldname = (String) map.get("fldname");
			newFld_strList.add(tblname + "@" + fldname);
		}
		for (Map<String,Object> map : oldFld)
		{
			String tblname = (String) map.get("tblname");
			String fldname = (String) map.get("fldname");
			oldFld_strList.add(tblname + "@" + fldname);
		}
		String newFld_str = StrUtil.linkListToString(newFld_strList, ",");
		String oldFld_str = StrUtil.linkListToString(oldFld_strList, ",");
		String addFld_strSet = StrUtil.getDiffset(newFld_str, oldFld_str, ",");
		//新增列
		addCol(db, addFld_strSet);
		String dropFld_strSet = StrUtil.getDiffset(oldFld_str, newFld_str, ",");
		//删除列
		if (isCleanAble)
		{
			delCol(db, dropFld_strSet);
		}
		//更新列
		updateCol(db);
	}

	/**
	 * 删除表
	 * @param db
	 * @throws Exception
	 */
	private static void dropTable(Database db) throws Exception
	{
		String drop_tbl_sql = "select tblname from dw_tbl_def where tblname not in (select tblname from dw_tbl_def_temp)";
		Object[] dropTbls = db.queryObject1Col(drop_tbl_sql, "tblname");
		for (Object obj : dropTbls)
		{
			String tblName = (String) obj;
			String dropTblSql = "drop table " + tblName;
			db.execute(dropTblSql);
			String updateflddef_Sql = "delete from dw_fld_def where tblname='" + tblName + "'";
			String updatetbldef_Sql = "delete from dw_tbl_def where tblname='" + tblName + "'";
			db.execute(updateflddef_Sql);
			db.execute(updatetbldef_Sql);
		}
	}

	/**
	 * 新增表
	 * @param db
	 * @throws Exception
	 */
	private static void addTable(Database db) throws Exception
	{
		String add_tbl_sql = "select tblname from dw_tbl_def_temp where tblname not in (select tblname from dw_tbl_def)";
		Object[] addTbls = db.queryObject1Col(add_tbl_sql, "tblname");
		if (addTbls.length == 0)
		{
			return;
		}
		//新增表，直接根据表明去flddef中取得相关字段的配置，一并生成建表sql,并执行
		Map<String,Object> params = new HashMap<>();
		String getAddTblColsSql = "select * from dw_fld_def_temp where " + SqlFilterUtil.getInFilter("tblname", addTbls, params);
		Map<String,List<Map<String,Object>>> addTblCols = db.queryMapListMap(getAddTblColsSql, params, new String[] { "tblname" });
		String dbType = db.getDbType();
		for (Object obj : addTbls)
		{
			String tblName = (String) obj;
			List<Map<String,Object>> addCols = addTblCols.get(tblName);
			StringBuffer createTblSqlBuffer = new StringBuffer();
			//生成建表语句，并执行
			createTblSqlBuffer.append("create table " + tblName + " (");
			List<String> keyCols = new ArrayList<>();
			if (addCols == null || addCols.size() == 0)
			{
				String error = "表：" + tblName + " 未找到对应的列信息！";
				throw new RuntimeException(error);
			}
			for (Map<String,Object> map : addCols)
			{
				createTblSqlBuffer.append(map.get("fldname") + " ");
				String typeInfo = CreateTblUtil.getTypeInfo(dbType, (String) map.get("fldtype"), map.get("fldlen").toString(), map.get("flddecimal").toString());
				createTblSqlBuffer.append(typeInfo);
				//默认值
				String defaultInfo = CreateTblUtil.getDefaultInfo(dbType, (String) map.get("flddefault"));
				if (defaultInfo != null)
				{
					createTblSqlBuffer.append(defaultInfo);
				}
				//not null
				String notNullInfo = CreateTblUtil.getNotNullInfo(dbType, (String) map.get("fldattr"));
				if (notNullInfo != null)
				{
					createTblSqlBuffer.append(notNullInfo);
				}
				//唯一性
				String uniqueInfo = CreateTblUtil.getUniqueInfo(dbType, (String) map.get("fldattr"));
				if (uniqueInfo != null)
				{
					createTblSqlBuffer.append(uniqueInfo);
				}
				//判断是否是主键
				boolean isPrimaryKey = CreateTblUtil.isPrimaryKey((String) map.get("fldattr"));
				if (isPrimaryKey)
				{
					//TODO 约定只有单主键，多主键通过唯一性约束
					keyCols.add((String) map.get("fldname") + " ");
				}
				createTblSqlBuffer.append(",");
			}
			String primaryKeyExpr = CreateTblUtil.getPrimaryKeyExpr(dbType, tblName, keyCols);
			createTblSqlBuffer.append(primaryKeyExpr + ")");
			db.execute(createTblSqlBuffer.toString());
			//执行更新定义表语句
			String updatetbldef_sql = "insert into dw_tbl_def select * from dw_tbl_def_temp where tblname='" + tblName + "'";
			String updateflddef_Sql = "insert into dw_fld_def select * from dw_fld_def_temp where tblname='" + tblName + "'";
			db.execute(updatetbldef_sql);
			db.execute(updateflddef_Sql);
		}
	}

	/**
	 * 更新tbldef
	 * @param db
	 * @throws Exception
	 */
	private static void updateTbl(Database db) throws Exception
	{
		String update_tbl_sql = DBUtil.getDiffColsSql("dw_tbl_def_temp", "dw_tbl_def", new String[] { "tblname" }, DatabaseConstant.tbldef_editable_cols);
		List<Map<String,Object>> updateTblMaps = db.queryListMap(update_tbl_sql);
		//这里不需要判断更改了哪些字段值，全部更新掉即可，因为没有需要根据列的而改变进行的处理
		for (Map<String,Object> map : updateTblMaps)
		{
			String tblName = (String) map.get("tblname");
			Map<String,Object> params = new HashMap<>();
			params.put("tblname", tblName);
			for (String col : DatabaseConstant.tbldef_editable_cols)
			{
				//params.put(col, map.get("tbldef_temp." + col));
				params.put(col, map.get(col));
			}
			try
			{
				db.update2("dw_tbl_def", params);
			} catch (Exception e)
			{
				throw new RuntimeException("表：" + tblName + e.getMessage());
			}
		}
	}

	/**
	 * 新增列
	 * @param db
	 * @param addFld_strSet
	 * @throws Exception
	 */
	private static void addCol(Database db, String addFld_strSet) throws Exception
	{
		if (addFld_strSet.equals(""))
		{
			return;
		}
		String addFld_strs[] = addFld_strSet.split(",");
		String dbType = db.getDbType();
		for (String s : addFld_strs)
		{
			//组装拆解
			String ary[] = s.split("@");
			String tblname = ary[0];
			String fldname = ary[1];
			Map<String,Object> params = new HashMap<>();
			params.put("tblname", tblname);
			params.put("fldname", fldname);
			String getAddColsSql = "select * from dw_fld_def_temp where tblname=:tblname and fldname=:fldname";
			Map<String,Object> map = db.queryMap(getAddColsSql, params);
			StringBuffer addColSqlBuf = new StringBuffer();
			addColSqlBuf.append("alter table " + tblname + " add column " + fldname);
			String typeInfo = CreateTblUtil.getTypeInfo(dbType, (String) map.get("fldtype"), map.get("fldlen").toString(), map.get("flddecimal").toString());
			addColSqlBuf.append(typeInfo);
			//默认值
			String defaultInfo = CreateTblUtil.getDefaultInfo(dbType, (String) map.get("flddefault"));
			if (defaultInfo != null)
			{
				addColSqlBuf.append(defaultInfo);
			}
			//not null
			String notNullInfo = CreateTblUtil.getNotNullInfo(dbType, (String) map.get("fldattr"));
			if (notNullInfo != null)
			{
				addColSqlBuf.append(notNullInfo);
			}
			//唯一性
			String uniqueInfo = CreateTblUtil.getUniqueInfo(dbType, (String) map.get("fldattr"));
			if (uniqueInfo != null)
			{
				addColSqlBuf.append(uniqueInfo);
			}
			try
			{
				db.execute(addColSqlBuf.toString());
				//更新定义表
				db.insert2("dw_fld_def", map);
			} catch (Exception e)
			{
				String error = addColSqlBuf.toString() + "\n" + e.getMessage();
				throw new RuntimeException(error);
			}
		}
	}

	/**
	 * 更新列
	 * @param db
	 * @throws Exception
	 */
	public static void updateCol(Database db) throws Exception
	{
		//联合查询的结果集中，mysql的会将后面重复的列名后面拼上序号，从1开始，至于其他数据库是不是这种情况，未作考证
		String update_fld_sql = DBUtil.getDiffColsSql("dw_fld_def_temp", "dw_fld_def", new String[] { "tblname", "fldname" }, DatabaseConstant.flddef_editable_cols);
		List<Map<String,Object>> updateColMaps = db.queryListMap(update_fld_sql);
		String dbType = db.getDbType();
		for (Map<String,Object> map : updateColMaps)
		{
			String tblname = (String) map.get("tblname");
			String fldname = (String) map.get("fldname");
			//更新后的列属性
			StringBuffer newFldSqlBuf = new StringBuffer();
			newFldSqlBuf.append(fldname);
			String typeInfo = CreateTblUtil.getTypeInfo(dbType, map.get("fldtype").toString(), map.get("fldlen").toString(), map.get("flddecimal").toString());
			newFldSqlBuf.append(typeInfo);
			//默认值
			String defaultInfo = CreateTblUtil.getDefaultInfo(dbType, (String) map.get("flddefault"));
			if (defaultInfo != null)
			{
				newFldSqlBuf.append(defaultInfo);
			}
			//not null
			String notNullInfo = CreateTblUtil.getNotNullInfo(dbType, (String) map.get("fldattr"));
			if (notNullInfo != null)
			{
				newFldSqlBuf.append(notNullInfo);
			}
			//唯一性
			String uniqueInfo = CreateTblUtil.getUniqueInfo(dbType, (String) map.get("fldattr"));
			if (uniqueInfo != null)
			{
				newFldSqlBuf.append(uniqueInfo);
			}
			String updateFldSql = CreateTblUtil.getUpdateColExpr(dbType, tblname, newFldSqlBuf);
			db.execute(updateFldSql);
			//TODO 主键更新，约定只有一个主键，多主键通过唯一性进行约束，先删除旧主键然后添加新主键
			//更新定义表
			Map<String,Object> updateParams = new HashMap<>();
			updateParams.put("tblname", tblname);
			updateParams.put("fldname", fldname);
			for (String col : DatabaseConstant.flddef_editable_cols)
			{
				updateParams.put(col, map.get(col));
			}
			db.update2("dw_fld_def", updateParams);
		}
	}

	/**
	 * 删除列
	 * @param db
	 * @param dropFld_strSet
	 * @throws Exception
	 */
	public static void delCol(Database db, String dropFld_strSet) throws Exception
	{
		if (dropFld_strSet.equals(""))
		{
			return;
		}
		String dropFld_strs[] = dropFld_strSet.split(",");
		for (String str : dropFld_strs)
		{
			String ary[] = str.split("@");
			String tblname = ary[0];
			String fldname = ary[1];
			Map<String,Object> params = new HashMap<>();
			params.put("tblname", tblname);
			params.put("fldname", fldname);
			//删除列要先清空该字段的值，属于危险操作
			String cleanColSql = "update " + tblname + " set " + fldname + "=null";
			db.execute(cleanColSql);
			//删除列
			String dropColSql = "alter table " + tblname + " drop column " + fldname;
			db.execute(dropColSql);
			//更新定义表
			String updateFlddefSql = "delete from dw_fld_def where tblname=:tblname and fldname=:fldname";
			db.update1(updateFlddefSql, params);
		}
	}

	/**
	 * 添加约定字段
	 * @param defInfos
	 * @param tblName
	 * @param fldId
	 * @param fldName
	 * @param fldNameZh
	 * @param fldType
	 * @param fldLen
	 * @param defaultVal
	 */
	private static void addDefaultFld(List<Map<String,String>> defInfos, String tblName, String fldId, String fldName, String fldNameZh, String fldType, String fldLen, String defaultVal)
	{
		Map<String,String> _del_fldInfo = new HashMap<>();
		_del_fldInfo.put("tblname", tblName);
		_del_fldInfo.put("fldid", fldId);
		_del_fldInfo.put("fldname", fldName);
		_del_fldInfo.put("fldname_zh", fldNameZh);
		_del_fldInfo.put("fldtype", fldType);
		_del_fldInfo.put("fldlen", fldLen);
		_del_fldInfo.put("flddecimal", "0");
		_del_fldInfo.put("fldattr", "000000");
		_del_fldInfo.put("flddefault", defaultVal);
		defInfos.add(_del_fldInfo);
	}
}
