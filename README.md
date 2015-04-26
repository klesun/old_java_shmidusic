This repository has submodules - so please clone with "--recursive" argument!<br />
I love lambdas, therefore you have to have Java 8 or higher!<br />
<br />
![Alt text](/midiana_for_git.png?raw=true "Optional Title")
<br />
To build and run you can do: <br />
```sh
/usr/java/jdk1.8.*/bin/javac -sourcepath "./src" -d "./bin" "./src/Main.Main.java -cp libs/*.jar"
cd bin
/usr/java/jdk1.8.*/bin/java -cp ../libs/guava-18.0.jar:. Main.Main
```
(or just ./run.sh if you have same OS and Java as me)<br />
<br />
List of available midi devices will be printed into console.<br />
<br />
To create music you'll have to have some midi-piano plugged into your midi-port (if you have one).<br />
<br />

| shortcut | action |
 -------- | ------ |
| // GLOBAL OPERATIONS | . |
| ctrl-o | Open a .json-extension file. Some of them you can find at https://drive.google.com/folderview?id=0B_PiTxsew2JrV3prNFR6QUdZQ2M&usp=sharing |
| ctrl-s | Save to .json file |
| ctrl-p | Play/Stop music |
| ctrl-0 | Disable/Enable midi-input |
| ctrl-(+ or =) | scale + |
| ctrl-(- or _) | scale - |
| ctrl-z | ctrl-z |
| ctrl-y | ctrl-y |
| Esc | configurations dialog (instruments and volumes for channels) |
| . | . |
| // FOCUSED ACCORD OPERATIONS | . |
| delete | delete WHOLE accord if Nota not selected |
| ctlr-UP/DOWN | Select next note in current accord (for following operations) |
| . | . |
| // FOCUSED NOTE OPERATIONS | . |
| "["/"]" | increase/decrease length of selected note (with ctrl - for whole accord) |
| [0..9] | mark note with the digit (color will change and you will be able to mute it) |
| delete | delete selected Nota |
| . | . |
| // PIANO | . |
| press-any-key | will insert it after focused accord (or into pointed accord if some his note is selected at the moment) |
| press-multiple-keys | will insert an accord after focused accord |
| press-the-very-left-Do-bekar | will insert muted note (like pause). It's very helpful, when you need to play next accord, before current ends |
| . | . |
| . | . |
*If you (for some reason) want to use my program, please contact me at arturklesun@gmail.com, i'll give you love and support. I feel lonely being the only person using this program =( <br />
