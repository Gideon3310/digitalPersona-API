package com.digitalpersona.onetouch.views;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.conection.SQLServerConection;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import com.genexus.ModelContext;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 *
 * @author janez - jamserv@gmail.com 319 - 588.5982
 */
public class FormSercurityAuth extends javax.swing.JFrame {

    public DPFPCapture verifyCapturer = DPFPGlobal.getCaptureFactory().createCapture();
    private SQLServerConection serverConection = SQLServerConection.getInstance(this);
    private DPFPVerification verificator = DPFPGlobal.getVerificationFactory().createVerification();
    private HashMap<String, String> hashProperties = new HashMap<String, String>();

    /**
     * Creates new form FormSercurityAuth
     */
    public FormSercurityAuth() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Fingerprint Verification | Artico Security");
        setResizable(false);

        doEvents();
        doInit();
    }

    /**
     *
     * @param remoteHandle
     * @param context
     */
    public FormSercurityAuth(int remoteHandle, ModelContext context) {
    }

    /**
     *
     * @param param
     */
    public void execute(String[] param) {
        initComponents();
        this.setLocationRelativeTo(null);
        this.setTitle("Fingerprint Verification | Artico Security");
        setResizable(false);

        doEvents();
        idDocumentVerify.setText(param[0]);

        serverConection.loadPropertiesWithParams(
                param[1],
                param[2],
                param[3],
                param[4],
                param[5]);

        doInit();
    }

    public void doInit() {
        verifyCapturer.startCapture();
        verifyCapturer.addDataListener(new DPFPDataAdapter() {
            @Override
            public void dataAcquired(final DPFPDataEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        makeReportVerify("...fingerprint was captured.");
                        if (idDocumentVerify.getText() == null || "".equals(idDocumentVerify.getText())) {
                            makeReportVerify("...ERROR::Field Document Id is Mandatory");
                        }
                        drawPictureVerify(convertSampleToBitmap(e.getSample()));
                        processVerify(e.getSample());
                    }
                });
            }
        });

        /**
         * run the app
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FormSercurityAuth.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FormSercurityAuth.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FormSercurityAuth.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FormSercurityAuth.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FormSercurityAuth().setVisible(true);
            }
        });
    }

    private void doEvents() {
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    protected Image convertSampleToBitmap(DPFPSample sample) {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }

    public void makeReportVerify(String string) {
        logVerify.append(string + "\n");
    }

    protected DPFPFeatureSet extractFeatures(DPFPSample sample, DPFPDataPurpose purpose) {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }

    public void drawPictureVerify(Image image) {
        picture_verify.setIcon(new ImageIcon(image.getScaledInstance(picture_verify.getWidth(), picture_verify.getHeight(), Image.SCALE_DEFAULT)));
    }

    protected void processVerify(DPFPSample sample) {
        DPFPTemplate temp2 = DPFPGlobal.getTemplateFactory().createTemplate();
        try {
            temp2.deserialize(getClientSignature(new Long(idDocumentVerify.getText())));
            DPFPFeatureSet features = extractFeatures(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);

            if (features != null) {
                DPFPVerificationResult result = verificator.verify(features, temp2);
                if (result.isVerified()) {
                    makeReportVerify("...fingerprint was VERIFIED.");
                } else {
                    makeReportVerify("...fingerprint was NOT VERIFIED.");
                }
            }
        } catch (IllegalArgumentException argumentException) {
        }
    }

    protected byte[] getClientSignature(long id) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = serverConection.getSQLConection();

            preparedStatement = connection.prepareStatement("SELECT blobStream FROM FingerPrint WHERE id = ?");
            preparedStatement.setLong(1, id);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();

            return resultSet.getBytes("blobStream");
        } catch (SQLException ex) {
            makeReportVerify("User NOT registered.");

            int dialogResult = JOptionPane.showConfirmDialog(null, "NOT VERIFIED. \n\n You want to register this user?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                EnrollmentView enrollmentView = new EnrollmentView(this);
                enrollmentView.setVisible(true);
                enrollmentView.setDinamycIdentify(idDocumentVerify.getText());
                verifyCapturer.stopCapture();
            }
        } finally {
            try {
                resultSet.close();
                preparedStatement.close();
                connection.close();
            } catch (SQLException ex) {
                makeReportVerify("ERROR:" + ex.getLocalizedMessage());
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel5 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        idDocumentVerify = new javax.swing.JTextField();
        picture_verify = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        logVerify = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Verify", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 15))); // NOI18N

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel6.setText("Document Id");

        idDocumentVerify.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        idDocumentVerify.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                idDocumentVerifyKeyPressed(evt);
            }
        });

        logVerify.setColumns(20);
        logVerify.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        logVerify.setRows(5);
        jScrollPane3.setViewportView(logVerify);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addComponent(idDocumentVerify, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 55, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addGap(18, 18, 18)
                .addComponent(picture_verify, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(idDocumentVerify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3)
                .addContainerGap())
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(picture_verify, javax.swing.GroupLayout.PREFERRED_SIZE, 356, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 72, Short.MAX_VALUE))
        );

        jMenu1.setText("Actions");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Add");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);
        jMenu1.add(jSeparator2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        jMenuItem3.setText("Exit");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        EnrollmentView enrollmentView = new EnrollmentView(this);
        enrollmentView.setVisible(true);
        verifyCapturer.stopCapture();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void idDocumentVerifyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idDocumentVerifyKeyPressed
    }//GEN-LAST:event_idDocumentVerifyKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField idDocumentVerify;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JTextArea logVerify;
    private javax.swing.JLabel picture_verify;
    // End of variables declaration//GEN-END:variables

}
