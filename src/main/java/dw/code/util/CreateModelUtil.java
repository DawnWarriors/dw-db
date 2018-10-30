package dw.code.util;

import dw.common.util.str.StrUtil;
import dw.db.trans.Database;
import dw.db.trans.DatabaseManager;
import dw.db.sql.SqlFilterUtil;
import dw.db.trans.TransactionManager;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CreateModelUtil
{
	private final String               lineSeparator  = System.getProperty("line.separator");
	private final String               baseDir        = System.getProperty("user.dir") + "/src/main/java/";
	private       Map<String,String[]> modelClassInfo = new HashMap<>();

	/**
	 * 创建Model java文件
	 * @param modelDirInfo Model包路径配置信息
	 * @return Model类信息
	 */
	public Map<String,String[]> work(Map<String,List<String>> modelDirInfo)
	{
		if (modelDirInfo == null || modelDirInfo.size() == 0)
		{
			return null;
		}
		Set<String> packageStrSet = modelDirInfo.keySet();
		for (String packageStr : packageStrSet)
		{
			List<String> tblNameList = modelDirInfo.get(packageStr);
			if (tblNameList != null && tblNameList.size() > 0)
			{
				createModelJavaFile(packageStr, tblNameList);
			}
		}
		return modelClassInfo;
	}

	private void checkDirExist(String dir)
	{
		File file = new File(dir);
		if (!file.exists() || !file.isDirectory())
		{
			file.mkdirs();
		}
	}

	private void createModelJavaFile(String packageStr, List<String> tblNameList)
	{
		String packageDir = baseDir + packageStr.replaceAll("\\.", "/");
		checkDirExist(packageDir);
		Database db = TransactionManager.getCurrentDBSession();
		Map<String,Object> paramGetter = new HashMap<>();
		String filter = SqlFilterUtil.getInFilter("tblname", tblNameList, paramGetter);
		String sql = "select tblid,tblname,tblname_zh from dw_tbl_def where " + filter;
		List<Map<String,Object>> tblInfos = db.queryListMap(sql, paramGetter);
		for (Map<String,Object> tblInfo : tblInfos)
		{
			String tblId = (String) tblInfo.get("tblid");
			String tblName = (String) tblInfo.get("tblname");
			String tblNameZh = (String) tblInfo.get("tblname_zh");
			String fldSql =
					"select fldname,fldname_zh,fldtype,flddecimal,fldattr from dw_fld_def where tblid='" + tblId + "' and tblname='" + tblName + "' and fldname not like '\\_%' order by fldid+0";
			String className = getClassNameByTblName(tblName);
			List<Map<String,Object>> fldInfos = db.queryListMap(fldSql);
			List<String> importInfoList = new ArrayList<>();
			List<String> classInfoList = new ArrayList<>();
			List<String> fldInfoList = new ArrayList<>();
			getImportInfo(importInfoList);
			getClassInfo(tblName, className, tblNameZh, classInfoList);
			getAllFldInfo(fldInfos, importInfoList, fldInfoList);
			fileWriteIn(className, packageDir, packageStr, importInfoList, classInfoList, fldInfoList);
			//记录modelClassInfo
			String classInfo[] = new String[] { packageStr + "." + className, className + ".class",className};
			modelClassInfo.put(tblName, classInfo);
		}
	}

	private void getImportInfo(List<String> importInfoList)
	{
		importInfoList.add("import dw.db.annotation.DwDbFld;");
		importInfoList.add("import dw.db.annotation.DwDbTbl;");
		importInfoList.add("import dw.db.base.ModelBase;");
		importInfoList.add("import lombok.Data;");
	}

	private void getClassInfo(String tblName, String className, String tblNameZh, List<String> classInfoList)
	{
		classInfoList.add("//createDate:" + (new Date()).toString());
		classInfoList.add("//数据库表:" + tblNameZh);
		classInfoList.add("@Data");
		classInfoList.add("@DwDbTbl(tblName = \"" + tblName + "\")");
		classInfoList.add("public class " + className + " extends ModelBase");
	}

	private void getAllFldInfo(List<Map<String,Object>> fldInfos, List<String> importInfoList, List<String> fldInfoList)
	{
		for (Map<String,Object> fldInfo : fldInfos)
		{
			String fldName = (String) fldInfo.get("fldname");
			String fldNameZh = (String) fldInfo.get("fldname_zh");
			String fldType = (String) fldInfo.get("fldtype");
			int flddecimal = (int) fldInfo.get("flddecimal");
			String fldattr = (String) fldInfo.get("fldattr");
			String type = getFldType(fldType, flddecimal, importInfoList);
			getFldInfo(fldName, fldNameZh, type, fldattr, fldInfoList);
		}
	}

	private String getFldType(String fldType, int flddecimal, List<String> importInfoList)
	{
		String imp = "";
		switch (fldType)
		{
		case "number":
			if (flddecimal > 0)
			{
				return "Double";
			} else
			{
				return "int";
			}
		case "date":
			imp = "import java.util.Date;";
			if (!importInfoList.contains(imp))
			{
				importInfoList.add(imp);
			}
			return "Date";
		case "varchar":
			return "String";
		case "decimal":
			imp = "import java.math.BigDecimal;";
			if (!importInfoList.contains(imp))
			{
				importInfoList.add(imp);
			}
			return "BigDecimal";
		case "bigint":
			return "long";
		default:
			return fldType;
		}
	}

	private void getFldInfo(String fldName, String fldNameZh, String type, String fldAttr, List<String> fldInfoList)
	{
		fldInfoList.add("\t@DwDbFld" + (fldAttr.endsWith("11") ? "(isKey = true)" : "") + lineSeparator);
		fldInfoList.add("\tprivate " + type + "\t" + fldName + ";\t\t//" + fldNameZh + lineSeparator);
	}

	private void fileWriteIn(String className, String packageDir, String packageStr, List<String> importInfoList, List<String> classInfoList, List<String> fldInfoList)
	{
		File file = new File(packageDir, className + ".java");
		if (!file.exists())
		{
			try
			{
				file.createNewFile();
				FileWriter fileWriter = new FileWriter(file);
				//package
				fileWriter.write("package " + packageStr + ";");
				fileWriter.write(lineSeparator);
				fileWriter.write(lineSeparator);
				//import
				for (String line : importInfoList)
				{
					fileWriter.write(line);
					fileWriter.write(lineSeparator);
				}
				fileWriter.write(lineSeparator);
				//class
				for (String line : classInfoList)
				{
					fileWriter.write(line);
					fileWriter.write(lineSeparator);
				}
				fileWriter.write("{" + lineSeparator);
				//fld
				for (String line : fldInfoList)
				{
					fileWriter.write(line);
				}
				fileWriter.write("}");
				fileWriter.close();
			} catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		} else
		{
			return;
		}
	}

	private String getClassNameByTblName(String tblName)
	{
		String className = StrUtil.underline2Camel(tblName, false);
		className = className.substring(0, 1).toUpperCase() + className.substring(1);
		return className + "Model";
	}
}
