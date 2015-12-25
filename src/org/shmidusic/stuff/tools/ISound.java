package org.shmidusic.stuff.tools;

// a note without length

public interface ISound extends Comparable<ISound>
{
	Integer getTune();
	Integer getChannel();

	default int compareTo(ISound n) {
		return ((n.getTune() - this.getTune()) << 4) + (n.getChannel() - this.getChannel());
	}
}
