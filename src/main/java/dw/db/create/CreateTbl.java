package dw.db.create;

import com.alibaba.druid.util.JdbcConstants;
import dw.common.util.list.ListUtil;
import dw.common.util.map.MapUtil;
import dw.common.util.str.StrUtil;
import dw.db.trans.DatabaseConstant;
import dw.db.sql.SqlFilterUtil;
import dw.db.trans.Database;
import dw.db.trans.TransactionManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.*;

/**
 * 规定一般顺序限制为：include，define，tbldef_temp, flddef_temp, refdef, indexdef
 * include顺序限制：先引入一般表结构定义，再引入define文件
 * 顺序错误可能会导致define失效，flddef_temp找不到table.id
 * 通过新表旧表的对比，决定是新增还是删除还是更新（建删表，加删改字段、约束和索引）
 * 更新：先找出修改的所有列，然后再确定是那张表的那个字段做了哪些修改
 * 1、清空新表数据（通过脚本生成先清空新表，为了直接修改表信息也能作用到数据库，一切修改都是修改新表）
 * 2、存储表结构信息至新表
 * 3、根据新旧差异操作数据库（留出接口可直接从这步开始，为了能直接修改表信息）
 * 4、清空旧表数据，将新表数据复制一份到旧表
 *
 * @author xins_cyf
 */
public class CreateTbl
{
	private       String tags[]        = { "#flddef", "#indexdef", "#refdef", "#tbldef", "#define", "#include" };
	//与tags对应
	private       String tables_temp[] = { "dw_fld_def_temp", "dw_index_def_temp", "dw_ref_def_temp", "dw_tbl_def_temp" };
	//数据库脚本入口文件
	private final String entranceFile  = "dw_tbl_base.txt";

	/**
	 * 数据库创建和更新入口
	 *
	 * @throws Exception 可能文件出错或者表操作出错，请根据控制台报错信息进行判断
	 */
	public void work() throws Exception
	{
		Map<String,String> defineMap = new HashMap<>();
		Map<String,List<Map<String,String>>> defs = new HashMap<>();
		String pathPre = CreateTbl.class.getClassLoader().getResource("").toURI().getPath() + "/dw/";
		//约定总入口只有一个
		getDBFileTree(entranceFile, defineMap, defs, pathPre);
		Database db = TransactionManager.getCurrentDBSession();
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
		for (int i = 0 ; i < 4 ; i++)
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
		updateTable(db, true);
	}

	/**
	 * 获取一个文件所有引入的文件集合中所有的def信息
	 *
	 * @param fileName  字段名
	 * @param defineMap 定义信息
	 * @param defs      定义集合
	 * @param pathPre   路径前缀
	 */
	private void getDBFileTree(String fileName, Map<String,String> defineMap, Map<String,List<Map<String,String>>> defs, String pathPre)
	{
		File file = new File(pathPre + fileName);
		if (!file.exists())
		{
			throw new RuntimeException(fileName+":文件未找到");
		}
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
	 * 当前文件中的定义优先级高于引入文件的定义；
	 * 后引入的文件优先级高于先引入的文件；
	 * 约定：所有的define都在指定的define文件里
	 *
	 * @param fileName 文件名
	 * @param file     文件内容
	 * @return 引用文件信息
	 */
	private List<String> getIncludeForOneFile(String fileName, String file)
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
	 *
	 * @param fileName 文件名
	 * @param file     文件内容
	 * @return 宏定义信息
	 */
	private Map<String,String> getDefineForOneFile(String fileName, String file)
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
	 *
	 * @param filenames 文件名
	 * @param file      文件内容
	 * @return 全部定义信息
	 */
	private Map<String,List<Map<String,String>>> getTagDefs(String filenames, String file)
	{
		Map<String,List<Map<String,String>>> confInfos = new HashMap<>();
		for (int i = 0 ; i < 4 ; i++)
		{
			confInfos.put(tags[i], getTagDef(tags[i], filenames, file));
		}
		return confInfos;
	}

	/**
	 * 单个文件单个tag
	 *
	 * @param startTag  起始标记
	 * @param filenames 文件名
	 * @param file      文件内容
	 * @return 单个标记信息
	 */
	private List<Map<String,String>> getTagDef(String startTag, String filenames, String file)
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
				for (int i = 0, len = definfo.length ; i < len ; i++)
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
	 *
	 * @param startTag 起始标记
	 * @param line     一行内容
	 * @return 是否是一个标签的定义结束
	 */
	private boolean isTagEnd(String startTag, String line)
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
	 *
	 * @param tag  标记
	 * @param line 一行内容
	 * @return 定义数据
	 */
	private String[] getDefInfo(String tag, String line)
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
	 *
	 * @param fileName  文件名
	 * @param lineIndex 行号索引
	 */
	private void throwError(String fileName, int lineIndex)
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
	 *
	 * @param db          数据库Session对象
	 * @param isCleanAble 是否可以清空字段值，因为清空属于危险操作，所以建议手动去执行SQL，留下这个参数做接口
	 */
	private void updateTable(Database db, boolean isCleanAble)
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
	 *
	 * @param db 数据库Session对象
	 */
	private void dropTable(Database db)
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
	 *
	 * @param db 数据库Session对象
	 */
	private void addTable(Database db)
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
				String typeInfo = getTypeInfo(dbType, (String) map.get("fldtype"), map.get("fldlen").toString(), map.get("flddecimal").toString());
				createTblSqlBuffer.append(typeInfo);
				//默认值
				String defaultInfo = getDefaultInfo(dbType, (String) map.get("flddefault"));
				if (defaultInfo != null)
				{
					createTblSqlBuffer.append(defaultInfo);
				}
				//not null
				String notNullInfo = getNotNullInfo(dbType, (String) map.get("fldattr"));
				if (notNullInfo != null)
				{
					createTblSqlBuffer.append(notNullInfo);
				}
				//唯一性
				String uniqueInfo = getUniqueInfo(dbType, (String) map.get("fldattr"));
				if (uniqueInfo != null)
				{
					createTblSqlBuffer.append(uniqueInfo);
				}
				//判断是否是主键
				boolean isPrimaryKey = isPrimaryKey((String) map.get("fldattr"));
				if (isPrimaryKey)
				{
					//TODO 约定只有单主键，多主键通过唯一性约束
					keyCols.add((String) map.get("fldname") + " ");
				}
				createTblSqlBuffer.append(",");
			}
			String primaryKeyExpr = getPrimaryKeyExpr(dbType, tblName, keyCols);
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
	 *
	 * @param db 数据库Session对象
	 */
	private void updateTbl(Database db)
	{
		String update_tbl_sql = getDiffColsSql("dw_tbl_def_temp", "dw_tbl_def", new String[] { "tblname" }, DatabaseConstant.tbldef_editable_cols);
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
	 *
	 * @param db            数据库Session对象
	 * @param addFld_strSet 添加字段字串
	 */
	private void addCol(Database db, String addFld_strSet)
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
			String typeInfo = getTypeInfo(dbType, (String) map.get("fldtype"), map.get("fldlen").toString(), map.get("flddecimal").toString());
			addColSqlBuf.append(typeInfo);
			//默认值
			String defaultInfo = getDefaultInfo(dbType, (String) map.get("flddefault"));
			if (defaultInfo != null)
			{
				addColSqlBuf.append(defaultInfo);
			}
			//not null
			String notNullInfo = getNotNullInfo(dbType, (String) map.get("fldattr"));
			if (notNullInfo != null)
			{
				addColSqlBuf.append(notNullInfo);
			}
			//唯一性
			String uniqueInfo = getUniqueInfo(dbType, (String) map.get("fldattr"));
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
	 *
	 * @param db 数据库Session对象
	 */
	private void updateCol(Database db)
	{
		//联合查询的结果集中，mysql的会将后面重复的列名后面拼上序号，从1开始，至于其他数据库是不是这种情况，未作考证
		String update_fld_sql = getDiffColsSql("dw_fld_def_temp", "dw_fld_def", new String[] { "tblname", "fldname" }, DatabaseConstant.flddef_editable_cols);
		List<Map<String,Object>> updateColMaps = db.queryListMap(update_fld_sql);
		String dbType = db.getDbType();
		for (Map<String,Object> map : updateColMaps)
		{
			String tblname = (String) map.get("tblname");
			String fldname = (String) map.get("fldname");
			//更新后的列属性
			StringBuffer newFldSqlBuf = new StringBuffer();
			newFldSqlBuf.append(fldname);
			String typeInfo = getTypeInfo(dbType, map.get("fldtype").toString(), map.get("fldlen").toString(), map.get("flddecimal").toString());
			newFldSqlBuf.append(typeInfo);
			//默认值
			String defaultInfo = getDefaultInfo(dbType, (String) map.get("flddefault"));
			if (defaultInfo != null)
			{
				newFldSqlBuf.append(defaultInfo);
			}
			//not null
			String notNullInfo = getNotNullInfo(dbType, (String) map.get("fldattr"));
			if (notNullInfo != null)
			{
				newFldSqlBuf.append(notNullInfo);
			}
			//唯一性
			String uniqueInfo = getUniqueInfo(dbType, (String) map.get("fldattr"));
			if (uniqueInfo != null)
			{
				newFldSqlBuf.append(uniqueInfo);
			}
			String updateFldSql = getUpdateColExpr(dbType, tblname, newFldSqlBuf);
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
	 *
	 * @param db             数据库Session对象
	 * @param dropFld_strSet 删除列字段字串
	 */
	private void delCol(Database db, String dropFld_strSet)
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
	 *
	 * @param defInfos   定义信息集合
	 * @param tblName    表名
	 * @param fldId      字段ID
	 * @param fldName    字段名
	 * @param fldNameZh  字段中文名
	 * @param fldType    字段类型
	 * @param fldLen     字段长度
	 * @param defaultVal 默认字段值
	 */
	private void addDefaultFld(List<Map<String,String>> defInfos, String tblName, String fldId, String fldName, String fldNameZh, String fldType, String fldLen, String defaultVal)
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

	/**
	 * 由于不同数据库所支持的数据类型不同，比如小数类型在不同数据库建表时类型关键字就不一样
	 * 所以需要格局数据库类型做一个适配
	 * 最终返回对应的类型信息
	 *
	 * @param dbtype     数据库类型
	 * @param fldtype    字段类型
	 * @param fldlen     字段长度
	 * @param flddecimal 小数位长度
	 * @return 浮点型数据创建SQL
	 */
	private String getTypeInfo(String dbtype, String fldtype, String fldlen, String flddecimal)
	{
		if (fldtype.equals("number"))
		{
			if (JdbcConstants.ORACLE.equals(dbtype))
			{
				return " " + fldtype + "(" + fldlen + "," + flddecimal + ")";
			} else
			{
				return " float(" + fldlen + "," + flddecimal + ")";
			}
		} else if (fldtype.equals("date"))
		{
			return " datetime ";
		} else if (fldtype.equals("varchar"))
		{
			if ("0".equals(fldlen))//最大长度
			{
				if (JdbcConstants.MYSQL.equals(dbtype))
				{
					return " text ";
				} else
				{
					return " " + fldtype + "(max)";
				}
			}
		}
		if (!"0".equals(flddecimal))
		{
			return " " + fldtype + "(" + fldlen + "," + flddecimal + ")";
		} else
		{
			return " " + fldtype + "(" + fldlen + ")";
		}
	}

	/**
	 * 主要用于处理特殊值，比如null和0
	 *
	 * @param dbtype     数据库类型
	 * @param defaultVal 默认值
	 * @return 默认值
	 */
	private String getDefaultInfo(String dbtype, String defaultVal)
	{
		if (defaultVal == null || defaultVal.equals("") || defaultVal.toLowerCase().equals("null"))
		{
			return "";
		}
		if (dbtype.equals(JdbcConstants.MYSQL))
		{
			return " default " + defaultVal + "";
		}
		return " default(" + defaultVal + ")";
	}

	/**
	 * 根据列限制属性，判断是否非空
	 *
	 * @param dbtype  数据库类型
	 * @param fldattr 字段属性
	 * @return 是否非空
	 */
	private String getNotNullInfo(String dbtype, String fldattr)
	{
		if (fldattr.charAt(5) == '1')
		{
			return " not null";
		}
		return null;
	}

	/**
	 * 根据列限制属性，判断是否是主键
	 *
	 * @param fldattr 字段属性
	 * @return 是否是主键
	 */
	private boolean isPrimaryKey(String fldattr)
	{
		if (fldattr.charAt(4) == '1')
		{
			return true;
		}
		return false;
	}

	/**
	 * 根据列限制属性，判断是否唯一
	 *
	 * @param dbtype  字段类型
	 * @param fldattr 字段属性
	 * @return 是否唯一
	 */
	private String getUniqueInfo(String dbtype, String fldattr)
	{
		if (fldattr.charAt(3) == '1')
		{
			return " unique";
		}
		return null;
	}

	/**
	 * 获取设置主键语句
	 *
	 * @param dbtype  数据库类型
	 * @param tblName 表名
	 * @param keyCols 主键字段
	 * @return 设置主键语句
	 */
	private String getPrimaryKeyExpr(String dbtype, String tblName, List<String> keyCols)
	{
		if (keyCols == null || keyCols.size() == 0)
		{
			throw new RuntimeException("表" + tblName + "未设置主键!");
		}
		String expr = "primary key(";
		for (int i = 0 ; i < keyCols.size() ; i++)
		{
			if (i > 0)
			{
				expr += ",";
			}
			expr += keyCols.get(i);
		}
		expr += ")";
		return expr;
	}

	/**
	 * 更新表语句
	 *
	 * @param dbType       数据库类型
	 * @param tblname      表名
	 * @param newFldSqlBuf 新SQL字符串
	 * @return 更新表语句
	 */
	private String getUpdateColExpr(String dbType, String tblname, StringBuffer newFldSqlBuf)
	{
		StringBuffer updateFldSqlBuf = new StringBuffer();
		updateFldSqlBuf.append("alter table " + tblname);
		if (JdbcConstants.MYSQL.equals(dbType) || JdbcConstants.ORACLE.equals(dbType))
		{
			updateFldSqlBuf.append(" modify ");
			updateFldSqlBuf.append(newFldSqlBuf);
		} else if (JdbcConstants.MYSQL.equals(dbType))
		{
			updateFldSqlBuf.append(" alter column ");
			updateFldSqlBuf.append(newFldSqlBuf);
		}
		return updateFldSqlBuf.toString();
	}

	/**
	 * 获取查询两个表的cols这些列值不同的记录的sql
	 * :联合查询tbl1和tbl2,联合字段unionCols,查询两个表的cols这些列值不同的记录
	 * :前提是unioncols和cols这些字段在两个表中均存在
	 *
	 * @param tbl1      实际表
	 * @param tbl2      临时表
	 * @param unionCols 联合查询列
	 * @param cols      列
	 * @return 对比SQL
	 */
	private String getDiffColsSql(String tbl1, String tbl2, String unionCols[], String cols[])
	{
		StringBuffer sqlBuf = new StringBuffer("select ");
		for (String s : unionCols)
		{
			sqlBuf.append(tbl1 + "." + s + " as " + s + ",");
		}
		for (int i = 0 ; i < cols.length ; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(",");
			}
			//tbl1和tbl2的数据都查出来了，为的是能够根据结果集判断修改了哪一个列属性
			sqlBuf.append(tbl1 + "." + cols[i]);
			sqlBuf.append(",");
			sqlBuf.append(tbl2 + "." + cols[i]);
		}
		sqlBuf.append(" from ");
		sqlBuf.append(tbl1 + " join " + tbl2);
		sqlBuf.append(" on ");
		for (int i = 0 ; i < unionCols.length ; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(" and ");
			}
			sqlBuf.append(tbl1 + "." + unionCols[i] + "=" + tbl2 + "." + unionCols[i]);
		}
		sqlBuf.append(" where ");
		for (int i = 0 ; i < cols.length ; i++)
		{
			if (i > 0)
			{
				sqlBuf.append(" or ");
			}
			sqlBuf.append(tbl1 + "." + cols[i] + " <> " + tbl2 + "." + cols[i]);
		}
		return sqlBuf.toString();
	}
}
