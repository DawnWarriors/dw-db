package dw.db.util;

import dw.db.annotation.DwDbFld;
import dw.db.annotation.DwDbTbl;
import dw.db.model.DBAnnotationModel;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DBAnnotationUtil
{
    /**
     * 获取DBModel的注解信息
     * @param cls
     * @return
     */
    public static DBAnnotationModel getAnnotationModel(Class<?> cls)
    {
        DBAnnotationModel annotationModel = new DBAnnotationModel();
        DwDbTbl fwDbTbl = cls.getAnnotation(DwDbTbl.class);
        annotationModel.setDwDbTbl(fwDbTbl);
        Field[] fileds = cls.getDeclaredFields();
        for (Field field : fileds)
        {
            String fldName = field.getName();
            DwDbFld fwDbFld = field.getAnnotation(DwDbFld.class);
            if (fwDbFld != null)
            {
                annotationModel.addDwDbFld(fldName, fwDbFld);
            }
        }
        return annotationModel;
    }

    /**
     * 根据注解信息，获取DBModel配置的表名
     * @param annotationModel
     * @return
     */
    public static String getTblName(DBAnnotationModel annotationModel)
    {
        DwDbTbl fwDbTbl = annotationModel.getDwDbTbl();
        return fwDbTbl.tblName();
    }

    /**
     * 根据注解信息，获取DBModel配置的属性名和字段名的映射Map
     * @param annotationModel
     * @return
     */
    public static Map<String,String> getFldNames(DBAnnotationModel annotationModel)
    {
        Map<String,String> fldNameMap = new HashMap<>();
        DwDbTbl fwDbTbl = annotationModel.getDwDbTbl();
        Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
        Set<String> keys = fwDbFldMap.keySet();
        for (String key : keys)
        {
            DwDbFld fwDbFld = fwDbFldMap.get(key);
            //未配置子表类路径
            if ("".equals(fwDbFld.childTblClsPath()) && fwDbFld.isFld())
            {
                String fldName = fwDbFld.fldName();
                if ("".equals(fldName))
                {
                    fldName = key;
                    //如果设置了转换小写
                    if (fwDbTbl.isAllFldLowerCase() || fwDbFld.isLowerCase())
                    {
                        fldName = fldName.toLowerCase();
                    }
                }
                fldNameMap.put(key, fldName);
            }
        }
        fldNameMap.put("_update_date", "_update_date");
        fldNameMap.put("_create_date", "_create_date");
        fldNameMap.put("_delete_flag", "_delete_flag");
        return fldNameMap;
    }

    /**
     * 根据注解信息获取子表类路径信息
     * @param annotationModel
     * @return
     */
    public static Map<String,String> getChildFldClsInfo(DBAnnotationModel annotationModel)
    {
        Map<String,String> fldNameCls = new HashMap<>();
        Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
        Set<String> keys = fwDbFldMap.keySet();
        for (String key : keys)
        {
            DwDbFld fwDbFld = fwDbFldMap.get(key);
            //配置子表类路径，且配置了子表加载过滤条件
            if (!"".equals(fwDbFld.childTblClsPath()) && !"".equals(fwDbFld.childTblLoadFilter()))
            {
                String clsPath = fwDbFld.childTblClsPath();
                String loadFilter = fwDbFld.childTblLoadFilter();
                fldNameCls.put(key, clsPath);
                fldNameCls.put(key + "$$FILTER", loadFilter);
            }
        }
        return fldNameCls;
    }

    /**
     * 根据注解信息，获取配置的表的主键
     * @param annotationModel
     * @return
     */
    public static Map<String,String> getKeyMap(DBAnnotationModel annotationModel)
    {
        Map<String,String> keyMap = new HashMap<>();
        Map<String,DwDbFld> fwDbFldMap = annotationModel.getDwDbFlds();
        Set<String> keys = fwDbFldMap.keySet();
        for (String key : keys)
        {
            DwDbFld fwDbFld = fwDbFldMap.get(key);
            //未配置子表类路径
            if (fwDbFld.isKey())
            {
                String fldName = fwDbFld.fldName();
                if ("".equals(fldName))
                {
                    fldName = key;
                }
                keyMap.put(key, fldName);
            }
        }
        return keyMap;
    }
}
