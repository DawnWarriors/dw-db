package dw.db.base;

import dw.common.util.map.MapUtil;
import dw.db.Database;
import dw.db.model.DBModelInfo;
import dw.db.trans.TransactionManager;
import dw.db.util.DBModelUtil;
import net.sf.json.JSONObject;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class ModelBase
{
	private   String __tblName;
	protected Date   _update_date;    //更新时间
	protected Date   _create_date;    //新建时间
	private int _delete_flag = 0;
	private String id;
	public  String token;        //token:控制新增和编辑动作
	/**
	 * 用于存放原始值
	 */
	private final Map<String,Object> _oldDataMap_ = new HashMap<>();

	public Map<String,Object> get_oldDataMap_()
	{
		return _oldDataMap_;
	}

	public void _setOldValue(String fldName, Object value)
	{
		_oldDataMap_.put(fldName, value);
	}

	/**
	 * 用于model自己设置原始值时使用
	 *
	 * @param dataMap
	 */
	protected void _set_oldDataMap_(Map<String,Object> dataMap)
	{
		MapUtil.mergeMap(_oldDataMap_, dataMap);
	}

	public ModelBase()
	{
		DBModelInfo info = DBModelUtil.getDBModelInfo(this.getClass());
		this.__tblName = info.getTblName();
	}

	/**
	 * 数据插入
	 */
	public void insert()
	{
		Date date = new Date();
		this.set_create_date(date);
		this.set_update_date(date);
		Database db = new TransactionManager().getCurrentDatabase();
		DBModelUtil.insert(db, this);
	}

	/**
	 * 数据更新
	 *
	 * @param forbidUpdateFields
	 */
	public void update(String[] forbidUpdateFields)
	{
		Date date = new Date();
		Database db = new TransactionManager().getCurrentDatabase();
		this.set_update_date(date);
		Map<String,Object> dataMap = db.queryMap("select * from " + __tblName + " where id='" + this.getId() + "'");
		//不提供更新的列，如果没有值，不会进行操作，如果有值，直接覆盖原值
		List<String> forbidFields = new ArrayList<>();
		//底层标识，不提供更新
		Set<String> keySet = dataMap.keySet();
		for (String key : keySet)
		{
			if (key.startsWith("_") && !key.equals("_update_date"))
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
		DBModelUtil.update(db, this);
	}

	/**
	 * 强制更新
	 */
	public void update_force()
	{
		Date date = new Date();
		Database db = new TransactionManager().getCurrentDatabase();
		this.set_update_date(date);
		Map<String,Object> dataMap = db.queryMap("select * from " + __tblName + " where id='" + this.getId() + "'");
		this._set_oldDataMap_(dataMap);
		DBModelUtil.update(db, this);
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
		Database db = new TransactionManager().getCurrentDatabase();
		this.set_update_date(new Date());
		this.set_delete_flag(1);
		DBModelUtil.update(db, this);
	}

	/**
	 * 物理删除，慎用！！！
	 */
	public void delete()
	{
		Database db = new TransactionManager().getCurrentDatabase();
		Map<String,Object> params = new HashMap<>();
		params.put("id", getId());
		db.delete(__tblName, params);
	}

	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * 获取主键值，所有子类继承实现
	 *
	 * @return
	 */
	protected String getId()
	{
		return null;
	}

	public void setToken(String token)
	{
		this.token = token;
	}

	public String getToken()
	{
		return this.token;
	}

	public void set_create_date(Date date)
	{
		this._create_date = date;
	}

	public Date get_create_date()
	{
		return _create_date;
	}

	public void set_update_date(Date date)
	{
		this._update_date = date;
	}

	public Date get_update_date()
	{
		return _update_date;
	}

	public String __getTableName()
	{
		return __tblName;
	}

	public void set_delete_flag(int flag)
	{
		this._delete_flag = flag;
	}

	public int get_delete_flag()
	{
		return this._delete_flag;
	}

	protected String getMODEL_ID()
	{
		return "";
	}

	/**
	 * 给指定属性设值
	 *
	 * @param fieldName
	 * @param fieldValue
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
	 * @param from
	 * @param <T>
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
	 * @return
	 */
	public JSONObject toJson()
	{
		return JSONObject.fromObject(this);
	}
}
