import GraphTmp.GraphMusica;
import Tools.out;
import java.io.*;

public class Main {
    public static void main(String[] args){
    	String OS_NAME = System.getProperty("os.name");
    	
    	try {
    		System.out.println(OS_NAME);
	    	if (OS_NAME.equals("Windows XP") || OS_NAME.equals("Windows 7")) { 
	    		PrintStream out = new PrintStream(System.out, true, "Cp866");
	    		System.setOut(out);
	    	}
	        System.out.println("Убейся головой об стену");
    	} catch (Exception e) {
    		System.out.println("blablablabalall");
    	}
    	    	
        GraphMusica app = new GraphMusica(); 
        app.setVisible(true);              
    }
}
