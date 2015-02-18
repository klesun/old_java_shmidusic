package jython;

import org.python.util.PythonInterpreter;

import Musica.NotnyStan;

public class JythonFactory {
	public static Object getJythonObject(String interfaceName, String pathToJythonModule){
   
		Object javaInt = null;
		PythonInterpreter interpreter = new PythonInterpreter();
		interpreter.execfile(pathToJythonModule);
		String tempName = pathToJythonModule.substring(pathToJythonModule.lastIndexOf("/")+1);
		tempName = tempName.substring(0, tempName.indexOf("."));
		System.out.println(tempName);
		String instanceName = tempName.toLowerCase();
		String javaClassName = tempName.substring(0,1).toUpperCase() + tempName.substring(1);
		String objectDef = "=" + javaClassName + "()";
		interpreter.exec(instanceName + objectDef);
		try {
			Class JavaInterface = Class.forName(interfaceName);
			javaInt = interpreter.get(instanceName).__tojava__(JavaInterface);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();  // Add logging here
		}
	
		return javaInt;
	}

	public static void saveKlsnAsJson(NotnyStan stan)
	{
//		EmployeeType jythonObject = (EmployeeType) JythonFactory.getJythonObject(
//				"jython.EmployeeType", "src/jython/Employee.py");
//		jythonObject.saveKlsnAsJson(stan);
	}
}