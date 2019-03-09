package dw.db.base;

import dw.common.util.map.MapUtil;
import dw.common.util.str.StrUtil;
import dw.db.trans.Database;
import dw.db.trans.TransactionManager;
import dw.db.util.DBIDUtil;
import lombok.Data;
import net.sf.json.JSONObject;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Data
public abstract class ModelBase
{
	private   String __tblName;
	protected Date   __update_date;    //更新时间
	protected Date   __create_date;    //新建时间
	private int __delete_flag = 0;
	private String id;
	public  String __token;        //token:控制新增和编辑动作
	private final Map<String,Object> __oldDataMap_ = new HashMap<>(); //持久对象原始值

	/**
	 * 用于model自己设置原始值时使用
	 *
	 * @param dataMap 数据集
	 */
	protected void _set_oldDataMap_(Map<String,Object> dataMap)
	{
		MapUtil.mergeMap(__oldDataMap_, dataMap);
	}

	public ModelBase()
	{
		this.__tblName = ObjectProxyFactory.getTblName(this.getClass());
	}

	/**
	 * 执行数据表插入
	 * 如果没有设置主键值，则默认会以UUID生成的ID作为数据主键
	 *
	 * @return 数据主键
	 */
	public String insert()
	{
		Date date = new Date();
		this.set__create_date(date);
		this.set__update_date(date);
		if (StrUtil.isStrTrimNull(this.getId()))
		{
			this.setId(DBIDUtil.createUUId());
		}
		Database db = TransactionManager.getCurrentDBSession();
		ObjectProxyFactory.insert(db, this);
		return this.getId();
	}

	/**
	 * 数据更新
	 *
	 * @param forbidUpdateFields 本次更新不参与更新的数据列列名数组
	 */
	public void update(String[] forbidUpdateFields)
	{
		Date date = new Date();
		Database db = TransactionManager.getCurrentDBSession();
		this.set__update_date(date);
		Map<String,Object> dataMap = db.queryMap("select * from " + __tblName + " where id='" + this.getId() + "'");
		//不提供更新的列，如果没有值，不会进行操作，如果有值，直接覆盖原值
		List<String> forbidFields = new ArrayList<>();
		//底层标识，不提供更新
		Set<String> keySet = dataMap.keySet();
		for (String key : keySet)
		{
			if (key.startsWith("_") && !key.equals("__update_date"))
			{
				forbidFields.add(key);
			}
		}
		//避免null值更新----用于解决前端没有传值会覆盖原值的问题
		Map<String,Object> newDataMap = MapUtil.objectToMap(this);
		Set<String> newFieldKeys = newDataMap.keySet();
		for (String newField : newFieldKeys)
		{
			if (newDataMap.get(newField) == null)
			{
				dataMap.put(newField, null);
			}
		}
		//避免更新
		if (forbidUpdateFields != null && forbidUpdateFields.length > 0)
		{
			for (String field : forbidUpdateFields)
			{
				//forbidFields.add(field);
				if (keySet.contains(field) && !forbidFields.contains(field))
				{
					dataMap.put(field, null);
				}
			}
		}
		//处理更新字段集合
		for (String key : forbidFields)
		{
			dataMap.remove(key);
		}
		this._set_oldDataMap_(dataMap);
		ObjectProxyFactory.update(db, this);
	}

	/**
	 * 强制更新
	 */
	public void update_force()
	{
		Date date = new Date();
		Database db = TransactionManager.getCurrentDBSession();
		this.set__update_date(date);
		Map<String,Object> dataMap = db.queryMap("select * from " + __tblName + " where id='" + this.getId() + "'");
		this._set_oldDataMap_(dataMap);
		ObjectProxyFactory.update(db, this);
	}

	/**
	 * 数据更新
	 */
	public void update()
	{
		update(null);
	}

	/**
	 * 逻辑删除
	 * ---不会真正物理删除，仅设置删除标志为1
	 */
	public void delete_logic()
	{
		Database db = TransactionManager.getCurrentDBSession();
		this.set__update_date(new Date());
		this.set__delete_flag(1);
		ObjectProxyFactory.update(db, this);
	}

	/**
	 * 物理删除，慎用！！！
	 */
	public void delete()
	{
		Database db = TransactionManager.getCurrentDBSession();
		Map<String,Object> params = new HashMap<>();
		params.put("id", getId());
		db.delete(__tblName, params);
	}

	/**
	 * 给指定属性设值
	 *
	 * @param fieldName 属性名
	 * @param fieldValue 属性值
	 */
	public void setValue(String fieldName, Object fieldValue)
	{
		Class<?> cls = this.getClass();
		try
		{
			PropertyDescriptor pd = new PropertyDescriptor(fieldName, cls);
			Method method = pd.getWriteMethod();
			method.invoke(this, fieldValue);
		} catch (IntrospectionException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 值copy
	 *
	 * @param from 值来源对象
	 * @param <T> 任意类型对象
	 */
	public <T> void copyValue(T from)
	{
		if (from == null)
		{
			return;
		}
		Map<String,Object> fromDataMap = MapUtil.objectToMap(from);
		Map<String,Object> toDataMap = MapUtil.objectToMap(this);
		Set<String> keys = toDataMap.keySet();
		for (String key : keys)
		{
			if (key.startsWith("_"))
			{
				continue;
			}
			Object value = fromDataMap.get(key);
			if (value == null)
			{
				continue;
			}
			this.setValue(key, value);
		}
	}

	/**
	 * 转换成JSON对象
	 *
	 * @return JSON对象
	 */
	public JSONObject toJson()
	{
		return JSONObject.fromObject(this);
	}

	/**
	 * 获取持久对象在被修改之前的值
	 *
	 * @return 持久对象从数据库查出来时对应的field, value组成的Map对象
	 */
	public Map<String,Object> get_oldDataMap_()
	{
		return __oldDataMap_;
	}

	/**
	 * 修改持久化对象旧值
	 *
	 * @param fldName 字段名
	 * @param value   字段值
	 */
	public void _setOldValue(String fldName, Object value)
	{
		__oldDataMap_.put(fldName, value);
	}
}
