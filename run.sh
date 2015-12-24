#cd bin
# linux
# java -cp "../libs/guava-18.0.jar:../libs/commons-math3-3.5.jar:." org.shmidusic.Main
# windows
# java -cp .;../libs/guava-18.0.jar;../libs/commons-math3-3.5.jar org.shmidusic.Main


function join { local IFS="$1"; shift; echo "$*"; }

cd bin

libs_query="$(join : ../libs/*):."

java -cp $libs_query org.shmidusic.Main

cd ..