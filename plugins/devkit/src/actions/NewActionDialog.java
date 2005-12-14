/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.devkit.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.psi.PsiManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author yole
 */
public class NewActionDialog extends DialogWrapper {
  private JPanel myRootPanel;
  private JList myGroupList;
  private JList myActionList;
  private JTextField myActionNameEdit;
  private JTextField myActionIdEdit;
  private JTextField myActionTextEdit;
  private JTextField myActionDescriptionEdit;
  private JRadioButton myAnchorFirstRadio;
  private JRadioButton myAnchorLastRadio;
  private JRadioButton myAnchorBeforeRadio;
  private JRadioButton myAnchorAfterRadio;
  private TextFieldWithBrowseButton myIconEdit;
  private Project myProject;
  private ButtonGroup myAnchorButtonGroup = new ButtonGroup();

  protected NewActionDialog(final Project project) {
    super(project, false);
    myProject = project;
    init();
    setTitle("New Action");
    final ActionManager actionManager = ActionManager.getInstance();
    final String[] actionIds = actionManager.getActionIds("");
    Arrays.sort(actionIds);
    final List<ActionGroup> actionGroups = new ArrayList<ActionGroup>();
    for(String actionId: actionIds) {
      if (actionManager.isGroup(actionId)) {
        final AnAction anAction = actionManager.getAction(actionId);
        if (anAction instanceof DefaultActionGroup) {
          actionGroups.add((ActionGroup) anAction);
        }
      }
    }
    myGroupList.setListData(actionGroups.toArray());
    myGroupList.setCellRenderer(new MyActionRenderer());
    myGroupList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        ActionGroup group = (ActionGroup) myGroupList.getSelectedValue();
        if (group == null) {
          myActionList.setListData(new Object[0]);
        }
        else {
          final AnAction[] actions = group.getChildren(null);
          // filter out actions that don't have IDs - they can't be used for anchoring in plugin.xml
          List<AnAction> realActions = new ArrayList<AnAction>();
          for(AnAction action: actions) {
            if (actionManager.getId(action) != null) {
              realActions.add(action);
            }
          }
          myActionList.setListData(realActions.toArray());
        }
      }
    });

    myActionList.setCellRenderer(new MyActionRenderer());
    myActionList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        updateControls();
      }
    });

    myActionIdEdit.getDocument().addDocumentListener(new MyDocumentListener());
    myActionNameEdit.getDocument().addDocumentListener(new MyDocumentListener());
    myActionTextEdit.getDocument().addDocumentListener(new MyDocumentListener());

    myAnchorButtonGroup = new ButtonGroup();
    myAnchorButtonGroup.add(myAnchorFirstRadio);
    myAnchorButtonGroup.add(myAnchorLastRadio);
    myAnchorButtonGroup.add(myAnchorBeforeRadio);
    myAnchorButtonGroup.add(myAnchorAfterRadio);
    myAnchorButtonGroup.setSelected(myAnchorFirstRadio.getModel(), true);

    updateControls();
  }

  protected JComponent createCenterPanel() {
    return myRootPanel;
  }

  @Override public JComponent getPreferredFocusedComponent() {
    return myActionIdEdit;
  }

  public String getActionId() {
    return myActionIdEdit.getText();
  }

  public String getActionName() {
    return myActionNameEdit.getText();
  }

  public String getActionText() {
    return myActionTextEdit.getText();
  }

  public String getActionDescription() {
    return myActionDescriptionEdit.getText();
  }

  public String getSelectedGroupId() {
    ActionGroup group = (ActionGroup) myGroupList.getSelectedValue();
    return group == null ? null : ActionManager.getInstance().getId(group);
  }

  public String getSelectedActionId() {
    AnAction action = (AnAction) myActionList.getSelectedValue();
    return action == null ? null : ActionManager.getInstance().getId(action);
  }

  public String getSelectedAnchor() {
    ButtonModel selection = myAnchorButtonGroup.getSelection();
    if (selection == myAnchorFirstRadio.getModel()) return "first";
    if (selection == myAnchorLastRadio.getModel()) return "last";
    if (selection == myAnchorBeforeRadio.getModel()) return "before";
    if (selection == myAnchorAfterRadio.getModel()) return "after";
    return null;
  }

  private void updateControls() {
    setOKActionEnabled(myActionIdEdit.getText().length() > 0 &&
                       myActionNameEdit.getText().length() > 0 &&
                       myActionTextEdit.getText().length() > 0 &&
                       PsiManager.getInstance(myProject).getNameHelper().isIdentifier(myActionNameEdit.getText()));

    myAnchorBeforeRadio.setEnabled(myActionList.getSelectedValue() != null);
    myAnchorAfterRadio.setEnabled(myActionList.getSelectedValue() != null);
  }

  private static class MyActionRenderer extends ColoredListCellRenderer {
    protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
      AnAction group = (AnAction) value;
      append(ActionManager.getInstance().getId(group), SimpleTextAttributes.REGULAR_ATTRIBUTES);
      final String text = group.getTemplatePresentation().getText();
      if (text != null) {
        append(" (" + text + ")", SimpleTextAttributes.REGULAR_ATTRIBUTES);
      }
    }
  }

  private class MyDocumentListener implements DocumentListener {
    public void insertUpdate(DocumentEvent e) {
      updateControls();
    }

    public void removeUpdate(DocumentEvent e) {
      updateControls();
    }

    public void changedUpdate(DocumentEvent e) {
      updateControls();
    }
  }
}
