javac -sourcepath ./src -d bin src/Main.java
cp src/main.py bin/
cd bin
jython main.py
cd ..
