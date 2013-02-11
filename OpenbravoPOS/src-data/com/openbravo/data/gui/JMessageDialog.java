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

package com.openbravo.data.gui;

import com.openbravo.pos.forms.AppLocal;

import java.awt.*;
import javax.swing.*;
import com.openbravo.data.loader.LocalRes;

/**
 *
 * @author  adrian
 */
public class JMessageDialog extends javax.swing.JDialog {
    
    public static final String NO_REPLACING = "#no_replacing#";

    /** Creates new form JMessageDialog */
    private JMessageDialog(java.awt.Frame parent, boolean modal) {        
        super(parent, modal);       
    }
    /** Creates new form JMessageDialog */
    private JMessageDialog(java.awt.Dialog parent, boolean modal) {        
        super(parent, modal);       
    }
    
    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }
        

    /**
     * Change field/table name in SQL exception to resource text if known field.
     * @param str SQL exception or extracted field/table name
     * @return key name or unchanged if not found
     */
    private static String getLabelForDbField(String str) {
        // fields
        if (str.contains("pricebuy")) return AppLocal.getIntString("label.prodpricebuy");
        if (str.contains("pricesell")) return AppLocal.getIntString("label.prodpricesell");
        if (str.contains("taxcat")) return AppLocal.getIntString("label.taxcategory");
        if (str.contains("category"))  return AppLocal.getIntString("label.prodcategory");
        if (str.contains("name"))  return AppLocal.getIntString("label.prodname");
        if (str.contains("reference"))  return AppLocal.getIntString("label.prodref");
        if (str.contains("code"))  return AppLocal.getIntString("label.prodbarcode");
        // tables
        if (str.equals("products"))  return AppLocal.getIntString("Menu.Products");
        if (str.equals("categories"))  return AppLocal.getIntString("label.prodcategory");
        return str;
    }
    
    /**
     * Find field name in SQL exception between brackets and before "=" sign.
     * Change to resource text if possible.
     * @param str SQL exception
     * @return key name
     */
    private static String getDbFieldName (String str) {
        str = str.replaceAll(".*detail: ((auf )*schlüssel »*|key )\\(",""); //detail: schlüssel »(code)= <<<vs>>> detail: auf schlüssel (id)
        return getLabelForDbField( str.replaceAll("\\)=\\(.*","") );
    }
    
    /**
     * Find field value in SQL exception between brackets and after "=" sign.
     * @param str SQL exception
     * @return field value
     */
    private static String getDbValue(String str) {
        return ( str.replaceAll(".*\\)=\\(","").replaceAll("\\).*","") );
    }

    /**
     * Make known exceptions to more convenient error messages from text 
     * resources, e.g. DB SQL exceptions.
     * @param str exception text
     * @return error specific text
     */
    private static String replaceErrorMessage(String msg) {
        if (msg == null) {
            return "";
        }
        String ret = NO_REPLACING;
        String str = msg.toLowerCase();
        
        //str = "org.postgresql.util.PSQLException: FEHLER: Aktualisieren oder Löschen in Tabelle »categories« verletzt Fremdschlüssel-Constraint »products_fk_1« von Tabelle »products«   Detail: Auf Schlüssel (id)=(9a4e0f4f-d9cc-45a5-943f-ee6495d86251) wird noch aus Tabelle »products« verwiesen.".toLowerCase();
        //str = "com.openbravo.basic.BasicException: org.postgresql.util.PSQLException: FEHLER: NULL-Wert in Spalte »pricebuy« verletzt Not-Null-Constraint".toLowerCase();

        
        while (str.contains("\n")) {str = str.replace("\n", "");}
        
        if (str.contains("sqlexception")) {
        
            // SQL constraints, check for locale-independent keywords
            if (str.contains("constraint")) {
                // not null constraint
                if (str.contains("null")) {
                    ret = AppLocal.getIntString("message.CannotCreateEmpty", getDbFieldName(str) );
                }
                // unique key constraint//
                else if (str.contains("unique")) {
                    ret = AppLocal.getIntString("message.CannotCreateKey", getDbFieldName(str), getDbValue(str));                    
                }   
                // all the rest assumed as foreign key constraint problem = cannot delete (no locale-independent keyword found)
                else {
                    String tab0 = getLabelForDbField( str.replaceAll(".*(aktualisieren oder löschen in tabelle »|update or delete on table \")", "").replaceAll("[«\"].*","") );
                    String tab1 = getLabelForDbField( str.replaceAll(".*(is still referenced from table \"|wird noch aus tabelle »)","").replaceAll("(« verwiesen.|\"\\.)$","") );
                    ret = AppLocal.getIntString("message.CannotDelete", getDbFieldName(str), getDbValue(str), tab0, tab1);
                }
            }
        }
        
        return ret;
    }
    
    /*
     * replaces in message dialog 'myMsg' if we recognize text of given error (in replaceErrorMessage())
     * 
     */
    private static JMessageDialog replaceErrorsWithKnownTemplates(JMessageDialog myMsg, MessageInf inf) {                
        String windowText = NO_REPLACING;
        
        if (inf.getCause() != null) {
            if (inf.getCause() instanceof Throwable) {
                Throwable t = (Throwable) inf.getCause();
                
                windowText += "\n" + t.getMessage();
                windowText = replaceErrorMessage(t.getMessage());                
            }
        }
        
        if (windowText.equals(NO_REPLACING)) return myMsg;
        
        myMsg.jtxtException.setVisible(false);
        myMsg.setTitle(AppLocal.getIntString("message.Error"));
        myMsg.jlblMessage.setText(windowText);
        myMsg.jscrException.setVisible(false);

        return myMsg;
    }
    
    public static void showMessage(Component parent, MessageInf inf) {
        Window window = getWindow(parent);      
        
        JMessageDialog myMsg;
        if (window instanceof Frame) { 
            myMsg = new JMessageDialog((Frame) window, true);
        } else {
            myMsg = new JMessageDialog((Dialog) window, true);
        }
        
        myMsg.initComponents();
        myMsg.applyComponentOrientation(parent.getComponentOrientation());
        myMsg.jscrException.setVisible(false);        
        myMsg.getRootPane().setDefaultButton(myMsg.jcmdOK);
        
        myMsg.jlblIcon.setIcon(inf.getSignalWordIcon());
        myMsg.jlblErrorCode.setText(inf.getErrorCodeMsg());
        myMsg.jlblErrorCode.setVisible(false);
        myMsg.jlblMessage.setText("<html>" + inf.getMessageMsg());
                
        // Capturamos el texto de la excepcion...
        if (inf.getCause() == null) {
            myMsg.jtxtException.setText(null);
        } else {            
            StringBuilder sb = new StringBuilder(); 
            
            if (inf.getCause() instanceof Throwable) {
                Throwable t = (Throwable) inf.getCause();
                while (t != null) {
                    sb.append(t.getClass().getName());
                    sb.append(": \n");
                    sb.append(t.getMessage());
                    sb.append("\n\n");
                    t = t.getCause();
                }
            } else if (inf.getCause() instanceof Throwable[]) {
                Throwable[] m_aExceptions = (Throwable[]) inf.getCause();
                for (int i = 0; i < m_aExceptions.length; i++) {
                    sb.append(m_aExceptions[i].getClass().getName());
                    sb.append(": \n");
                    sb.append(m_aExceptions[i].getMessage());
                    sb.append("\n\n");
                }             
            } else if (inf.getCause() instanceof Object[]) {
                Object [] m_aObjects = (Object []) inf.getCause();
                for (int i = 0; i < m_aObjects.length; i++) {
                    sb.append(m_aObjects[i].toString());
                    sb.append("\n\n");
                }             
            } else if (inf.getCause() instanceof String) {
                sb.append(inf.getCause().toString());
            } else {
                sb.append(inf.getCause().getClass().getName());
                sb.append(": \n");
                sb.append(inf.getCause().toString());
            }
            myMsg.jtxtException.setText(sb.toString());  
            
            // if exception text is there, automatically expand
            if (sb.length() > 0) {
                myMsg.jscrException.setVisible(true);
                myMsg.setSize(myMsg.getWidth(), 310);
                synchronized(myMsg.getTreeLock()) { //no idea why this is needed (...but it avoids: java.lang.IllegalStateException: This function should be called while holding treeLock    at java.awt.Component.checkTreeLock(Component.java:1196)
                    myMsg.validateTree();
                }
            }
        }       
        myMsg.jtxtException.setCaretPosition(0);            
        
        //FairPOS - replacing error messages (-> simplicity for users)
        myMsg = replaceErrorsWithKnownTemplates(myMsg, inf); //comment this line to disable replacing
        
        //myMsg.show();
        myMsg.setVisible(true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jlblErrorCode = new javax.swing.JLabel();
        jlblMessage = new javax.swing.JLabel();
        jscrException = new javax.swing.JScrollPane();
        jtxtException = new javax.swing.JTextArea();
        jlblIcon = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jcmdOK = new javax.swing.JButton();

        setTitle(LocalRes.getIntString("title.message")); // NOI18N
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        jlblErrorCode.setFont(jlblErrorCode.getFont().deriveFont(jlblErrorCode.getFont().getStyle() & ~java.awt.Font.BOLD, jlblErrorCode.getFont().getSize()-2));
        jlblErrorCode.setText("jlblErrorCode");
        jPanel4.add(jlblErrorCode);

        jlblMessage.setText("jlblMessage");
        jlblMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jlblMessage.setMinimumSize(new java.awt.Dimension(200, 100));
        jlblMessage.setPreferredSize(new java.awt.Dimension(200, 100));
        jPanel4.add(jlblMessage);

        jscrException.setAlignmentX(0.0F);

        jtxtException.setEditable(false);
        jscrException.setViewportView(jtxtException);

        jPanel4.add(jscrException);

        getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);

        jlblIcon.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jlblIcon.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(jlblIcon, java.awt.BorderLayout.LINE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jcmdOK.setText(LocalRes.getIntString("button.ok")); // NOI18N
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel2.add(jcmdOK);

        jPanel3.add(jPanel2, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-455)/2, (screenSize.height-171)/2, 455, 171);
    }// </editor-fold>//GEN-END:initComponents

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        // Add your handling code here:
        setVisible(false);
        dispose();
    }//GEN-LAST:event_jcmdOKActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JButton jcmdOK;
    private javax.swing.JLabel jlblErrorCode;
    private javax.swing.JLabel jlblIcon;
    private javax.swing.JLabel jlblMessage;
    private javax.swing.JScrollPane jscrException;
    private javax.swing.JTextArea jtxtException;
    // End of variables declaration//GEN-END:variables
    
}
