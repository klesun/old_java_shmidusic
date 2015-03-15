package Pointerable;

import java.util.ArrayList;

public interface IAccord {

	public abstract IAccord add(Nota newbie);
	public abstract ArrayList<Nota> getNotaList();
	public abstract int getFirstKeydownTimestamp();

	public abstract String getSlog();
	public abstract IAccord setSlog(String value);
}
