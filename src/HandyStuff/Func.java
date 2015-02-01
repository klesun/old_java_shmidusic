package HandyStuff;

public class Func {
	public static void main() {
		
	}

	public static void print_r(Object[] arik) {System.out.println("Huj"+print_r(arik, 0));}
	public static String print_r(Object[] arik, int level) {
		String outp = "";
		for (Object obj: arik) {
			System.out.print(obj+"\n");
			outp += obj instanceof Object[] 
					? obj.getClass()+"\n"+ print_r( (Object[])obj, level+1 ) 
					: (new String(new char[level]).replace("\0", "   "))+obj+"\n";
		}
		return outp;
	}
}
