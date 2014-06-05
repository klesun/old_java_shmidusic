// TODO: мэрджить ноты, типа целая+(половинная+четвёртая=половинная с точкой) = целая с двумя точками
// потому что делать точки плюсиком - это убого!

package Musica;

import GraphTmp.DrawPanel;
import BackEnd.MidiCommon;
import Tools.Pointer;
import Tools.Phantom;

import java.io.*;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class NotnyStan {
	public static final int DEFAULT_ZNAM = 64;
	public int cislic = 64;
	Nota unclosed[] = new Nota[256];
	int EPSILON = 50;
	
	public static int tempo = 120;	
	public static double volume = 0.5;

    public enum aMode {
        append,
        rewrite,
        insert,
        playin,
        passive,
        
        tictacin; // Написать
        // идея вообще такая: тиктакает метроном, а ты нажимаешь аккорды
        
        // ещё туду: изменять длинны нот в аккорде по отдельности
        // а ещё, если в аккорде только одна нота, но она тянется ещё после того, как играет следующий
        // аккорд, добавлять знак паузы к этой ноте. Таким образом обозначим, что длинна аккорда - длинна
        // самой короткой в нём ноты (или паузы)
    } 
    public aMode mode;
	
    public boolean bassKey, vioKey2, bassKey2;

    public int noshuCount = 0;

    public DrawPanel drawPanel;
    public int to4kaOt4eta = 66; // какой позиции соответствует нижняя до скрипичного ключа
    int sis = 0;
    
    public int stepInOneSys = 0;
    public Phantom phantomka = new Phantom(this);
    public Nota firstDeleted = new Nota(1);
    public Nota lastDeleted = firstDeleted; // Тут мы чутка нелогично сделаем:
    										// next-ы будут уходить в глубь,
    										// последний next - первая удалённая
    										// нота
    int closerCount = 0;
    
    public NotnyStan(MidiDevice device){
        bassKey = true;
        vioKey2 = false;
        bassKey2 = false;

        Pointer.setStan(this);
        Pointer.beginNota = phantomka;
        mode = aMode.insert;

        this.device = device;
        openOutDevice();
    }
    

    long totalTime;      
    
    byte[] keysPressed = new byte[32]; //256 битов
    public void addNote(int tune, int forca, int elapsed) {    	
    	
    	if (forca == 0) {
    		if (unclosed[tune] == null) return;
    		Nota closer = new Nota(tune, (long)elapsed);
    		--closerCount;
    		unclosed[tune].length = (int)(closer.myTime - unclosed[tune].myTime);
    		return;
    	}
    	// костыль
    	forca = 127;
    	if (mode == aMode.passive || mode == aMode.playin) {
    		// Показать, какую ноту ты нажимаешь
    		return;
    	}
    	
    	Nota nota = new Nota(tune, (long)elapsed);
    	unclosed[tune] = nota;
    	++closerCount;
    	
    	// Делаем проверку: если с прошлого нажатия прошло меньше Эпсилон времени, значит - это один аккорд
    	if (Pointer.pos >= 0){
    		if (Pointer.curNota instanceof Nota) {
	    		Nota prev = (Nota)Pointer.curNota;
		    	if (nota.myTime - prev.myTime < EPSILON) {
		    		prev.append(nota);
		    		drawPanel.repaint();
		    		return;
		    	}
    		}
    	}

    	switch (mode) {
    	case append:
    		if (Pointer.curNota instanceof Nota == false) break;
    		((Nota)Pointer.curNota).append(nota);
    		drawPanel.repaint();
    		playAccord();
	        break;    
    	case insert:    		
    		
    		nota.prev = Pointer.curNota;
    		nota.next = Pointer.curNota.next;
    		Pointer.curNota.next = nota;
    		if (nota.next != null) nota.next.prev = nota;
    		
    		nota.isFirst = true;	        
        	++noshuCount;
        	Pointer.move(1);
    		
    		//if (noshuCount > 3) drawPanel.checkCam();
    		
    		break;
        default: break;
    	}
        
    	drawPanel.repaint();
    }

    public void playAccord(){
    	// Сделай тут что-нибудь
    }

    int deleted = 0;
    
    public void retrieveNotu(){
    	if (deleted == 0) return;
    	Nota nota = lastDeleted;
    	lastDeleted = lastDeleted.next;
    	nota.next = nota.prev.next;
    	nota.prev.next = nota;
    	if (nota.next != null) nota.next.prev = nota;    	
    	nota.retrieve = lastRetrieved;
    	lastRetrieved = nota;
    	++noshuCount;
    	--deleted;
    	if ( Pointer.isAfter(nota) ) ++Pointer.pos;
    	drawPanel.repaint();
    }
    Nota lastRetrieved = null;
    
    public void detrieveNotu(){ 
    	int rez = -1;
    	Nota nota = lastRetrieved;
    	lastRetrieved = nota.retrieve;
    	if (nota == null) return;
    	if (Pointer.curNota == nota) {
    		delNotu();
    		return;
    	} else rez = Pointer.moveTo(nota);
    	if (rez == 0) delNotu();
    	else {
    		System.out.println("Воскресшей ноты на стане нет");
    	}
    	drawPanel.repaint();    	
    }
    public int delNotu(){
        if (Pointer.curNota instanceof Nota == false) return -1;
        Nota nota = (Nota)Pointer.curNota;
        //nota.clearAccord();
        if (nota.prev != phantomka) {
            Pointer.move(-1);
            nota.prev.next = nota.next;
            if (nota.next != null) nota.next.prev = nota.prev;
        } else if (nota.next != null) {
            Pointer.move(1);
            nota.prev.next = nota.next;
            if (nota.next != null) nota.next.prev = nota.prev;

            --Pointer.pos;
        } else {
            Pointer.moveOut();
            phantomka.next = null;
        }
        
        nota.next = lastDeleted;
        lastDeleted = nota;
        --noshuCount;
        ++deleted;
        drawPanel.repaint();
        return 0;
    }
    final byte NEWACCORD = 0;
    final byte EOS = 1; // End Of String
    final byte LYRICS = 2;
    final byte VERSION = 3;
    final byte STUFF = 4; // End Of String
    final int MAXSLOG = 255;
    final int MINTUNE = 32;

    int fileMode = LYRICS;

    public int saveFile( File f ){
    	if (ourFile == null) ourFile = f;    	
    	FileOutputStream strmOut;
        try {
            strmOut = new FileOutputStream( f );            
            int rezPos = Pointer.pos;
            Pointer.moveOut();
            // TODO: Записать данные из фантомки (темп, тактовая длина...)
            
            while (Pointer.move(1) != -1) {
            	strmOut.write( NEWACCORD ); // Ноль здесь будет знаком конца аккорда
            	Nota n = (Nota)Pointer.curNota;
            	do {     
	            	strmOut.write( (byte)n.tune );	            		            		           
	            	strmOut.write( (byte)n.cislic );
	            	
	            	n = n.accord;
            	} while (n != null);
            	if (Pointer.curNota.slog != null && Pointer.curNota.slog != "") {
	                strmOut.write( EOS ); // Говорим, что дальше идёт текст
	                strmOut.write( Pointer.curNota.slog.getBytes("UTF-8") );
	                strmOut.write( EOS );
            	}
            }
            Pointer.moveTo(rezPos);
            strmOut.close();
        } catch (IOException e) { 
        	System.out.println("С файлом (кто бы мог подумать) что-то не так");
    		return -1;
        }
        return 0;
    }

    
    public int changeMode(){
    	if (mode == aMode.insert) mode = aMode.passive;
    	else mode = aMode.insert;
    	
    	System.out.println(mode);
    	return 0;
    }

    public int klsnOpen( File f ){
    	out("Открыли файл");
    	if (ourFile == null) ourFile = f;
    	try {
    		FileInputStream strmIn = new FileInputStream( f );
    		if (f.canRead() == false) {
    			strmIn.close();
    			return -2;
    		}
    		
    		clearStan();
    		int b;

    		b = strmIn.read();
    		int tune;
    		int cislic;
            Nota last = null;
            while ( b != -1 ) {
            	// TODO: А ещё добавить считывание фантомки
                switch (b) {
                case NEWACCORD:
                    tune = strmIn.read();
                    cislic = strmIn.read();
                    out("cislic from file = "+cislic);
                    last = addFromFile(tune, cislic);
                    b = strmIn.read();

                    while (b >= MINTUNE && b != -1) {
                        cislic = strmIn.read();
                        ((Nota)Pointer.curNota).append(new Nota(b, (int)cislic));
                        b = strmIn.read();
                    }
                    break;
                case EOS:
                    byte[] bajti = new byte[MAXSLOG];
                    int i = 0;
                    b = strmIn.read();
                    while (b != EOS) {
                        if (i < MAXSLOG) bajti[i] = (byte)b;
                        else System.out.println("На одну ноту повешен очень длинный текст, моя программа ещё к такому не готова!");
                        ++i;
                        b = strmIn.read();
                        if (b == -1) System.exit(66);
                    } 
                    b = strmIn.read();
                    last.setSlog( new String(bajti, 0, i, "UTF-8") );
                    
                    break;
                case STUFF:

                    break;
                case VERSION:

                    break;
                default:
                    // Пропускаем неизвестные тэги (главное, чтобы в этих тэгах
                	// все числа были больше 32)
                	// TODO: проверить на работоспособность
                	out("Неизвестные тэги?");
                	
                	int subB = b;
                	while ( (b = strmIn.read()) != subB );
                	b = strmIn.read();
                    break;
                }
            } // while b!=-1

    		strmIn.close();
    	} catch (IOException e){
    		System.out.println("Не открывается файл для чтения");
    		return -1;
    	}
    	out("Закрыли файл");
    	return 0;    	
    }


    
    
    private Nota addFromFile(int tune, int cislic){
        out("От вас получили: "+cislic);
    	Nota newbie = new Nota(tune, (int)cislic);
        out("Однако за время пути: "+newbie.cislic);
        newbie.prev = Pointer.curNota;
		Pointer.curNota.next = newbie;
		newbie.isFirst = true;	        
    	++noshuCount;
    	Pointer.move(1);
		if (noshuCount > 3) drawPanel.checkCam();
        return newbie;
    }
    
    private void clearStan() {
    	while (noshuCount > 0) {
    		delNotu();    	
    	}
    }
        
    public MidiDevice device;
    public boolean stop = true;
    MidiDevice outputDevice = null;
	public static Receiver sintReceiver = null;
	MidiDevice.Info	info;
    ShortMessage onMessage = new ShortMessage();
    ShortMessage offMessage = new ShortMessage();
    
    File ourFile = null;
    
    public int playEntire(){
        stop = false;
    	playMusThread thr;
    	thr = new playMusThread(this);
    	thr.start();
    	return 0;
    }
    public void stopMusic(){
        stop = true;
    }
    
    private int openOutDevice(){
    	info = MidiCommon.getMidiDeviceInfo(device.getDeviceInfo().getName(), true);
    	try {
            outputDevice = MidiSystem.getMidiDevice(info);
            outputDevice.open();
            sintReceiver = outputDevice.getReceiver();
        } catch (MidiUnavailableException e) {
            System.out.println("Не открывается аут ваш"); }
        if (sintReceiver == null) {  System.out.println("Не отдался нам ресивер");
                                    System.exit(1); }
        return 0;
    }
    
    public boolean isChanSep = false;
    public Nota Acc = null;
    public void nextAcc(){    
    	if (Pointer.curNota instanceof Nota == false) {
    		if (Pointer.curNota instanceof Phantom) ((Phantom)Pointer.curNota).tabPressed();
            drawPanel.repaint();
    		return;
    	}
    	if (isChanSep){
    		if (Acc.accord != null) {
    			Acc = Acc.accord;
    			++Pointer.AcNo;
    		} else { 
    			Acc = (Nota)Pointer.curNota;
    			Pointer.AcNo = 0;
    		}
    	} else {
    		isChanSep = true;
    		Acc = (Nota)Pointer.curNota;
    		Pointer.AcNo = 0;
    	}
    	drawPanel.repaint();
    }
    
    private void out(String str) {
    	System.out.println(str);
    }

    public void checkValues(Phantom  rak) {
        int cislic=rak.cislic, znamen=rak.znamen, tempo=rak.valueTempo; double volume=rak.valueVolume;
        if (cislic!=this.cislic || znamen!=DEFAULT_ZNAM || tempo!=NotnyStan.tempo || volume!=this.volume) {
            // Всё равно ж перерисовывать надо будет
            int k = DEFAULT_ZNAM / znamen;
            this.cislic = cislic*k;
            this.tempo = tempo;
            this.volume = volume;
            drawPanel.repaint();
        }
    }

    public void triolnutj() {
        if (Pointer.curNota.isTriol) {
            Pointer.curNota.isTriol = false;
            drawPanel.repaint();
            return;
        }
        // Проверка, всё ли с нотами впорядке
        if (Pointer.curNota instanceof Nota == false) return;
        Nota base = ((Nota)Pointer.curNota);
        if (base.accord == null) {
            Nota tmp = Pointer.curNota.next;
            for (int i = 0; i < 2; ++i) {
                if (tmp == null) return;
                if (tmp instanceof Nota == false) return;
                if (tmp.isTriol || tmp.cislic != base.cislic) return;
                tmp = tmp.next;
            }
        } else {
            // TODO:
            return;
        }
        base.isTriol = true;
        drawPanel.repaint();
    }

    public void slianie() {

    }
    
}


