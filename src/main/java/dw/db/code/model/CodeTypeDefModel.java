package dw.db.code.model;

import dw.db.code.constant.CodeRuleConstant;
import dw.db.util.DBCacheUtil;
import dw.common.util.str.StrUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodeTypeDefModel
{
	String					codecode;
	String					tblname;
	String					fldname;
	List<CodeTypeDefGModel>	codeTypeDefGModels	= new ArrayList<>();

	public CodeTypeDefModel(Map<String,Object> eveMap, String codeCode)
	{
		if (StrUtil.isStrTrimNull(codeCode))
		{
			throw new RuntimeException("编码编号为空！");
		}
		String sql = "select * from codetypedef where codecode='" + codeCode + "'";
		List<Map<String,Object>> dataMapList = DBCacheUtil.getDBCacheBySql(eveMap, "codetypedef", sql);
		if (dataMapList == null || dataMapList.size() == 0)
		{
			throw new RuntimeException("未找到编号为" + codeCode + "的编码格式！");
		}
		Map<String,Object> dataMap = dataMapList.get(0);
		this.codecode = codeCode;
		this.tblname = StrUtil.objToString(dataMap.get("tblname"));
		this.fldname = StrUtil.objToString(dataMap.get("fldname"));
		//获取规则内容
		sql = "select * from codetypedefg where codecode='" + codeCode + "' order by ordno";
		dataMapList = DBCacheUtil.getDBCacheBySql(eveMap, "codetypedefg", sql);
		if (dataMapList == null || dataMapList.size() == 0)
		{
			throw new RuntimeException("未找到编号为" + codeCode + "的编码对应的规则！");
		}
		for (int i = 0; i < dataMapList.size(); i++)
		{
			dataMap = dataMapList.get(i);
			this.codeTypeDefGModels.add(new CodeTypeDefGModel(dataMap));
		}
	}

	public String getCodecode()
	{
		return codecode;
	}

	public String getTblname()
	{
		return tblname;
	}

	public String getFldname()
	{
		return fldname;
	}

	public List<CodeTypeDefGModel> getCodeTypeDefGModels()
	{
		return codeTypeDefGModels;
	}

	public int getCodeIncStep()
	{
		for (int i = 0; i < this.codeTypeDefGModels.size(); i++)
		{
			CodeTypeDefGModel gModel = codeTypeDefGModels.get(i);
			if (gModel.getRuletype().equals(CodeRuleConstant.INCNUM))
			{
				int incStep = gModel.getIncstep();
				if (incStep > 0)
				{
					return incStep;
				} else
				{
					return 0;
				}
			}
		}
		return 1;
	}
}
