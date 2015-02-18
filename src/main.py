#!/usr/bin/jython
# -*- coding: utf-8 -*-

# from GraphTmp import GraphMusica
from java.lang import System
from java.io import *

OS_NAME = System.getProperty
try:
	if (OS_NAME == "Windows XP" or OS_NAME == "Windows 7"): 
		out = PrintStream(System.out, True, "Cp866")
		System.setOut(out)
except Exception:
	print("Убейся головой о стену")

# app = GraphMusica()
# app.setVisible(True)
	