/*

<This Java Class is part of the jMusic API version 1.5, March 2004.>:50  2001

Copyright (C) 2000 Andrew Sorensen & Andrew Brown

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or any
later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

package org.shmidusic.stuff.midi.standard_midi_file.event;

import org.shmidusic.stuff.midi.standard_midi_file.MidiUtil;
import org.shmidusic.stuff.tools.INote;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/***********************************************************
Description:
The NoteOff event is one of a set of events whose parent 
class is VoiceEvt.  In total these classes cover all voice
event types found in most MIDI file formats.
These classes will usually be added to a linked list as type
VoiceEvt.
(see class VoiceEvt for more information)	
@author Andrew Sorensen
************************************************************/

public final class NoteOff implements VoiceEvt, Cloneable, INoteEvent {
    private final short id = 004;
	private short pitch;
	final private short velocity = 0;
	private short midiChannel;
	private int time;
	
	/**A public constractor used to create default (empty) note off 
	   events.*/
	public NoteOff(){
		this.pitch = 0;
		this.midiChannel = 0;
		this.time = 0;
	}

	/**A public constructor used to create note off events containing
	 pitch, velocity, MIDI channel and time information.*/
	@Deprecated
	public NoteOff(short pitch, short velocity, short midiChannel, int time){
		this(pitch, midiChannel, time);
	}

	public NoteOff(INote note, int time)
	{
		this(note.getTune().shortValue(), note.getChannel().shortValue(), time);
	}

	private NoteOff(short pitch, short midiChannel, int time)
	{
		this.pitch = pitch;
		this.midiChannel = midiChannel;
		this.time = time;
	}

	//-------------------------------------
	//Pitch
	/**Returns a note off events pitch value*/
	public short getPitch(){
		return pitch;
	}
	/**Sets a note off events pitch value*/
	public void setPitch(short pitch){
		this.pitch = pitch;
	}
	//--------------------------------------
	//Velocity
	/**Returns a note off events velocity value*/
	public short getVelocity(){
		return velocity;
	}
	/**Sets a note off events velocity value*/
	@Deprecated
	public void setVelocity(short velocity){
		if (velocity != 0) {
			System.out.println("You should not use this, NoteOff with velocity not equal to 0 is bad and does not work");
			Runtime.getRuntime().exit(-100);
		}
	}
	//---------------------------------------
	//MIDI Channel
	public short getMidiChannel(){
		return midiChannel;
	}
	public void setMidiChannel(short midiChannel){
		this.midiChannel = midiChannel;
	}
	//---------------------------------------
	//Time
	public int getTime(){
		return time;
	}
	public void setTime(int time){
		this.time = time;
	}
	//----------------------------------------
	//Return ID
	public short getID(){
		return id;
	}
	//-----------------------------------------
	//Copy Object
	public Event copy() throws CloneNotSupportedException{	
		NoteOff event;
		try{
			event = (NoteOff) this.clone();
		}catch(CloneNotSupportedException e){
			System.out.println(e);
			event = new NoteOff();
		}
		return event;
	}
	//------------------------------------------
	// Write data out to disk
	public int write(DataOutputStream dos) throws IOException{
		// thanks for not commenting it, it was so obvious that this event was used only for reading,
		// but to write note off we use NoteOn event!
		int bytes_out = MidiUtil.writeVarLength(this.time, dos);
		dos.writeByte((byte) (0x80 + midiChannel));
		dos.writeByte((byte) pitch);
		dos.writeByte((byte) velocity);
		return bytes_out+3;
	}

	//------------------------------------------
	// Read data in from disk
	public int read(DataInputStream dis) throws IOException{
		this.pitch = (short)dis.readUnsignedByte();
		dis.readUnsignedByte(); // velocity, that should always be 0 =P
		return 2;
	}

	//------------------------------------------
	//Print
	public void print(){
		System.out.println("Note Off(004): [time = " + time + "][midiChannel = " + midiChannel + "][pitch = " + pitch + "][velocity = " + velocity + "]");
	}
}
