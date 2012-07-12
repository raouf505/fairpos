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

package com.openbravo.pos.admin;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.JMessageDialog;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.BatchSentence;
import com.openbravo.data.loader.BatchSentenceResource;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.JRootApp;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

/**
 * Import resources from disk to DB, overwrite all.<br>
 * Useful for using an external editor.
 * @author Harald
 */
public class ImportResources extends JPanel implements JPanelView {

    private static final String NO_IMPORT = "---";
    private AppView m_App;
    private String m_lastStatus = NO_IMPORT;
    private JTextComponent m_jText;

    public ImportResources (AppView oApp) {
        m_App = oApp;
        m_jText = new JTextArea(m_lastStatus);
        add (m_jText);
    }
    
    /**
     * Run the import itself, script taken from src-pos/com/openbravo/pos/scripts/<DB-type>-importResources.sql.
     * @param app
     * @return true - imported correctly
     */
    private boolean runImport (AppView app) {

        DataLogicSystem dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_lastStatus = "";
        
        String sScript = dlSystem.getInitScript() + "-importResources.sql";

        if (JRootApp.class.getResource(sScript) == null) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_DANGER, AppLocal.getIntString("message.noImportResourcesScript", app.getSession().DB.getName())));
            return false;
        } else {
            // import script exists
            if (JOptionPane.showConfirmDialog(this
                    , AppLocal.getIntString("message.ImportResources") // Really import resources?
                    , AppLocal.getIntString("message.title")
                    , JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {  

                try {
                    BatchSentence bsentence = new BatchSentenceResource(app.getSession(), sScript);

                    // method list() is executing the SQL query
                    java.util.List l = bsentence.list();
                    if (l.size() > 0) {
                        JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("Database.ScriptWarning"), l.toArray(new Throwable[l.size()])));
                        m_lastStatus = AppLocal.getIntString("Database.ScriptWarning") + "\n";
                    }
               } catch (BasicException e) {
                    JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_DANGER, AppLocal.getIntString("Database.ScriptError"), e));
                    return false;
                }     
            } else {
                m_lastStatus = NO_IMPORT;
                m_jText.setText(m_lastStatus);
                return false;
            }
        }   

        m_lastStatus += AppLocal.getIntString("message.ImportResourcesDone");
        m_jText.setText(m_lastStatus);
        return true;

    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.ImportResources");
    }

    @Override
    public void activate() throws BasicException {
        runImport(m_App);
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
    
}
