/*
 * Copyright (c) 2014-2015, Draque Thompson, draquemail@gmail.com
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package PolyGlot.Screens;

import PolyGlot.Nodes.ConWord;
import PolyGlot.DictCore;
import PolyGlot.Nodes.GenderNode;
import PolyGlot.CustomControls.InfoBox;
import PolyGlot.CustomControls.PComboBox;
import PolyGlot.CustomControls.PDialog;
import PolyGlot.CustomControls.PTextArea;
import PolyGlot.CustomControls.PTextField;
import PolyGlot.Nodes.TypeNode;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author draque
 */
public final class ScrQuickWordEntry extends PDialog {

    private final String cstSELET = "";
    private final ScrLexicon parent;

    /**
     * Creates new form scrQuickWordEntry
     *
     * @param _core Dictionary core
     * @param _parent parent dictionary interface
     */
    public ScrQuickWordEntry(DictCore _core, ScrLexicon _parent) {
        core = _core;
        parent = _parent;

        setupKeyStrokes();
        initComponents();
        setupListeners();

        // conword is always required and is initially selected
        txtConWord.setBackground(core.getRequiredColor());
        txtConWord.requestFocus();

        if (core.getPropertiesManager().isLocalMandatory()) {
            txtLocalWord.setBackground(core.getRequiredColor());
            chkLocal.setEnabled(false);
        }
        if (core.getPropertiesManager().isTypesMandatory()) {
            cmbType.setForeground(core.getRequiredColor());
            chkType.setEnabled(false);
        }
        
        populateTypes();
        populateGenders();
        setupCustomLabels();
    }
    
    private void setupCustomLabels() {
        jLabel2.setText(core.conLabel() + " word");
        jLabel3.setText(core.localLabel() + " word");
    }
    
    // Overridden to meet coding standards...
    @Override
    protected final void setupKeyStrokes() {
        super.setupKeyStrokes();
    }
    
    /**
     * Sets up all component listeners
     */
    private void setupListeners() {
        KeyListener enterListener = new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                // User only wants to enter word if no popups are visible
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        && (!cmbGender.isPopupVisible())
                        && !cmbType.isPopupVisible()) {
                    tryRecord();
                }
            }

            @Override public void keyReleased(KeyEvent e) {/*DO NOTHING*/}
            @Override public void keyTyped(KeyEvent e) { /*DO NOTHING*/}
        };

        txtConWord.setFont(core.getPropertiesManager().getFontCon());
        txtConWord.addKeyListener(enterListener);
        txtConWord.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                setProc();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setProc();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setProc();
            }
        });
        txtDefinition.addKeyListener(enterListener);
        txtLocalWord.addKeyListener(enterListener);
        txtProc.addKeyListener(enterListener);
        cmbGender.addKeyListener(enterListener);
        cmbType.addKeyListener(enterListener);
    }
    
    /**
     * Sets pronunciation value of word
     */
    private void setProc() {
        txtProc.setText(core.getPronunciationMgr()
                .getPronunciation(txtConWord.getText()));
    }
    
    @Override
    public boolean thisOrChildrenFocused() {
        return this.isFocusOwner();
    }
    
    @Override
    public void updateAllValues(DictCore _core) {
        core = _core;
        // ensure this is on the UI component stack to avoid read/writelocks...
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                populateTypes();
                populateGenders();
            }
        };
        SwingUtilities.invokeLater(runnable);
    }
    
    private void populateTypes() {
        Iterator<TypeNode> typeIt = core.getTypes().getNodeIterator();
        cmbType.removeAllItems();
        cmbType.addItem(new TypeNode());
        while (typeIt.hasNext()) {
            TypeNode curType = typeIt.next();
            cmbType.addItem(curType);
        }
    }

    private void populateGenders() {
        Iterator<GenderNode> gendIt = core.getGenders().getNodeIterator();
        cmbGender.removeAllItems();
        cmbGender.addItem(cstSELET);
        while (gendIt.hasNext()) {
            GenderNode curGen = gendIt.next();
            cmbGender.addItem(curGen);
        }
    }
    
    /**
     * records a word if appropriate, flashes required fields otherwise
     */
    private void tryRecord() {
        ConWord word = new ConWord();
        
        word.setValue(txtConWord.getText());
        word.setLocalWord(txtLocalWord.getText());
        word.setPronunciation(txtProc.getText());
        word.setDefinition(txtDefinition.getText());
        word.setGender(cmbGender.getSelectedItem().toString());
        word.setWordTypeId(((TypeNode)cmbType.getSelectedItem()).getId());
        
        ConWord test = core.isWordLegal(word);
        String testResults = "";
        
        if (!test.getValue().isEmpty()) {
            ((PTextField)txtConWord).makeFlash(core.getRequiredColor(), true);
            testResults += test.getValue();
        }
        if (!test.getLocalWord().isEmpty()) {
            ((PTextField)txtLocalWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getLocalWord());
        }
        if (!test.getPronunciation().isEmpty()) {
            ((PTextField)txtProc).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getPronunciation());
        }
        if (!test.getDefinition().isEmpty()) {
            // errors having to do with type patterns returned in def field.
            ((PComboBox)cmbType).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getDefinition());
        }
        if (!test.getGender().isEmpty()) {
            ((PComboBox)cmbGender).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.getGender());
        }
        if (!test.typeError.isEmpty()) {
            ((PComboBox)cmbType).makeFlash(core.getRequiredColor(), true);
            testResults += ("\n" + test.typeError);
        } 
        if (core.getPropertiesManager().isWordUniqueness()
                && core.getWordCollection().testWordValueExists(txtConWord.getText())) {
            ((PTextField)txtConWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\nConWords set to enforced unique: this local exists elsewhere.");
        }
        if (core.getPropertiesManager().isLocalUniqueness()
                && core.getWordCollection().testLocalValueExists(txtLocalWord.getText())) {
            ((PTextField)txtLocalWord).makeFlash(core.getRequiredColor(), true);
            testResults += ("\nLocal words set to enforced unique: this work exists elsewhere.");
        }
        
        
        if (!testResults.isEmpty()) {
            InfoBox.warning("Illegal Values", "Word contains illegal values:\n\n" 
                    + testResults, this);
            return;
        }
        
        try {
            word.setValue(txtConWord.getText());
            word.setDefinition(txtDefinition.getText());
            word.setLocalWord(txtLocalWord.getText());
            word.setPronunciation(txtProc.getText());
            word.setGender(cmbGender.getSelectedItem().toString());
            word.setWordTypeId(((TypeNode)cmbType.getSelectedItem()).getId());
            
            int wordId = core.getWordCollection().addWord(word);
            blankWord();
            txtConWord.requestFocus();
            
            parent.refreshWordList(wordId);
        } catch (Exception e) {
            InfoBox.error("Word Error", "Unable to insert word: " + e.getMessage(), this);
        }
    }
    
    /**
     * blanks out current conword fields
     */
    private void blankWord() {
        txtConWord.setText("");
        txtDefinition.setText("");
        txtLocalWord.setText("");
        txtProc.setText("");
        cmbGender.setSelectedIndex(0);
        cmbType.setSelectedIndex(0);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        chkLocal = new javax.swing.JCheckBox();
        chkType = new javax.swing.JCheckBox();
        chkGender = new javax.swing.JCheckBox();
        chkProc = new javax.swing.JCheckBox();
        chkDefinition = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        txtConWord = new PTextField(core);
        txtLocalWord = new PTextField(core, true);
        cmbType = new PComboBox();
        cmbGender = new PComboBox();
        txtProc = new PTextField(core, true);
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDefinition = new PTextArea();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnDone = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Word Quickentry");

        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        chkLocal.setSelected(true);
        chkLocal.setText("Local Word");
        chkLocal.setToolTipText("Enable/Disable Local Word Entry");
        chkLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLocalActionPerformed(evt);
            }
        });

        chkType.setSelected(true);
        chkType.setText("Type");
        chkType.setToolTipText("Enable/Disable Type Entry");
        chkType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkTypeActionPerformed(evt);
            }
        });

        chkGender.setSelected(true);
        chkGender.setText("Gender");
        chkGender.setToolTipText("Enable/Disable Gender Entry");
        chkGender.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkGenderActionPerformed(evt);
            }
        });

        chkProc.setSelected(true);
        chkProc.setText("Pronunciation");
        chkProc.setToolTipText("Enable/Disable Pronunciation Entry");
        chkProc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkProcActionPerformed(evt);
            }
        });

        chkDefinition.setSelected(true);
        chkDefinition.setText("Definition");
        chkDefinition.setToolTipText("Enable/Disable Definition Entry");
        chkDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDefinitionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkLocal)
                    .addComponent(chkType))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(chkGender)
                        .addGap(40, 40, 40)
                        .addComponent(chkDefinition))
                    .addComponent(chkProc))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkLocal)
                    .addComponent(chkGender)
                    .addComponent(chkDefinition))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chkType)
                    .addComponent(chkProc))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));

        cmbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Definition");

        txtDefinition.setColumns(20);
        txtDefinition.setLineWrap(true);
        txtDefinition.setRows(5);
        txtDefinition.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtDefinition);

        jLabel2.setText("Con Word");

        jLabel3.setText("Local Word");

        jLabel4.setText("Type");

        jLabel5.setText("Gender");

        jLabel6.setText("Pronunciation");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(22, 22, 22)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txtLocalWord)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cmbGender, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(txtProc, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(27, 27, 27)
                        .addComponent(txtConWord)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2)
                                    .addComponent(txtConWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtLocalWord, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtProc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btnDone.setText("Done");
        btnDone.setToolTipText("Exit quickentry window");
        btnDone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDoneActionPerformed(evt);
            }
        });

        jLabel7.setText("Hit Enter/Return to save word and clear values");
        jLabel7.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnDone))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDone)
                    .addComponent(jLabel7)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoneActionPerformed
        dispose();
    }//GEN-LAST:event_btnDoneActionPerformed

    private void chkLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLocalActionPerformed
        txtLocalWord.setEnabled(chkLocal.isSelected());
    }//GEN-LAST:event_chkLocalActionPerformed

    private void chkGenderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkGenderActionPerformed
        cmbGender.setEnabled(chkGender.isSelected());
    }//GEN-LAST:event_chkGenderActionPerformed

    private void chkDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDefinitionActionPerformed
        txtDefinition.setEnabled(chkDefinition.isSelected());
    }//GEN-LAST:event_chkDefinitionActionPerformed

    private void chkTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTypeActionPerformed
        cmbType.setEnabled(chkType.isSelected());
    }//GEN-LAST:event_chkTypeActionPerformed

    private void chkProcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkProcActionPerformed
        txtProc.setEnabled(chkProc.isSelected());
    }//GEN-LAST:event_chkProcActionPerformed

    /**
     * @param _core Dictionary Core
     * @param _parent parent dictionary interface
     * @return created window
     */
    public static ScrQuickWordEntry run(DictCore _core, ScrLexicon _parent) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            InfoBox.error("Window Error", "Unable to open quick word entry screen: " + ex.getLocalizedMessage(), _parent);
        }
        //</editor-fold>
        
        //</editor-fold>

        ScrQuickWordEntry ret = new ScrQuickWordEntry(_core, _parent);
        ret.setModal(true);
        return ret;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDone;
    private javax.swing.JCheckBox chkDefinition;
    private javax.swing.JCheckBox chkGender;
    private javax.swing.JCheckBox chkLocal;
    private javax.swing.JCheckBox chkProc;
    private javax.swing.JCheckBox chkType;
    private javax.swing.JComboBox cmbGender;
    private javax.swing.JComboBox cmbType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtConWord;
    private javax.swing.JTextArea txtDefinition;
    private javax.swing.JTextField txtLocalWord;
    private javax.swing.JTextField txtProc;
    // End of variables declaration//GEN-END:variables
}
