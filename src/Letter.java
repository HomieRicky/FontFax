import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Letter implements Serializable{

    public transient Mat letterImg;
    public String letter;
    public boolean set;

    public Letter(Mat m, String l) {
        this.letterImg = m;
        this.letter = l;
        set = true;
    }

    public Letter() {
        set = false;
    }

    private void writeObject(ObjectOutputStream o) throws IOException {
        o.defaultWriteObject();
        o.writeObject(letter);
        int size = this.letterImg.rows()*this.letterImg.cols()*this.letterImg.channels();
        o.writeInt(this.letterImg.rows());
        o.writeInt(this.letterImg.cols());
        o.writeInt(this.letterImg.channels());
        byte data[] = new byte[size];
        this.letterImg.get(0, 0, data);
        o.write(data);
    }

    private void readObject(ObjectInputStream i) throws ClassNotFoundException, IOException {
        i.defaultReadObject();
        letter = (String) i.readObject();
        int rows = i.readInt();
        int cols = i.readInt();
        int chans = i.readInt();
        byte data[] = new byte[rows*cols*chans];
        letterImg = new Mat(rows, cols, CvType.CV_8UC(chans));
        i.readFully(data);
        letterImg.put(0, 0, data);
        set = true;
    }
}
