![Alt text](/midiana_for_git.png?raw=true "Optional Title")

To build and run you can do: <br />
```sh
/usr/java/jdk1.8*/bin/javac -sourcepath "./src" -d "./bin" "./src/Main.Main.java"
cd bin
/usr/java/jdk1.8.*/bin/java Main.Main
```
(or just ./run)<br />
<br />
List of available midi devices will be printed into console.<br />
<br />
To create music you'll have to have some midi-piano plugged into your midi-port (if you have one). You can contact me, i'll be glad to help you.<br />
<br />

| shortcut | action |
 -------- | ------ |
| // GLOBAL OPERATIONS | . |
| ctrl-o | Open a .klsn-extension file. Some of them you can find at https://drive.google.com/folderview?id=0B_PiTxsew2JrZlg1eWlzdVlPTUE&usp=sharing |
| ctrl-s | Save to .klsn file |
| ctrl-p | Play/Stop music |
| ctrl-0 | Disable/Enable midi-input |
| ctrl-(+ or =) | scale + |
| ctrl-(- or _) | scale - |
| delete | delete WHOLE accord |
| ctrl-z | revieve last deleted accord |
| ctrl-y | delete back last revieved accord |
| . | . |
| // FOCUSED NOTE OPERATIONS | . |
| shift (if pointing a note) | Select next note in current accord (for following operations) |
| NUM_PLUS | increase length of selected note (if not selected - of whole accord) |
| NUM_MINUS | decrease length of selected note (-||-) |
| [0..9] (when note selected) | mark note with the digit (color will change and you will be able to mute it) |
| alt-[0..9] | mute notes with the digit |
| . | . |
| // PARAMETERS (instument, volume, tact-size, tempo) | . |
| shift (if pointing the numbers near Violin Key) | select next parameter to change |
| NUM_PLUS/NUM_MINUS | increase decrease selected parameter value |
| . | . |
| // PIANO | . |
| press-any-key | will insert it after pointed note |
| press-multiple-keys | will insert an accord agter pointed note |
| press-the-very-left-Do-bekar | will insert muted note (like pause). It's very helpful, when you need to play next accord, before current ends |
| . | . |
| . | . |
*If you (for some reason) want to use my program, please contact me at arturklesun@gmail.com, i'll give you love and support. I feel lonely being the only person using this program =( <br />
