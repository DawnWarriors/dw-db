package dw.db.model;

import java.util.Map;
import java.util.Set;

public class DBModelInfo
{
	String				classPath;
	String				tblName;
	Map<String,String>	fldNameInfos;
	Map<String,String>	childTblInfos;
	Map<String,String>	keys;
	String				selectSql;
	Set<String>			fldNames;

	public String getClassPath()
	{
		return classPath;
	}

	public void setClassPath(String classPath)
	{
		this.classPath = classPath;
	}

	public String getTblName()
	{
		return tblName;
	}

	public void setTblName(String tblName)
	{
		this.tblName = tblName;
	}

	public Map<String,String> getFldNameInfos()
	{
		return fldNameInfos;
	}

	public void setFldNameInfos(Map<String,String> fldNameInfos)
	{
		this.fldNames = fldNameInfos.keySet();
		this.fldNameInfos = fldNameInfos;
	}

	public Map<String,String> getChildTblInfos()
	{
		return childTblInfos;
	}

	public void setChildTblInfos(Map<String,String> childTblInfos)
	{
		this.childTblInfos = childTblInfos;
	}

	public Map<String,String> getKeys()
	{
		return keys;
	}

	public void setKeys(Map<String,String> keys)
	{
		this.keys = keys;
	}

	public String getSelectSql()
	{
		return selectSql;
	}

	public void setSelectSql(String selectSql)
	{
		this.selectSql = selectSql;
	}

	public Set<String> getFldNames()
	{
		return fldNames;
	}
}
