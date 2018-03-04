import org.opencv.core.Mat;

import javax.swing.*;

public class QuickDisplay extends JFrame {

    JLabel lbl;

    public QuickDisplay(Mat m) {
        lbl = new JLabel(new ImageIcon(FontMaker.matToBufferedImage(m)));
        JPanel jp = new JPanel();
        jp.add(lbl);
        add(jp);
        pack();
        setVisible(true);
    }

    public void update(Mat m) {
        lbl.setIcon(new ImageIcon(FontMaker.matToBufferedImage(m)));
        super.repaint();
        pack();
    }

}
