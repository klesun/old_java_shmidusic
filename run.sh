/usr/java/jdk1.8.0_40/bin/javac -sourcepath "./src" -d "./bin" "./src/Main/Main.java" -cp libs/*.jar
cd bin
/usr/java/jdk1.8.0_40/bin/java -cp ../libs/guava-18.0.jar:. Main.Main

