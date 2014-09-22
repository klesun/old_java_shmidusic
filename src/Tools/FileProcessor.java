package Tools;

import GraphTmp.DrawPanel;
import Musica.NotnyStan;
import Pointiki.Nota;
import Pointiki.Phantom;
import Pointiki.Pointer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by irak on 6/14/14.
 */
public class FileProcessor {


    final static byte NEWACCORD = 0;
    final static byte EOS = 1; // End Of String
    final static byte LYRICS = 2;
    final static byte VERSION = 3;
    final static byte PHANT = 4;
    final static byte FLAGS = 16;
    final static int MAXSLOG = 255;
    final static int MINTUNE = 32;
    static File ourFile = null;
    static NotnyStan stan = null;

    public static void init(NotnyStan stanNew) {
        stan = stanNew;
    }

    public static int saveFile( File f ){
        if (ourFile == null) ourFile = f;
        FileOutputStream strmOut;
        try {
            strmOut = new FileOutputStream( f );
            int rezPos = Pointer.pos;
            Pointer.moveOut();
            // TODO: Записать данные из фантомки (темп, тактовая длина...)
            strmOut.write( VERSION );
            strmOut.write( 32 ); // С этого момента начинаем счёт версий. Первая будет 32, потому что а хули нет?
            strmOut.write( VERSION );

            strmOut.write( PHANT );

            strmOut.write((byte)( (stan.tempo >> 8) & 255 ));
            strmOut.write((byte)(stan.tempo & 255));
            strmOut.write((byte)Math.round(stan.volume * 100));
            strmOut.write((byte)(stan.cislic));

            strmOut.write( PHANT );
            boolean tri = false;
            byte bajt = 0;

            while (Pointer.move(1)) {
                strmOut.write( NEWACCORD ); // Ноль здесь будет знаком конца аккорда
                Nota n = (Nota)Pointer.curNota;
                if (n.isTriol) tri = true;
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
                if (tri) bajt |= (1 << 3);
                if (bajt != 0) {
                    strmOut.write(FLAGS);
                    strmOut.write(bajt);
                    strmOut.write(FLAGS);
                    bajt = 0;
                    tri = false;
                }
            }
            Pointer.moveTo(rezPos);
            strmOut.close();
        } catch (IOException e) {
            out("С файлом (кто бы мог подумать) что-то не так");
            return -1;
        }
        return 0;
    }

    public static int klsnOpen( File f ){
        out("Открыли файл");
        if (ourFile == null) ourFile = f;
        try {
            FileInputStream strmIn = new FileInputStream( f );
            if (f.canRead() == false) {
                strmIn.close();
                return -2;
            }

            stan.clearStan();
            int b;
            int version;
            b = strmIn.read();
            if (b != VERSION) version = 0;
            else {
                version = b = strmIn.read();
                strmIn.read();
                b = strmIn.read();
            }
            int tune;
            int cislic;
            Nota last = null;
            while ( b != -1 ) {
                switch (b) {
                    case PHANT: // TODO: Возможно, ошибка
                        int temptempo = 0;
                        temptempo |= ((int)(strmIn.read()) << 8);
                        temptempo |= (strmIn.read());
                        Phantom phantomka = stan.phantomka;
                        phantomka.valueTempo = temptempo;
                        phantomka.valueVolume = ((double)strmIn.read()) / 100;
                        phantomka.setCislicFromFile(strmIn.read());

                        stan.checkValues(phantomka);
                        strmIn.read();
                        b = strmIn.read();
                        break;
                    case NEWACCORD:
                        tune = strmIn.read();
                        cislic = strmIn.read();
                        last = stan.addFromFile(tune, cislic);
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
                        out(last.slog);

                        break;
                    case FLAGS:
                        b = strmIn.read();
                        // Может генерить ошибку, если у нас что-то неправильно
                        if ( (b & (1 << 3)) == (1 << 3) ) last.isTriol = true;
                        strmIn.read();
                        b = strmIn.read();
                        break;
                    default:
                        // Пропускаем неизвестные тэги (главное, чтобы в этих тэгах
                        // все числа были больше 32)
                        out("Неизвестные тэги? "+b);

                        int subB = b;
                        while ( (b = strmIn.read()) != subB );
                        b = strmIn.read();
                        break;
                }
            } // while b!=-1

            strmIn.close();
        } catch (IOException e){
            out("Не открывается файл для чтения");
            return -1;
        }
        out("Закрыли файл");
        return 0;
    }

    public static void savePNG ( File f ) {
        DrawPanel albert = stan.drawPanel;
        if (albert == null) out("Что ты пытаешься сохранить, мудак?!");
        BufferedImage img = new BufferedImage(albert.getWidth(),albert.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.createGraphics();
        g.setColor(Color.GREEN);
        g.fillRect(15,15,80,80);
        albert.paintComponent(g);

        try {
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            out("Ошибка рисования");
        }
    }

    public static void savePDF ( File f ) {
        // TODO: todo
    }


    private static void out(String str) {
        System.out.println(str);
    }

}
