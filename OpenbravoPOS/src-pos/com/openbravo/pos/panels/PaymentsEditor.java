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
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.IKeyed;
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
    
    private ComboBoxValModel m_ReasonModel;
    
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
       
        m_ReasonModel = new ComboBoxValModel();
        m_ReasonModel.add(new PaymentReasonNegative("cashout", AppLocal.getIntString("transpayment.cashout")));              
        m_ReasonModel.add(new PaymentReasonPositive("cashin", AppLocal.getIntString("transpayment.cashin")));
        m_jreason.setModel(m_ReasonModel);
        
        jTotal.addEditorKeys(m_jKeys);

        m_jreason.addActionListener(dirty);
        m_jreason.setFocusable(false);
        
        jTotal.addPropertyChangeListener("Text", dirty);
        
        // disable "-" button, makes no sense here
        m_jKeys.setMode(JEditorKeys.MODE_INTEGER_POSITIVE);
        
        // add template buttons
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
        deleteLineButton.setFocusable(false);
        deleteNotesButton.setFocusable(false);
        
        writeValueEOF();

    }
    
    public void writeValueEOF() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal(null, null);
        m_sNotes = null;
        enableButtons(false);
    }  
    
    public void writeValueInsert() {
        m_sId = null;
        m_sPaymentId = null;
        datenew = null;
        setReasonTotal("cashout", null);
        jTotal.activate();
        m_sNotes = null;
        jNotes.setText(m_sNotes);
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
        jTotal.activate();
        m_sNotes = (String) payment[6];
        jNotes.setText(m_sNotes);
        enableButtons(false);
    }
    
    private void enableButtons (boolean enable) {
        m_jreason.setEnabled(enable);
        jTotal.setEnabled(enable);   
        jNotes.setEnabled(enable);
        for (Component b : jPanelTemplates.getComponents()) {
            b.setEnabled(enable);
        }
        deleteLineButton.setEnabled(enable);
        deleteNotesButton.setEnabled(enable);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                jTotal.requestFocus();
            }
        });
    }
    
    public Object createValue() throws BasicException {
        Object[] payment = new Object[7];
        payment[0] = m_sId == null ? UUID.randomUUID().toString() : m_sId;
        payment[1] = m_App.getActiveCashIndex();
        payment[2] = datenew == null ? new Date() : datenew;
        payment[3] = m_sPaymentId == null ? UUID.randomUUID().toString() : m_sPaymentId;
        payment[4] = m_ReasonModel.getSelectedKey();
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();
        Double dtotal = jTotal.getDoubleValue();
        payment[5] = reason == null ? dtotal : reason.addSignum(dtotal);
        m_sNotes = jNotes.getText();
        if (m_sNotes == null || m_sNotes.length()<1) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.PaymentReasonNeeded")));
            return null;
        }
        else {
            payment[6] = m_sNotes.substring(0, m_sNotes.length()-1);
        }
        return payment;
    }
    
    public Component getComponent() {
        return this;
    }
    
    public void refresh() {
    }  
    
    private void setReasonTotal(Object reasonfield, Object totalfield) {
        
        m_ReasonModel.setSelectedKey(reasonfield);
        Double value = (Double)totalfield;
             
        PaymentReason reason = (PaymentReason) m_ReasonModel.getSelectedItem();     
        
        if (reason == null) {
            jTotal.setDoubleValue(value);
        } else {
            jTotal.setDoubleValue(reason.positivize(value));
        }  
    }
    
    private static abstract class PaymentReason implements IKeyed {
        private String m_sKey;
        private String m_sText;
        
        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }
        public Object getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);
        
        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d;
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() < 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d == null ? null : new Double(-d.doubleValue());
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() > 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        m_jreason = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jTotal = new com.openbravo.editor.JEditorCurrency();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jNotes = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        jPanelTemplates = new javax.swing.JPanel();
        jPanelTemplates1 = new javax.swing.JPanel();
        deleteNotesButton = new javax.swing.JButton();
        deleteLineButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.editor.JEditorKeys();

        setLayout(new java.awt.BorderLayout());

        jLabel5.setText(AppLocal.getIntString("label.paymentreason")); // NOI18N

        m_jreason.setFocusable(false);

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
            .addGap(0, 736, Short.MAX_VALUE)
        );
        jPanelTemplatesLayout.setVerticalGroup(
            jPanelTemplatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 186, Short.MAX_VALUE)
        );

        jPanelTemplates1.setFocusable(false);
        jPanelTemplates1.setRequestFocusEnabled(false);

        deleteNotesButton.setText(AppLocal.getIntString("Button.paymentdeletenotes")); // NOI18N
        deleteNotesButton.setFocusable(false);
        deleteNotesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteNotesButtonActionPerformed(evt);
            }
        });

        deleteLineButton.setText(AppLocal.getIntString("Button.paymentdeleteline")); // NOI18N
        deleteLineButton.setFocusable(false);
        deleteLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLineButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelTemplates1Layout = new javax.swing.GroupLayout(jPanelTemplates1);
        jPanelTemplates1.setLayout(jPanelTemplates1Layout);
        jPanelTemplates1Layout.setHorizontalGroup(
            jPanelTemplates1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelTemplates1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(deleteLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteNotesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(407, Short.MAX_VALUE))
        );
        jPanelTemplates1Layout.setVerticalGroup(
            jPanelTemplates1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelTemplates1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanelTemplates1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(deleteLineButton)
                    .addComponent(deleteNotesButton)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelTemplates, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(m_jreason, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTotal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 736, Short.MAX_VALUE))
                .addGap(96, 96, 96))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(167, 167, 167)
                .addComponent(jPanelTemplates1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(m_jreason, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jPanelTemplates, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelTemplates1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void deleteNotesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteNotesButtonActionPerformed
                jNotes.setText("");
    }//GEN-LAST:event_deleteNotesButtonActionPerformed

    private void deleteLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLineButtonActionPerformed
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
    }//GEN-LAST:event_deleteLineButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteLineButton;
    private javax.swing.JButton deleteNotesButton;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextArea jNotes;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelTemplates;
    private javax.swing.JPanel jPanelTemplates1;
    private javax.swing.JScrollPane jScrollPane1;
    private com.openbravo.editor.JEditorCurrency jTotal;
    private com.openbravo.editor.JEditorKeys m_jKeys;
    private javax.swing.JComboBox m_jreason;
    // End of variables declaration//GEN-END:variables
    
}
