package schemecalc;

public class Util
{
	private static int errorCount = 0;
	
	public static void resetErrorNum() { errorCount = 0; }
	
	public static boolean occuredError() { return errorCount != 0; }
	public static int getErrorNum() { return errorCount; }
	
	public static void OnError(String str) { OnError(str, ""); }
	public static void OnError(String str1, String str2)
	{
		System.out.print("#Error ");
		if (str2 == null || str2.length() == 0)
			System.out.print(str1);
		else
			System.out.print(String.format("%s(%s)", str1, str2));
		errorCount++;
	}
}
