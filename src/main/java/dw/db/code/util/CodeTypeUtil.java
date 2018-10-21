package dw.db.code.util;

import dw.db.code.constant.CodeRuleConstant;
import dw.db.code.model.CodeTypeDefGModel;
import dw.db.code.model.CodeTypeDefModel;
import dw.common.util.str.StrUtil;

import java.util.List;

public class CodeTypeUtil
{
	/**
	 * 根据编码格式获取对应的编码格式字符串
	 * @param codeTypeDefModel
	 * @return
	 */
	public static String getCodeExprByCodeTypeRule(CodeTypeDefModel codeTypeDefModel)
	{
		List<CodeTypeDefGModel> rules = codeTypeDefModel.getCodeTypeDefGModels();
		String expr = "";
		for (int i = 0; i < rules.size(); i++)
		{
			CodeTypeDefGModel rule = rules.get(i);
			String ruleType = rule.getRuletype();
			switch (ruleType)
			{
			case CodeRuleConstant.STATICTEXT:
				expr += rule.getInitval();
				break;
			case CodeRuleConstant.YEAR:
				if (rule.getLength() == 2)
				{
					expr += "${CURYEAR2}";
				} else
				{
					expr += "${CURYEAR}";
				}
				break;
			case CodeRuleConstant.MONTH:
				expr += "${CURMONTH}";
				break;
			case CodeRuleConstant.DAY:
				expr += "${CURDAY}";
				break;
			case CodeRuleConstant.HOUR:
				expr += "${CURHOUR}";
				break;
			case CodeRuleConstant.MINUTE:
				expr += "${CURMINUTE}";
				break;
			case CodeRuleConstant.SECOND:
				expr += "${CURSECOND}";
				break;
			case CodeRuleConstant.MILLISECOND:
				expr += "${CURMILLISECOND}";
				break;
			case CodeRuleConstant.INCNUM:
				expr += StrUtil.createStr(rule.getLength(), '_');
				break;
			default:
				break;
			}
		}
		return expr;
	}
}
