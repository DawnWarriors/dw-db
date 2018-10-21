package str;

import dw.common.util.str.StrUtil;

public class Underline2CamelTest
{
	public static void main(String args[])
	{
		String tblName = "sys_user";
		String className = StrUtil.underline2Camel(tblName, true);
		className = className.substring(0, 1).toUpperCase() + tblName.substring(1);
		System.out.println(className);
	}
}
