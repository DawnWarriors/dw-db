package localThread;

public class LocalThreadTest
{
	ThreadLocal<Integer> integerThreadLocal = new ThreadLocal<>();

	public Integer getValue()
	{
		return integerThreadLocal.get();
	}

	public static void main(String s[])
	{
		System.out.println(new LocalThreadTest().getValue());
	}
}
