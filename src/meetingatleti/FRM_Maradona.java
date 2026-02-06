package meetingatleti;

import javax.swing.*;
import java.awt.*;

/**
 * @author diego
 */
public class FRM_Maradona extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FRM_Maradona.class.getName());

    // Icone originali
    private ImageIcon ImageMaradona_1 = new ImageIcon("Mara_1.png");
    private ImageIcon ImageMaradona_2 = new ImageIcon("Mara_2.png");
    private boolean toggle = true; // Per switchare tra le due foto

    public FRM_Maradona() {
        initComponents();
        // Setup iniziale della label
        lbl_Mara.setText("");
        lbl_Mara.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        setLocationRelativeTo(null); // Centra la finestra
    }

    // Metodo di scaling (preso dal tuo esempio FOFA)
    public static ImageIcon scaleIcon(ImageIcon icon, int w, int h) {
        if (w <= 0 || h <= 0) return icon;
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
                               


    // End of variables declaration                   
}
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar1 = new javax.swing.JProgressBar();
        jButton1 = new javax.swing.JButton();
        lbl_Mara = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        lbl_Mara.setText("jLabel1");
        lbl_Mara.setName("lbl_Mara"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(142, 142, 142)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(138, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lbl_Mara, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(61, Short.MAX_VALUE)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_Mara, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// Effetto barra di caricamento
        jProgressBar1.setValue(100);
        
        // Logica per mostrare/cambiare immagine
        if (toggle) {
            lbl_Mara.setIcon(scaleIcon(ImageMaradona_1, lbl_Mara.getWidth(), lbl_Mara.getHeight()));
        } else {
            lbl_Mara.setIcon(scaleIcon(ImageMaradona_2, lbl_Mara.getWidth(), lbl_Mara.getHeight()));
        }
        toggle = !toggle; // Inverte per il prossimo click
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(() -> {
            new FRM_Maradona().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JLabel lbl_Mara;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JLabel lblTitolo;
    private javax.swing.JPanel pnlCentrale;
    private javax.swing.JPanel pnlSud;
    
    public static ImageIcon scaleIconKeepRatio(ImageIcon icon, int maxW, int maxH) {
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();

        double ratio = Math.min((double) maxW / w, (double) maxH / h);

        int newW = (int) (w * ratio);
        int newH = (int) (h * ratio);

        Image img = icon.getImage().getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }
}
