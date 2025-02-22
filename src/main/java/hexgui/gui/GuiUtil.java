// GuiUtil.java

package hexgui.gui;

import hexgui.util.Platform;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/** GUI utility classes and static functions. */
public class GuiUtil {
  /** Constant used for padding in dialogs. */
  public static final int PAD = 5;

  /** Constant used for small padding in dialogs. */
  public static final int SMALL_PAD = 2;

  public static void addStyle(JTextPane pane, String name, Color foreground, Color background) {
    StyledDocument doc = pane.getStyledDocument();
    StyleContext context = StyleContext.getDefaultStyleContext();
    Style defaultStyle = context.getStyle(StyleContext.DEFAULT_STYLE);
    Style style = doc.addStyle(name, defaultStyle);
    StyleConstants.setForeground(style, foreground);
    StyleConstants.setBackground(style, background);
  }

  public static void copyToClipboard(String text) {
    StringSelection selection = new StringSelection(text);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    ClipboardOwner owner =
        new ClipboardOwner() {
          public void lostOwnership(Clipboard clipboard, Transferable contents) {}
        };
    clipboard.setContents(selection, owner);
  }

  /**
   * Wrapper object for JComboBox items. JComboBox can have focus and keyboard navigation problems
   * if duplicate String objects are added. See JDK 1.5 doc for JComboBox.addItem.
   */
  public static Object createComboBoxItem(final String item) {
    return new Object() {
      public String toString() {
        return item;
      }
    };
  }

  /**
   * Create empty border with normal padding.
   *
   * @see #PAD
   */
  public static Border createEmptyBorder() {
    return EMPTY_BORDER;
  }

  /**
   * Create empty box with size of normal padding.
   *
   * @see #PAD
   */
  public static Box.Filler createFiller() {
    return new Box.Filler(FILLER_DIMENSION, FILLER_DIMENSION, FILLER_DIMENSION);
  }

  /**
   * Create empty border with small padding.
   *
   * @see #SMALL_PAD
   */
  public static Border createSmallEmptyBorder() {
    return SMALL_EMPTY_BORDER;
  }

  /**
   * Create empty box with size of small padding.
   *
   * @see #SMALL_PAD
   */
  public static Box.Filler createSmallFiller() {
    return new Box.Filler(SMALL_FILLER_DIMENSION, SMALL_FILLER_DIMENSION, SMALL_FILLER_DIMENSION);
  }

  public static String getClipboardText() {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable content = clipboard.getContents(null);
    if (content == null || !content.isDataFlavorSupported(DataFlavor.stringFlavor)) return null;
    try {
      return (String) content.getTransferData(DataFlavor.stringFlavor);
    } catch (UnsupportedFlavorException e) {
      return null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Return a style sheet for message labels using HTML.
   *
   * @return A string with a HTML-head tag containing a style tag with formatting options or an
   *     empty string.
   */
  public static String getMessageCss() {
    if (Platform.isMac())
      return "<head><style type=\"text/css\">"
          + "b { font: 13pt \"Lucida Grande\" }"
          + "p { font: 11pt \"Lucida Grande\"; margin-top: 8px }"
          + "</style></head>";
    else return "<head><style type=\"text/css\">" + "p { margin-top: 8px }" + "</style></head>";
  }

  /**
   * Get size of default monspaced font. Can be used for setting the initial size of some GUI
   * elements.
   */
  public static int getDefaultMonoFontSize() {
    return MONOSPACED_FONT.getSize();
  }

  public static ImageIcon getIcon(String icon, String name) {
    String resource = "hexgui/images/" + icon + ".png";
    URL url = GuiUtil.class.getClassLoader().getResource(resource);
    return new ImageIcon(url, name);
  }

  /**
   * Manually break message into multiple lines for multi-line labels. Needed for multi-line
   * messages in option panes, because pack() on JOptionPane does not compute the option pane size
   * correctly, if a maximum width is set and the label text is automatically broken into multiple
   * lines. The workaround with calling invalidate() and pack() a second time does not work either
   * in this case. See also Sun Bug ID 4545951 (still in Linux JDK 1.5.0_04-b05 or Mac 1.4.2_12)
   */
  public static String insertLineBreaks(String message) {
    final int MAX_CHAR_PER_LINE = 72;
    int length = message.length();
    if (length < MAX_CHAR_PER_LINE) return message;
    StringBuilder buffer = new StringBuilder();
    int startLine = 0;
    int lastWhiteSpace = -1;
    for (int pos = 0; pos < length; ++pos) {
      char c = message.charAt(pos);
      if (pos - startLine > 72) {
        int endLine = (lastWhiteSpace > startLine ? lastWhiteSpace : pos);
        if (buffer.length() > 0) buffer.append("<br>");
        buffer.append(message.substring(startLine, endLine));
        startLine = endLine;
      }
      if (Character.isWhitespace(c)) lastWhiteSpace = pos;
    }
    if (buffer.length() > 0) buffer.append("<br>");
    buffer.append(message.substring(startLine));
    return buffer.toString();
  }

  /**
   * Call SwingUtilities.invokeAndWait. Ignores possible exceptions (apart from printing a warning
   * to System.err
   */
  public static void invokeAndWait(Runnable runnable) {
    try {
      SwingUtilities.invokeAndWait(runnable);
    } catch (InterruptedException e) {
      System.err.println("Thread interrupted");
    } catch (java.lang.reflect.InvocationTargetException e) {
      System.err.println("InvocationTargetException");
    }
  }

  public static boolean isActiveWindow(Window window) {
    KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    return (manager.getActiveWindow() == window);
  }

  /**
   * Check window for normal state. Checks if window is not maximized (in either or both directions)
   * and not iconified.
   */
  public static boolean isNormalSizeMode(JFrame window) {
    int state = window.getExtendedState();
    int mask =
        Frame.MAXIMIZED_BOTH | Frame.MAXIMIZED_VERT | Frame.MAXIMIZED_HORIZ | Frame.ICONIFIED;
    return ((state & mask) == 0);
  }

  public static void paintImmediately(JComponent component) {
    component.paintImmediately(component.getVisibleRect());
  }

  public static void removeKeyBinding(JComponent component, String key) {
    int condition = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
    InputMap inputMap = component.getInputMap(condition);
    // According to the docs, null should remove the action, but it does
    // not seem to work with Sun Java 1.4.2, new Object() works
    inputMap.put(KeyStroke.getKeyStroke(key), new Object());
  }

  /** Set antialias rendering hint if graphics is instance of Graphics2D. */
  public static void setAntiAlias(Graphics graphics) {
    if (graphics instanceof Graphics2D) {
      Graphics2D graphics2D = (Graphics2D) graphics;
      graphics2D.setRenderingHint(
          RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
  }

  /** Set text field non-editable. Also sets it non-focusable. */
  public static void setEditableFalse(JTextField field) {
    field.setEditable(false);
    field.setFocusable(false);
  }

  /** Set Go icon on frame. */
  public static void setGoIcon(Frame frame) {
    URL url = s_iconURL;
    if (url != null) frame.setIconImage(new ImageIcon(url).getImage());
  }

  /**
   * Set property to render button in bevel style on the Mac. Only has an effect if Quaqua Look and
   * Feel is used.
   */
  public static void setMacBevelButton(JButton button) {
    button.putClientProperty("Quaqua.Button.style", "bevel");
  }

  public static void setMonospacedFont(JComponent component) {
    if (MONOSPACED_FONT != null) component.setFont(MONOSPACED_FONT);
  }

  public static void addStyle(JTextPane textPane, String name, Color foreground) {
    addStyle(textPane, name, foreground, null, false);
  }

  public static void addStyle(
      JTextPane textPane, String name, Color foreground, Color background, boolean bold) {
    StyledDocument doc = textPane.getStyledDocument();
    StyleContext context = StyleContext.getDefaultStyleContext();
    Style def = context.getStyle(StyleContext.DEFAULT_STYLE);
    Style style = doc.addStyle(name, def);
    if (foreground != null) StyleConstants.setForeground(style, foreground);
    if (background != null) StyleConstants.setBackground(style, background);
    StyleConstants.setBold(style, bold);
  }

  public static void setStyle(JTextPane textPane, int start, int length, String name) {
    StyledDocument doc = textPane.getStyledDocument();
    Style style;
    if (name == null) {
      StyleContext context = StyleContext.getDefaultStyleContext();
      style = context.getStyle(StyleContext.DEFAULT_STYLE);
    } else style = doc.getStyle(name);
    doc.setCharacterAttributes(start, length, style, true);
  }

  public static void setUnlimitedSize(JComponent component) {
    Dimension size = new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    component.setMaximumSize(size);
  }

  static {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    // There are problems on some platforms with transparency (e.g. Linux
    // Sun Java 1.5.0). Best solution for now is to take an icon without
    // transparency
    s_iconURL = loader.getResource("hexgui/images/hexgui-48x48-notrans.png");
  }

  private static final Font MONOSPACED_FONT = Font.decode("Monospaced");

  private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

  private static final Border SMALL_EMPTY_BORDER =
      BorderFactory.createEmptyBorder(
          SMALL_PAD, SMALL_PAD,
          SMALL_PAD, SMALL_PAD);

  private static final Dimension FILLER_DIMENSION = new Dimension(PAD, PAD);

  private static final Dimension SMALL_FILLER_DIMENSION = new Dimension(SMALL_PAD, SMALL_PAD);

  private static URL s_iconURL;
}
