package cn.edu.zju.db.datagen.gui.util.calendar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

/**
 * 2010-8-5 10:23:44
 *
 * @author mkk(monkeyking1987@126.com)
 * @version 1.0
 * @see javax.swing.JFileChooser ,JCalendarChooser time chooser <br>
 *      how to use: 1.new object JTimeChooser;<br>
 *      2.call showTimeDialog() to obtain a Calendar object
 */
public class JTimeChooser extends JDialog implements ActionListener {

    private static final long serialVersionUID = -3758522951261503946L;

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 */
    public JTimeChooser(Dialog parent) {
        super(parent, true);
        this.setTitle(title);
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 */
    public JTimeChooser(Dialog parent, String title) {
        super(parent, title, true);
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 * @param location
	 *            the location of display
	 */
    public JTimeChooser(Dialog parent, String title, Point location) {
        super(parent, title, true);
        this.location = location;
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 * @param location
	 *            the location of display
	 * @param showYears
	 *            the year when display, default is 100 years, 50 years ago to 50 years later<br>
	 *            e.g., if the year now is 2010, the parameter showYears = 30 year, the year displayed is 1995-2024<br>
	 *            note : the value of showYears should be greater than 0, otherwise use default
	 */
    public JTimeChooser(Dialog parent, String title, Point location,
                        int showYears) {
        super(parent, title, true);
        this.location = location;
        if (showYears > 0) {
            this.showYears = showYears;
        }
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 */
    public JTimeChooser(Frame parent) {
        super(parent, true);
        this.setTitle(title);
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param location
	 *            the location of display
	 */
    public JTimeChooser(Frame parent, Point location) {
        super(parent, true);
        this.setTitle(title);
        this.location = location;
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 * @param location
	 *            the location of display
	 */
    public JTimeChooser(Frame parent, String title, Point location) {
        super(parent, title, true);
        this.location = location;
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 * @param location
	 *            the location of display
	 * @param showYears
	 *            the year when display, default is 100 years, 50 years ago to 50 years later<br>
	 *            e.g., if the year now is 2010, the parameter showYears = 30 year, the year displayed is 1995-2024<br>
	 *            note : the value of showYears should be greater than 0, otherwise use default
	 */
    public JTimeChooser(Frame parent, String title, Point location,
                        int showYears) {
        super(parent, title, true);
        this.location = location;
        if (showYears > 0) {
            this.showYears = showYears;
        }
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param location
	 *            the location of display
	 */
    public JTimeChooser(Dialog parent, Point location) {
        super(parent, true);
        this.setTitle(title);
        this.location = location;
        // initialize
        this.initDatas();
    }

    /**
	 * constructor<br>
	 * call showTimeDialog() to obtain the time value after construction
	 * 
	 * @param parent
	 *            parent module
	 * @param title
	 *            the title of frame
	 */
    public JTimeChooser(Frame parent, String title) {
        super(parent, title, true);
        // initialize
        this.initDatas();
    }

    /**
     * initialize data
     */
    private void initDatas() {
        this.calendar = Calendar.getInstance();
        this.year1 = this.calendar.get(Calendar.YEAR);
        this.month1 = this.calendar.get(Calendar.MONTH);
        this.day1 = this.calendar.get(Calendar.DAY_OF_MONTH);
        this.years = new String[showYears];
        this.months = new String[12];
        // initialize months
        for (int i = 0; i < this.months.length; i++) {
            this.months[i] = " " + formatDay(i + 1);
        }
        // initialize years
        int start = this.year1 - showYears / 2;
        for (int i = start; i < start + showYears; i++) {
            this.years[i - start] = String.valueOf(i);
        }
        // set the hh:mm:ss to 00:00:00
        this.calendar.set(Calendar.HOUR_OF_DAY, 0);
        this.calendar.set(Calendar.MINUTE, 0);
        this.calendar.set(Calendar.SECOND, 0);

    }

    /**
	 * get the start point of left-top point
	 * 
	 * @param width
	 *            length
	 * @param height
	 *            width
	 * @return
	 */
    private Dimension getStartDimension(int width, int height) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        dim.width = dim.width / 2 - width / 2;
        dim.height = dim.height / 2 - height / 2;
        return dim;
    }

    /**
	 * call this method to obtain the time value after construction
	 * 
	 * @return the initialized object Calendar
	 */
    public Calendar showTimeDialog() {
        this.initCompents();
        return this.calendar;
    }

    /**
     * initialize the components
     */
    private void initCompents() {
        this.setLayout(new BorderLayout());
        // north panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel3.setBackground(Color.WHITE);
        showNorthPanel(panel3);
        this.add(panel3, BorderLayout.NORTH);
        // middle panel
        this.add(printCalendar(), BorderLayout.CENTER);
        // south panel
        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.WHITE);
        panel2.setLayout(new FlowLayout(FlowLayout.CENTER));
        this.showSouthPanel(panel2);
        this.add(panel2, BorderLayout.SOUTH);

        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        // set the location
        if (this.location == null) {
            Dimension dim = getStartDimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            setLocation(dim.width, dim.height);
        } else {
            setLocation(this.location);
        }
        this.setVisible(true);
    }

    /**
     * show the north panel
     *
     * @param panel
     */
    private void showNorthPanel(JPanel panel) {
        this.button2 = new JButton("Previous");
        this.button2.setToolTipText("Previous");
        this.button2.addActionListener(this);
        panel.add(this.button2);
        this.comboBox1 = new JComboBox(this.years);
        this.comboBox1.setSelectedItem(String.valueOf(year1));
        this.comboBox1.setToolTipText("Year");
        this.comboBox1.setMaximumRowCount(rowlens);
        this.comboBox1.setActionCommand("year");
        this.comboBox1.addActionListener(this);
        panel.add(this.comboBox1);
        this.comboBox2 = new JComboBox(this.months);
        this.comboBox2.setSelectedItem(" " + formatDay(month1 + 1));
        this.comboBox2.setToolTipText("Month");
        this.comboBox2.setMaximumRowCount(rowlens);
        this.comboBox2.addActionListener(this);
        this.comboBox2.setActionCommand("month");
        panel.add(this.comboBox2);
        this.button3 = new JButton("Next");
        this.button3.setToolTipText("Next");
        this.button3.addActionListener(this);
        panel.add(this.button3);
    }

    /**
     * show the south panel
     *
     * @param panel
     */
    private void showSouthPanel(JPanel panel) {
        JPanel panel_23 = new JPanel();
        panel_23.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JLabel label21 = new JLabel("Time: ");
        label21.setForeground(Color.GRAY);
        panel_23.add(label21);
        this.comboBox3 = new JComboBox(this.getHours());
        this.comboBox3.setMaximumRowCount(rowlens);
        this.comboBox3.setToolTipText("Hr");
        this.comboBox3.setActionCommand("hour");
        this.comboBox3.addActionListener(this);
        panel_23.add(this.comboBox3);
        JLabel label22 = new JLabel("Hour ");
        label22.setForeground(Color.GRAY);
        panel_23.add(label22);
        this.comboBox4 = new JComboBox(this.getMins());
        this.comboBox4.setToolTipText("Min");
        this.comboBox4.setMaximumRowCount(rowlens);
        this.comboBox4.setActionCommand("minute");
        this.comboBox4.addActionListener(this);
        panel_23.add(this.comboBox4);
        JLabel label23 = new JLabel("Min ");
        label23.setForeground(Color.GRAY);
        panel_23.add(label23);
        this.comboBox5 = new JComboBox(this.getMins());
        this.comboBox5.setToolTipText("Sec");
        this.comboBox5.setActionCommand("second");
        this.comboBox5.addActionListener(this);
        this.comboBox5.setMaximumRowCount(rowlens);
        panel_23.add(this.comboBox5);
        JLabel label24 = new JLabel("Sec");
        label24.setForeground(Color.GRAY);
        panel_23.add(label24);
        panel.add(panel_23);
        this.button1 = new JButton("OK");
        this.button1.setToolTipText("OK");
        this.button1.addActionListener(this);
        panel.add(button1);
    }

    /**
     * get the hour value
     *
     * @return
     */
    private Object[] getHours() {
        Object[] hs = new Object[24];
        for (int i = 0; i < hs.length; i++) {
            hs[i] = i;
        }
        return hs;
    }

    /**
     * get the minutes or seconds value
     *
     * @return
     */
    private Object[] getMins() {
        Object[] hs = new Object[60];
        for (int i = 0; i < hs.length; i++) {
            hs[i] = i;
        }
        return hs;
    }

    /**
     * print the calendar
     *
     * @return
     */
    private JPanel printCalendar() {
        panel = new JPanel();
        panel.setLayout(new GridLayout(7, 7, 0, 0));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        int year2 = calendar.get(Calendar.YEAR);
        int month2 = calendar.get(Calendar.MONTH);
        // set the day to the first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        // to get the day of the current week
        int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
        // print the header
        JButton b = null;
        for (int i = 0; i < tits.length; i++) {
            b = new JButton("<html><b>" + tits[i] + "</b></html>");
            b.setForeground(new Color(100, 0, 102));
            b.setEnabled(false);
            panel.add(b);
        }
        int count = 0;
        for (int i = Calendar.SUNDAY; i < weekDay; i++) {
            b = new JButton(" ");
            b.setEnabled(false);
            panel.add(b);
            count++;
        }
        int currday = 0;
        String dayStr = null;
        do {
            currday = calendar.get(Calendar.DAY_OF_MONTH);
            dayStr = formatDay(currday);
            // show if the day, month, year is correct
            if (currday == day1 && month1 == month2 && year1 == year2) {
                b = new JButton("[" + dayStr + "]");
                b.setForeground(Color.RED);
            } else {
                b = new JButton(dayStr);
                b.setForeground(Color.BLUE);
            }
            count++;
            b
                    .setToolTipText(year2 + "-" + formatDay(month2 + 1) + "-"
                                            + dayStr);
            b.setBorder(BorderFactory.createEtchedBorder());
            b.addActionListener(this);
            panel.add(b);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        } while (calendar.get(Calendar.MONTH) == month2);
        this.calendar.add(Calendar.MONTH, -1);
        if (!flag) {
            // set the time to the current day
            this.calendar.set(Calendar.DAY_OF_MONTH, this.day1);
            flag = true;
        }
        for (int i = count; i < 42; i++) {
            b = new JButton(" ");
            b.setEnabled(false);
            panel.add(b);
        }
        return panel;
    }

    /**
     * format the day
     *
     * @param day
     * @return
     */
    private String formatDay(int day) {
        if (day < 10) {
            return "0" + day;
        }
        return String.valueOf(day);
    }

    /**
     * event handler
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("Previous".equals(command)) {
            // 1.month+1
            this.calendar.add(Calendar.MONTH, 1);
            // 2.update the year and month displayed
            int year5 = calendar.get(Calendar.YEAR);
            // check whether the day beyond the maxYear or not
            int maxYear = this.year1 + this.showYears / 2 - 1;
            if (year5 > maxYear) {
                this.calendar.add(Calendar.MONTH, -1);
                return;
            }
            int month5 = calendar.get(Calendar.MONTH) + 1;
            this.comboBox1.setSelectedItem(String.valueOf(year5));
            this.comboBox2.setSelectedItem(" " + this.formatDay(month5));
            // 3.update the panel
            this.updatePanel();
        } else if ("Next".equals(command)) {
            // 1.month-1
            this.calendar.add(Calendar.MONTH, -1);
            // 2.update the year and month displayed
            int year5 = calendar.get(Calendar.YEAR);
            // check whether the day beyond the minYear or not
            int minYear = this.year1 - this.showYears / 2;
            if (year5 < minYear) {
                this.calendar.add(Calendar.MONTH, 1);
                return;
            }
            int month5 = calendar.get(Calendar.MONTH) + 1;
            this.comboBox1.setSelectedItem(String.valueOf(year5));
            this.comboBox2.setSelectedItem(" " + this.formatDay(month5));
            // 3.update the panel
            this.updatePanel();
        } else if ("OK".equals(command)) {
            this.dispose();
        } else if (command.matches("^\\d+$")) {
            // foreground color
            JButton b = (JButton) e.getSource();
            if (this.button4 == null) {
                this.button4 = b;
            } else {
                this.button4.setForeground(Color.BLUE);
                this.button4.setFont(b.getFont());
                this.button4 = b;
            }
            b.setForeground(Color.BLACK);
            b.setFont(button4.getFont().deriveFont(Font.BOLD));
            // set the day
            int day9 = Integer.parseInt(command);
            this.calendar.set(Calendar.DAY_OF_MONTH, day9);
        } else if (command.startsWith("[")) {
            // foreground color
            JButton b = (JButton) e.getSource();
            if (this.button4 == null) {
                this.button4 = b;
            } else {
                this.button4.setForeground(Color.BLUE);
                this.button4.setFont(b.getFont());
                this.button4 = b;
            }
            b.setForeground(Color.BLACK);
            b.setFont(button4.getFont().deriveFont(Font.BOLD));
            // set the day
            this.calendar.set(Calendar.DAY_OF_MONTH, this.day1);
        } else if ("hour".equalsIgnoreCase(command)) {
            // set the value of hour
            int value = Integer.parseInt(this.comboBox3.getSelectedItem()
                                                 .toString().trim());
            this.calendar.set(Calendar.HOUR_OF_DAY, value);
        } else if ("minute".equalsIgnoreCase(command)) {
            // set the value of minute
            int value = Integer.parseInt(this.comboBox4.getSelectedItem()
                                                 .toString().trim());
            this.calendar.set(Calendar.MINUTE, value);
        } else if ("second".equalsIgnoreCase(command)) {
            // set the value of second
            int value = Integer.parseInt(this.comboBox5.getSelectedItem()
                                                 .toString().trim());
            this.calendar.set(Calendar.SECOND, value);
        } else if ("year".equalsIgnoreCase(command)) {
            // set the value of year
            int value = Integer.parseInt(this.comboBox1.getSelectedItem()
                                                 .toString().trim());
            this.calendar.set(Calendar.YEAR, value);
            this.updatePanel();
        } else if ("month".equalsIgnoreCase(command)) {
            // set the value of month
            int value = Integer.parseInt(this.comboBox2.getSelectedItem()
                                                 .toString().trim());
            this.calendar.set(Calendar.MONTH, value - 1);
            this.updatePanel();
        }
    }

    /**
     * update the panel
     */
    private void updatePanel() {
        this.remove(this.panel);
        this.add(this.printCalendar(), BorderLayout.CENTER);
        this.validate();
        this.repaint();
    }

    /**
     * get the show year
     *
     * @return
     */
    public int getShowYears() {
        return showYears;
    }

    // default width and height
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 280;
    // default show year
    private int showYears = 100;
    // ok button
    private JButton button1 = null;
    // previous, next button
    private JButton button2 = null;
    private JButton button3 = null;
    // temporary button
    private JButton button4 = null;
    // combo box
    private JComboBox comboBox1 = null;
    private JComboBox comboBox2 = null;
    // calendar object
    private Calendar calendar = null;
    // arrays for years and months
    private String[] years = null;
    private String[] months = null;
    // current year, month, day
    private int year1, month1, day1;
    private JPanel panel = null;
    private String tits[] = {"Su", "Mo", "Tu", "We", "Th", "Fr", "St"};
    // for hour, minutes, seconds
    private JComboBox comboBox3 = null;
    private JComboBox comboBox4 = null;
    private JComboBox comboBox5 = null;
    // the row lengths for combo box
    private int rowlens = 5;
    private String title = "Choose Time";
    private Point location = null;
    private boolean flag;
}
