package Gui.staff;

import Gui.staff.Staff;
import Gui.staff.pointerable.Accord;
import Gui.staff.pointerable.Nota;
import Gui.staff.pointerable.Phantom;
import Musica.*;

public class Pointer {
	public static boolean SOUND_ON = true;
	public static boolean SOUND_OFF = false;

	public static Staff stan = null;
	public static int init(Staff newStan){
		stan = newStan;
		return 0;
	}

	private static Pointer instance = null;

	public static Pointer getInstance() {
		if (Pointer.instance == null) {
			Pointer.instance = new Pointer();
		}
		return Pointer.instance;
	}

	public static int moveOut(){
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return 66;
		}
		return 0;
	}

	public static int moveToBegin(){
		stan.checkValues(stan.phantomka);
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return 66;
		}
		if (stan.getAccordList().size() == 0) return -1;
		return 0;
	}
	public static int moveToEnd(){
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return 66;
		}
		while (move(1) != false);
		return 0;
	}

	public static int moveTo(int q){
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return 66;
		}
		if ((q >= stan.noshuCount) || (q < -1)) {System.out.println("Stack overflow"); return -1;}    	

		moveToBegin();
		move(q);

		return 0;
	}
	public static int moveTo(Accord accord){
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return 66;
		}
		moveToBegin();
		return 0;
	}

	public static boolean move(int q) {
		return move(q, false);
	}

	public static boolean moveSis(int n) { // TODO: logic mistake... somewhere here
		int stepCount = n * stan.parentSheetMusic.getStepInOneSystemCount();
		while (stepCount > 0) {
			moveRealtime(1, SOUND_OFF);
		}
		while (stepCount < 0) {
			moveRealtime(-1, SOUND_OFF);
		}
		return true;
	}

	public static boolean moveRealtime(int q, boolean shouldISound){
		while (q < 0) {
			move(-1, shouldISound);
			q += 1;
		}
		while (q > 0) {
			move(1, shouldISound);
			q -= 1;
		}
		return true;
	}

	public static boolean move(int q, boolean withSound){

		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return false;
		}        
		stan.setFocusedIndex(stan.getFocusedIndex() + q);

		if (withSound && stan.getFocusedAccord() != null) {
			PlayMusThread.playAccord(stan.getFocusedAccord());
		}

		return true;
	}

	public static boolean isAfter(Accord accord){
		if (stan == null) {
			System.out.println("There is no stan you're talking about");
			return false;
		}
		for (Accord tmp: stan.getAccordList()) {
			if (tmp == accord) return false; 
		}
		return true;
	}

	public static int nNotiVAccorde = -1;					// -1 - весь аккорд
	public static boolean pointsOneNotaInAccord = false;	
	public static Nota accordinaNota = null;				// Типа нота, на которую сейчас указывает Поинтер (не аккорд!)
	public static void resetAcc() {
		accordinaNota = null;
		Pointer.nNotiVAccorde = -1;
	}

	public static Nota getCurrentAccordinuNotu() {
		return accordinaNota;
	}
}
