package dw.db.create;

import dw.common.util.str.StrUtil;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class CreateDao
{
	private final String lineSeparator = System.getProperty("line.separator");
	private final String baseDir       = System.getProperty("user.dir") + "/src/main/java/";

	public void work(Map<String,List<String>> daoDirInfo, Map<String,String[]> modelClassInfo)
	{
		if (daoDirInfo == null || daoDirInfo.size() == 0)
		{
			return;
		}
		Set<String> packageStrSet = daoDirInfo.keySet();
		for (String packageStr : packageStrSet)
		{
			List<String> tblNameList = daoDirInfo.get(packageStr);
			if (tblNameList != null && tblNameList.size() > 0)
			{
				createDaoJavaFile(packageStr, tblNameList, modelClassInfo);
			}
		}
	}

	private void createDaoJavaFile(String packageStr, List<String> tblNameList, Map<String,String[]> modelClassInfo)
	{
		String packageDir = baseDir + packageStr.replaceAll("\\.", "/");
		checkDirExist(packageDir);
		for (String tblName : tblNameList)
		{
			String modelClassAry[] = modelClassInfo.get(tblName);
			String className = getClassNameByTblName(tblName);
			List<String> importInfoList = new ArrayList<>();
			List<String> classInfoList = new ArrayList<>();
			List<String> methodInfoList = new ArrayList<>();
			getImportInfo(importInfoList, modelClassAry[0]);
			getClassInfo(className, classInfoList);
			getMethodInfo(className, tblName, modelClassAry[1], methodInfoList);
			fileWriteIn(tblName, packageDir, packageStr, importInfoList, classInfoList, methodInfoList);
		}
	}

	private void getImportInfo(List<String> importInfoList, String modelClassPath)
	{
		importInfoList.add("import dw.db.annotation.DwDao;");
		importInfoList.add("import dw.db.base.DaoBase;");
		importInfoList.add("import org.springframework.stereotype.Component;");
		importInfoList.add("import " + modelClassPath + ";");
	}

	private void getClassInfo(String className, List<String> classInfoList)
	{
		classInfoList.add("//createDate:" + (new Date()).toString());
		classInfoList.add("@DwDao");
		classInfoList.add("@Component");
		classInfoList.add("public class " + className + " extends DaoBase");
	}

	private void getMethodInfo(String className, String tblName, String modelClass, List<String> methodInfoList)
	{
		methodInfoList.add("public " + className + "()");
		methodInfoList.add("{");
		methodInfoList.add("\tsuper(\"" + tblName + "\"," + modelClass + ");");
		methodInfoList.add("}");
	}

	private void fileWriteIn(String tblName, String packageDir, String packageStr, List<String> importInfoList, List<String> classInfoList, List<String> methodInfoList)
	{
		String className = getClassNameByTblName(tblName);
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
				//method
				for (String line : methodInfoList)
				{
					fileWriter.write("\t" + line);
					fileWriter.write(lineSeparator);
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
		return className + "Dao";
	}

	private void checkDirExist(String dir)
	{
		File file = new File(dir);
		if (!file.exists() || !file.isDirectory())
		{
			file.mkdirs();
		}
	}
}
