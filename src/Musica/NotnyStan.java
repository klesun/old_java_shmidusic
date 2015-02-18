// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Musica;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BackEnd.MidiCommon;
import GraphTmp.DrawPanel;
import Pointiki.Accord;
import Pointiki.Nota;
import Pointiki.Phantom;
import Pointiki.Pointer;
import Pointiki.Pointerable;
import Tools.DeviceEbun;
import static Tools.DeviceEbun.MidiOutputDevice;
import static Tools.DeviceEbun.sintReceiver;
import Tools.FileProcessor;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

public class NotnyStan {
	public byte channelFlags = -1;
	int sessionId = (int)Math.random()*Integer.MAX_VALUE;

	public static final int CHANNEL = 0;
	public static final int DEFAULT_ZNAM = 64;
	public int cislic = 64;
	Nota unclosed[] = new Nota[256];
	int EPSILON = 50;
	
	public static int tempo = 120;
	public static int instrument = 0;
	public static double volume = 0.5;

    public enum aMode {
        append,
        rewrite,
        insert,
        playin,
        passive,
        
        tictacin; // Написать
        // идея вообще такая: тиктакает метроном, а ты нажимаешь аккорды
    } 
    public aMode mode;
	
    public boolean bassKey, vioKey2, bassKey2;

    public int noshuCount = 0;

    public DrawPanel drawPanel;
    public int to4kaOt4eta = 66; // какой позиции соответствует нижняя до скрипичного ключа
    
    public Phantom phantomka = new Phantom(this);
    public Pointerable firstDeleted = new Nota(1);
    public Pointerable lastDeleted = firstDeleted;	// next-ы будут уходить в глубь,
    										// последний next - первая удалённая нота
    int closerCount = 0;
    
    public NotnyStan(){
        bassKey = true;
        vioKey2 = false;
        bassKey2 = false;

        Pointer.init(this);
        FileProcessor.init(this);
        Pointer.beginNota = phantomka;
        mode = aMode.insert;

        DeviceEbun.openInDevice(this);
        DeviceEbun.openOutDevice();
    }


    public void addPhantom() {
        Phantom phan = new Phantom(this);
        phan.prev = Pointer.pointsAt;
        phan.next = Pointer.pointsAt.getNext();
        Pointer.pointsAt.setNext( phan );
        if (phan.next != null) phan.next.prev = phan;

        ++noshuCount;
        Pointer.move(1);
        drawPanel.repaint();
    }
    

    public void addNotu(int tune, int forca, int elapsed) {     	
    	if (forca == 0) {
    		if (unclosed[tune] == null) return;
    		Nota closer = new Nota(tune, (long)elapsed);
    		--closerCount;
    		unclosed[tune].length = (int)(closer.myTime - unclosed[tune].myTime);
    		return;
    	}
    	if (mode == aMode.passive || mode == aMode.playin) {
    		// Показать, какую ноту ты нажимаешь
    		return;
    	}
    	
    	Nota nota = new Nota(tune, (long)elapsed);    	
    	unclosed[tune] = nota;
    	++closerCount;
    	
    	// Делаем проверку: если с прошлого нажатия прошло меньше Эпсилон времени, значит - это один аккорд
    	if (Pointer.pos >= 0){
    		if (Pointer.pointsAt instanceof Nota) {
	    		Nota prev = (Nota)Pointer.pointsAt;
		    	if (nota.myTime - prev.myTime < EPSILON) {
		    		prev.append(nota);
		    		drawPanel.repaint();
		    		return;
		    	}
    		}
    	}

    	switch (mode) {
    	case append:
    		if (Pointer.pointsAt instanceof Nota == false) break;
    		((Nota)Pointer.pointsAt).append(nota);
    		drawPanel.repaint();
	        break;    
    	case insert:    		
    		
    		nota.prev = Pointer.pointsAt;
    		nota.next = Pointer.pointsAt.getNext();
    		Pointer.pointsAt.next = nota;
    		if (nota.next != null) nota.getNext().prev = nota;
    		
    		nota.isFirst = true;	        
        	++noshuCount;
        	Pointer.move(1);
    		
    		//if (noshuCount > 3) drawPanel.checkCam();
    		
    		break;
        default: break;
    	}
        
    	drawPanel.repaint();
    }

    int deleted = 0;
    
    public void retrieveLast(){
    	if (deleted == 0) return;
    	Pointerable cur = lastDeleted;
    	lastDeleted = lastDeleted.next;
    	cur.next = cur.prev.next;
    	cur.prev.next = cur;
    	if (cur.next != null) cur.next.prev = cur;
    	cur.retrieve = lastRetrieved;
    	lastRetrieved = cur;
    	++noshuCount;
    	--deleted;
    	if ( Pointer.isAfter(cur) ) ++Pointer.pos;
    	drawPanel.repaint();
    }
    Pointerable lastRetrieved = null;
    
    public void detrieveNotu(){ 
    	int rez = -1;
    	Pointerable nota = lastRetrieved;
    	if (nota == null) return;
    	lastRetrieved = nota.retrieve;    	
    	if (Pointer.pointsAt == nota) {
    		delNotu();
    		return;
    	} else rez = Pointer.moveTo(nota);
    	if (rez == 0) delNotu();
    	else {
    		out("Воскресшей ноты на стане нет");
    	}
    	drawPanel.repaint();    	
    }
    
    public boolean delNotu(){
        if (Pointer.pointsAt == phantomka) return Pointer.move(1);
        Pointerable elem = Pointer.pointsAt;
        //nota.clearAccord();
        if (elem.prev != null) {
            Pointer.move(-1);
            elem.prev.next = elem.next;
            if (elem.next != null) elem.next.prev = elem.prev;
        } else if (elem.next != null) {
            Pointer.move(1);
            elem.prev.next = elem.next;
            if (elem.next != null) elem.next.prev = elem.prev;

            --Pointer.pos;
        } else {
            Pointer.moveOut();
            phantomka.next = null;
        }
        
        elem.next = lastDeleted;
        lastDeleted = elem;
        --noshuCount;
        ++deleted;
        drawPanel.repaint();
        return true;
    }

    public int changeMode(){
        if (mode == aMode.insert) mode = aMode.passive;
        else mode = aMode.insert;

        out(mode+"");
        return 0;
    }

    
    public Nota addFromFile(int tune, int cislic, int channel){
    	Nota newbie = new Nota(tune, (int)cislic, channel);
        newbie.prev = Pointer.pointsAt;
		Pointer.pointsAt.next = newbie;
		newbie.isFirst = true;	        
    	++noshuCount;
    	Pointer.move(1);
		if (noshuCount > 3) drawPanel.checkCam();
        return newbie;
    }
    
    public void clearStan() {
    	while (delNotu());
    }

    private void out(String str) {
    	System.out.println(str);
    }

    public void checkValues(Phantom rak) {
        int cislic = rak.cislic, znamen=rak.znamen, tempo = rak.valueTempo; double volume = rak.valueVolume; int instrument = rak.valueInstrument;
        if (cislic!=this.cislic || znamen!=DEFAULT_ZNAM || tempo!=NotnyStan.tempo || volume!=this.volume) {
            // Всё равно ж перерисовывать надо будет
            int k = DEFAULT_ZNAM / znamen;
            this.cislic = cislic*k;
            this.tempo = tempo;
            this.volume = volume;
			setInstrument(instrument);
            drawPanel.repaint();
        }
    }

	private void setInstrument(int instrument) {
		if (instrument != this.instrument) {
			try {
				DeviceEbun.changeInstrument(instrument);
				this.instrument = instrument;
			} catch (InvalidMidiDataException e) { System.out.println("Сука инструмент менять нихуя не получается"); }
		}
	}

    public void triolnutj() {
		if (Pointer.pointsAt instanceof Nota) {
			Nota baseNota = (Nota)Pointer.pointsAt;
			if (baseNota.isTriol) {
				baseNota.isTriol = false;
				drawPanel.repaint();
				return;
			}
			// TODO: пробежаться по этим трём аккордам, сложить аккЛен, если делится с остатком на три - браковать, без остатка - всё хорошо
			// Проверка, всё ли с нотами впорядке
			Nota tmp = (Nota)(Pointer.pointsAt);
			for (int i = 0; i < 2; ++i) {
				if (tmp.next instanceof Nota==false) return;
				tmp = (Nota)(tmp.next);
				if (tmp == null) return;
				if (tmp instanceof Nota == false) return;
				if (tmp.isTriol/* || tmp.cislic != base.cislic*/) return;
			}
			baseNota.isTriol = true;
			drawPanel.repaint();
		}
    }

	public int changeChannelFlag(int channel) {
		if (channel > 7 || channel < 0) return -1;
		channelFlags ^= 1 << channel;
		return 0;
	}

	public Boolean getChannelFlag(int channel) {
		if (channel > 7 || channel < 0) return false;
		return (channelFlags & (1 << channel)) > 0;
	}

	public List<Pointerable> getRootNoteList() {
		// TODO: бляяяяять
		List<Pointerable> rootNoteList = new ArrayList<Pointerable>();  
		Pointerable curNota = Pointer.beginNota;
		while (curNota != null) {
			rootNoteList.add(curNota);
			curNota = curNota.getNext();
		}
		Map<String, Object> huj = new HashMap<String, Object>();

		return rootNoteList;
	}

	public Accord[] getExternalRepresentation() {
		List<Pointerable> noteList = getRootNoteList();
		return new Accord[13];
	}

	// TODO: убрать нахуй
	public void slianie() {
	
	}

	// TODO: убрать нахуй
	public boolean checken = true;
	public void checkTessi() {
		mode = aMode.passive;
		Pointer.moveToBegin();
		Voices golosa = new Voices();
		while (!checken && Pointer.move(1)) {
			golosa.calculate(Pointer.pointsAt);    		    		
		}
		checken = true;
	}
}


