// MessageDialogs.java

package hexgui.gui;

import static hexgui.gui.GuiUtil.insertLineBreaks;

import hexgui.util.Platform;
import hexgui.util.PrefUtil;
import hexgui.util.StringUtils;
import java.awt.Component;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/** Simple message dialogs. */
public final class MessageDialogs {
  public MessageDialogs(String applicationName) {
    m_applicationName = applicationName;
  }

  public void showError(Component frame, String mainMessage, String optionalMessage) {
    showError(frame, mainMessage, optionalMessage, true);
  }

  public void showError(
      Component frame, String mainMessage, String optionalMessage, boolean isCritical) {
    int type;
    if (isCritical) type = JOptionPane.ERROR_MESSAGE;
    else type = JOptionPane.PLAIN_MESSAGE;
    Object[] options = {"Close"};
    Object defaultOption = options[0];
    String title = "Error" + " - " + m_applicationName;
    show(
        null,
        frame,
        title,
        mainMessage,
        optionalMessage,
        type,
        JOptionPane.DEFAULT_OPTION,
        options,
        defaultOption,
        -1);
  }

  public void showError(Component frame, String message, Exception e) {
    showError(frame, message, e, true);
  }

  public void showError(Component frame, String message, Exception e, boolean isCritical) {
    showError(frame, message, StringUtils.getErrorMessage(e), isCritical);
  }

  public void showInfo(
      Component frame, String mainMessage, String optionalMessage, boolean isCritical) {
    showInfo(null, frame, mainMessage, optionalMessage, isCritical);
  }

  public void showInfo(
      String disableKey,
      Component frame,
      String mainMessage,
      String optionalMessage,
      boolean isCritical) {
    if (checkDisabled(disableKey)) return;
    int type;
    if (isCritical) type = JOptionPane.INFORMATION_MESSAGE;
    else type = JOptionPane.PLAIN_MESSAGE;
    Object[] options = {"Close"};
    Object defaultOption = options[0];
    String title = "Information" + " - " + m_applicationName;
    show(
        disableKey,
        frame,
        title,
        mainMessage,
        optionalMessage,
        type,
        JOptionPane.DEFAULT_OPTION,
        options,
        defaultOption,
        -1);
  }

  public int showYesNoCancelQuestion(
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      String nonDestructiveOption) {
    return showYesNoCancelQuestion(
        null, parent, mainMessage, optionalMessage, destructiveOption, nonDestructiveOption);
  }

  /**
   * Show a question with two options and cancel.
   *
   * @return 0 for the destructive option; 1 for the non-destructive option; 2 for cancel
   */
  public int showYesNoCancelQuestion(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      String nonDestructiveOption) {
    if (checkDisabled(disableKey)) return 0;
    Object[] options = new Object[3];
    int destructiveIndex;
    if (Platform.isMac()) {
      options[0] = nonDestructiveOption;
      options[1] = "Cancel";
      options[2] = destructiveOption;
      destructiveIndex = 2;
    } else {
      options[0] = nonDestructiveOption;
      options[1] = destructiveOption;
      options[2] = "Cancel";
      destructiveIndex = -1;
    }
    Object defaultOption = options[0];
    int type = JOptionPane.QUESTION_MESSAGE;
    String title = "Question" + " - " + m_applicationName;
    Object value =
        show(
            disableKey,
            parent,
            title,
            mainMessage,
            optionalMessage,
            type,
            JOptionPane.YES_NO_CANCEL_OPTION,
            options,
            defaultOption,
            destructiveIndex);
    int result;
    if (value == destructiveOption) result = 0;
    else if (value == nonDestructiveOption) result = 1;
    else result = 2;
    return result;
  }

  public void showWarning(
      Component parent, String mainMessage, String optionalMessage, boolean isCritical) {
    showWarning(null, parent, mainMessage, optionalMessage, isCritical);
  }

  public void showWarning(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      boolean isCritical) {
    if (checkDisabled(disableKey)) return;
    int type;
    if (isCritical) type = JOptionPane.WARNING_MESSAGE;
    else type = JOptionPane.PLAIN_MESSAGE;
    Object[] options = {"Close"};
    Object defaultOption = options[0];
    String title = "Warning" + " - " + m_applicationName;
    show(
        disableKey,
        parent,
        title,
        mainMessage,
        optionalMessage,
        type,
        JOptionPane.DEFAULT_OPTION,
        options,
        defaultOption,
        -1);
  }

  public boolean showQuestion(
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      boolean isCritical) {
    return showQuestion(null, parent, mainMessage, optionalMessage, destructiveOption, isCritical);
  }

  public boolean showQuestion(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      boolean isCritical) {
    return showQuestion(
        disableKey, parent, mainMessage, optionalMessage, destructiveOption, "Cancel", isCritical);
  }

  /**
   * Show warning message to confirm destructive actions.
   *
   * @return true, if destructive was chosen; false if cancel was chosen.
   */
  public boolean showQuestion(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      String affirmativeOption,
      String cancelOption,
      boolean isCritical) {
    if (checkDisabled(disableKey)) return true;
    Object[] options = new Object[2];
    if (Platform.isMac()) {
      options[0] = cancelOption;
      options[1] = affirmativeOption;
    } else {
      options[0] = affirmativeOption;
      options[1] = cancelOption;
    }
    Object defaultOption = affirmativeOption;
    int type;
    if (isCritical)
      // No reason to show a warning icon for confirmation dialogs
      // of frequent actions
      type = JOptionPane.QUESTION_MESSAGE;
    else type = JOptionPane.PLAIN_MESSAGE;
    String title = "Question" + " - " + m_applicationName;
    Object result =
        show(
            disableKey,
            parent,
            title,
            mainMessage,
            optionalMessage,
            type,
            JOptionPane.YES_NO_OPTION,
            options,
            defaultOption,
            -1);
    return (result == affirmativeOption);
  }

  public boolean showWarningQuestion(
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      boolean isCritical) {
    return showWarningQuestion(
        null, parent, mainMessage, optionalMessage, destructiveOption, isCritical);
  }

  public boolean showWarningQuestion(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      boolean isCritical) {
    return showWarningQuestion(
        disableKey, parent, mainMessage, optionalMessage, destructiveOption, "Cancel", isCritical);
  }

  /**
   * Show warning message to confirm destructive actions.
   *
   * @return true, if destructive was chosen; false if cancel was chosen.
   */
  public boolean showWarningQuestion(
      String disableKey,
      Component parent,
      String mainMessage,
      String optionalMessage,
      String destructiveOption,
      String nonDestructiveOption,
      boolean isCritical) {
    if (checkDisabled(disableKey)) return true;
    Object[] options = new Object[2];
    if (Platform.isMac()) {
      options[0] = nonDestructiveOption;
      options[1] = destructiveOption;
    } else {
      options[0] = destructiveOption;
      options[1] = nonDestructiveOption;
    }
    Object defaultOption = nonDestructiveOption;
    int type;
    if (isCritical) type = JOptionPane.WARNING_MESSAGE;
    else type = JOptionPane.PLAIN_MESSAGE;
    String title = "Warning" + " - " + m_applicationName;
    Object result =
        show(
            disableKey,
            parent,
            title,
            mainMessage,
            optionalMessage,
            type,
            JOptionPane.YES_NO_OPTION,
            options,
            defaultOption,
            -1);
    return (result == destructiveOption);
  }

  private final String m_applicationName;

  private final Set<String> m_disabled = new TreeSet<String>();

  private static void addFiller(JComponent component) {
    Box.Filler filler = GuiUtil.createFiller();
    filler.setAlignmentX(Component.LEFT_ALIGNMENT);
    component.add(filler);
  }

  private boolean checkDisabled(String disableKey) {
    if (disableKey == null) return false;
    Preferences prefs = PrefUtil.createNode("net/sf/hexgui/gui/messagedialogs/disabled");
    boolean permanentlyDisabled = prefs.getBoolean(disableKey, false);
    if (permanentlyDisabled) return true;
    // Make sure this entry exists (right now these settings can only
    // be directly edited in the backing store)
    prefs.putBoolean(disableKey, permanentlyDisabled);
    return m_disabled.contains(disableKey);
  }

  private Object show(
      String disableKey,
      Component parent,
      String title,
      String mainMessage,
      String optionalMessage,
      int messageType,
      int optionType,
      Object[] options,
      Object defaultOption,
      int destructiveIndex) {
    if (optionalMessage == null) optionalMessage = "";
    boolean isMac = Platform.isMac();
    Box box = Box.createVerticalBox();

    String css = GuiUtil.getMessageCss();

    JLabel label =
        new JLabel(
            "<html>"
                + css
                + "<b>"
                + insertLineBreaks(mainMessage)
                + "</b><p>"
                + insertLineBreaks(optionalMessage)
                + "</p>");
    label.setAlignmentX(Component.LEFT_ALIGNMENT);
    box.add(label);

    addFiller(box);
    addFiller(box);
    JCheckBox disableCheckBox = null;
    if (disableKey != null) {
      if (messageType == JOptionPane.QUESTION_MESSAGE)
        disableCheckBox = new JCheckBox("Do not show this message again");
      else if (messageType == JOptionPane.WARNING_MESSAGE)
        disableCheckBox = new JCheckBox("Do not show this warning again");
      else disableCheckBox = new JCheckBox("Do not show this message again");
      disableCheckBox.setToolTipText("Do not show this message again");
      disableCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      box.add(disableCheckBox);
    }
    if (isMac)
      // Don't show icons on Mac, problem with icon generation in
      // Quaqua 3.7.2
      messageType = JOptionPane.PLAIN_MESSAGE;
    JOptionPane optionPane =
        new JOptionPane(box, messageType, optionType, null, options, defaultOption);
    if (destructiveIndex >= 0) {
      String key = "Quaqua.OptionPane.destructiveOption";
      optionPane.putClientProperty(key, Integer.valueOf(destructiveIndex));
    }
    if (isMac && parent.isVisible())
      // Dialogs don't have titles on the Mac
      title = null;
    JDialog dialog = optionPane.createDialog(parent, title);
    dialog.setVisible(true);
    dialog.dispose();
    if (disableKey != null && disableCheckBox.isSelected()) m_disabled.add(disableKey);
    return optionPane.getValue();
  }
}
