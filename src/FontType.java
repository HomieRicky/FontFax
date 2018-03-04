import org.opencv.core.Mat;

import java.io.Serializable;
import java.util.ArrayList;

public class FontType implements Serializable {

    private ArrayList<Letter> alphabet;

    public FontType() {
        alphabet = new ArrayList<>();
    }

    public boolean hasLetter(String c) {
        for(Letter l : alphabet) {
            if(l.letter.equals(c)) return true;
        }
        return false;
    }

    public void addOrReplaceLetter(String c, Mat m) {
        if(hasLetter(c)) {
            for(Letter l : alphabet) {
                if(l.letter.equals(c)) l = new Letter(m, c);
            }
        } else {
            alphabet.add(new Letter(m, c));
        }
    }

    public boolean clearLetter(String c) {
        for(Letter l : alphabet) {
            if(l.letter.equals(c)) {
                alphabet.remove(l);
                return true;
            }
        }
        return false;
    }

    public Mat getLetterImg(char c) {
        for(Letter l : alphabet) {
            if(l.letter.equals(c)) return l.letterImg;
        }
        return null;
    }

    public int[] mapIndicesToString(String in) {
        int size = 0;
        ArrayList<Integer> indicesByLength = new ArrayList<>();
        ArrayList<Integer> sizes = new ArrayList<>();
        for(Letter l : alphabet) if(l.letter.length() > size) size = l.letter.length();
        while(size > 0) {
            for(int i = 0; i < alphabet.size(); i++) if(alphabet.get(i).letter.length() == i) {
                indicesByLength.add(i);
                sizes.add(alphabet.get(i).letter.length());
            }
            size--;
        }
        int ibl[] = indicesByLength.stream().mapToInt(i->i).toArray();
        int s[] = sizes.stream().mapToInt(i->i).toArray();

        ArrayList<Integer> indices = new ArrayList<>();
        for(int i = 0; i < in.length();) {
            for(int j = 0; j < ibl.length; j++) {
                if(i+s[j] <= in.length()) {
                    if(in.substring(i, i+s[j]).equals(alphabet.get(ibl[j]).letter)) {
                        indices.add(j);
                        break;
                    }
                }
            }
        }

        return indices.stream().mapToInt(i->i).toArray();
    }

}
