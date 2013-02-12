//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.UUID;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.editor.JEditorKeys;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author adrianromero
 */
public class PaymentsEditor extends javax.swing.JPanel implements EditorRecord {
    
    private boolean m_negativePayment = false;
    
    private String m_sId;
    private String m_sPaymentId;
    private Date datenew;
    private String m_sNotes;
    protected DataLogicSystem dlSystem;
    
    private AppView m_App;
    
    /** Creates new form JPanelPayments */
    public PaymentsEditor(AppView oApp, DirtyManager dirty) {
        
        m_App = oApp;
        
        initComponents();
       
        jTotal.addEditorKeys(m_jKeys);

        jPaymentIn.addActionListener(dirty);
        jPaymentOut.addActionListener(dirty);
        
        jTotal.addPropertyChangeListener("Text", dirty);
        jNotes.addPropertyChangeListener("Text", dirty);
        
        // disable "-" button, makes no sense here
        m_jKeys.setMode(JEditorKeys.MODE_INTEGER_POSITIVE);
        
        // add template buttons with texts from resources
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        String[] templates = dlSystem.getResourceAsText("payments.templates").split("\n");
        jPanelTemplates.setLayout(new GridLayout(0, 2,6, 6));
        for (String template:templates) {
            JButton button = new JButton();
            button.setText(template);
            button.setFocusPainted(false);
            button.setFocusable(false);
            button.setRequestFocusEnabled(false);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setVerticalTextPosition(SwingConstants.BOTTOM);
            button.setMargin(new Insets(2, 2, 2, 2));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object button = e.getSource();
                    if (jNotes.isEnabled() && button instanceof JButton) {
                        String text = jNotes.getText();
                        if (!text.contains(((JButton)button).getText())) {
                            text += ((JButton)button).getText() + "\n";
                            jNotes.setText(text);
                        }
                    }
                }
            });
            jPanelTemplates.add(button);
        }
        
        jPanelTemplates.setVisible(true);
        
        jNotes.setFocusable(false);
        jDeleteLineButton.setFocusable(false);
        jDeleteNotesButton.setFocusable(false);
        
        writeValueEOF();

    }
    
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_sNotes = null;
        jNotes.setText(null);
        enableButtons(false);
    }  
    
    public void writeValueInsert() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_sNotes = null;
        jNotes.setText(null);
        enableButtons(true);
    }

    public void writeValueDelete(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_sNotes = (String) payment[6];
        jNotes.setText(m_sNotes);
        enableButtons(false);
    }
    
    public void writeValueEdit(Object value) {
        Object[] payment = (Object[]) value;
        m_sId = (String) payment[0];
        datenew = (Date) payment[2];
        m_sPaymentId = (String) payment[3];
        setReasonTotal(payment[4], payment[5]);
        m_sNotes = (String) payment[6];
        jNotes.setText(m_sNotes);
        enableButtons(false);
    }
    
    private void enableButtons (boolean enable) {
        jTotal.setEnabled(enable);   
        jTotal.activate();
        jNotes.setEnabled(enable);
        for (Component b : jPanelTemplates.getComponents()) {
            b.setEnabled(enable);
        }
        jDeleteLineButton.setEnabled(enable);
        jDeleteNotesButton.setEnabled(enable);
    }
    
    public Object createValue() throws BasicException {
        Object[] payment = new Object[7];
        payment[0] = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        payment[1] = m_App.getActiveCashIndex();
        payment[2] = datenew == null ? new Date() : datenew;
        payment[3] = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        payment[4] = m_negativePayment ? "cashout" : "cashin";
        Double dtotal = jTotal.getDoubleValue();
        if (dtotal!=null) 
            dtotal /= 100;
        payment[5] = m_negativePayment ? -dtotal : dtotal;
        payment[6] = m_sNotes.substring(0, m_sNotes.length()-1);
        m_App.openDrawer();
        return payment;
    }
    
    public Component getComponent() {
        return this;
    }
    
    public void refresh() {
    }  
    
    private void setReasonTotal(Object reasonfield, Object totalfield) {
        
        if (reasonfield != null && !reasonfield.toString().isEmpty()) {
            jLabelReason.setText(AppLocal.getIntString("transpayment." + reasonfield));
        }
        else {
            jLabelReason.setText("");
        }
        Double value = (Double)totalfield;
        if (value!=null) {
            value *= 100;
            jTotal.setDoubleValue(Math.abs(value));
        }
        else {
            jTotal.setDoubleValue(null);
        }
    }
  
    /**
     * Check if notes are filled and put them into member, otherwise display message.
     * @return true - notes are OK
     */
    private boolean checkNotes() {
        m_sNotes = jNotes.getText();
        if (m_sNotes == null || m_sNotes.length()<1) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.PaymentReasonNeeded")));
            return false;
        }
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTotal = new com.openbravo.editor.JEditorCurrency();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jNotes = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jPanelTemplates = new javax.swing.JPanel();
        jLabelReason = new javax.swing.JLabel();
        jDeleteLineButton = new javax.swing.JButton();
        jDeleteNotesButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();
        jPaymentOut = new javax.swing.JButton();
        jPaymentIn = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jLabel3.setText(AppLocal.getIntString("label.paymenttotal")); // NOI18N

        jLabel6.setText(AppLocal.getIntString("label.paymentnotes")); // NOI18N

        jNotes.setColumns(20);
        jNotes.setRows(5);
        jScrollPane1.setViewportView(jNotes);

        jLabel7.setText(AppLocal.getIntString("label.paymenttemplates")); // NOI18N

        jPanelTemplates.setFocusable(false);
        jPanelTemplates.setRequestFocusEnabled(false);

        javax.swing.GroupLayout jPanelTemplatesLayout = new javax.swing.GroupLayout(jPanelTemplates);
        jPanelTemplates.setLayout(jPanelTemplatesLayout);
        jPanelTemplatesLayout.setHorizontalGroup(
            jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 382, Short.MAX_VALUE)
        );
        jPanelTemplatesLayout.setVerticalGroup(
            jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 186, Short.MAX_VALUE)
        );

        jDeleteLineButton.setText(AppLocal.getIntString("button.paymentdeleteline")); // NOI18N
        jDeleteLineButton.setFocusable(false);
        jDeleteLineButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jDeleteLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteLineButtonActionPerformed(evt);
            }
        });

        jDeleteNotesButton.setText(AppLocal.getIntString("button.paymentdeletenotes")); // NOI18N
        jDeleteNotesButton.setFocusable(false);
        jDeleteNotesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jDeleteNotesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addComponent(jLabelReason, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(95, 95, 95)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDeleteNotesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jDeleteLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanelTemplates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(338, 338, 338))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabelReason, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDeleteLineButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDeleteNotesButton)
                .addGap(16, 16, 16)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jPanelTemplates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });

        jPaymentOut.setText(AppLocal.getIntString("transpayment.cashout")); // NOI18N
        jPaymentOut.setFocusable(false);
        jPaymentOut.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jPaymentOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPaymentOutActionPerformed(evt);
            }
        });

        jPaymentIn.setText(AppLocal.getIntString("transpayment.cashin")); // NOI18N
        jPaymentIn.setFocusable(false);
        jPaymentIn.setMargin(new java.awt.Insets(2, 0, 2, 0));
        jPaymentIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jPaymentInActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPaymentOut, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPaymentIn, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(m_jKeys, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jPaymentOut, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPaymentIn, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(137, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void jDeleteNotesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteNotesButtonActionPerformed
                jNotes.setText("");
    }//GEN-LAST:event_jDeleteNotesButtonActionPerformed

    private void jDeleteLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDeleteLineButtonActionPerformed
       String newText = "";
        String[] lines = jNotes.getText().split("\n");
        int i=0;
        // delete last line
        for (String line:lines) {
            i++;
            if (i < lines.length)
                newText += line;
            else break;
            newText += "\n";
        }
        jNotes.setText(newText);
    }//GEN-LAST:event_jDeleteLineButtonActionPerformed

    private void jPaymentOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPaymentOutActionPerformed
        if (!checkNotes()) return;
        m_negativePayment = true;
        ((JPanelTable)getParent().getParent()).save();
    }//GEN-LAST:event_jPaymentOutActionPerformed

    private void jPaymentInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPaymentInActionPerformed
        if (!checkNotes()) return;
        m_negativePayment = false;
        ((JPanelTable)getParent().getParent()).save();
    }//GEN-LAST:event_jPaymentInActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jDeleteLineButton;
    private javax.swing.JButton jDeleteNotesButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabelReason;
    private javax.swing.JTextArea jNotes;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelTemplates;
    private javax.swing.JButton jPaymentIn;
    private javax.swing.JButton jPaymentOut;
    private javax.swing.JScrollPane jScrollPane1;
    private com.openbravo.editor.JEditorCurrency jTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    // End of variables declaration//GEN-END:variables
    
}
