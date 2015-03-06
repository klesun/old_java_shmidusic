![Alt text](/midiana_for_git.png?raw=true "Optional Title")

You wanna "java Main.class" to run the program.<br />
<br />
Program will most probably say something like "midi input device not detected blabla, type a number" you should type 1 and press enter.<br />
<br />
To create music you'll have to have some midi-piano plugged into your midi-port (if you have one). It may not work, i didn't tested with other device than my piano, but i don't care.<br />
<br />
//////////////////<br />
commands:<br />
<br />
// MAIN OPERATIONS<br />
ctrl-o    Open a .klsn-extension file. Some of them you can find at https://drive.google.com/folderview?id=0B_PiTxsew2JrZlg1eWlzdVlPTUE&usp=sharing<br />
<br />
ctrl-s    Save to .klsn file<br />
ctrl-p    Play/Stop music<br />
ctrl-0    Disable/Enable midi-input<br />
ctrl-(+ or =)   scale +<br />
ctrl-(- or _)   scale -<br />
delete    delete WHOLE accord<br />
ctrl-z    revieve last deleted accord<br />
ctrl-y    delete back last revieved accord<br />
<br />
// SELECTED NOTE OPERATIONS<br />
shift (if pointing a note)  Select next note in current accord (for following operations)<br />
NUM_PLUS  increase length of selected note (if not selected - of whole accord)<br />
NUM_MINUS  decrease length of selected note (-||-)<br />
[0..9] (when note selected)  mark note with the digit (color will change and you will be able to mute it)<br />
alt-[0..9]    mute notes with the digit<br />
<br />
// PARAMETERS (instument, volume, tact-size, tempo)<br />
shift (if pointing the numbers near Violin Key)   select next parameter to change<br />
NUM_PLUS/NUM_MINUS    increase decrease selected parameter value <br />
<br />
// PIANO<br />
press-any-key         will insert it after pointed note<br />
press-multiple-keys   will insert an accord agter pointed note<br />
press-the-very-left-Do-bekar    will insert muted note (like pause). It's very helpful, when you need to play next accord, before current ends<br />
<br />
//================================================================<br />
<br />
*If you (for some reason) want to use my program, please contact me at arturklesun@gmail.com, i'll give you love and support. I will be very glad if you contact me, 'cause "Баги сами себя не выпилят"<br />
