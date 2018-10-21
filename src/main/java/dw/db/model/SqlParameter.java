package dw.db.model;

import java.util.List;
import java.util.Map;

public class SqlParameter
{
	String				sql			= "";
	List<String>		paramNames	= null;
	Map<String,Object>	params		= null;

	public SqlParameter(String sql, List<String> paramNames, Map<String,Object> params)
	{
		this.sql = sql;
		this.paramNames = paramNames;
		this.params = params;
	}

	public String getSql()
	{
		return sql;
	}

	public void setSql(String sql)
	{
		this.sql = sql;
	}

	public List<String> getParamNames()
	{
		return paramNames;
	}

	public void setParamNames(List<String> paramNames)
	{
		this.paramNames = paramNames;
	}

	public Map<String,Object> getParams()
	{
		return params;
	}

	public void setParams(Map<String,Object> params)
	{
		this.params = params;
	}
}
