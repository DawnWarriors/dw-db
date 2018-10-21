package dw.db.code.util;

import dw.db.Database;
import dw.db.code.model.CodeTypeDefModel;
import dw.common.util.macro.MacroUtil;
import dw.common.util.str.StrUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO 缓存KEY发生变化之后，需要清除缓存
 * @author xins_cyf
 */
public class MakeCodeUtil
{
    /**
     * 根据编码定义生成编码
     * @param eveMap
     * @param codeCode
     * @return
     */
    public static String getCodeByCodeTypeDef(Map<String,Object> eveMap, String codeCode)
    {
        CodeTypeDefModel codeTypeDefModel = new CodeTypeDefModel(eveMap, codeCode);
        String tblName = codeTypeDefModel.getTblname();
        String fldName = codeTypeDefModel.getFldname();
        int incStep = codeTypeDefModel.getCodeIncStep();
        String codeExpr = CodeTypeUtil.getCodeExprByCodeTypeRule(codeTypeDefModel);
        return getCode(eveMap, tblName, fldName, codeExpr, incStep);
    }

    /**
     * 根据编码定义生成编码
     * @param db
     * @param codeCode
     * @return
     */
    public static String getCodeByCodeTypeDef(Database db, String codeCode)
    {
        Map<String,Object> eveMap = new HashMap<>();
        eveMap.put("DB", db);
        return getCodeByCodeTypeDef(eveMap, codeCode);
    }

    /**
     * 一次性生成多个编码
     * @param eveMap
     * @param tableName
     * @param colName
     * @param expr
     * @param count
     * @param step 递增步长
     * @return
     */
    public static String[] getCodes(Map<String,Object> eveMap, String tableName, String colName, String expr, int count, int step)
    {
        String cacheKey = getCodeCacheKey(eveMap, tableName, colName, expr);
        //年月日替换
        expr = MacroUtil.macroReplace(expr);
        //TODO 缓存解决方案,使用符合通用标准的Redis框架
        //FmCache fmCache = FmCacheManager.getCache(FmCache.fmbase);
        //String oldCode = fmCache.getString(cacheKey);
        String oldCode ="";
        //要处理ID格式发生变化的情况，所以要拿oldID和IDlike进行匹配，如果不匹配，也是要重新生成新缓存
        if (oldCode == null || oldCode.length() == 0 || !isCodeMatch(oldCode, expr))
        {
            String sql = "select max(" + colName + ") from " + tableName + " where " + colName + " like '" + expr + "'";
            Database db = (Database) eveMap.get("DB");
            Object obj = db.queryObject(sql);
            if (obj == null)
            {
                oldCode = expr.replaceAll("_", "0");
            } else
            {
                oldCode = (String) obj;
            }
        }
        int beginIndex = expr.indexOf("_");
        int endIndex = expr.lastIndexOf("_");
        String newCodes[] = StrUtil.numStrIncrease(oldCode, beginIndex, endIndex + 1, count, step);
        //TODO
        //fmCache.putString(cacheKey, newCodes[count - 1]);
        return newCodes;
    }

    /**
     * 一次性生成多个编码
     * @param db
     * @param tableName
     * @param colName
     * @param expr
     * @param count
     * @param step
     * @return
     */
    public static String[] getCodes(Database db, String tableName, String colName, String expr, int count, int step)
    {
        Map<String,Object> eveMap = new HashMap<>();
        eveMap.put("DB", db);
        return getCodes(eveMap, tableName, colName, expr, count, step);
    }

    /**
     * 一次性生成多个编码，递增步长默认为1
     * @param eveMap
     * @param tableName
     * @param colName
     * @param expr
     * @param count
     * @return
     */
    public static String[] getCodes(Map<String,Object> eveMap, String tableName, String colName, String expr, int count)
    {
        return getCodes(eveMap, tableName, colName, expr, count, 1);
    }

    /**
     * 一次性生成多个编码，递增步长默认为1
     * @param db
     * @param tableName
     * @param colName
     * @param expr
     * @param count
     * @return
     */
    public static String[] getCodes(Database db, String tableName, String colName, String expr, int count)
    {
        Map<String,Object> eveMap = new HashMap<>();
        eveMap.put("DB", db);
        return getCodes(eveMap, tableName, colName, expr, count, 1);
    }
    /**
     * 生成一个编码，递增步长默认为1
     * @param eveMap
     * @param tableName
     * @param colName
     * @param expr
     * @return
     */
    public static String getCode(Map<String,Object> eveMap, String tableName, String colName, String expr)
    {
        String codes[] = getCodes(eveMap, tableName, colName, expr, 1);
        return codes[0];
    }

    /**
     * 生成一个编码，递增步长默认为1
     * @param db
     * @param tableName
     * @param colName
     * @param expr
     * @return
     */
    public static String getCode(Database db, String tableName, String colName, String expr)
    {
        Map<String,Object> eveMap = new HashMap<>();
        eveMap.put("DB", db);
        String codes[] = getCodes(eveMap, tableName, colName, expr, 1);
        return codes[0];
    }

    /**
     * 生成一个编码
     * @param eveMap
     * @param tableName
     * @param colName
     * @param expr
     * @param step 递增步长
     * @return
     */
    public static String getCode(Map<String,Object> eveMap, String tableName, String colName, String expr, int step)
    {
        String codes[] = getCodes(eveMap, tableName, colName, expr, 1, step);
        return codes[0];
    }

    /**
     * 生成一个编码
     * @param db
     * @param tableName
     * @param colName
     * @param expr
     * @param step
     * @return
     */
    public static String getCode(Database db, String tableName, String colName, String expr, int step)
    {
        Map<String,Object> eveMap = new HashMap<>();
        eveMap.put("DB", db);
        String codes[] = getCodes(eveMap, tableName, colName, expr, 1, step);
        return codes[0];
    }

    private static boolean isCodeMatch(String oldCode, String codeExpr)
    {
        if (oldCode == null || oldCode.length() != codeExpr.length())
        {
            return false;
        }
        codeExpr = codeExpr.replaceAll("_", "\\\\d");
        return oldCode.matches(codeExpr);
    }

    /**
     * 获取对应的CODE缓存key
     * @param eveMap
     * @param tableName
     * @return
     */
    private static String getCodeCacheKey(Map<String,Object> eveMap, String tableName, String colName, String expr)
    {
        //TODO 多系统共用缓存服务器的解决方案
        //String sysName = Config.getSysConfig("SYSNAME");
        //String sysCode = Config.getSysConfig("SYSCODE");
        String sysName = "";
        String sysCode = "";
        return "TBLCODE:" + sysName + ":" + sysCode + ":" + tableName + ":" + colName + ":" + expr + ":";
    }
}
