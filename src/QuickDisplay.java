import org.opencv.core.Mat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class QuickDisplay extends JFrame {

    JLabel lbl;
    FontType font;

    public QuickDisplay(Mat m, FontType ft) {
        lbl = new JLabel(new ImageIcon(FontMaker.matToBufferedImage(m)));
        JButton btn = new JButton("Save Letter");
        JLabel jtfLbl = new JLabel("Letter(s)");
        JTextField jtf = new JTextField("LETTER");
        jtf.setEditable(true);

        JPanel jp = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.gridheight = 2;
        g.weighty = 1.0;
        g.weightx = 0.9;
        jp.add(lbl, g);
        g.ipadx = 15;
        g.gridx = 1;
        g.gridy = 0;
        g.gridheight = 1;
        g.weighty = 0.5;
        g.weightx = 0.3;
        jp.add(jtfLbl, g);
        g.gridx = 2;
        g.gridy = 0;
        g.weightx = 0.7;
        jp.add(jtf, g);
        g.gridx = 1;
        g.gridy = 1;
        g.gridwidth = 2;
        g.weightx = 0.1;
        jp.add(btn, g);

        QuickDisplay q = this;
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(jtf.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "You must put something in the letter field.", "Alert", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                ft.addOrReplaceLetter(jtf.getText(), m);
                dispatchEvent(new WindowEvent(q, WindowEvent.WINDOW_CLOSING));
            }
        });

        add(jp);
        pack();
        setVisible(true);
    }

}
