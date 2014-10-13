diff --git a/.gitignore b/.gitignore
index ba077a4..8946caa 100644
--- a/.gitignore
+++ b/.gitignore
@@ -1 +1,2 @@
 bin
+/nbproject/private/
\ No newline at end of file
diff --git a/src/GraphTmp/DrawPanel.java b/src/GraphTmp/DrawPanel.java
index 128260d..271e6bf 100644
--- a/src/GraphTmp/DrawPanel.java
+++ b/src/GraphTmp/DrawPanel.java
@@ -231,7 +231,6 @@ public class DrawPanel extends JPanel {
             	trolling = false;
             	triol = 0;
             	curCislic += triolTaktSum / 3;
-            	out("curCislic/triolTaktSum: "+curCislic+" "+triolTaktSum);
             	triolTaktSum = 0;            	
             	checkTakt(g);            	
             }
diff --git a/src/Musica/NotnyStan.java b/src/Musica/NotnyStan.java
index 7495e76..5c64bf9 100644
--- a/src/Musica/NotnyStan.java
+++ b/src/Musica/NotnyStan.java
@@ -218,8 +218,8 @@ public class NotnyStan {
     }
 
     
-    public Nota addFromFile(int tune, int cislic){
-    	Nota newbie = new Nota(tune, (int)cislic);
+    public Nota addFromFile(int tune, int cislic, int channel){
+    	Nota newbie = new Nota(tune, (int)cislic, channel);
         newbie.prev = Pointer.curNota;
 		Pointer.curNota.next = newbie;
 		newbie.isFirst = true;	        
diff --git a/src/Pointiki/Nota.java b/src/Pointiki/Nota.java
index 6815d10..c2b614e 100644
--- a/src/Pointiki/Nota.java
+++ b/src/Pointiki/Nota.java
@@ -11,7 +11,7 @@ import javax.imageio.ImageIO;
 
 import GraphTmp.DrawPanel;
 
-public class Nota extends Pointerable {
+final public class Nota extends Pointerable {
 	public int channel = -1;
 	public boolean userDefinedChannel = false;
 	
@@ -47,10 +47,12 @@ public class Nota extends Pointerable {
         	bufInited = true;        	
         }
     }
-    public Nota(int tune, int cislic) {
-    	this(tune);
-    	this.cislic = cislic;
-    }
+
+	public Nota(int tune, int cislic, int channel) {
+		this(tune);
+		this.cislic = cislic;
+		setChannel(channel);
+	}
     
     public Nota(int tune, long elapsed){  	    	
         this(tune);
@@ -99,12 +101,42 @@ public class Nota extends Pointerable {
         }
     }
 
+	private static String normalizeString(String str, int desiredLength) {
+		return String.format("%1$-" + desiredLength + "s", str);
+//		return str.length() < desiredLength 
+//				? str +
+	}
+
+	public String getInfoString() {
+		return	normalizeString(strTune(this.pos) + (isBemol ? "-бемоль" : ""),12) +
+				normalizeString(okt + " " + oktIdxToString(okt), 19) +
+				normalizeString(channel+"", 2);
+	}
+
     @Override
     public String toString() {
-        String s = "midi: "+tune+"; pos: "+pos+"; okt: "+okt+"; "+strTune(pos);
-        return s;
+		String result = "аккорд:\n";
+		Nota curNota = this;
+		while (curNota != null) {
+			result += "\t" + curNota.getInfoString() + "\n";
+			curNota = curNota.accord; } 
+        String s = "nota: "+tune+"; pos: "+pos+"; okt: "+okt+"; "+strTune(pos);
+        return result;
     }
 
+	private static String oktIdxToString(int idx) {
+		return  idx == 1 ?	"субконтроктава" :
+				idx == 2 ?	"контроктава" :
+				idx == 3 ?	"большая октава" :
+				idx == 4 ?	"малая октава" :
+				idx == 5 ?	"первая октава" :
+				idx == 6 ?	"вторая октава" :
+				idx == 7 ?	"третья октава" :
+				idx == 8 ?	"четвёртая октава" :
+				idx == 9 ?	"пятая октава" :
+							"не знаю          ";
+	}
+
     private String strTune(int n){
         if (n < 0) {
             n += Integer.MAX_VALUE - Integer.MAX_VALUE%12;
@@ -246,8 +278,6 @@ public class Nota extends Pointerable {
     public static BufferedImage notaImgCol[] = new BufferedImage[8];
     public static BufferedImage[][] voicedNotas = new BufferedImage[10][8];
     static void bufInit() {
-    	System.out.println("Эта функция запускается лишь один раз - при создании первого экземпляра класса");
-
     	File notRes[] = new File[8];
         for (int idx = -1; idx<7; ++idx){
         	String str = "imgs/" + pow(2, idx) + "_sized.png";
@@ -290,7 +320,6 @@ public class Nota extends Pointerable {
         
         for (int chan = 0; chan < 10; ++chan) {        	
             for (int idx = 0; idx < 8; ++idx) {
-            	
             	voicedNotas[chan][idx] = new BufferedImage(w1, h1, BufferedImage.TYPE_INT_ARGB);
 	            g = voicedNotas[chan][idx].createGraphics();
 	            g.setColor(calcCol(chan));
@@ -302,8 +331,15 @@ public class Nota extends Pointerable {
         }
     }
     
-    private static Color calcCol(int n) {    	
-    	return Color.RED;
+    private static Color calcCol(int n) {
+    	return	n == -1 ? new Color(0,0,0) : // black
+				n == 0 ? new Color(255,0,0) : // red
+				n == 1 ? new Color(255,127,0) : // orange
+				n == 2 ? new Color(127,127,0) : // yellow
+				n == 3 ? new Color(0,255,0) : // green
+				n == 4 ? new Color(0,255,255) : // cyan
+				n == 5 ? new Color(255,0,255) : // magenta
+				Color.DARK_GRAY;
     }
 
     public BufferedImage getImage() {
@@ -350,4 +386,8 @@ public class Nota extends Pointerable {
     	}
     	return list;
     }
+
+	public void setChannel(int channel) {
+		this.channel = channel;
+	}
 }
\ No newline at end of file
diff --git a/src/Pointiki/Pointer.java b/src/Pointiki/Pointer.java
index 16658a3..0b47757 100644
--- a/src/Pointiki/Pointer.java
+++ b/src/Pointiki/Pointer.java
@@ -99,24 +99,14 @@ public class Pointer {
     
     public static boolean moveRealtime(int q, boolean shouldISound){
     	while (q < 0) {
-	    	Pointerable n = curNota; 
-	    	if (n == null) return false;
-	    	if ( (n=n.prev)==null?false:(n=n.prev)==null?false:(n=n.prev)==null?false:n.isTriol )
-	            move(-3, shouldISound);
-	        else {
-	            move(-1, shouldISound);
-	        }
-	    	q += 1;
+			if (curNota == null) return false;
+			move(-1, shouldISound);
+			q += 1;
     	}
     	while (q > 0) {
-	    	Pointerable n = curNota; 
-	    	if (n == null) return false;
-	    	if ( (n=n.next)==null?false:(n=n.next)==null?false:(n=n.next)==null?false:n.isTriol )
-	            move(3, shouldISound);
-	        else {
-	            move(1, shouldISound);
-	        }
-	    	q -= 1;
+			if (curNota == null) return false;
+			move(1, shouldISound);
+			q -= 1;
     	}
     	return true;
     }
@@ -146,11 +136,14 @@ public class Pointer {
         }                
         gpos += delta;
         curNota.underPtr = true;
+		accordinaNota = null;
+        if (curNota instanceof Phantom) stan.checkValues((Phantom)curNota);
+
         
         if (withSound) playMusThread.playAccordDivided(curNota, 1);
         //stan.drawPanel.checkCam();
         stan.drawPanel.repaint();
-        if (curNota instanceof Phantom) stan.checkValues((Phantom)curNota);
+		System.out.print(curNota.toString());
         return true;
     }
     
@@ -188,4 +181,8 @@ public class Pointer {
     	}
     }
 
+	public static Nota getCurrentAccordinuNotu() {
+		return accordinaNota;
+	}
+
 }
diff --git a/src/Tools/FileProcessor.java b/src/Tools/FileProcessor.java
index 6d08a06..7dfd35a 100644
--- a/src/Tools/FileProcessor.java
+++ b/src/Tools/FileProcessor.java
@@ -14,16 +14,14 @@ import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
-/**
- * Created by irak on 6/14/14.
- */
 public class FileProcessor {
-
-
     final static byte NEWACCORD = 0;
     final static byte EOS = 1; // End Of String
     final static byte LYRICS = 2;
     final static byte VERSION = 3;
+		final static byte VERSION_BEFORE_VERSIONING = 0;
+		final static byte VERSION_32_FIRST = 32;
+		final static byte VERSION_33_CHANNELS = 33;
     final static byte PHANT = 4;
     final static byte FLAGS = 16;
     final static int MAXSLOG = 255;
@@ -35,7 +33,31 @@ public class FileProcessor {
         stan = stanNew;
     }
 
-    public static int saveFile( File f ){
+    public static void savePNG ( File f ) {
+        DrawPanel albert = stan.drawPanel;
+        if (albert == null) out("Что ты пытаешься сохранить, мудак?!");
+        BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
+        Graphics g = img.createGraphics();
+        g.setColor(Color.GREEN);
+        g.fillRect(15,15,80,80);
+        albert.paintComponent(g);
+
+        try {
+            ImageIO.write(img, "png", f);
+        } catch (IOException e) {
+            out("Ошибка рисования");
+        }
+    }
+
+    public static void savePDF ( File f ) {
+        // TODO: todo
+    }
+
+    private static void out(String str) {
+        System.out.println(str);
+    }
+
+    public static int saveKlsnFile( File f ){
         if (ourFile == null) ourFile = f;
         FileOutputStream strmOut;
         try {
@@ -91,7 +113,7 @@ public class FileProcessor {
         return 0;
     }
 
-    public static int klsnOpen( File f ){
+    public static int openKlsnFile( File f ) {
         out("Открыли файл");
         if (ourFile == null) ourFile = f;
         try {
@@ -105,109 +127,181 @@ public class FileProcessor {
             int b;
             int version;
             b = strmIn.read();
-            if (b != VERSION) version = 0;
-            else {
+            if (b != VERSION) {
+				version = 0;
+				System.out.println("Хуйня, товарищи, версия-то не указана!");
+			} else {
                 version = b = strmIn.read();
                 strmIn.read();
                 b = strmIn.read();
             }
-            int tune;
-            int cislic;
-            Nota last = null;
-            while ( b != -1 ) {
-                switch (b) {
-                    case PHANT: // TODO: Возможно, ошибка
-                        int temptempo = 0;
-                        temptempo |= ((int)(strmIn.read()) << 8);
-                        temptempo |= (strmIn.read());
-                        Phantom phantomka = stan.phantomka;
-                        phantomka.valueTempo = temptempo;
-                        phantomka.valueVolume = ((double)strmIn.read()) / 100;
-                        phantomka.setCislicFromFile(strmIn.read());
-
-                        stan.checkValues(phantomka);
-                        strmIn.read();
-                        b = strmIn.read();
-                        break;
-                    case NEWACCORD:
-                        tune = strmIn.read();
-                        cislic = strmIn.read();
-                        last = stan.addFromFile(tune, cislic);
-                        b = strmIn.read();
-
-                        while (b >= MINTUNE && b != -1) {
-                            cislic = strmIn.read();
-                            ((Nota)Pointer.curNota).append(new Nota(b, (int)cislic));
-                            b = strmIn.read();
-                        }
-                        break;
-                    case EOS:
-                        byte[] bajti = new byte[MAXSLOG];
-                        int i = 0;
-                        b = strmIn.read();
-                        while (b != EOS) {
-                            if (i < MAXSLOG) bajti[i] = (byte)b;
-                            else System.out.println("На одну ноту повешен очень длинный текст, моя программа ещё к такому не готова!");
-                            ++i;
-                            b = strmIn.read();
-                            if (b == -1) System.exit(66);
-                        }
-                        b = strmIn.read();
-                        last.setSlog( new String(bajti, 0, i, "UTF-8") );
-                        out(last.slog);
-
-                        break;
-                    case FLAGS:
-                        b = strmIn.read();
-                        // Может генерить ошибку, если у нас что-то неправильно
-                        if ( (b & (1 << 3)) == (1 << 3) ) last.isTriol = true;
-                        strmIn.read();
-                        b = strmIn.read();
-                        break;
-                    default:
-                        // Пропускаем неизвестные тэги (главное, чтобы в этих тэгах
-                        // все числа были больше 32)
-                        out("Неизвестные тэги? "+b);
-
-                        int subB = b;
-                        while ( (b = strmIn.read()) != subB );
-                        b = strmIn.read();
-                        break;
-                }
-            } // while b!=-1
+
+			switch (version) {
+				case VERSION_32_FIRST:
+					klsnOpen32(b, strmIn);
+					break;
+				case VERSION_33_CHANNELS:
+					klsnOpen33(b, strmIn);
+					break;
+				case VERSION_BEFORE_VERSIONING:
+					klsnOpen32(b, strmIn);
+					break;
+				default:
+					// throw new UnknownVersionOrWrongFileException();
+					break;
+			}
 
             strmIn.close();
+			return 0;
         } catch (IOException e){
             out("Не открывается файл для чтения");
             return -1;
         }
-        out("Закрыли файл");
-        return 0;
+        /*out("Закрыли файл");
+        return 0;*/
     }
 
-    public static void savePNG ( File f ) {
-        DrawPanel albert = stan.drawPanel;
-        if (albert == null) out("Что ты пытаешься сохранить, мудак?!");
-        BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
-        Graphics g = img.createGraphics();
-        g.setColor(Color.GREEN);
-        g.fillRect(15,15,80,80);
-        albert.paintComponent(g);
+	private static int klsnOpen32 (int firstByte, FileInputStream strmIn) throws IOException {
+		int b = firstByte;
+		int tune;
+		int cislic;
+		Nota last = null;
+		while ( b != -1 ) {
+			switch (b) {
+				case PHANT: // TODO: Возможно, ошибка
+					int temptempo = 0;
+					temptempo |= ((int)(strmIn.read()) << 8);
+					temptempo |= (strmIn.read());
+					Phantom phantomka = stan.phantomka;
+					phantomka.valueTempo = temptempo;
+					phantomka.valueVolume = ((double)strmIn.read()) / 100;
+					phantomka.setCislicFromFile(strmIn.read());
 
-        try {
-            ImageIO.write(img, "png", f);
-        } catch (IOException e) {
-            out("Ошибка рисования");
-        }
-    }
+					stan.checkValues(phantomka);
+					strmIn.read();
+					b = strmIn.read();
+					break;
+				case NEWACCORD:
+					tune = strmIn.read();
+					cislic = strmIn.read();
+					last = stan.addFromFile(tune, cislic, -1);
+					b = strmIn.read();
 
-    public static void savePDF ( File f ) {
-        // TODO: todo
-    }
+					while (b >= MINTUNE && b != -1) {
+						cislic = strmIn.read();
+						((Nota)Pointer.curNota).append(new Nota(b, (int)cislic, -1));
+						b = strmIn.read();
+					}
+					break;
+				case EOS:
+					byte[] bajti = new byte[MAXSLOG];
+					int i = 0;
+					b = strmIn.read();
+					while (b != EOS) {
+						if (i < MAXSLOG) bajti[i] = (byte)b;
+						else System.out.println("На одну ноту повешен очень длинный текст, моя программа ещё к такому не готова!");
+						++i;
+						b = strmIn.read();
+						if (b == -1) System.exit(66);
+					}
+					b = strmIn.read();
+					last.setSlog( new String(bajti, 0, i, "UTF-8") );
+					out(last.slog);
 
+					break;
+				case FLAGS:
+					b = strmIn.read();
+					// Может генерить ошибку, если у нас что-то неправильно
+					if ( (b & (1 << 3)) == (1 << 3) ) last.isTriol = true;
+					strmIn.read();
+					b = strmIn.read();
+					break;
+				default:
+					// Пропускаем неизвестные тэги (главное, чтобы в этих тэгах
+					// все числа были больше 32)
+					out("Неизвестные тэги? "+b);
 
-    private static void out(String str) {
-        System.out.println(str);
-    }
+					int subB = b;
+					while ( (b = strmIn.read()) != subB );
+					b = strmIn.read();
+					break;
+			}
+		} // while b!=-1
+		return 0;
+	}
+
+	// чистейший копипаст... но структура файла становится совсем другой от одной строчки, так что так, наверное, лучше...
+	// когда будет готово, можно просто пересохранить все файлы и снести оригинал
+	private static int klsnOpen33 (int firstByte, FileInputStream strmIn) throws IOException {
+		int b = firstByte;
+		int tune;
+		int cislic;
+		int channel;
+		Nota last = null;
+		while ( b != -1 ) {
+			switch (b) {
+				case PHANT: // TODO: Возможно, ошибка
+					int temptempo = 0;
+					temptempo |= ((int)(strmIn.read()) << 8);
+					temptempo |= (strmIn.read());
+					Phantom phantomka = stan.phantomka;
+					phantomka.valueTempo = temptempo;
+					phantomka.valueVolume = ((double)strmIn.read()) / 100;
+					phantomka.setCislicFromFile(strmIn.read());
+
+					stan.checkValues(phantomka);
+					strmIn.read();
+					b = strmIn.read();
+					break;
+				case NEWACCORD:
+					tune = strmIn.read();
+					cislic = strmIn.read();
+					channel = strmIn.read();
+					last = stan.addFromFile(tune, cislic, channel);
+					b = strmIn.read();
+
+					while (b >= MINTUNE && b != -1) {
+						tune = b;
+						cislic = strmIn.read();
+						channel = strmIn.read();
+						((Nota)Pointer.curNota).append(new Nota(tune, (int)cislic, (int)channel));
+						b = strmIn.read();
+					}
+					break;
+				case EOS:
+					byte[] bajti = new byte[MAXSLOG];
+					int i = 0;
+					b = strmIn.read();
+					while (b != EOS) {
+						if (i < MAXSLOG) bajti[i] = (byte)b;
+						else System.out.println("На одну ноту повешен очень длинный текст, моя программа ещё к такому не готова!");
+						++i;
+						b = strmIn.read();
+						if (b == -1) System.exit(66);
+					}
+					b = strmIn.read();
+					last.setSlog( new String(bajti, 0, i, "UTF-8") );
+					out(last.slog);
+
+					break;
+				case FLAGS:
+					b = strmIn.read();
+					// Может генерить ошибку, если у нас что-то неправильно
+					if ( (b & (1 << 3)) == (1 << 3) ) last.isTriol = true;
+					strmIn.read();
+					b = strmIn.read();
+					break;
+				default:
+					// Пропускаем неизвестные тэги (главное, чтобы в этих тэгах
+					// все числа были больше 32)
+					out("Неизвестные тэги? "+b);
 
+					int subB = b;
+					while ( (b = strmIn.read()) != subB );
+					b = strmIn.read();
+					break;
+			}
+		} // while b!=-1
+		return 0;
+	}
 }
diff --git a/src/Tools/KeyEventi.java b/src/Tools/KeyEventi.java
index 671e614..54aa155 100644
--- a/src/Tools/KeyEventi.java
+++ b/src/Tools/KeyEventi.java
@@ -66,7 +66,7 @@ public class KeyEventi implements KeyListener {
                             fn = new File(fn + ".klsn");
                         }
 
-                        FileProcessor.saveFile(fn);
+                        FileProcessor.saveKlsnFile(fn);
                     }
                     if (rVal == JFileChooser.CANCEL_OPTION) {
                         break;
@@ -77,7 +77,7 @@ public class KeyEventi implements KeyListener {
                     if (i == 0) {
                         int sVal = chooserSave.showOpenDialog(parent);
                         if (sVal == JFileChooser.APPROVE_OPTION) {
-                            FileProcessor.klsnOpen(chooserSave.getSelectedFile());
+                            FileProcessor.openKlsnFile(chooserSave.getSelectedFile());
                         }
                         if (sVal == JFileChooser.CANCEL_OPTION) {
                             break;
@@ -174,12 +174,12 @@ public class KeyEventi implements KeyListener {
                 break;
             case KeyEvent.VK_UP:
                 playMusThread.shutTheFuckUp();
-                Pointer.moveSis(1);
+                Pointer.moveSis(-1);
                 stan.drawPanel.checkCam();
                 break;
             case KeyEvent.VK_DOWN:
                 playMusThread.shutTheFuckUp();
-                Pointer.moveSis(-1);
+                Pointer.moveSis(1);
                 stan.drawPanel.checkCam();
                 break;
             case KeyEvent.VK_HOME:
@@ -263,13 +263,17 @@ public class KeyEventi implements KeyListener {
                 }
                 Albert.repaint();
                 break;
+			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
+				if (curNota instanceof Phantom) {
+                    ((Phantom)curNota).tryToWrite( e.getKeyChar() );
+                    break;
+                } else if (curNota instanceof Nota) {
+					Nota nota = Pointer.getCurrentAccordinuNotu();
+					if (nota != null) nota.setChannel(e.getKeyCode() - '0'); }
+				break;
             default:
                 if (stan.mode == NotnyStan.aMode.playin) break;
 
-            	if (curNota instanceof Nota == false) {
-                    ((Phantom)curNota).tryToWrite( e.getKeyChar() );
-                    break;
-                }
                 System.out.println("Keycode "+e.getKeyCode());
             	if (e.getKeyCode() >= 32 || e.getKeyCode() == 0) {
             		// Это символ - напечатать
