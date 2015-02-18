package jython;

import Musica.NotnyStan;

public interface EmployeeType {
	public String getEmployeeFirst();
	public String getEmployeeLast();
	public String getEmployeeId();
	public String saveKlsnAsJson(NotnyStan stan);
    
}