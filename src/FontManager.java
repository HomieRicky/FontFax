import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

public class FontManager extends JFrame {

    JTextField letterField;
    JTextField angleField;
    JTextField sizeField;
    JTextField xToYField;
    JTextField toleranceField;

    JButton loadImageBtn;
    JButton loadFontBtn;
    JButton saveFontBtn;
    JButton saveLetterBtn;
    JButton newFontBtn;
    JButton eyedropperBtn;

    JLabel lbl;

    static Mat image = new Mat(400, 400, CvType.CV_8UC4, new Scalar(255, 255, 0, 128));
    static float xToY = 1.0f;
    static int width = 32;
    static int angle = 0;
    static int padding = (int) Math.sqrt((width*width)+((width*xToY)*(width*xToY))) + 5;
    static Mat paddedImg = new Mat(image.rows()+padding+padding, image.cols()+padding+padding, image.type());
    static RotatedRect roiRect = new RotatedRect();
    static Mat roi = new Mat();
    static Mat displayImage = new Mat(image.rows(), image.cols(), image.type(), Scalar.all(0));
    static Point boxPos = new Point(100, 100);
    static int tolerance = 5;
    static Scalar filterColour = new Scalar(0, 0, 0);

    String usedString = "";

    FontType fontType;

    public FontManager(FontType font) {
        roiRect.angle = angle;
        roiRect.center = new Point(0, 0);
        roiRect.size = new Size(width, (int) width*xToY);

        fontType = font;
        JPanel mainPanel = new JPanel(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.ipadx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        letterField = new JTextField("letter");
        letterField.setEditable(true);
        mainPanel.add(letterField, g);
        g.gridx = 0;
        g.gridy = 1;
        angleField = new JTextField("angle");
        angleField.setEditable(true);
        mainPanel.add(angleField, g);
        g.gridx = 0;
        g.gridy = 2;
        sizeField = new JTextField("size");
        sizeField.setEditable(true);
        mainPanel.add(sizeField, g);
        g.gridx = 0;
        g.gridy = 3;
        xToYField = new JTextField("x to y ratio");
        xToYField.setEditable(true);
        mainPanel.add(xToYField, g);
        g.gridx = 0;
        g.gridy = 4;
        toleranceField = new JTextField("tolerance");
        toleranceField.setEditable(true);
        mainPanel.add(toleranceField, g);
        g.gridx = 0;
        g.gridy = 5;
        loadImageBtn = new JButton("Load Image");
        mainPanel.add(loadImageBtn, g);
        g.gridx = 0;
        g.gridy = 6;
        loadFontBtn = new JButton("Load Font");
        mainPanel.add(loadFontBtn, g);
        g.gridx = 0;
        g.gridy = 7;
        saveFontBtn = new JButton("Save Font");
        mainPanel.add(saveFontBtn, g);
        g.gridx = 0;
        g.gridy = 8;
        saveLetterBtn = new JButton("Save Letter");
        mainPanel.add(saveLetterBtn, g);
        g.gridx = 0;
        g.gridy = 9;
        newFontBtn = new JButton("New Font");
        mainPanel.add(newFontBtn, g);
        g.gridx = 1;
        g.gridy = 0;
        g.gridheight = 10;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.VERTICAL;
        //g.anchor = GridBagConstraints.PAGE_END;
        updateStuff(xToY, width, angle);
        lbl = new JLabel(new ImageIcon(display4ChanImg(displayImage)));
        mainPanel.add(lbl, g);

        angleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String s = angleField.getText();
                try {
                    int i = (int) Double.parseDouble(s);
                    updateStuff(xToY, width, i);
                    lbl.setIcon(new ImageIcon(display4ChanImg(displayImage)));
                } catch (NumberFormatException e1) {}
                super.keyTyped(e);
            }
        });

        sizeField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String s = sizeField.getText();
                try {
                    int i = (int) Double.parseDouble(s);
                    updateStuff(xToY, i, angle);
                    lbl.setIcon(new ImageIcon(display4ChanImg(displayImage)));
                } catch (NumberFormatException e1) {}
                super.keyTyped(e);
            }
        });

        xToYField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String s = xToYField.getText();
                try {
                    float i = (float) Double.parseDouble(s);
                    updateStuff(i, width, angle);
                    lbl.setIcon(new ImageIcon(display4ChanImg(displayImage)));
                } catch (NumberFormatException e1) {}
                super.keyTyped(e);
            }
        });

        toleranceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String s = toleranceField.getText();
                try {
                    int i = (int) Double.parseDouble(s);
                    tolerance = i;
                } catch (NumberFormatException e1) {}
                super.keyTyped(e);
            }
        });

        loadImageBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jfc = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & PNG", "jpg", "png");
                jfc.setFileFilter(filter);
                int ret = jfc.showOpenDialog(getParent());
                if(ret == JFileChooser.APPROVE_OPTION) {
                    image = Imgcodecs.imread(jfc.getSelectedFile().getAbsolutePath());
                    updateStuff(xToY, width, angle);
                    lbl.setIcon(new ImageIcon(display4ChanImg(displayImage)));
                    pack();
                }
            }
        });

        saveLetterBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                getFilteredLetter();
            }
        });

        lbl.addMouseListener(new MouseAdapter() {
            private boolean movingBox = false;
            private boolean mousePressed = false;

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mousePressed = true;
                if(inBox(new Point(e.getX(), e.getY()))) {
                    movingBox = true;
                    lbl.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(movingBox && mousePressed) {
                    boxPos = new Point(e.getX() + padding, e.getY() + padding);
                    updateStuff(xToY, width, angle);
                    lbl.setIcon(new ImageIcon(display4ChanImg(displayImage)));
                    movingBox = false;
                }
                mousePressed = false;
                lbl.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                mousePressed = false;
                movingBox = false;
                lbl.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        add(mainPanel);
        pack();
        setVisible(true);


    }


    private BufferedImage display4ChanImg(Mat in) {
        Mat m = new Mat(in.rows(), in.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(in, m, Imgproc.COLOR_BGRA2BGR);
        return FontMaker.matToBufferedImage(m);
    }

    private static void updateStuff(float ratio, int size, int ang) {
        xToY = ratio;
        width = size;
        angle = ang;
        padding = (int) Math.sqrt((width*width)+((width*xToY)*(width*xToY))) + 5;
        paddedImg = new Mat(image.rows()+padding+padding, image.cols()+padding+padding, image.type(), Scalar.all(0));
        image.copyTo(paddedImg.submat(new Rect(padding, padding, image.cols(), image.rows())));
        roiRect = new RotatedRect(boxPos, new Size(width, (int) (width*xToY)), angle);

        Mat paddedCopy = paddedImg.clone();
        Point points[] = new Point[4];
        roiRect.points(points);
        for(int i = 0; i < 4; i++) Imgproc.line(paddedCopy, points[i], points[(i+1)%4], Scalar.all(128), 2);

        displayImage = paddedCopy.submat(new Rect(padding, padding, image.cols(), image.rows()));

    }

    public static boolean inBox(Point p) {
        Point points[] = new Point[4];
        roiRect.points(points);
        for(int i = 0; i < 4; i++) if(Line2D.linesIntersect(p.x, p.y, roiRect.center.x-padding, roiRect.center.y-padding, points[i].x-padding, points[i].y-padding, points[(i+1)%4].x-padding, points[(i+1)%4].y-padding)) return false;
        return true;
    }

    public static Mat getFilteredLetter() {
        Mat m = new Mat();
        Imgproc.cvtColor(paddedImg, m, Imgproc.COLOR_BGRA2BGR);
        //System.out.println(m.toString());

        //https://www.pyimagesearch.com/2017/01/02/rotate-images-correctly-with-opencv-and-python/
        Mat M = Imgproc.getRotationMatrix2D(new Point(boxPos.x, boxPos.y), angle, 1.0);
        Mat rotatedBigImg = new Mat();
        double cos = Math.abs(M.get(0, 0)[0]);
        double sin = Math.abs(M.get(0, 1)[0]);
        int nW = (int) ((m.rows() * sin) + (m.cols() * cos));
        int nH = (int) ((m.rows() * cos) + (m.cols() * sin));
        M.put(0, 2, M.get(0, 2)[0] + (nW / 2) - (m.cols() / 2));
        M.put(1, 2, M.get(1, 2)[0] + (nH / 2) - (m.rows() / 2));
        Imgproc.warpAffine(m, rotatedBigImg, M, new Size(nW, nH));
        //get new poly location

        int midX = m.cols()/2;
        int midY = m.rows()/2;
        int bigMidX = rotatedBigImg.cols()/2;
        int bigMidY = rotatedBigImg.rows()/2;
        double posX = boxPos.x;
        double posY = boxPos.y;

        int xDisp = (int) (bigMidX + (posX-midX));
        int yDisp = (int) (bigMidY + (posY-midY));

        Rect roi = new Rect(new Point(xDisp-width/2, yDisp-(width*xToY)/2), new Size(width, width*xToY));
        Mat maskMat = new Mat(rotatedBigImg.rows(), rotatedBigImg.cols(), CvType.CV_8UC1, Scalar.all(0));
        maskMat.submat(roi).setTo(Scalar.all(Imgproc.GC_PR_FGD));
        //Imgproc.cvtColor(roiMat, roiMat, Imgproc.COLOR_BGR2GRAY);

        Mat fgdMdl = new Mat();
        Mat bkgMdl = new Mat();
        Imgproc.grabCut(rotatedBigImg, maskMat, roi, fgdMdl, bkgMdl, tolerance, Imgproc.GC_INIT_WITH_MASK);
        byte data[] = new byte[maskMat.rows()*maskMat.cols()];
        Core.multiply(maskMat, Scalar.all(32), maskMat);
        maskMat.get(0, 0, data);
        for(int i = 0; i < data.length; i++) {
            if(data[i]>0x40) {
                data[i]=(byte)0xff;
            } else {
                data[i]=0x00;
            }
        }
        maskMat.put(0, 0, data);
        Mat roiMat = maskMat.submat(roi);
        new QuickDisplay(roiMat);
        return new Mat();
    }
}
