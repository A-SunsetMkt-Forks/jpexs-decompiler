/*
 *  Copyright (C) 2010-2015 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.abc.RenameType;
import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author JPEXS
 */
public class RenameDialog extends AppDialog implements ActionListener {

    private static final String ACTION_OK = "OK";

    private static final String ACTION_CANCEL = "CANCEL";

    private final JRadioButton typeNumberRadioButton = new JRadioButton(translate("rename.type.typenumber"));

    private final JRadioButton randomWordRadioButton = new JRadioButton(translate("rename.type.randomword"));

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private boolean confirmed = false;

    public RenameType getRenameType() {
        if (!isConfirmed()) {
            return null;
        }
        if (typeNumberRadioButton.isSelected()) {
            return RenameType.TYPENUMBER;
        }
        return RenameType.RANDOMWORD;
    }

    public RenameDialog() {
        setSize(300, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        int renameType = Configuration.lastRenameType.get();
        ButtonGroup group = new ButtonGroup();
        group.add(typeNumberRadioButton);
        group.add(randomWordRadioButton);
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
        pan.add(typeNumberRadioButton);
        pan.add(randomWordRadioButton);
        typeNumberRadioButton.setSelected(renameType == 1);
        randomWordRadioButton.setSelected(renameType == 2);
        setLayout(new BorderLayout());
        add(new JLabel(translate("rename.type")), BorderLayout.NORTH);
        add(pan, BorderLayout.CENTER);
        JPanel panButtons = new JPanel(new FlowLayout());
        panButtons.add(okButton);
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        panButtons.add(cancelButton);
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        add(panButtons, BorderLayout.SOUTH);
        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        setTitle(translate("dialog.title"));
        getRootPane().setDefaultButton(okButton);
        pack();
        View.centerScreen(this);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            confirmed = false;
        }
        super.setVisible(b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case ACTION_OK:
                confirmed = true;
                Configuration.lastRenameType.set((Integer) (getRenameType() == RenameType.TYPENUMBER ? 1 : 2));
                setVisible(false);
                break;
            case ACTION_CANCEL:
                confirmed = false;
                setVisible(false);
                break;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public RenameType display() {
        setVisible(true);
        return getRenameType();
    }
}
