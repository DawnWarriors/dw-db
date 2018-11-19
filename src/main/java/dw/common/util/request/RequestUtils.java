package dw.common.util.request;

import dw.common.util.list.ListUtil;
import dw.common.util.model.ModelUtil;
import dw.common.util.str.StrUtil;

import javax.servlet.http.HttpServletRequest;
import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RequestUtils
{
	/**
	 * 通过attribute设值
	 *
	 * @param obj Bean对象
	 * @param request HttpServletRequest
	 * @param <T> 泛型
	 */
	public static <T> void getModelFromRequest(T obj, HttpServletRequest request) {
		getModelFromRequest(obj, request, 0);
	}

	/**
	 * 通过parameter设值
	 *
	 * @param obj Bean对象
	 * @param request HttpServletRequest
	 * @param <T> 泛型
	 */
	public static <T> void getModelFromRequestParameter(T obj, HttpServletRequest request) {
		getModelFromRequest(obj, request, 1);
	}

	/**
	 * 设置model属性值
	 *
	 * @param obj Bean对象
	 * @param request HttpServletRequest
	 * @param option  0：attribute     1：parameter
	 * @param <T> 泛型
	 */
	private static <T> void getModelFromRequest(T obj, HttpServletRequest request, int option) {
		try {
			Field[] fields = obj.getClass().getDeclaredFields();
			List<Field> fieldsList = new ArrayList<>(Arrays.asList(fields));
			Field[] superFields = obj.getClass().getSuperclass().getDeclaredFields();
			List<Field> superFieldList = new ArrayList<>(Arrays.asList(superFields));
			List<Field> allFields = new ArrayList<>();
			ListUtil.mergeList(allFields, fieldsList);
			ListUtil.mergeList(allFields, superFieldList);
			for (Field field : allFields) {
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
					continue;
				}
				String fldName = field.getName();
				if (fldName.startsWith("_")) {
					continue;
				}
				Object objValue = null;
				if (option == 0) {
					objValue = request.getAttribute(fldName);
				} else if (option == 1) {
					objValue = request.getParameter(fldName);
				}
				String value = StrUtil.objToString(objValue, null);
				ModelUtil.setValue(obj, fldName, field, value);
			}
		} catch (IllegalAccessException | IntrospectionException | IllegalArgumentException | InvocationTargetException | ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
