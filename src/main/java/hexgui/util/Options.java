package hexgui.util;

import java.io.*;
import java.util.*;

/** Parser for command line options. Options begin with a single '-' character. */
public class Options {
  /**
   * Parse options.
   *
   * @param args Command line args from main method
   * @param specs Specification of allowed options. Contains option names (without '-'). Options
   *     that need an argument must have a ':' appended. The special argument '--' stops option
   *     parsing, all following arguments are treated as non-option arguments.
   * @throws Exception If options are not valid according to specs.
   */
  public Options(String[] args, String[] specs) throws Exception {
    for (String spec : specs) {
      if (spec.length() > 0) m_map.put(spec, null);
    }
    parseArgs(args);
  }

  /** Check if option is present. */
  public boolean contains(String option) {
    String value = get(option, null);
    return (value != null);
  }

  /**
   * Return string option value.
   *
   * @param option The option key.
   * @return The option value or an empty string, if option is not present.
   */
  public String get(String option) {
    return get(option, "");
  }

  /**
   * Return string option value.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @return The option value or defaultValue, if option is not present.
   */
  public String get(String option, String defaultValue) {
    assert isValidOption(option);
    String value = getValue(option);
    if (value == null) return defaultValue;
    return value;
  }

  /**
   * Get remaining arguments that are not options.
   *
   * @return The sequence of non-option arguments.
   */
  public ArrayList<String> getArguments() {
    return m_args;
  }

  /**
   * Check that the number of non-option arguments is zero.
   *
   * @throws Exception If there are any non-option arguments.
   */
  public void checkNoArguments() throws Exception {
    if (!m_args.isEmpty())
      throw new Exception("Command does not allow arguments that are not options");
  }

  /**
   * Parse double option.
   *
   * @param option The option key.
   * @return The option value or 0, if option is not present.
   * @throws Exception If option value is not a double.
   */
  public double getDouble(String option) throws Exception {
    return getDouble(option, 0);
  }

  /**
   * Parse double option.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @return The option value or defaultValue, if option is not present.
   * @throws Exception If option value is not a double.
   */
  public double getDouble(String option, double defaultValue) throws Exception {
    String value = get(option, Double.toString(defaultValue));
    if (value == null) return defaultValue;
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      throw new Exception("Option -" + option + " needs float value");
    }
  }

  /**
   * Parse integer option.
   *
   * @param option The option key.
   * @return The option value or 0, if option is not present.
   * @throws Exception If option value is not an integer.
   */
  public int getInteger(String option) throws Exception {
    return getInteger(option, 0);
  }

  /**
   * Parse integer option.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @return The option value or defaultValue, if option is not present.
   * @throws Exception If option value is not an integer.
   */
  public int getInteger(String option, int defaultValue) throws Exception {
    String value = get(option, Integer.toString(defaultValue));
    if (value == null) return defaultValue;
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new Exception("Option -" + option + " needs integer value");
    }
  }

  /**
   * Parse integer option with range check.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @param min The minimum valid value.
   * @return The option value or defaultValue, if option is not present.
   * @throws Exception If option value is less than min.
   */
  public int getInteger(String option, int defaultValue, int min) throws Exception {
    int value = getInteger(option, defaultValue);
    if (value < min) throw new Exception("Option -" + option + " must be greater than " + min);
    return value;
  }

  /**
   * Parse integer option with range check.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @param min The minimum valid value.
   * @param max The maximum valid value.
   * @return The option value or defaultValue, if option is not present.
   * @throws Exception If option value is less than min or greater than max.
   */
  public int getInteger(String option, int defaultValue, int min, int max) throws Exception {
    int value = getInteger(option, defaultValue);
    if (value < min || value > max)
      throw new Exception("Option -" + option + " must be in [" + min + ".." + max + "]");
    return value;
  }

  /**
   * Parse long integer option.
   *
   * @param option The option key.
   * @return The option value or 0, if option is not present.
   * @throws Exception If option value is not a long integer.
   */
  public long getLong(String option) throws Exception {
    return getLong(option, 0L);
  }

  /**
   * Parse long integer option.
   *
   * @param option The option key.
   * @param defaultValue The default value.
   * @return The option value or defaultValue, if option is not present.
   * @throws Exception If option value is not a long integer.
   */
  public long getLong(String option, long defaultValue) throws Exception {
    String value = get(option, Long.toString(defaultValue));
    if (value == null) return defaultValue;
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new Exception("Option -" + option + " needs long integer value");
    }
  }

  /**
   * Read options from a file given with the option "config". Requires that "config" is an allowed
   * option.
   *
   * @throws Exception If options in file are not valid according to the specification.
   */
  public void handleConfigOption() throws Exception {
    if (!contains("config")) return;
    String filename = get("config");
    InputStream inputStream;
    try {
      inputStream = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      throw new Exception("File not found: " + filename);
    }
    Reader reader = new InputStreamReader(inputStream);
    BufferedReader bufferedReader = new BufferedReader(reader);
    try {
      StringBuilder buffer = new StringBuilder(256);
      String line;
      while (true) {
        line = bufferedReader.readLine();
        if (line == null) break;
        buffer.append(line);
        buffer.append(' ');
      }
      parseArgs(StringUtils.splitArguments(buffer.toString()));
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } finally {
      try {
        bufferedReader.close();
      } catch (IOException e) {
        System.err.println(e.getMessage());
      }
    }
  }

  /**
   * Creates a new Options instance from command line. Automatically calls handleConfigOption.
   *
   * @param args The command line split into arguments.
   * @param specs Option specification as in constructor.
   * @return The new Options instance.
   * @throws Exception If options are not valid according to specs.
   */
  public static Options parse(String[] args, String[] specs) throws Exception {
    Options opt = new Options(args, specs);
    opt.handleConfigOption();
    return opt;
  }

  private final ArrayList<String> m_args = new ArrayList<String>();

  private final Map<String, String> m_map = new TreeMap<String, String>();

  private String getSpec(String option) throws Exception {
    if (m_map.containsKey(option)) return option;
    else if (m_map.containsKey(option + ":")) return option + ":";
    throw new Exception("Unknown option -" + option);
  }

  private String getValue(String option) {
    assert isValidOption(option);
    if (m_map.containsKey(option)) return m_map.get(option);
    return m_map.get(option + ":");
  }

  private boolean isOptionKey(String s) {
    return (s.length() > 0 && s.charAt(0) == '-');
  }

  private boolean isValidOption(String option) {
    return (m_map.containsKey(option) || m_map.containsKey(option + ":"));
  }

  private boolean needsValue(String spec) {
    return (spec.length() > 0 && spec.substring(spec.length() - 1).equals(":"));
  }

  private void parseArgs(String args[]) throws Exception {
    boolean stopParse = false;
    int n = 0;
    while (n < args.length) {
      String s = args[n];
      ++n;
      if (s.equals("--")) {
        stopParse = true;
        continue;
      }
      if (isOptionKey(s) && !stopParse) {
        String spec = getSpec(s.substring(1));
        if (needsValue(spec)) {
          if (n >= args.length) throw new Exception("Option " + s + " needs value");
          String value = args[n];
          ++n;
          m_map.put(spec, value);
        } else m_map.put(spec, "1");

      } else m_args.add(s);
    }
  }
}
