/*

<This Java Class is part of the jMusic API version 1.5, March 2004.>:48  2001

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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**************************************************************
The Event interface is the public interface for ALL MIDI event
classes.
@author Andrew Sorensen
***************************************************************/

public interface Event
{
	/**Retrieve an events time*/
	int getTime();
	
	/**Set an events time*/
	void setTime(int time);

	/**Retrieve an events id*/
	short getID();

	/**Makes a copy of an event*/
	Event copy() throws CloneNotSupportedException;

	/**Print this events data in a System.out.println format*/
	void print();
	
	/**write out event data to disk*/
	int write(DataOutputStream dos) throws IOException;

	/**read in event data from disk*/
	int read(DataInputStream dis) throws IOException;

	/** @debug */
	default String strMe() // Застрокуй братуху, застрокуй! 
	{
		return getClass().getSimpleName() + " " + getTime();
	}
}
