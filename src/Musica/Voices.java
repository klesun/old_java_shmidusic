package Musica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import Pointiki.Nota;
import Pointiki.Pointer;
import Pointiki.Pointerable;



public class Voices {
	int[] taktLeft = {0,0,0,0};

	Map<String, Set<Nota>> noti = new HashMap<String, Set<Nota>>();
	Set<Nota> soprano = new HashSet<Nota>();
	Set<Nota> alt = new HashSet<Nota>();
	Set<Nota> tenore = new HashSet<Nota>();
	Set<Nota> bass = new HashSet<Nota>();
	boolean vse4isti = true;
	int gryazj = 0;

	public Voices() {
		noti.put("soprano", soprano);
		noti.put("alt", alt);
		noti.put("tenore", tenore);
		noti.put("bass", bass);
	}
	
	public void calculate(Pointerable anonimus) {
		if (Pointer.pointsAt instanceof Nota == false) return;
		Nota nota = (Nota)anonimus;
		Nota root = nota;
		int count = nota.getNoteCountInAccord();
		int accLen = nota.getAccLen();
		if (vse4isti) {
			setVoices(root); // 0 - soprano, 1 - alt, 2 - tenor, 3 - bass
		} 
	}

	ArrayList<Nota> allnotas = new ArrayList<Nota>();
	private void setVoices(Nota root) {	
		allnotas.clear();
		allnotas = root.getAccordList();
		soprano.clear();
		alt.clear();
		tenore.clear();
		bass.clear();
		
		int mainMidMid = getMainMid();
		int menMidMid = getMenMid(mainMidMid);
		int womenMidMid = getWomenMid(mainMidMid);
		
		for (Nota tmp: allnotas) {
			if (tmp.userDefinedChannel) continue;
			if (tmp.tune > mainMidMid) { // baba
				if (tmp.tune > womenMidMid) { // soprano
					soprano.add(tmp);
					tmp.tessi = 0;
				} else { // alt
					alt.add(tmp);
					tmp.tessi = 1;
				}
			} else { // muzik
				if (tmp.tune > menMidMid) { // tenore
					tenore.add(tmp);
					tmp.tessi = 2;
				} else { // bass
					bass.add(tmp);
					tmp.tessi = 3;
				}
			}
		}
		
	}
	
	private int getMainMid() {
		int count = allnotas.size();	int sum = 0;
		for (Nota tmp: allnotas) sum+= tmp.tune;
		return sum / count;
	}
	private int getMenMid(int mid) {
		int sum = 0;	int count = 0;
		for (Nota tmp: allnotas) {
			if (tmp.tune > mid) continue;			
			sum+= tmp.tune;
			++count;
		}
		return sum / count;
	}
	private int getWomenMid(int mid) {
		int sum = 0;	int count = 0;
		for (Nota tmp: allnotas) {
			if (tmp.tune <= mid) continue;			
			sum += tmp.tune;
			++count;
		}
		return sum / count;
	}
}
