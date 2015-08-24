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

Shortcuts for interacting with program are listed in ["SheetMusic", "Staff", "Chord", "Note"] menu entries. The only absent shortcuts are:
<br />

| shortcut      | action        |
| ------------- |:-------------:|
| [0-9]      | Change focused Note channel if a Note is focused; move focus to Note with corresponding number elsewhere |
| alt-[a-z0-9] <b>OR</b> {a key on midi keyboard} | Change focused Note channel if a Note is focused; move focus to Note with corresponding number elsewhere |

<br />
Some music you can find at https://drive.google.com/folderview?id=0B_PiTxsew2JrV3prNFR6QUdZQ2M&usp=sharing

*If you (for some reason) want to use my program, please contact me at arturklesun@gmail.com, i'll give you love and support. I feel lonely being the only person using this program =( <br />
