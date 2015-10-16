javac -sourcepath "./src" -d "./bin" "./src/org/shmidusic/stuff/scripts/MidiToReadableMidi.java" -cp "libs/commons-math3-3.5.jar:libs/guava-18.0.jar"
cd bin 
java org.shmidusic.stuff.scripts.MidiToReadableMidi
cd .. 
