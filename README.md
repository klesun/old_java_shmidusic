This repository has submodules - so please clone with "--recursive" argument!<br />
I love lambdas, therefore you have to have Java 8 or higher!<br />
<br />
![Alt text](/midiana_for_git.png?raw=true "Optional Title")
<br />
To build and run you can do: <br />
```sh
$ ./build.sh
$ ./run.sh
```
(or you can also find some jar in project root. it will be a bit outdated: )<br />
```sh
$ java -jar midiana.jar
```
<br />
List of available midi devices will be printed into console.<br />
<br />
To create music you'll have to have some midi-piano plugged into your midi-port or usb (not sure 'bout usb, did not test).<br />
<br />

| shortcut | action |
 -------- | ------ |
| // GLOBAL OPERATIONS | . |
| ctrl-o | Open a midi.json-extension file. Some of them you can find at https://drive.google.com/folderview?id=0B_PiTxsew2JrV3prNFR6QUdZQ2M&usp=sharing |
| ctrl-s | Save sheet music to midi.json file |
| ctrl-p | Play/Stop music |
| ctrl-0 | Disable midi-input |
| ctrl-9 | Enable midi-input |
| ctrl-(+ or =) | scale + |
| ctrl-(- or _) | scale - |
| Esc | configurations dialog (instruments and volumes for channels) |
| . | . |
| // FOCUSED ACCORD OPERATIONS | . |
| delete | delete WHOLE chord if Nota not selected |
| ctlr-UP/DOWN | Select next note in current chord (for following operations) |
| . | . |
| // FOCUSED NOTE OPERATIONS | . |
| "["/"]" | increase/decrease length of selected note (with ctrl - for whole chord) |
| [0..9] | mark note with the digit (color will change and you will be able to mute it) |
| delete | delete selected Nota |
| . | . |
| // PIANO | . |
| press-any-key | will insert it after focused chord (or into pointed chord if some his note is selected at the moment) |
| press-multiple-keys | will insert an chord after focused chord |
| press-the-very-left-Do-bekar | will insert muted note (like pause). It's very helpful, when you need to play next chord, before current ends |
| . | . |
| . | . |
*If you (for some reason) want to use my program, please contact me at arturklesun@gmail.com, i'll give you love and support. I feel lonely being the only person using this program =( <br />

<br />
![Alt text](/midiana.jpg?raw=true "Optional Title")
<br />
