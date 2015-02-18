# Jython source file
from jython import EmployeeType
from Musica import NotnyStan

class Employee(EmployeeType): # implements EmployeTeype
	def __init__(self):
		self.first = "Josh"
		self.last  = "Juneau"
		self.id = "myempid"
	
	def getEmployeeFirst(self):
		return self.first
	
	def getEmployeeLast(self):
		return self.last
	
	def getEmployeeId(self):
		return self.id
	
	def saveKlsnAsJson(self, stan):
		':type stan: NotnyStan'
		data = NotnyStan.getExternalRepresentation()
		
		
		return u'Сохранил ёпта'
		