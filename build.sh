#javac -sourcepath "./src" -d "./bin" "./src/main/Main.java" -cp "libs/commons-math3-3.5.jar:libs/guava-18.0.jar"
#cp -R src/Gui/imgs bin/Gui/

function join { local IFS="$1"; shift; echo "$*"; }

libs_query=$(join : libs/*)

mkdir -p bin

javac -sourcepath "./src" -d "./bin" "./src/main/Main.java" -cp $libs_query
cp -R src/Gui/imgs bin/Gui/
