package lambda;

import java.util.Arrays;

public class LambdaTest
{
	public static void main(String args[])
	{
		Object[] s = {1,"12222",null};
		Class<?>[] parameterTypes = Arrays.stream(s).map(arg -> {
			return arg == null ? Object.class : arg.getClass();
		}).toArray(Class<?>[]::new);
		System.out.println(parameterTypes);
	}
}
