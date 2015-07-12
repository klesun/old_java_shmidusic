function join { local IFS="$1"; shift; echo "$*"; }

libs_query=$(join : libs/*)

mkdir -p bin

javac -sourcepath "./src" -d "./bin" "./src/main/Main.java" -cp $libs_query
cp -R src/Gui/imgs bin/Gui/
