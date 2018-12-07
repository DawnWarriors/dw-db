package lazyload;

import dw.db.base.ModelBase;
import dw.db.constant.DwDbConstant;
import dw.db.util.DBSubsetUtil;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class DwModelLazyLoadServiceTest
{
	public static void main1(String a[]) throws ClassNotFoundException
	{
		//DwModelLazyLoadService dwModelLazyLoadService = new DwModelLazyLoadService();
		Class<?> cls = S.class;
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields)
		{
			String fieldName = field.getName();
			Type type = field.getGenericType();
			if (type instanceof ParameterizedTypeImpl)
			{
				ParameterizedTypeImpl parameterizedType = (ParameterizedTypeImpl) type;
				Type[] types = parameterizedType.getActualTypeArguments();
				if (types.length > 0)
				{
					for (Type type1 : types)
					{
						System.out.print("111 " + fieldName + " " + type1 + " ----");
					}
					System.out.println("111 " + fieldName + " " + type.getTypeName() + " @@@ ");
				} else
				{
					System.out.println("222 " + fieldName + " " + type);
				}
			} else
			{
				System.out.println("333 " + fieldName + " " + type);
				System.out.println(type.getTypeName());
				System.out.println(type);
				if (type.getTypeName().contains(".") && !type.getTypeName().endsWith("]"))
				{
					boolean isImplementInterface = ModelBase.class.isAssignableFrom(Class.forName(type.getTypeName()));
					System.out.println(isImplementInterface);
				}
			}
		}
	}

	public static void main(String a[]) throws ClassNotFoundException
	{
		Class<?> cls = S.class;
		Field[] fields = cls.getDeclaredFields();
		for (Field field : fields)
		{
			DwDbConstant.SubsetTypeInfo subsetTypeInfo = DBSubsetUtil.getSubsetTypeInfoByField(field);
			System.out.print(field.getName() + ":::");
			if (subsetTypeInfo != null)
				System.out.print(subsetTypeInfo.getSubsetType());
			System.out.print("\n");
		}
	}
}
class S extends ModelBase
{
	List<OrderModel>         ss;
	String                   mm;
	int                      o;
	List<?>                  ll;
	List                     zz;
	Map<String,Object>       tt;
	OrderModel               orderModel;
	String[]                 aa;
	List<Map<String,Object>> lpp;
	OrderModel[]             orderModels;
	Object                   obj;
	Object[]                 objs;
	Object[][]               objss;
	double                   p;
}
class OrderModel
{
}