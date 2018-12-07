package dw.db.util;

import dw.common.util.str.StrUtil;
import dw.common.util.type.TypeUtil;
import dw.db.annotation.DwDbSub;
import dw.db.base.ModelBase;
import dw.db.constant.DwDbConstant;
import dw.db.constant.DwDbConstant.SubsetTypeInfo;
import dw.db.sql.SqlFilterUtil;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.WildcardTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class DBSubsetUtil
{
	//获取字段类型和ModelClass
	public static SubsetTypeInfo getSubsetTypeInfoByField(Field field)
	{
		try
		{
			SubsetTypeInfo subsetTypeInfo = new DwDbConstant.SubsetTypeInfo();
			Type fieldType = field.getGenericType();
			if (fieldType instanceof ParameterizedTypeImpl)
			{
				ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) fieldType;
				Type[] types = parameterizedType.getActualTypeArguments();
				if (types.length == 1 && "java.util.List".equals(parameterizedType.getRawType().getName()))
				{
					Type type = types[0];
					// type = ?
					if (type instanceof WildcardTypeImpl)
					{
						return null;
					}
					// type = List<Map<String,Object>>
					if (type instanceof ParameterizedTypeImpl)
					{
						if ("java.util.Map<java.lang.String, java.lang.Object>".equals(type.getTypeName()))
						{
							subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.LIST_MAP);
						} else
						{
							return null;
						}
					} else
					{
						//type = List<Model>
						Class modelCls = Class.forName(type.getTypeName());
						if (ModelBase.class.isAssignableFrom(modelCls))
						{
							subsetTypeInfo.setModelClass(modelCls);
							subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.DW_MODEL_LIST);
						} else
						{
							return null;
						}
					}
				} else if (types.length == 2 && "java.util.Map<java.lang.String, java.lang.Object>".equals(parameterizedType.getTypeName()))
				{
					subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.MAP);
				} else
				{
					return null;
				}
			} else
			{
				String fldTypeName = fieldType.getTypeName();
				//数组
				if (fldTypeName.endsWith("]"))
				{
					if (fldTypeName.equals("java.lang.Object[]"))
					{
						//type = Object[]
						subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.OBJECT_ARRAY);
					} else if (fldTypeName.equals("java.lang.Object[][]"))
					{
						//type = Object[][]
						subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.OBJECT_ARRAY_2);
					} else
					{
						return null;
					}
				} else if (TypeUtil.isBaseType(fldTypeName))
				{
					subsetTypeInfo.setType(field.getType());
					subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.OBJECT);
				}  else if (ModelBase.class.isAssignableFrom(Class.forName(fldTypeName)))
				{
					// type = model
					subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.DW_MODEL);
					subsetTypeInfo.setModelClass(Class.forName(fldTypeName));
				} else if ("java.lang.Object".equals(fldTypeName))
				{
					// type = Object
					subsetTypeInfo.setType(field.getType());
					subsetTypeInfo.setSubsetType(DwDbConstant.SubsetType.OBJECT);
				}else
				{
					return null;
				}
			}
			return subsetTypeInfo;
		} catch (ClassNotFoundException e)
		{
			return null;
		}
	}

	/**
	 * 根据dwDbSub获取加载数据所需信息
	 *
	 * @param dwDbSub 子集字段注解
	 * @return 子集加载条件
	 */
	public static DwDbConstant.SubsetSelectInfo getSubsetSelectInfoByDwDbSub(DwDbSub dwDbSub)
	{
		DwDbConstant.SubsetSelectInfo subsetSelectInfo = new DwDbConstant.SubsetSelectInfo();
		String loadSql = dwDbSub.loadSql();
		String loadFilter = dwDbSub.loadFilter();
		String finalFilter = loadFilter;
		boolean isNeedParams = false;
		if (StrUtil.isNotStrTrimNull(dwDbSub.unions()))
		{
			Map<String,String> unionFilterMap = new HashMap<>();
			String unionFilter = "";
			String kvs[] = dwDbSub.unions().split("&");
			for (String kv : kvs)
			{
				kv = kv.trim();
				if (!kv.contains(":"))
				{
					kv += ":id";
				}
				String kv_[] = kv.split(":");
				unionFilter = SqlFilterUtil.addFilter(unionFilter, "and", kv_[0].trim() + "=:" + kv_[1].trim());
				unionFilterMap.put(kv_[0], kv_[1]);
			}
			subsetSelectInfo.setUnionFilterMap(unionFilterMap);
			finalFilter = SqlFilterUtil.addFilter(finalFilter, "and", unionFilter);
			isNeedParams = true;
		}
		subsetSelectInfo.setSql(loadSql);
		subsetSelectInfo.setLoadFilter(loadFilter);
		subsetSelectInfo.setOrdBy(dwDbSub.ordBy());
		subsetSelectInfo.setNeedParams(isNeedParams);
		subsetSelectInfo.setFinalFilter(finalFilter);
		subsetSelectInfo.setLazy(dwDbSub.lazy());
		if (StrUtil.isNotStrTrimNull(loadSql))
		{
			String finalSql = loadSql;
			if (StrUtil.isNotStrTrimNull(finalFilter))
			{
				finalSql = finalSql + " where " + finalFilter;
			}
			subsetSelectInfo.setFinalSql(finalSql);
		}
		//filter:2 sql:4
		int selectType = 0;
		if (StrUtil.isNotStrTrimNull(loadSql))
		{
			selectType |= 4;
		}
		if (StrUtil.isNotStrTrimNull(finalFilter))
		{
			selectType |= 2;
		}
		subsetSelectInfo.setSelectType(selectType);
		return subsetSelectInfo;
	}
}
