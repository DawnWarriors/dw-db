package dw.db.sql;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
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
}
