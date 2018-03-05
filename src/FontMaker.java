import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;

public class FontMaker extends JFrame implements ClipboardOwner {

    public static JButton fontMgrBtn = new JButton("Font Manager");
    public static JButton updateBtn = new JButton("Update Text");
    public static JButton copyTextBtn = new JButton("Copy to Clipboard");
    public static JTextArea editorArea = new JTextArea(400,100);

    public Mat text = new Mat(400, 400, CvType.CV_8UC3, new Scalar(255, 0, 255));
    public static FontType loadedFont = null;


    public static void main(String args[]) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        new FontMaker();
    }

    public static void setFont(FontType ft) {
        loadedFont = ft;
    }

    public FontMaker() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        editorArea.setSize(400, 200);
        editorArea.setLineWrap(true);
        editorArea.setEditable(true);
        editorArea.setMargin(new Insets(0, 0, 12, 12));
        JScrollPane jsp = new JScrollPane(editorArea);
        jsp.add(editorArea);
        jsp.setPreferredSize(new Dimension(400, 200));
        jsp.setMinimumSize(new Dimension(400, 20));
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        fontMgrBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                new FontManager(loadedFont);
            }
        });

        updateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if(loadedFont == null) {
                    JOptionPane.showMessageDialog(null, "No font loaded! Use the Font Manager to load or create one.", "Alert", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                text = generateTextImg(editorArea.getText());

                JLabel lbl = new JLabel(new ImageIcon(matToBufferedImage(text)));
                JFrame j = new JFrame();
                j.add(lbl);
                j.pack();
                //j.setDefaultCloseOperation(EXIT_ON_CLOSE);
                j.setVisible(true);
            }
        });

        FontMaker f = this;
        copyTextBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                TransferableImage trans = new TransferableImage(matToBufferedImage(text));
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                c.setContents(trans, f);
            }
        });



        gbc.ipadx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        mainPanel.add(fontMgrBtn, gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        mainPanel.add(updateBtn, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        mainPanel.add(copyTextBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.PAGE_END;
        mainPanel.add(jsp, gbc);
        add(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("Lost Ownership of Clipboard");
    }

    //Utility methods
    public static BufferedImage matToBufferedImage(Mat frame) {
        //Mat() to BufferedImage
        int type = 0;
        if (frame.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else if (frame.channels() == 3) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage image = new BufferedImage(frame.width(), frame.height(), type);
        WritableRaster raster = image.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        byte[] data = dataBuffer.getData();
        frame.get(0, 0, data);
        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage img) {
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat m = new Mat(480, 640, CvType.CV_8UC3);
        m.put(0, 0, pixels);
        return m;
    }


    //https://coderanch.com/t/333565/java/BufferedImage-System-Clipboard
    private class TransferableImage implements Transferable {

        Image i;

        public TransferableImage( Image i ) {
            this.i = i;
        }

        public Object getTransferData( DataFlavor flavor )
                throws UnsupportedFlavorException, IOException {
            if ( flavor.equals( DataFlavor.imageFlavor ) && i != null ) {
                return i;
            }
            else {
                throw new UnsupportedFlavorException( flavor );
            }
        }

        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[ 1 ];
            flavors[ 0 ] = DataFlavor.imageFlavor;
            return flavors;
        }

        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for ( int i = 0; i < flavors.length; i++ ) {
                if ( flavor.equals( flavors[ i ] ) ) {
                    return true;
                }
            }

            return false;
        }
    }

    public Mat generateTextImg(String s) {
        int indices[] = loadedFont.mapIndicesToString(s);
        int totalWidth = 0;
        int maxHeight = 0;
        for(int i : indices) {
            totalWidth += loadedFont.getLetterImg(i).cols();
            maxHeight = loadedFont.getLetterImg(i).rows() > maxHeight ? loadedFont.getLetterImg(i).rows() : maxHeight;
        }
        Mat retMat = new Mat(maxHeight, totalWidth, loadedFont.getLetterImg(indices[0]).type(), Scalar.all(0));
        int widthIndex = 0;
        for(int i : indices) {
            Mat curMat = loadedFont.getLetterImg(i);
            curMat.copyTo(retMat.submat(0, curMat.rows(), widthIndex, widthIndex+curMat.cols()));
            widthIndex += curMat.cols();
        }
        return retMat;
    }


}

