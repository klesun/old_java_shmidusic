package org.klesun_model;

// this class stores history to undo/redo
// for now, i plan woodenly just store SheetMusic snapshot
// after each handled event (if it differs from previous snapshot)

import com.google.common.collect.EvictingQueue;
import org.json.JSONObject;
import org.shmidusic.sheet_music.SheetMusic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.stream.StreamSupport;

public class SnapshotStorage
{
	/** @TODO: storing snapshots is obviously not efficient (especially with the json string generation)
	 * hang change listeners to fields of Models and store diff instead of snapshots */
	final private Deque<JSONObject> snapshots = new ArrayDeque<>();
	final private Deque<JSONObject> redoSnapshots = new ArrayDeque<>();

	public void add(JSONObject snapshot)
	{
		if (snapshots.isEmpty() || !areEqual(snapshot, snapshots.peekLast())) {

			snapshots.add(snapshot);
			redoSnapshots.clear();

			if (snapshots.size() > 20) {
				snapshots.removeFirst();
			}
		}
	}

	public Explain<JSONObject> undo()
	{
		if (!snapshots.isEmpty()) {
			JSONObject current = snapshots.pollLast();
			redoSnapshots.add(current);

			return new Explain<>(!snapshots.isEmpty()
					? snapshots.peekLast()
					: new SheetMusic().getJsonRepresentation());
		} else {
			return new Explain<>(false, "Here History Starts");
		}
	}

	public Explain<JSONObject> redo()
	{
		if (!redoSnapshots.isEmpty()) {
			JSONObject current = redoSnapshots.pollLast();
			snapshots.add(current);

			return new Explain<>(current);
		} else {
			return new Explain<>(false, "Here History Ends");
		}
	}

	// probably will be faster if we assert without string generation...
	private static boolean areEqual(JSONObject a, JSONObject b)	{
		return a.toString().equals(b.toString());
	}
}
