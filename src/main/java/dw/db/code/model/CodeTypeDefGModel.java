package dw.db.code.model;

import dw.common.util.num.IntUtil;
import dw.common.util.str.StrUtil;

import java.util.Map;

public class CodeTypeDefGModel
{
	String	ruletype;
	int		length;
	String	initval;
	int		incstep;

	public CodeTypeDefGModel(Map<String,Object> dataMap)
	{
		this.ruletype = StrUtil.objToString(dataMap.get("ruletype"));
		this.length = IntUtil.objToInt(dataMap.get("length"));
		this.initval = StrUtil.objToString(dataMap.get("initval"));
		this.incstep = IntUtil.objToInt(dataMap.get("incstep"));
	}

	public String getRuletype()
	{
		return ruletype;
	}

	public int getLength()
	{
		return length;
	}

	public String getInitval()
	{
		return initval;
	}

	public int getIncstep()
	{
		return incstep;
	}
}
