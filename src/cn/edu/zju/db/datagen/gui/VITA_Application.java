package cn.edu.zju.db.datagen.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import cn.edu.zju.db.datagen.algorithm.Algorithm;
import cn.edu.zju.db.datagen.algorithm.FPT;
import cn.edu.zju.db.datagen.algorithm.PXM;
import cn.edu.zju.db.datagen.algorithm.TRI;
import cn.edu.zju.db.datagen.database.DB_Connection;
import cn.edu.zju.db.datagen.database.DB_FileUploader;
import cn.edu.zju.db.datagen.database.DB_Import;
import cn.edu.zju.db.datagen.database.DB_WrapperDelete;
import cn.edu.zju.db.datagen.database.DB_WrapperLoad;
import cn.edu.zju.db.datagen.database.spatialobject.AccessPoint;
import cn.edu.zju.db.datagen.database.spatialobject.Connectivity;
import cn.edu.zju.db.datagen.database.spatialobject.Connector;
import cn.edu.zju.db.datagen.database.spatialobject.Floor;
import cn.edu.zju.db.datagen.database.spatialobject.Partition;
import cn.edu.zju.db.datagen.database.spatialobject.UploadObject;
import cn.edu.zju.db.datagen.gui.util.InteractionState;
import cn.edu.zju.db.datagen.gui.util.calendar.JTimeChooser;
import cn.edu.zju.db.datagen.indoorobject.IndoorObjsFactory;
import cn.edu.zju.db.datagen.indoorobject.movingobject.DstMovingObj;
import cn.edu.zju.db.datagen.indoorobject.movingobject.MovingObj;
import cn.edu.zju.db.datagen.indoorobject.movingobject.RegularMultiDestCustomer;
import cn.edu.zju.db.datagen.indoorobject.station.Pack;
import cn.edu.zju.db.datagen.indoorobject.station.Station;
import cn.edu.zju.db.datagen.indoorobject.utility.IdrObjsUtility;
import cn.edu.zju.db.datagen.spatialgraph.D2DGraph;
import diva.util.java2d.Polygon2D;

@SuppressWarnings("serial")
public class VITA_Application extends JApplet {

	private static JFrame mainFrame;
	private static Dimension size = new Dimension(1370, 880);

	private static String lastSelectedFileName = null;

	private JTextField txtselectedNameField;
	private JTextField txtStationMaxNumInPart;
	private JTextField txtStationMaxNumInArea;
	private JTextField txtScanRange;
	private JTextField txtScanRate;
	private JTextField txtMaxMovObjNumInPart;
	private JTextField txtMaximumLifeSpan;
	private JTextField txtMaxStepLength;
	private JTextField txtMoveRate;
	private JTextField txtStartTime;
	private JTextField txtEndTime;
	private JTextField txtRssiInputPath;

	private JButton btnImport;
	private JButton btnDeleteFile;
	private JButton btnView;
	private JButton btnDecompAll;
	private JButton btnDeleteFloor;
	private JButton btnDeleteEntity;
	private JButton btnStationGenerate;
	private JButton btnObjectInit;
	private JButton btnObjectStart;
	private JButton btnObjectStop;
	private JButton btnSnapShot;
	private JButton btnPositionGenerate;

	private JPanel filePanel;
	private JPanel mapPanel;
	private JPanel controlPanel;
	private JPanel dbiPanel;
	private JPanel uclPanel;
	private JPanel movingObjectPanel;
	private JPanel playPanel;
	private JPanel stationPanel;
	private JPanel positionAlgPanel;

	private JLabel lblConnectedPartitions1;
	private JLabel lblselectedEntityLabel2;
	private JLabel lblConnectedPartitions2;
	private JLabel lblStationDistributerType2;
	private JLabel lblMovingObjectType2;
	private JLabel lblMaximumLifeSpan2;
	private JLabel lblMaximumVelocity2;
	private JLabel lblPositionAlgorithm2;
	private JLabel lblmaximumNumberIn;
	private JLabel lblCommentsMaxMovObjNumInPart;
	private JLabel lblmaxStepLength;
	private JLabel lblmoveRate;
	private JLabel lblstartTime;
	private JLabel lblendTime;
	private JLabel lblStationDistributerType1;
	private JLabel lblMaxNumberIn;
	private JLabel lblScanRate1;
	private JLabel lblScanRate2;
	private JLabel lblMovingObjectType1;
	private JLabel lblPositioningParamenters;
	private JLabel lblPositionAlgorithm1;
	private JLabel lblPropertiesFile;
	private JLabel lblGenerationPeriod1;
	private JLabel lblMaximumVelocity1;
	private JLabel lblMaximumLifeSpan1;
	private JLabel lblMaxMovObjNumInPart;
	private JLabel lblMovingObjectParamenters;
	private JLabel lblScanRange2;
	private JLabel lblScanRange1;
	private JLabel lblCommentStationMaxNumInPart;
	private JLabel lblStationType;
	private JLabel lblStationParameters;
	private JLabel lblUserConfigurationLoader;
	private JLabel lblselectedEntityLabel1;
	private JLabel lblselectedFloorLabel;
	private JLabel lblDBIEntities;
	private JLabel lblInputRssiPath;

	public static JTextArea txtConsoleArea;
	private JTextArea positionPropertiesArea;

	private JCheckBox chckbxPositiongData;
	private JCheckBox chckbxTrajectory;
	private JCheckBox chckbxTracking;
	private JCheckBox chckbxPositioningDevice;
	private JCheckBox chckbxEnvironment;

	private JScrollPane scrollPaneConsole;
	private JScrollPane positionPropertiesScrollPane;
	private JScrollPane scrollPanePart;

	private JTabbedPane tabbedVITAPane;

	private JComboBox<String> stationTypeComboBox;
	private JComboBox<String> stationDistriTypeComboBox;
	private JComboBox<String> movingObjectTypeComboBox;
	private JComboBox<String> movObjDistributerTypeComboBox;
	private JComboBox<String> positionAlgorithmComboBox;
	private JComboBox<UploadObject> fileComboBox;
	private JComboBox<Floor> floorCombobox;

	private Calendar startCalendar;
	private Calendar endCalendar;

	private MapPainter mapPainter;
	private Connection connection = null;
	private Floor chosenFloor = null;
	private Partition selectedPart = null;
	private AccessPoint selectedAP = null;
	private Connector selectedCon = null;
	private UploadObject fileChosen = null;
	private JList<Partition> connectedPartsList;
	private DefaultListModel<Partition> connectedPartsModel;
	private ArrayList<UploadObject> files = null;
	private ArrayList<MovingObj> movingObjs = new ArrayList<MovingObj>();
	private ArrayList<MovingObj> destMovingObjs = new ArrayList<MovingObj>();

	private boolean empty = false;
	private double zoom = 1;
	private double previousX;
	private double previousY;
	private double currentX;
	private double currentY;

	private Map<Path2D, Partition> partitionsMap = new HashMap<Path2D, Partition>();
	private Map<Path2D, AccessPoint> accesspointsMap = new HashMap<Path2D, AccessPoint>();
	private Map<Path2D, Connector> connsMap = new HashMap<Path2D, Connector>();

	private HashMap<String, String> stationTypeMap = new HashMap<String, String>();
	private HashMap<String, String> movingObjTypeMap = new HashMap<String, String>();
	private HashMap<String, String> movObjInitMap = new HashMap<String, String>();
	private HashMap<String, String> stationInitMap = new HashMap<String, String>();
	private HashMap<String, String> positionAlgorithmMap = new HashMap<String, String>();

	private List<Partition> possibleConnectedPartsList;
	private JLabel lblConsole;
	private JLabel lblGenerationPeriod2;
	private JLabel lblMovObjDistributerType2;
	private JLabel lblMovObjDistributerType1;

	public VITA_Application() {
	}

	public void init() {
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					gui();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void gui() {

		this.setSize(new Dimension(1370, 870));
		getContentPane().setLayout(null);

		/*---------------------- view panel start here! ----------------------*/

		filePanel = new JPanel();
		filePanel.setBackground(Color.WHITE);
		filePanel.setBounds(10, 10, 800, 30);
		getContentPane().add(filePanel);
		filePanel.setBorder(null);
		filePanel.setLayout(null);

		btnImport = new JButton("");
		btnImport.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		btnImport.setIcon(new ImageIcon(VITA_Application.class.getResource("/cn/edu/zju/db/datagen/gui/import.png")));
		btnImport.setBackground(Color.WHITE);
		btnImport.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		btnImport.setBounds(0, 0, 30, 30);
		filePanel.add(btnImport);

		fileComboBox = new JComboBox<UploadObject>();
		fileComboBox.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		fileComboBox.setBackground(Color.WHITE);
		fileComboBox.setBounds(45, 0, 430, 30);
		filePanel.add(fileComboBox);

		btnDeleteFile = new JButton("");
		btnDeleteFile.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		btnDeleteFile
				.setIcon(new ImageIcon(VITA_Application.class.getResource("/cn/edu/zju/db/datagen/gui/rubbish.png")));
		btnDeleteFile.setBackground(Color.WHITE);
		btnDeleteFile.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		btnDeleteFile.setBounds(480, 0, 30, 30);
		filePanel.add(btnDeleteFile);

		btnView = new JButton("View");
		btnView.setBackground(Color.WHITE);
		btnView.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		btnView.setBounds(555, 0, 120, 30);
		filePanel.add(btnView);

		btnDecompAll = new JButton("Decompose");
		btnDecompAll.setBackground(Color.WHITE);
		btnDecompAll.setFont(new Font("Lucida Grande", Font.PLAIN, 14));
		btnDecompAll.setBounds(680, 0, 120, 30);
		filePanel.add(btnDecompAll);

		/*---------------------- map panel start here! ----------------------*/

		mapPanel = new JPanel();
		mapPanel.setBounds(10, 45, 800, 800);
		getContentPane().add(mapPanel);
		mapPanel.setBackground(Color.WHITE);
		mapPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		mapPanel.setLayout(null);

		/*---------------------- control panel start here! ----------------------*/

		controlPanel = new JPanel();
		controlPanel.setBackground(Color.WHITE);
		controlPanel.setBounds(820, 10, 540, 835);
		getContentPane().add(controlPanel);
		controlPanel.setLayout(null);

		/*---------------------- DBI Entities ----------------------*/

		lblConsole = new JLabel("Console");
		lblConsole.setBounds(5, 0, 160, 30);
		controlPanel.add(lblConsole);
		lblConsole.setForeground(new Color(74, 144, 226));
		lblConsole.setFont(new Font("Dialog", Font.BOLD, 20));
		lblConsole.setBackground(Color.WHITE);

		scrollPaneConsole = new JScrollPane();
		scrollPaneConsole.setBounds(5, 30, 530, 120);
		controlPanel.add(scrollPaneConsole);
		scrollPaneConsole.setBorder(new LineBorder(Color.LIGHT_GRAY));
		scrollPaneConsole.setBackground(Color.WHITE);
		scrollPaneConsole.setWheelScrollingEnabled(true);

		txtConsoleArea = new JTextArea();
		txtConsoleArea.setFont(new Font("Dialog", Font.ITALIC, 14));
		txtConsoleArea.setRows(6);
		txtConsoleArea.setBackground(Color.WHITE);
		txtConsoleArea.setSize(530, 65);
		scrollPaneConsole.setViewportView(txtConsoleArea);
		txtConsoleArea.setEditable(false);
		txtConsoleArea.requestFocus();

		lblDBIEntities = new JLabel("DBI Entities");
		lblDBIEntities.setForeground(new Color(74, 144, 226));
		lblDBIEntities.setBackground(Color.WHITE);
		lblDBIEntities.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblDBIEntities.setBounds(5, 150, 160, 30);
		controlPanel.add(lblDBIEntities);

		dbiPanel = new JPanel();
		dbiPanel.setBackground(Color.WHITE);
		dbiPanel.setBorder(null);
		dbiPanel.setBounds(5, 180, 530, 190);
		controlPanel.add(dbiPanel);
		dbiPanel.setLayout(null);

		lblselectedFloorLabel = new JLabel("Selected Floor: ");
		lblselectedFloorLabel.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblselectedFloorLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblselectedFloorLabel.setBounds(20, 10, 135, 20);
		dbiPanel.add(lblselectedFloorLabel);

		floorCombobox = new JComboBox<Floor>();
		floorCombobox.setFont(new Font("Dialog", Font.PLAIN, 13));
		floorCombobox.setBackground(Color.WHITE);
		floorCombobox.setSize(310, 30);
		floorCombobox.setLocation(180, 5);
		dbiPanel.add(floorCombobox);

		btnDeleteFloor = new JButton("");
		btnDeleteFloor.setBackground(Color.WHITE);
		btnDeleteFloor.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		btnDeleteFloor
				.setIcon(new ImageIcon(VITA_Application.class.getResource("/cn/edu/zju/db/datagen/gui/rubbish.png")));
		btnDeleteFloor.setBounds(500, 8, 16, 25);
		dbiPanel.add(btnDeleteFloor);

		lblselectedEntityLabel1 = new JLabel("Selected Entity ");
		lblselectedEntityLabel1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblselectedEntityLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblselectedEntityLabel1.setBounds(20, 45, 135, 20);
		dbiPanel.add(lblselectedEntityLabel1);

		lblselectedEntityLabel2 = new JLabel("Name: ");
		lblselectedEntityLabel2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblselectedEntityLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblselectedEntityLabel2.setBounds(20, 60, 135, 20);
		dbiPanel.add(lblselectedEntityLabel2);

		txtselectedNameField = new JTextField();
		txtselectedNameField.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtselectedNameField.setBounds(180, 50, 310, 30);
		dbiPanel.add(txtselectedNameField);
		txtselectedNameField.setColumns(10);

		btnDeleteEntity = new JButton("");
		btnDeleteEntity.setBorder(new LineBorder(new Color(0, 0, 0), 0));
		btnDeleteEntity.setBackground(Color.WHITE);
		btnDeleteEntity
				.setIcon(new ImageIcon(VITA_Application.class.getResource("/cn/edu/zju/db/datagen/gui/rubbish.png")));
		btnDeleteEntity.setBounds(500, 53, 16, 25);
		dbiPanel.add(btnDeleteEntity);

		lblConnectedPartitions1 = new JLabel("Connected ");
		lblConnectedPartitions1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblConnectedPartitions1.setHorizontalAlignment(SwingConstants.TRAILING);
		dbiPanel.add(lblConnectedPartitions1);
		lblConnectedPartitions1.setBounds(20, 90, 135, 20);

		lblConnectedPartitions2 = new JLabel("Partitions: ");
		lblConnectedPartitions2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblConnectedPartitions2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblConnectedPartitions2.setBounds(20, 105, 135, 20);
		dbiPanel.add(lblConnectedPartitions2);

		scrollPanePart = new JScrollPane();
		scrollPanePart.setBorder(new LineBorder(Color.LIGHT_GRAY));
		scrollPanePart.setBackground(Color.WHITE);
		scrollPanePart.setBounds(180, 90, 310, 85);
		dbiPanel.add(scrollPanePart);

		connectedPartsModel = new DefaultListModel<Partition>();
		connectedPartsList = new JList<Partition>(connectedPartsModel);
		connectedPartsList.setBackground(Color.WHITE);
		connectedPartsList.setFont(new Font("Dialog", Font.PLAIN, 14));
		connectedPartsList.setVisibleRowCount(6);
		connectedPartsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		connectedPartsList.setSelectedIndex(0);
		connectedPartsList.setLayoutOrientation(JList.VERTICAL);
		connectedPartsList.setBounds(20, 20, 300, 70);
		scrollPanePart.setViewportView(connectedPartsList);

		possibleConnectedPartsList = new ArrayList<>();

		/*---------------------- User Configuration----------------------*/

		lblUserConfigurationLoader = new JLabel("User Configuration");
		lblUserConfigurationLoader.setForeground(new Color(74, 144, 226));
		lblUserConfigurationLoader.setBackground(Color.WHITE);
		lblUserConfigurationLoader.setFont(new Font("Lucida Grande", Font.BOLD, 20));
		lblUserConfigurationLoader.setBounds(5, 370, 260, 30);
		controlPanel.add(lblUserConfigurationLoader);

		uclPanel = new JPanel();
		uclPanel.setBackground(Color.WHITE);
		uclPanel.setLayout(null);
		uclPanel.setBorder(null);
		uclPanel.setBounds(5, 400, 530, 440);
		controlPanel.add(uclPanel);

		tabbedVITAPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedVITAPane.setFont(new Font("Dialog", Font.ITALIC, 18));
		tabbedVITAPane.setBackground(Color.WHITE);
		tabbedVITAPane.setBounds(0, 0, 530, 440);
		uclPanel.add(tabbedVITAPane);

		// Infrastructure Layer start here!

		stationPanel = new JPanel();
		stationPanel.setBackground(Color.WHITE);
		tabbedVITAPane.addTab("Infrastrcuture", null, stationPanel, null);
		stationPanel.setLayout(null);

		lblStationParameters = new JLabel("Export: ");
		lblStationParameters.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblStationParameters.setBackground(Color.WHITE);
		lblStationParameters.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStationParameters.setBounds(10, 10, 135, 20);
		stationPanel.add(lblStationParameters);

		chckbxPositioningDevice = new JCheckBox("Positioning Device");
		chckbxPositioningDevice.setFont(new Font("Dialog", Font.PLAIN, 14));
		chckbxPositioningDevice.setBackground(Color.WHITE);
		chckbxPositioningDevice.setBounds(300, 10, 190, 20);
		chckbxPositioningDevice.setSelected(true);
		stationPanel.add(chckbxPositioningDevice);

		chckbxEnvironment = new JCheckBox("Environment");
		chckbxEnvironment.setSelected(true);
		chckbxEnvironment.setFont(new Font("Dialog", Font.PLAIN, 14));
		chckbxEnvironment.setBackground(Color.WHITE);
		chckbxEnvironment.setBounds(170, 10, 150, 20);
		chckbxEnvironment.setSelected(true);
		stationPanel.add(chckbxEnvironment);

		lblStationType = new JLabel("Device Type: ");
		lblStationType.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblStationType.setBackground(Color.WHITE);
		lblStationType.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStationType.setBounds(10, 45, 135, 20);
		stationPanel.add(lblStationType);

		stationTypeComboBox = new JComboBox<String>();
		stationTypeComboBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		stationTypeComboBox.setBackground(Color.WHITE);
		stationTypeComboBox.setBounds(170, 45, 310, 30);
		stationPanel.add(stationTypeComboBox);

		lblStationDistributerType1 = new JLabel("Deployment ");
		lblStationDistributerType1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblStationDistributerType1.setBackground(Color.WHITE);
		lblStationDistributerType1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStationDistributerType1.setBounds(10, 80, 135, 20);
		stationPanel.add(lblStationDistributerType1);

		lblStationDistributerType2 = new JLabel("Model: ");
		lblStationDistributerType2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblStationDistributerType2.setBackground(Color.WHITE);
		lblStationDistributerType2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblStationDistributerType2.setBounds(10, 95, 135, 20);
		stationPanel.add(lblStationDistributerType2);

		stationDistriTypeComboBox = new JComboBox<String>();
		stationDistriTypeComboBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		stationDistriTypeComboBox.setBackground(Color.WHITE);
		stationDistriTypeComboBox.setBounds(170, 85, 310, 30);
		stationPanel.add(stationDistriTypeComboBox);

		lblMaxNumberIn = new JLabel("Device Number: ");
		lblMaxNumberIn.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaxNumberIn.setBackground(Color.WHITE);
		lblMaxNumberIn.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaxNumberIn.setBounds(10, 145, 135, 20);
		stationPanel.add(lblMaxNumberIn);

		txtStationMaxNumInPart = new JTextField();
		txtStationMaxNumInPart.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtStationMaxNumInPart.setBackground(Color.WHITE);
		txtStationMaxNumInPart.setName("");
		txtStationMaxNumInPart.setColumns(10);
		txtStationMaxNumInPart.setBounds(170, 125, 310, 30);
		stationPanel.add(txtStationMaxNumInPart);

		lblCommentStationMaxNumInPart = new JLabel("*maximum number in a partition");
		lblCommentStationMaxNumInPart.setForeground(Color.DARK_GRAY);
		lblCommentStationMaxNumInPart.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblCommentStationMaxNumInPart.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCommentStationMaxNumInPart.setBounds(280, 155, 200, 15);
		stationPanel.add(lblCommentStationMaxNumInPart);

		txtStationMaxNumInArea = new JTextField();
		txtStationMaxNumInArea.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtStationMaxNumInArea.setBackground(Color.WHITE);
		txtStationMaxNumInArea.setName("");
		txtStationMaxNumInArea.setColumns(10);
		txtStationMaxNumInArea.setBounds(170, 170, 310, 30);
		stationPanel.add(txtStationMaxNumInArea);

		lblmaximumNumberIn = new JLabel("*maximum number in 100 m^2");
		lblmaximumNumberIn.setHorizontalAlignment(SwingConstants.TRAILING);
		lblmaximumNumberIn.setForeground(Color.DARK_GRAY);
		lblmaximumNumberIn.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblmaximumNumberIn.setBounds(280, 200, 200, 15);
		stationPanel.add(lblmaximumNumberIn);

		lblScanRange1 = new JLabel("Detection Range ");
		lblScanRange1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblScanRange1.setBackground(Color.WHITE);
		lblScanRange1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblScanRange1.setBounds(10, 215, 135, 20);
		stationPanel.add(lblScanRange1);

		lblScanRange2 = new JLabel("(meter): ");
		lblScanRange2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblScanRange2.setBackground(Color.WHITE);
		lblScanRange2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblScanRange2.setBounds(10, 230, 135, 20);
		stationPanel.add(lblScanRange2);

		txtScanRange = new JTextField();
		txtScanRange.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtScanRange.setBackground(Color.WHITE);
		txtScanRange.setText((String) null);
		txtScanRange.setColumns(10);
		txtScanRange.setBounds(170, 220, 310, 30);
		stationPanel.add(txtScanRange);

		lblScanRate1 = new JLabel("Detection ");
		lblScanRate1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblScanRate1.setBackground(Color.WHITE);
		lblScanRate1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblScanRate1.setBounds(10, 255, 135, 20);
		stationPanel.add(lblScanRate1);

		lblScanRate2 = new JLabel("Frequency (ms): ");
		lblScanRate2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblScanRate2.setBackground(Color.WHITE);
		lblScanRate2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblScanRate2.setBounds(10, 270, 135, 20);
		stationPanel.add(lblScanRate2);

		txtScanRate = new JTextField();
		txtScanRate.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtScanRate.setBackground(Color.WHITE);
		txtScanRate.setText((String) null);
		txtScanRate.setColumns(10);
		txtScanRate.setBounds(170, 260, 310, 30);
		stationPanel.add(txtScanRate);

		btnStationGenerate = new JButton("Generate Device");
		btnStationGenerate.setFont(new Font("Dialog", Font.BOLD, 14));
		btnStationGenerate.setBackground(Color.WHITE);
		btnStationGenerate.setBounds(320, 340, 160, 40);
		stationPanel.add(btnStationGenerate);

		// Moving Object Layer start here!

		movingObjectPanel = new JPanel();
		movingObjectPanel.setBackground(Color.WHITE);
		tabbedVITAPane.addTab("Moving Object", null, movingObjectPanel, null);
		movingObjectPanel.setLayout(null);

		lblMovingObjectParamenters = new JLabel("Export: ");
		lblMovingObjectParamenters.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMovingObjectParamenters.setBackground(Color.WHITE);
		lblMovingObjectParamenters.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMovingObjectParamenters.setBounds(10, 10, 135, 20);
		movingObjectPanel.add(lblMovingObjectParamenters);

		chckbxTrajectory = new JCheckBox("Trajectory");
		chckbxTrajectory.setFont(new Font("Dialog", Font.PLAIN, 14));
		chckbxTrajectory.setBackground(Color.WHITE);
		chckbxTrajectory.setBounds(170, 10, 120, 20);
		chckbxTrajectory.setSelected(true);
		MovingObj.setTrajectoryFlag(true);
		movingObjectPanel.add(chckbxTrajectory);

		chckbxTracking = new JCheckBox("Raw RSSI");
		chckbxTracking.setFont(new Font("Dialog", Font.PLAIN, 14));
		chckbxTracking.setBackground(Color.WHITE);
		chckbxTracking.setBounds(300, 10, 140, 20);
		chckbxTracking.setSelected(true);
		MovingObj.setTrackingFlag(true);
		movingObjectPanel.add(chckbxTracking);

		lblMovingObjectType1 = new JLabel("Moving Object ");
		lblMovingObjectType1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMovingObjectType1.setBackground(Color.WHITE);
		lblMovingObjectType1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMovingObjectType1.setBounds(10, 45, 135, 20);
		movingObjectPanel.add(lblMovingObjectType1);

		lblMovingObjectType2 = new JLabel("Type: ");
		lblMovingObjectType2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMovingObjectType2.setBackground(Color.WHITE);
		lblMovingObjectType2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMovingObjectType2.setBounds(10, 60, 135, 20);
		movingObjectPanel.add(lblMovingObjectType2);

		movingObjectTypeComboBox = new JComboBox<String>();
		movingObjectTypeComboBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		movingObjectTypeComboBox.setBackground(Color.WHITE);
		movingObjectTypeComboBox.setBounds(170, 50, 310, 30);
		movingObjectPanel.add(movingObjectTypeComboBox);

		lblMovObjDistributerType1 = new JLabel("Initial ");
		lblMovObjDistributerType1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMovObjDistributerType1.setBackground(Color.WHITE);
		lblMovObjDistributerType1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMovObjDistributerType1.setBounds(10, 90, 135, 20);
		movingObjectPanel.add(lblMovObjDistributerType1);

		lblMovObjDistributerType2 = new JLabel("Distribution: ");
		lblMovObjDistributerType2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMovObjDistributerType2.setBackground(Color.WHITE);
		lblMovObjDistributerType2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMovObjDistributerType2.setBounds(10, 105, 135, 20);
		movingObjectPanel.add(lblMovObjDistributerType2);

		movObjDistributerTypeComboBox = new JComboBox<String>();
		movObjDistributerTypeComboBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		movObjDistributerTypeComboBox.setBackground(Color.WHITE);
		movObjDistributerTypeComboBox.setBounds(170, 85, 310, 30);
		movingObjectPanel.add(movObjDistributerTypeComboBox);

		lblMaxMovObjNumInPart = new JLabel("Object Number: ");
		lblMaxMovObjNumInPart.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaxMovObjNumInPart.setBackground(Color.WHITE);
		lblMaxMovObjNumInPart.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaxMovObjNumInPart.setBounds(10, 135, 135, 20);
		movingObjectPanel.add(lblMaxMovObjNumInPart);

		txtMaxMovObjNumInPart = new JTextField();
		txtMaxMovObjNumInPart.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtMaxMovObjNumInPart.setBackground(Color.WHITE);
		txtMaxMovObjNumInPart.setColumns(10);
		txtMaxMovObjNumInPart.setBounds(170, 125, 310, 30);
		movingObjectPanel.add(txtMaxMovObjNumInPart);

		lblCommentsMaxMovObjNumInPart = new JLabel("*maximum number in a partition");
		lblCommentsMaxMovObjNumInPart.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCommentsMaxMovObjNumInPart.setForeground(Color.DARK_GRAY);
		lblCommentsMaxMovObjNumInPart.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblCommentsMaxMovObjNumInPart.setBounds(280, 155, 200, 15);
		movingObjectPanel.add(lblCommentsMaxMovObjNumInPart);

		lblMaximumLifeSpan1 = new JLabel("Maximum Life ");
		lblMaximumLifeSpan1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaximumLifeSpan1.setBackground(Color.WHITE);
		lblMaximumLifeSpan1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumLifeSpan1.setBounds(10, 170, 135, 20);
		movingObjectPanel.add(lblMaximumLifeSpan1);

		lblMaximumLifeSpan2 = new JLabel("Span (s):  ");
		lblMaximumLifeSpan2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaximumLifeSpan2.setBackground(Color.WHITE);
		lblMaximumLifeSpan2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumLifeSpan2.setBounds(10, 185, 135, 20);
		movingObjectPanel.add(lblMaximumLifeSpan2);

		txtMaximumLifeSpan = new JTextField();
		txtMaximumLifeSpan.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtMaximumLifeSpan.setBackground(Color.WHITE);
		txtMaximumLifeSpan.setText((String) null);
		txtMaximumLifeSpan.setColumns(10);
		txtMaximumLifeSpan.setBounds(170, 175, 310, 30);
		movingObjectPanel.add(txtMaximumLifeSpan);

		lblMaximumVelocity1 = new JLabel("Maximum Speed ");
		lblMaximumVelocity1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaximumVelocity1.setBackground(Color.WHITE);
		lblMaximumVelocity1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumVelocity1.setBounds(10, 225, 135, 20);
		movingObjectPanel.add(lblMaximumVelocity1);

		lblMaximumVelocity2 = new JLabel(" (m/ms): ");
		lblMaximumVelocity2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblMaximumVelocity2.setBackground(Color.WHITE);
		lblMaximumVelocity2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblMaximumVelocity2.setBounds(10, 240, 135, 20);
		movingObjectPanel.add(lblMaximumVelocity2);

		txtMaxStepLength = new JTextField();
		txtMaxStepLength.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtMaxStepLength.setBackground(Color.WHITE);
		txtMaxStepLength.setColumns(10);
		txtMaxStepLength.setBounds(170, 225, 145, 30);
		movingObjectPanel.add(txtMaxStepLength);

		lblmaxStepLength = new JLabel("*max step length");
		lblmaxStepLength.setHorizontalAlignment(SwingConstants.TRAILING);
		lblmaxStepLength.setForeground(Color.DARK_GRAY);
		lblmaxStepLength.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblmaxStepLength.setBounds(195, 255, 120, 15);
		movingObjectPanel.add(lblmaxStepLength);

		txtMoveRate = new JTextField();
		txtMoveRate.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtMoveRate.setBackground(Color.WHITE);
		txtMoveRate.setColumns(10);
		txtMoveRate.setBounds(335, 225, 145, 30);
		movingObjectPanel.add(txtMoveRate);

		lblmoveRate = new JLabel("*move rate");
		lblmoveRate.setHorizontalAlignment(SwingConstants.TRAILING);
		lblmoveRate.setForeground(Color.DARK_GRAY);
		lblmoveRate.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblmoveRate.setBounds(360, 255, 120, 15);
		movingObjectPanel.add(lblmoveRate);

		lblGenerationPeriod1 = new JLabel("Generation ");
		lblGenerationPeriod1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblGenerationPeriod1.setBackground(Color.WHITE);
		lblGenerationPeriod1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblGenerationPeriod1.setBounds(10, 275, 135, 20);
		movingObjectPanel.add(lblGenerationPeriod1);

		lblGenerationPeriod2 = new JLabel("Period: ");
		lblGenerationPeriod2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblGenerationPeriod2.setBackground(Color.WHITE);
		lblGenerationPeriod2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblGenerationPeriod2.setBounds(10, 290, 135, 20);
		movingObjectPanel.add(lblGenerationPeriod2);

		txtStartTime = new JTextField();
		txtStartTime.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtStartTime.setBackground(Color.WHITE);
		txtStartTime.setColumns(10);
		txtStartTime.setBounds(170, 275, 145, 30);
		movingObjectPanel.add(txtStartTime);

		lblstartTime = new JLabel("*start time");
		lblstartTime.setHorizontalAlignment(SwingConstants.TRAILING);
		lblstartTime.setForeground(Color.DARK_GRAY);
		lblstartTime.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblstartTime.setBounds(195, 305, 120, 15);
		movingObjectPanel.add(lblstartTime);

		txtEndTime = new JTextField();
		txtEndTime.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtEndTime.setBackground(Color.WHITE);
		txtEndTime.setColumns(10);
		txtEndTime.setBounds(335, 275, 145, 30);
		movingObjectPanel.add(txtEndTime);

		lblendTime = new JLabel("*end time");
		lblendTime.setHorizontalAlignment(SwingConstants.TRAILING);
		lblendTime.setForeground(Color.DARK_GRAY);
		lblendTime.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblendTime.setBounds(360, 305, 120, 15);
		movingObjectPanel.add(lblendTime);

		playPanel = new JPanel();
		playPanel.setBackground(Color.WHITE);
		playPanel.setBounds(20, 340, 460, 40);
		movingObjectPanel.add(playPanel);
		playPanel.setLayout(null);

		btnObjectInit = new JButton("Init");
		btnObjectInit.setFont(new Font("Dialog", Font.BOLD, 14));
		btnObjectInit.setBackground(Color.WHITE);
		btnObjectInit.setBounds(12, 0, 100, 40);
		playPanel.add(btnObjectInit);

		btnObjectStart = new JButton("Start");
		btnObjectStart.setFont(new Font("Dialog", Font.BOLD, 14));
		btnObjectStart.setBackground(Color.WHITE);
		btnObjectStart.setBounds(124, 0, 100, 40);
		playPanel.add(btnObjectStart);

		btnObjectStop = new JButton("Stop");
		btnObjectStop.setFont(new Font("Dialog", Font.BOLD, 14));
		btnObjectStop.setBackground(Color.WHITE);
		btnObjectStop.setBounds(236, 0, 100, 40);
		playPanel.add(btnObjectStop);

		btnSnapShot = new JButton("Snapshot");
		btnSnapShot.setFont(new Font("Dialog", Font.BOLD, 14));
		btnSnapShot.setBackground(Color.WHITE);
		btnSnapShot.setBounds(348, 0, 100, 40);
		playPanel.add(btnSnapShot);

		// Positioning Layer start here!

		positionAlgPanel = new JPanel();
		positionAlgPanel.setBackground(Color.WHITE);
		tabbedVITAPane.addTab("Positioning", null, positionAlgPanel, null);
		positionAlgPanel.setLayout(null);

		lblPositioningParamenters = new JLabel("Export: ");
		lblPositioningParamenters.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblPositioningParamenters.setBackground(Color.WHITE);
		lblPositioningParamenters.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPositioningParamenters.setBounds(10, 10, 135, 20);
		positionAlgPanel.add(lblPositioningParamenters);

		chckbxPositiongData = new JCheckBox("Positioning Data");
		chckbxPositiongData.setFont(new Font("Dialog", Font.PLAIN, 14));
		chckbxPositiongData.setBackground(Color.WHITE);
		chckbxPositiongData.setBounds(170, 10, 160, 20);
		chckbxPositiongData.setSelected(true);
		positionAlgPanel.add(chckbxPositiongData);

		lblPositionAlgorithm1 = new JLabel("Positioning ");
		lblPositionAlgorithm1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblPositionAlgorithm1.setBackground(Color.WHITE);
		lblPositionAlgorithm1.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPositionAlgorithm1.setBounds(10, 45, 135, 20);
		positionAlgPanel.add(lblPositionAlgorithm1);

		lblPositionAlgorithm2 = new JLabel("Algorithm: ");
		lblPositionAlgorithm2.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblPositionAlgorithm2.setBackground(Color.WHITE);
		lblPositionAlgorithm2.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPositionAlgorithm2.setBounds(10, 60, 135, 20);
		positionAlgPanel.add(lblPositionAlgorithm2);

		positionAlgorithmComboBox = new JComboBox<String>();
		positionAlgorithmComboBox.setFont(new Font("Dialog", Font.PLAIN, 13));
		positionAlgorithmComboBox.setBackground(Color.WHITE);
		positionAlgorithmComboBox.setBounds(170, 45, 310, 30);
		positionAlgPanel.add(positionAlgorithmComboBox);

		lblPropertiesFile = new JLabel("Properties: ");
		lblPropertiesFile.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblPropertiesFile.setBackground(Color.WHITE);
		lblPropertiesFile.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPropertiesFile.setBounds(10, 140, 135, 20);
		positionAlgPanel.add(lblPropertiesFile);

		lblInputRssiPath = new JLabel("Input RSSI Path: ");
		lblInputRssiPath.setHorizontalAlignment(SwingConstants.TRAILING);
		lblInputRssiPath.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 14));
		lblInputRssiPath.setBackground(Color.WHITE);
		lblInputRssiPath.setBounds(10, 100, 135, 20);
		positionAlgPanel.add(lblInputRssiPath);

		txtRssiInputPath = new JTextField();
		txtRssiInputPath.setFont(new Font("Dialog", Font.PLAIN, 13));
		txtRssiInputPath.setColumns(10);
		txtRssiInputPath.setBackground(Color.WHITE);
		txtRssiInputPath.setBounds(170, 95, 310, 30);
		positionAlgPanel.add(txtRssiInputPath);

		positionPropertiesScrollPane = new JScrollPane();
		positionPropertiesScrollPane.setBounds(170, 140, 310, 170);
		positionAlgPanel.add(positionPropertiesScrollPane);

		positionPropertiesArea = new JTextArea();
		positionPropertiesArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
		positionPropertiesArea.setBackground(Color.WHITE);
		positionPropertiesScrollPane.setViewportView(positionPropertiesArea);
		positionPropertiesArea.setBorder(new LineBorder(Color.LIGHT_GRAY));

		btnPositionGenerate = new JButton("Generate Positioning Data");
		btnPositionGenerate.setFont(new Font("Dialog", Font.BOLD, 14));
		btnPositionGenerate.setBackground(Color.WHITE);
		btnPositionGenerate.setBounds(240, 340, 240, 40);
		positionAlgPanel.add(btnPositionGenerate);

		addActionListeners();
		addFocusListeners();

		switchStateForButtons(InteractionState.BEFORE_IMPORT);

		updateFileChooser();

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				repaint();
			}

		}, 0, 100);
	}

	private void toggleBtnObjectStart() {
		// TODO Auto-generated method stub
		if (btnObjectInit.isEnabled() && (!txtStartTime.getText().equals("")) && !txtEndTime.getText().equals(""))
			btnObjectStart.setEnabled(true);
	}

	private void addFocusListeners() {

		txtStartTime.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Frame dialogFrame = new Frame();
				Dialog dialog = new Dialog(dialogFrame);
				dialog.setLayout(null);
				JTimeChooser jtc = new JTimeChooser(dialog);
				startCalendar = jtc.showTimeDialog();
				System.out.println(startCalendar.getTime().toString());
				txtStartTime.setText(IdrObjsUtility.sdf.format(startCalendar.getTime()));
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});

		txtEndTime.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {

				btnObjectStart.setEnabled(false);

				Frame dialogFrame = new Frame();
				Dialog dialog = new Dialog(dialogFrame);
				dialog.setLayout(null);
				JTimeChooser jtc = new JTimeChooser(dialog);
				endCalendar = jtc.showTimeDialog();
				System.out.println(endCalendar.getTime().toString());

				try {
					Date selectedStartTime = IdrObjsUtility.sdf.parse(txtStartTime.getText());
					if (endCalendar.getTime().before(selectedStartTime)) {
						JOptionPane.showMessageDialog(mainFrame, "The end time should be later than start time!",
								"Error", JOptionPane.ERROR_MESSAGE);
						txtEndTime.setText("");
					} else {
						txtEndTime.setText(IdrObjsUtility.sdf.format(endCalendar.getTime()));
						toggleBtnObjectStart();
					}
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});

		txtRssiInputPath.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				String previousPath = txtRssiInputPath.getText();
				String inputPath = decideOutputPath();
				if (inputPath != null) {
					txtRssiInputPath.setText(inputPath);
				} else {
					txtRssiInputPath.setText(previousPath);
				}

				if ((!txtRssiInputPath.getText().equals("")) && (txtRssiInputPath.getText() != null)) {
					btnPositionGenerate.setEnabled(true);
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {

			}

			@Override
			public void mouseReleased(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

		});

	}

	private void toggleGeneratationBtns(boolean state) {
		btnDeleteFloor.setEnabled(state);
		btnDeleteEntity.setEnabled(state);
		btnStationGenerate.setEnabled(state);
		btnObjectInit.setEnabled(state);
		toggleMovingObjectGenerationBtns(false);
		btnPositionGenerate.setEnabled(state);
	}

	private void toggleMovingObjectGenerationBtns(boolean state) {
		btnObjectStart.setEnabled(state);
		btnObjectStop.setEnabled(state);
		btnSnapShot.setEnabled(state);
	}

	private void switchStateForButtons(int state) {

		switch (state) {
		case InteractionState.BEFORE_IMPORT:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(false);
			btnView.setEnabled(false);
			btnDecompAll.setEnabled(false);
			toggleGeneratationBtns(false);
			break;
		case InteractionState.AFTER_IMPORT:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(true);
			btnView.setEnabled(true);
			btnDecompAll.setEnabled(false);
			toggleGeneratationBtns(false);
			break;
		case InteractionState.AFTER_UPLOAD_FILE:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(true);
			btnView.setEnabled(true);
			btnDecompAll.setEnabled(false);
			toggleGeneratationBtns(false);
			break;
		case InteractionState.AFTER_VIEW_FILE_NO_CHANGE:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(true);
			btnView.setEnabled(false);
			btnDecompAll.setEnabled(true);
			toggleGeneratationBtns(true);
			break;
		case InteractionState.AFTER_VIEW_FILE_CHANGED:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(true);
			btnView.setEnabled(true);
			btnDecompAll.setEnabled(true);
			toggleGeneratationBtns(true);
			break;
		case InteractionState.AFTER_DECOMPOSE:
			btnImport.setEnabled(true);
			btnDeleteFile.setEnabled(true);
			btnView.setEnabled(false);
			btnDecompAll.setEnabled(false);
			toggleGeneratationBtns(true);
			break;
		default:
			btnImport.setEnabled(false);
			btnDeleteFile.setEnabled(false);
			btnView.setEnabled(false);
			btnDecompAll.setEnabled(false);
			toggleGeneratationBtns(false);
		}

	}

	private void addActionListeners() {

		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uploadFile();
				updateFileChooser();
				if (files.size() > 0) {
					fileComboBox.setSelectedItem(files.get(files.size() - 1));
					printUploadInfo();
					switchStateForButtons(InteractionState.AFTER_IMPORT);
				}
			}
		});

		fileComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {

				fileChosen = (UploadObject) fileComboBox.getSelectedItem();

				if (fileChosen != null) {
					if ((lastSelectedFileName != null) && (!fileChosen.getFilename().equals(lastSelectedFileName))) {
						switchStateForButtons(InteractionState.AFTER_VIEW_FILE_CHANGED);
					}
					lastSelectedFileName = fileChosen.getFilename();
				}
			}

		});

		btnView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				viewFile();

				List<Partition> isolatedPartitions = ClearIllegal
						.calIsolatedPartitions(DB_WrapperLoad.partitionDecomposedT);
				for (Partition isolatedPartition : isolatedPartitions) {
					// ClearIllegal.connectPossiblePartitons(isolatedPartition);
				}
			}
		});

		btnDeleteFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteFile();
			}

		});

	}

	private void printUploadInfo() {
		txtConsoleArea.append("You have extracted: \n");
		txtConsoleArea.append(DB_WrapperLoad.floorT.size() + " floors\n");
		txtConsoleArea.append(DB_WrapperLoad.partitionT.size() + " partitions\n");
		txtConsoleArea.append(DB_WrapperLoad.accesspointT.size() + " doors\n");
		txtConsoleArea.append(DB_WrapperLoad.connectorT.size() + " stairs\n");
		txtConsoleArea.append("----------------------------------------------------------------\n");
	}

	private void printPartAPInfo() {
		txtConsoleArea.append("Viewing File " + fileChosen.getUploadId() + ". " + fileChosen.getFilename() + "\n");
		txtConsoleArea.append("There are in total: \n");
		txtConsoleArea.append(DB_WrapperLoad.floorT.size() + " floors\n");
		txtConsoleArea.append(DB_WrapperLoad.partitionT.size() + " partitions\n");
		txtConsoleArea.append(DB_WrapperLoad.accesspointT.size() + " doors\n");
		txtConsoleArea.append(DB_WrapperLoad.connectorT.size() + " stairs\n");
		txtConsoleArea.append("----------------------------------------------------------------\n");
	}

	private void uploadFile() {
		JFileChooser chooser = new JFileChooser();
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".ifc") || f.isDirectory();
			}

			public String getDescription() {
				return "Ifc Files";
			}
		};
		chooser.setFileFilter(filter);

		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			UploadObject object = new UploadObject();
			object.setFilename(file.getName());
			object.setFile_type("IFC");
			object.setFile_size((int) file.length());
			object.setDescription("");
			if (isFileExisted(object) == true) {
				System.out.println("File already existed!");
				JOptionPane.showMessageDialog(mainFrame, "File already existed!", "Error", JOptionPane.ERROR_MESSAGE);
				txtConsoleArea.append("File Already Existed! PASS\n");
				return;
			}

			txtConsoleArea.append("Uploading File......");
			DB_FileUploader uploader = new DB_FileUploader();
			boolean status = uploader.saveObjectToDB(object, file);
			if (!status) {
				System.out.println("ERROR");
				JOptionPane.showMessageDialog(mainFrame, "Unkown Error!", "Error", JOptionPane.ERROR_MESSAGE);
			} else {
				// Update the table
				updateFileChooser();
				importFile();
				JOptionPane.showMessageDialog(mainFrame, "Uploading File is done!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private boolean isFileExisted(UploadObject uploadFile) {
		for (UploadObject uo : files) {
			if (uploadFile.getFilename().equals(uo.getFilename())
					&& uploadFile.getFile_size() == uploadFile.getFile_size()) {
				return true;
			}
		}
		return false;
	}

	private void importFile() {
		connection = DB_Connection.connectToDatabase("conf/moovework.properties");
		try {
			fileChosen = (UploadObject) fileComboBox.getSelectedItem();
			DB_Import.importAll(connection, fileChosen.getUploadId(), DB_WrapperLoad.loadFile(connection, fileChosen));
		} catch (SQLException ex) {
			Logger lgr = Logger.getLogger(PreparedStatement.class.getName());
			lgr.log(Level.SEVERE, ex.getMessage(), ex);
		}
		// Close the preparedstatement and connection if necessary.
		finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException ex) {
				Logger lgr = Logger.getLogger(PreparedStatement.class.getName());
				lgr.log(Level.SEVERE, ex.getMessage(), ex);
			}
		}
	}

	private void viewFile() {
		int n = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to view the selected file?",
				"Confirmation", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			connection = DB_Connection.connectToDatabase("conf/moovework.properties");

			try {
				fileChosen = (UploadObject) fileComboBox.getSelectedItem();
				DB_WrapperLoad.loadALL(connection, fileChosen.getUploadId());
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			mapPainter = new MapPainter(fileChosen.getUploadId());
			mapPanel.add(mapPainter);
			switchStateForButtons(InteractionState.AFTER_VIEW_FILE_NO_CHANGE);
			printPartAPInfo();
		} else if (n == JOptionPane.NO_OPTION) {
			// Nothing
		} else {
			// Nothing
		}
		return;
	}

	private void deleteFile() {
		int n = JOptionPane.showConfirmDialog(mainFrame, "Are you sure you want to delete the selected file?",
				"Confirmation", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			connection = DB_Connection.connectToDatabase("conf/moovework.properties");

			try {
				fileChosen = (UploadObject) fileComboBox.getSelectedItem();
				txtConsoleArea.append(fileChosen.getFilename() + " is deleted!!!\n");
				DB_WrapperDelete.deleteFile(connection, fileChosen);
				connection.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			fileChosen = null;
			updateFileChooser();
			JOptionPane.showMessageDialog(mainFrame, "Deleting File is done!", "Information",
					JOptionPane.INFORMATION_MESSAGE);
			// updateButtons();
		} else if (n == JOptionPane.NO_OPTION) {
			// Nothing
		} else {
			// Nothing
		}
		return;
	}

	private void updateFileChooser() {
		initFileList();
		loadFileChooser();
	}

	private void initFileList() {
		connection = DB_Connection.connectToDatabase("conf/moovework.properties");
		try {
			files = DB_WrapperLoad.loadFileTable(connection);
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadFileChooser() {
		fileComboBox.removeAllItems();
		for (UploadObject uo : files) {
			fileComboBox.addItem(uo);
		}
		if (files.size() > 0) {
			fileComboBox.setSelectedItem(files.get(files.size() - 1));
			switchStateForButtons(InteractionState.AFTER_UPLOAD_FILE);
		} else {
			switchStateForButtons(InteractionState.BEFORE_IMPORT);
		}
	}

	private String decideOutputPath() {
		String default_path = System.getProperty("user.dir"); // + "//export
		// files";
		String outputPath = default_path;
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(default_path));
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int intRetVal = jfc.showOpenDialog(mainFrame);
		if (intRetVal == JFileChooser.APPROVE_OPTION) {
			outputPath = jfc.getSelectedFile().getPath();
			return outputPath;
		} else {
			return null;
		}

	}

	private class MapPainter extends JPanel {

		private MovingAdapter ma = new MovingAdapter();
		private int fileIDX;
		private boolean stationsGen = false;
		private boolean movingObjsGen = false;

		MapPainter(int fileID) {
			fileIDX = fileID;
			setSize(800, 800);
			setPreferredSize(new Dimension(800, 800));

			btnObjectStart.setEnabled(false);
			btnObjectStop.setEnabled(false);
			btnSnapShot.setEnabled(false);

			loadFloorChooser();

			clearMapPainterActionListener();
			addMapPainterActionListener();

			initUCLComboBox();
			loadPropFromFile("conf/pattern.properties");

			addMouseMotionListener(ma);
			addMouseWheelListener(ma);
			addMouseListener(ma);

			setDoubleBuffered(true);
			setBorder(BorderFactory.createLineBorder(Color.black));
			setOpaque(true);
			setBackground(Color.white);

			D2DGraph buildingD2D = new D2DGraph(DB_WrapperLoad.partitionDecomposedT,
					DB_WrapperLoad.accessPointConnectorT);
			// BuildingD2DGrpah buildingD2D = new BuildingD2DGrpah();
			// BuildingD2DGrpah.partitions =
			// DB_WrapperLoad.partitionDecomposedT;
			// BuildingD2DGrpah.accessPoints =
			// DB_WrapperLoad.accessPointConnectorT;
			// BuildingD2DGrpah.connectors = DB_WrapperLoad.connectorT;
			buildingD2D.generateD2DDistance();
			for (Floor floor : DB_WrapperLoad.floorT) {
				floor.setPartitionsRTree(IdrObjsUtility.generatePartRTree(floor));
				floor.setD2dGraph(buildingD2D);
			}

		}

		private void addMapPainterActionListener() {
			floorCombobox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					chosenFloor = (Floor) floorCombobox.getSelectedItem();
					selectedPart = null;
					selectedAP = null;
					selectedCon = null;
				}

			});

			btnDecompAll.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {

					int n = JOptionPane.showConfirmDialog(mainFrame,
							"Are you sure you want to decompose the selected file?", "Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.YES_OPTION) {
						Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");
						clearIllegal();

						System.out.println(
								"\nBefore decomposed partitions " + DB_WrapperLoad.partitionDecomposedT.size() + "\n");
						try {
							DB_Import.decompose(con);
							DB_WrapperLoad.loadALL(con, fileIDX);
							con.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}

						D2DGraph buildingD2D = new D2DGraph(DB_WrapperLoad.partitionDecomposedT,
								DB_WrapperLoad.accessPointConnectorT);
						// BuildingD2DGrpah.partitions =
						// DB_WrapperLoad.partitionDecomposedT;
						// BuildingD2DGrpah.accessPoints =
						// DB_WrapperLoad.accessPointConnectorT;
						// BuildingD2DGrpah buildingD2D = new
						// BuildingD2DGrpah();
						buildingD2D.generateD2DDistance();

						for (Floor floor : DB_WrapperLoad.floorT) {
							floor.setPartitionsRTree(IdrObjsUtility.generatePartRTree(floor));
							floor.setD2dGraph(buildingD2D);
							System.out.println(
									floor.getName() + " " + floor.getPartsAfterDecomposed().size() + " partitions\n");

						}

						System.out.println("In total: " + DB_WrapperLoad.partitionDecomposedT.size() + " partitions\n");
						JOptionPane.showMessageDialog(mainFrame, "Decomposing File is done!", "Information",
								JOptionPane.INFORMATION_MESSAGE);
						switchStateForButtons(InteractionState.AFTER_DECOMPOSE);
						selectedAP = null;
						selectedPart = null;
						loadFloorChooser();
						updateSelectPartsList();
						repaint();
					} else if (n == JOptionPane.NO_OPTION) {
						// Nothing
					} else {
						// Nothing
					}

				}
			});

			btnDeleteEntity.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int n = JOptionPane.showConfirmDialog(mainFrame,
							"Are you sure you want to delete the selected entity?", "Confirmation",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.YES_OPTION) {
						Connection con = DB_Connection.connectToDatabase("conf/moovework.properties");
						try {
							if (selectedPart != null) {
								DB_WrapperDelete.deletePartition(con, selectedPart);
							} else if (selectedAP != null) {
								DB_WrapperDelete.deleteAccessPoint(con, selectedAP);
							}
							DB_WrapperLoad.loadALL(con, fileIDX);
							con.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						JOptionPane.showMessageDialog(mainFrame, "Deleting Entity is done!", "Information",
								JOptionPane.INFORMATION_MESSAGE);
						loadFloorChooser();
						repaint();
					} else if (n == JOptionPane.NO_OPTION) {
						// Nothing
					} else {
						// Nothing
					}

				}

			});

			connectedPartsList.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent e) {
					repaint();
				}
			});

			btnStationGenerate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					storePropFromGUI("conf/pattern.properties");
					generateStations();
					stationsGen = true;
					if (movingObjsGen == true) {
						toggleBtnObjectStart();
					}
					if (chckbxPositioningDevice.isSelected() || chckbxEnvironment.isSelected()) {
						String outputPath = decideOutputPath();
						if (outputPath != null) {
							if (chckbxEnvironment.isSelected()) {
								String envDir = createEnvironmentOutputDir(outputPath);
								exportEnvironment(envDir);
							}
							if (chckbxPositioningDevice.isSelected()) {
								String stationDir = createStationOutputDir(outputPath);
								exportStations(stationDir);
							}
							JOptionPane.showMessageDialog(mainFrame, "Generating Infrastrcture Data is done!",
									"Information", JOptionPane.INFORMATION_MESSAGE);
						}
						String selected_station_type = stationTypeComboBox.getSelectedItem().toString();
						if (selected_station_type.equals("WIFI")) {
							initAlgorithmMap();
						} else {
							initAlgorithmMapForRFIDAndBT();
						}
					}
				}

			});

			chckbxTrajectory.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (chckbxTrajectory.isSelected() == true) {
						MovingObj.setTrajectoryFlag(true);
					} else {
						MovingObj.setTrajectoryFlag(false);
					}
				}

			});

			chckbxTracking.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (chckbxTracking.isSelected() == true) {
						MovingObj.setTrackingFlag(true);
					} else {
						MovingObj.setTrackingFlag(false);
					}
				}

			});

			btnObjectInit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					storePropFromGUI("conf/pattern.properties");
					generateMovingObjs();
					movingObjsGen = true;
					if (stationsGen == true) {
						btnObjectStart.setEnabled(true);
					}
				}
			});

			btnObjectStart.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (btnObjectStart.getText().equals("Start")) {
						if (chckbxTrajectory.isSelected() || chckbxTracking.isSelected()) {
							String outputPath = decideOutputPath();
							if (outputPath != null) {
								createMovingObjectOutputDir(outputPath);
								storePropFromGUI("conf/pattern.properties");
								btnStationGenerate.setEnabled(false);
								btnObjectInit.setEnabled(false);
								btnObjectStart.setText("Pause");
								btnObjectStop.setEnabled(true);
								btnSnapShot.setEnabled(false);
								setStartEndTimer();
								updateAlgProps();
							}
						}
					} else if (btnObjectStart.getText().equals("Pause")) {
						btnSnapShot.setEnabled(true);
						pauseIndoorObj();
						btnObjectStart.setText("Resume");
					} else if (btnObjectStart.getText().equals("Resume")) {
						btnSnapShot.setEnabled(false);
						pauseIndoorObj();
						btnObjectStart.setText("Pause");
					}
				}

			});

			btnObjectStop.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					stopIndoorObj();
					btnStationGenerate.setEnabled(true);
					btnObjectInit.setEnabled(true);
					btnObjectStart.setEnabled(false);
					btnObjectStop.setEnabled(false);
					btnObjectStart.setText("Start");
					btnSnapShot.setEnabled(false);
					// System.out.println(IdrObjsUtility.trajDir);
				}

			});

			btnSnapShot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String outputPath = decideOutputPath();
					if (outputPath != null) {
						createSnapshotOutputDir(outputPath);
						snapShot(movingObjs);
					}

				}
			});

			positionAlgorithmComboBox.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (positionAlgorithmComboBox.getSelectedItem() != null) {
						String algorithmType = positionAlgorithmComboBox.getSelectedItem().toString();
						loadAlgorithmProp(algorithmType);
					}
				}
			});

			chckbxPositiongData.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (chckbxPositiongData.isSelected() == true) {
						Algorithm.exportFlag = true;
					} else {
						Algorithm.exportFlag = false;
					}
				}
			});

			btnPositionGenerate.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (chckbxPositiongData.isSelected()) {
						String outputPath = decideOutputPath();
						if (outputPath != null) {
							String postioningOutputPath = createPositioningOutputDir(outputPath);
							storeAlgProp();
							String selected_algorithm = positionAlgorithmComboBox.getSelectedItem().toString();

							ExecutorService threadPool = Executors.newCachedThreadPool();

							if ("Trilateration".equals(selected_algorithm)) {
								TRI tri = new TRI("conf/trilateration.properties", txtRssiInputPath.getText(),
										postioningOutputPath);
								tri.calAlgorithmForAll(threadPool);
							} else if ("Fingerprinting".equals(selected_algorithm)) {
								FPT fpt = new FPT("conf/fingerprint.properties", txtRssiInputPath.getText(),
										postioningOutputPath);
								fpt.calAlgorithmForAll(threadPool);
							} else {
								PXM pxm = new PXM("conf/proximity.properties", txtRssiInputPath.getText(),
										postioningOutputPath);
								pxm.calAlgorithmForAll(threadPool);
							}
							exportPositioningConfiguration(postioningOutputPath);

							threadPool.shutdown();

							try {
								threadPool.awaitTermination(50, TimeUnit.SECONDS);
							} catch (InterruptedException e) {
								//
								e.printStackTrace();
							}

							JOptionPane.showMessageDialog(mainFrame, "Generating Indoor Positioning Data is done!",
									"Information", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			});

		}

		/**
		 * illegal element --> self-intersection partitions, isolated access
		 * points warning element --> isolated partitions print all the
		 * isolation partitions and access points get all the self-intersection
		 * partition, and delete them all, because they can caught an exception
		 * when decompose delete all the isolation access points, try to fix
		 * isolate partitions remember to reload all the space object, because
		 * some partitions may have benn deleted
		 */
		private void clearIllegal() {
			PrintIsolatedObject.printAllIsolation(DB_WrapperLoad.partitionDecomposedT,
					DB_WrapperLoad.accessPointConnectorT);

			List<Partition> intersectionPartitions = ClearIllegal
					.calIntersectionPartitions(DB_WrapperLoad.partitionDecomposedT);
			System.out.println(intersectionPartitions);
			List<AccessPoint> isolatedAccessPoints = ClearIllegal
					.calIsolatedAccessPoints(DB_WrapperLoad.accessPointConnectorT);
			System.out.println(isolatedAccessPoints);

			Connection connection = DB_Connection.connectToDatabase("conf/moovework.properties");

			try {
				for (Partition intersectionPartition : intersectionPartitions) {
					System.out.println("delete " + intersectionPartition);
					DB_WrapperDelete.deletePartition(connection, intersectionPartition);
				}

				for (AccessPoint isolatedAccessPoint : isolatedAccessPoints) {
					System.out.println("delete " + isolatedAccessPoint);
					DB_WrapperDelete.deleteAccessPoint(connection, isolatedAccessPoint);
				}

				DB_WrapperLoad.loadALL(connection, fileChosen.getUploadId());
				for (Floor floor : DB_WrapperLoad.floorT) {
					floor.setPartitionsRTree(IdrObjsUtility.generatePartRTree(floor));
				}

				List<Partition> isolatedPartitions = ClearIllegal
						.calIsolatedPartitions(DB_WrapperLoad.partitionDecomposedT);
				for (Partition isolatedPartition : isolatedPartitions) {
					ClearIllegal.connectPossiblePartitons(isolatedPartition);
				}

				DB_WrapperLoad.loadALL(connection, fileChosen.getUploadId());
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void setStartEndTimer() {

			Timer startTimer = new Timer();

			try {
				IdrObjsUtility.objectGenerateStartTime = startCalendar == null
						? IdrObjsUtility.sdf.parse(txtStartTime.getText()) : startCalendar.getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			try {
				IdrObjsUtility.objectGenerateEndTime = endCalendar == null
						? IdrObjsUtility.sdf.parse(txtEndTime.getText()) : endCalendar.getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			long generatonPeriod = IdrObjsUtility.objectGenerateEndTime.getTime()
					- IdrObjsUtility.objectGenerateStartTime.getTime();

			Date startPoint = new Date(System.currentTimeMillis());
			IdrObjsUtility.startClickedTime = startPoint;
			Date endPoint = new Date(startPoint.getTime() + generatonPeriod);

			startTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					startIndoorObj();
				}
			}, startPoint);

			Timer endTimer = new Timer();

			endTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					btnObjectStop.doClick();
				}
			}, endPoint);
		}

		private void updateAlgProps() {
			// IdrObjsUtility.changeAllAlgInputPath();
			String algorithmType = positionAlgorithmComboBox.getSelectedItem().toString();
			loadAlgorithmProp(algorithmType);
		}

		private void loadAlgorithmProp(String algorithmType) {
			if (algorithmType.equals("Trilateration")) {
				loadAlgPropToGUI("conf/trilateration.properties");
			}
			if (algorithmType.equals("Fingerprinting")) {
				loadAlgPropToGUI("conf/fingerprint.properties");
			}
			if (algorithmType.equals("Proximity_Analysis")) {
				loadAlgPropToGUI("conf/proximity.properties");
			}
		}

		private void loadAlgPropToGUI(String algPropName) {
			System.out.println(algPropName);
			positionPropertiesArea.setText("");
			try {
				FileReader fileReader = new FileReader(algPropName);
				BufferedReader buff = new BufferedReader(fileReader);
				String line = null;
				while ((line = buff.readLine()) != null) {
					positionPropertiesArea.append(line + "\n");
				}
				fileReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void storeAlgProp() {
			String algorithmType = positionAlgorithmComboBox.getSelectedItem().toString();
			String outputFileName = null;
			System.out.println(algorithmType);
			if (algorithmType.equals("Trilateration")) {
				outputFileName = "conf/trilateration.properties";
			}
			if (algorithmType.equals("Fingerprinting")) {
				outputFileName = "conf/fingerprint.properties";
			}
			if (algorithmType.equals("Proximity_Analysis")) {
				outputFileName = "conf/proximity.properties";
			}

			try {
				FileWriter writer = new FileWriter(outputFileName);
				String positionPropsTxt = positionPropertiesArea.getText();

				boolean dateFlag = true;
				String[] lines = positionPropsTxt.split("\n");
				for (String line : lines) {
					System.out.println(line);
					// update the time-stamp for properties file
					if (dateFlag && "#".equals(line.substring(0, 1))) {
						dateFlag = false;
						writer.write("#" + new Date(System.currentTimeMillis()).toString() + "\n");
					} else
						writer.write(line + "\n");
				}

				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void clearMapPainterActionListener() {
			for (ActionListener al : floorCombobox.getActionListeners()) {
				floorCombobox.removeActionListener(al);
			}

			for (ActionListener al : btnDecompAll.getActionListeners()) {
				btnDecompAll.removeActionListener(al);
			}

			for (ActionListener al : btnDeleteEntity.getActionListeners()) {
				btnDeleteEntity.removeActionListener(al);
			}

			for (ListSelectionListener al : connectedPartsList.getListSelectionListeners()) {
				connectedPartsList.removeListSelectionListener(al);
			}

			for (ActionListener al : btnStationGenerate.getActionListeners()) {
				btnStationGenerate.removeActionListener(al);
			}

			for (ActionListener al : btnObjectInit.getActionListeners()) {
				btnObjectInit.removeActionListener(al);
			}

			for (ActionListener al : btnObjectStart.getActionListeners()) {
				btnObjectStart.removeActionListener(al);
			}

			for (ActionListener al : btnObjectStop.getActionListeners()) {
				btnObjectStop.removeActionListener(al);
			}

			for (ActionListener al : btnSnapShot.getActionListeners()) {
				btnSnapShot.removeActionListener(al);
			}

			for (ActionListener al : stationTypeComboBox.getActionListeners()) {
				stationTypeComboBox.removeActionListener(al);
			}
		}

		private void initUCLComboBox() {
			initStationTypeMap();
			initStationInitMap();
			initMovingObjTypeMap();
			initMovObjInitMap();
			initAlgorithmMap();
		}

		private void initStationTypeMap() {
			try {
				FileReader fileReader = new FileReader("conf/StationsTypeMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				stationTypeMap.clear();
				stationTypeComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					stationTypeMap.put(splitedString[0], splitedString[1]);
					stationTypeComboBox.addItem(splitedString[0]);
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void initMovingObjTypeMap() {
			try {
				FileReader fileReader = new FileReader("conf/MovingObjsTypeMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				movingObjTypeMap.clear();
				movingObjectTypeComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					movingObjTypeMap.put(splitedString[0], splitedString[1]);
					movingObjectTypeComboBox.addItem(splitedString[0]);
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void initMovObjInitMap() {
			try {
				FileReader fileReader = new FileReader("conf/MovObjsInitMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				movObjInitMap.clear();
				movObjDistributerTypeComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					movObjInitMap.put(splitedString[0], splitedString[1]);
					movObjDistributerTypeComboBox.addItem(splitedString[0]);
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void initStationInitMap() {
			try {
				FileReader fileReader = new FileReader("conf/StationsInitMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				stationInitMap.clear();
				stationDistriTypeComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					stationInitMap.put(splitedString[0], splitedString[1]);
					stationDistriTypeComboBox.addItem(splitedString[0]);
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void initAlgorithmMap() {
			try {
				FileReader fileReader = new FileReader("conf/algorithmTypeMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				positionAlgorithmMap.clear();
				for (int i = 0; i < positionAlgorithmComboBox.getItemCount(); i++) {
					System.out.println("--" + positionAlgorithmComboBox.getItemAt(i));
				}
				positionAlgorithmComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					positionAlgorithmMap.put(splitedString[0], splitedString[1]);
					positionAlgorithmComboBox.addItem(splitedString[0]);
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void initAlgorithmMapForRFIDAndBT() {
			try {
				FileReader fileReader = new FileReader("conf/algorithmTypeMap.properties");
				BufferedReader buff = new BufferedReader(fileReader);
				String line;
				positionAlgorithmMap.clear();
				for (int i = 0; i < positionAlgorithmComboBox.getItemCount(); i++) {
					System.out.println("--" + positionAlgorithmComboBox.getItemAt(i));
				}
				positionAlgorithmComboBox.removeAllItems();
				while ((line = buff.readLine()) != null) {
					String[] splitedString = new String[2];
					splitedString = line.split("=");
					if (!splitedString[0].equals("Fingerprinting")) {
						positionAlgorithmMap.put(splitedString[0], splitedString[1]);
						positionAlgorithmComboBox.addItem(splitedString[0]);
					}
				}
				fileReader.close();
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			chosenFloor = (Floor) floorCombobox.getSelectedItem();

			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			AffineTransform tx = getCurrentTransform();

			Stroke Pen1, Pen2, PenDash;
			Pen1 = new BasicStroke(1.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
			Pen2 = new BasicStroke(3.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
			float dash1[] = { 5.0f };
			PenDash = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

			paintIFCObjects(chosenFloor, g2, tx, Pen1, Pen2, PenDash);

			IdrObjsUtility.paintMovingObjs(chosenFloor, g2, tx, Pen1, movingObjs, new Color(116, 124, 155));
			IdrObjsUtility.paintMovingObjs(chosenFloor, g2, tx, Pen1, destMovingObjs, new Color(116, 124, 155));
			IdrObjsUtility.paintStations(chosenFloor, g2, tx, Pen1, new Color(245, 166, 35, 120));

			// Point2D.Double point1 = new Point2D.Double(343, 250);
			// DstMovingObj dstMovingObj = new
			// DstMovingObj(DB_WrapperLoad.floorT.get(3), point1);
			// dstMovingObj.setCurrentPartition(IdrObjsUtility.findPartitionForPoint(DB_WrapperLoad.floorT.get(1),
			// point1));
			// Point2D.Double point2 = new Point2D.Double(405, 180);
			// dstMovingObj.setCurDestPoint(point2);
			// dstMovingObj.setCurDestFloor(DB_WrapperLoad.floorT.get(2));
			// // paintTrajectory(g2, tx, Pen2, dstMovingObj, chosenFloor);
		}

		private void paintIFCObjects(Floor chosenFloor, Graphics2D g2, AffineTransform tx, Stroke Pen1, Stroke Pen2,
				Stroke PenDash) {
			empty = false;

			if (chosenFloor == null) {
				empty = true;

			} else if (chosenFloor.getPartitions().isEmpty()) {
				empty = true;
			}

			if (!empty) {
				paintPartitions(g2, tx, Pen1, Pen2);
				paintAccessPoints(g2, tx, PenDash, Pen1);
				paintConnectors(g2, tx, Pen1, PenDash);
			} else {
				g2.drawString("No partitions on this floor!", 100, 300);
			}
		}

		private void paintPartitions(Graphics2D g2, AffineTransform tx, Stroke Pen1, Stroke Pen2) {
			partitionsMap.clear();
			for (Partition r : chosenFloor.getPartsAfterDecomposed()) {
				Polygon2D.Double po = r.getPolygon2D();

				Path2D poNew = (Path2D) tx.createTransformedShape(po);
				partitionsMap.put(poNew, r);

				if (r == selectedPart) {
					g2.setColor(new Color(74, 144, 226));
				} else if (connectedPartsList.getSelectedValuesList().contains(r)) {
					g2.setColor(new Color(103, 109, 116));
				} else {
					g2.setColor(new Color(249, 248, 246));
				}
				g2.fill(poNew);

				if (r == selectedPart) {
					g2.setColor(new Color(173, 173, 173));
					g2.setStroke(Pen1);
				} else if (connectedPartsList.getSelectedValuesList().contains(r)) {
					g2.setColor(new Color(173, 173, 173));
					g2.setStroke(Pen1);
				} else {
					g2.setColor(new Color(173, 173, 173));
					g2.setStroke(Pen1);
				}
				g2.draw(poNew);

				paintNameOnPart(r, g2, poNew);
			}
			if (selectedPart != null) {
				paintConnectedPartitions(g2, tx, Pen1, Pen2);
			}
		}

		private void paintNameOnPart(Partition r, Graphics2D g2, Path2D poNew) {
			if (r.getPolygonGIS().numPoints() < 6) {

				String s = r.getName();

				FontRenderContext frc = g2.getFontRenderContext();
				Font font = g2.getFont().deriveFont(10f);
				g2.setColor(Color.black);
				g2.setFont(font);
				float sw = (float) font.getStringBounds(s, frc).getWidth();
				LineMetrics lm = font.getLineMetrics(s, frc);
				float sh = lm.getAscent() + lm.getDescent();

				float sx = (float) (poNew.getBounds2D().getX() + (poNew.getBounds2D().getWidth() - sw) / 2);
				float sy = (float) (poNew.getBounds2D().getY() + (poNew.getBounds2D().getHeight() + sh) / 2
						- lm.getDescent());
				g2.drawString(s, sx, sy);
			}
		}

		private void paintConnectedPartitions(Graphics2D g2, AffineTransform tx, Stroke Pen1, Stroke Pen2) {
			for (Partition part : selectedPart.getConParts()) {
				if (part.getFloor() != selectedPart.getFloor()) {
					continue;
				}
				Polygon2D.Double po = part.getPolygon2D();
				Path2D poNew = (Path2D) tx.createTransformedShape(po);

				g2.setColor(new Color(203, 209, 216));
				g2.fill(poNew);

				g2.setColor(new Color(173, 173, 173));
				g2.setStroke(Pen1);
				g2.draw(poNew);

				paintNameOnPart(part, g2, poNew);

			}
		}

		private void paintAccessPoints(Graphics2D g2, AffineTransform tx, Stroke PenDash, Stroke Pen1) {
			accesspointsMap.clear();
			for (AccessPoint ap : chosenFloor.getAccessPoints()) {

				Path2D clickBox = (Path2D) tx.createTransformedShape(ap.getLine2DClickBox());

				Path2D newLine = (Path2D) tx.createTransformedShape(ap.getLine2D());

				accesspointsMap.put(clickBox, ap);

				if (ap == selectedAP) {
					g2.setColor(new Color(74, 144, 226));
					g2.setStroke(Pen1);
				} else if (ap.getApType().equals(2)) {
					g2.setBackground(new Color(116, 124, 155));
					g2.setColor(new Color(248, 231, 28));
					g2.setStroke(PenDash);
				} else {
					g2.setColor(new Color(144, 19, 254));
					g2.setStroke(Pen1);
				}
				g2.draw(newLine);
			}
		}

		private void paintConnectors(Graphics2D g2, AffineTransform tx, Stroke Pen1, Stroke penDash) {
			connsMap.clear();
			g2.setStroke(penDash);
			for (Connector c : chosenFloor.getConnectors()) {
				Point2D.Double point1 = c.getLocation2D();
				Point2D.Double point2 = c.getUpperLocation2D();
				double x, y, w, h;
				if (point2 == null) {
					x = point1.getX();
					y = point1.getY();
					w = 3;
					h = 3;
				} else {
					x = Math.min(point1.getX(), point2.getX());
					y = Math.min(point1.getY(), point2.getY());
					w = Math.abs(point1.getX() - point2.getX());
					h = Math.abs(point1.getY() - point2.getY());
				}
				Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, w + 3, h + 3);
				Path2D newRect = (Path2D) tx.createTransformedShape(rectangle);
				connsMap.put(newRect, c);
				if (selectedCon == c) {
					g2.setColor(new Color(74, 144, 226));

					for (Partition part : selectedCon.getPartitions()) {
						if (part.getFloor() != selectedCon.getFloor()) {
							continue;
						}
						Polygon2D.Double po = part.getPolygon2D();
						Path2D poNew = (Path2D) tx.createTransformedShape(po);

						g2.setColor(new Color(203, 209, 216));
						g2.fill(poNew);

						g2.setColor(new Color(173, 173, 173));
						g2.setStroke(Pen1);
						g2.draw(poNew);

						paintNameOnPart(part, g2, poNew);

					}
					g2.setColor(new Color(144, 19, 254));

				} else {
					g2.setColor(new Color(49, 76, 206));
				}
				g2.setStroke(penDash);
				g2.draw(newRect);
			}

			for (Connector c : DB_WrapperLoad.connectorT) {
				if (c.getUpperFloor() == chosenFloor) {
					Point2D.Double point1 = c.getLocation2D();
					Point2D.Double point2 = c.getUpperLocation2D();
					double x, y, w, h;
					if (point2 == null) {
						x = point1.getX();
						y = point1.getY();
						w = 3;
						h = 3;
					} else {
						x = Math.min(point1.getX(), point2.getX());
						y = Math.min(point1.getY(), point2.getY());
						w = Math.abs(point1.getX() - point2.getX());
						h = Math.abs(point1.getY() - point2.getY());
					}
					Rectangle2D.Double rectangle = new Rectangle2D.Double(x, y, w + 3, h + 3);
					Path2D newRect = (Path2D) tx.createTransformedShape(rectangle);
					connsMap.put(newRect, c);
					if (selectedCon == c) {
						g2.setColor(new Color(74, 144, 226));
					} else {
						g2.setColor(new Color(49, 188, 77));

					}
					g2.draw(newRect);
				}
			}
		}

		private void updateSelectPartsList() {
			connectedPartsModel.clear();
			if (selectedPart != null) {
				txtselectedNameField.setText(selectedPart.getName() + " AND ID: " + selectedPart.getItemID());
				for (Partition partition : selectedPart.getConParts()) {
					connectedPartsModel.addElement(partition);
				}
			} else if (selectedAP != null) {
				txtselectedNameField.setText(selectedAP.getName() + " AND ID: " + selectedAP.getItemID());
				for (Partition p : selectedAP.getPartitions()) {
					connectedPartsModel.addElement(p);
				}
			} else if (selectedCon != null) {
				txtselectedNameField.setText(selectedCon.getName() + " AND ID: " + selectedCon.getItemID());
				for (Partition p : selectedCon.getPartitions()) {

					connectedPartsModel.addElement(p);
				}

			} else {

			}
		}

		private AffineTransform getCurrentTransform() {

			AffineTransform tx = new AffineTransform();

			double centerX = (double) getWidth() / 2;
			double centerY = (double) getHeight() / 2;

			tx.translate(centerX, centerY);
			tx.scale(zoom, zoom);
			tx.translate(currentX - centerX, currentY - centerY);

			return tx;
		}

		private void incrementZoom(double amount) {
			zoom += amount;
			zoom = Math.max(0.00001, zoom);
			repaint();
		}

		private Point2D getTranslatedPoint(double panelX, double panelY) {

			AffineTransform tx = getCurrentTransform();
			Point2D point2d = new Point2D.Double(panelX, panelY);
			try {
				return tx.inverseTransform(point2d, null);
			} catch (NoninvertibleTransformException ex) {
				ex.printStackTrace();
				return null;
			}
		}

		private void loadFloorChooser() {
			String floor_globalid = null;
			if (floorCombobox.getSelectedItem() != null) {
				Floor f = (Floor) floorCombobox.getSelectedItem();
				floor_globalid = f.getGlobalID();
			}
			floorCombobox.removeAllItems();
			for (int i = 0; i < DB_WrapperLoad.floorT.size(); i++) {
				Floor f = DB_WrapperLoad.floorT.get(i);
				floorCombobox.addItem(f);
			}
			if (floor_globalid != null) {
				for (int i = 0; i < floorCombobox.getModel().getSize(); i++) {
					if (floorCombobox.getModel().getElementAt(i).getGlobalID().equals(floor_globalid)) {
						floorCombobox.setSelectedItem(floorCombobox.getModel().getElementAt(i));
						break;
					}
				}
			}
		}

		private void generateStations() {
			for (Floor floor : DB_WrapperLoad.floorT) {
				floor.getStations().clear();
			}
			IndoorObjsFactory initlizer = new IndoorObjsFactory();
			for (Floor floor : DB_WrapperLoad.floorT) {
				ArrayList<Station> stations = new ArrayList<Station>();
				initlizer.generateStationsOnFloor(floor, stations);
				floor.setStations(stations);
				floor.setStationsRTree(IdrObjsUtility.generateStationRTree(floor.getStations()));
			}
		}

		private void exportEnvironment(String envDir) {

			Date date = new Date(System.currentTimeMillis());
			String time = IdrObjsUtility.dir_sdf.format(date);

			String currentPath = envDir + "//" + time;
			new File(currentPath).mkdirs();

			File file = null;
			FileOutputStream outStr = null;
			BufferedOutputStream buff = null;

			String floor_outputPath = currentPath + "//Floors" + ".txt";
			try {
				file = new File(floor_outputPath);
				file.createNewFile();
				outStr = new FileOutputStream(file);
				buff = new BufferedOutputStream(outStr);
				for (Floor floor : DB_WrapperLoad.floorT) {
					buff.write((floor.toString() + "\n").getBytes());
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String accessPoint_outputPath = currentPath + "//Access Points" + ".txt";
			try {
				file = new File(accessPoint_outputPath);
				file.createNewFile();
				outStr = new FileOutputStream(file);
				buff = new BufferedOutputStream(outStr);
				for (Floor floor : DB_WrapperLoad.floorT) {
					for (AccessPoint ap : floor.getAccessPoints())
						buff.write((ap.toString() + "\n").getBytes());
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String parition_outputPath = currentPath + "//Partitions" + ".txt";
			try {
				file = new File(parition_outputPath);
				file.createNewFile();
				outStr = new FileOutputStream(file);
				buff = new BufferedOutputStream(outStr);
				for (Floor floor : DB_WrapperLoad.floorT) {
					for (Partition par : floor.getPartitions())
						buff.write((par.toString2() + "\n").getBytes());
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String connector_outputPath = currentPath + "//Connectors" + ".txt";
			try {
				file = new File(connector_outputPath);
				file.createNewFile();
				outStr = new FileOutputStream(file);
				buff = new BufferedOutputStream(outStr);
				for (Floor floor : DB_WrapperLoad.floorT) {
					for (Connector connector : floor.getConnectors())
						buff.write((connector.toString() + "\n").getBytes());
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String connectivity_outputPath = currentPath + "//Connectivity" + ".txt";
			try {
				file = new File(connectivity_outputPath);
				file.createNewFile();
				outStr = new FileOutputStream(file);
				buff = new BufferedOutputStream(outStr);
				for (Connectivity connectivity : DB_WrapperLoad.connectivityT) {
					buff.write((connectivity.toString() + "\n").getBytes());
				}
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void exportMovingPatternsConfiguration(String outputPath, String time) {

			// System.out.println(outputPath);
			String mo_configuration_outputPath = outputPath + "//Moving_Object_Configuration_" + time + ".txt";
			// System.out.println(mo_configuration_outputPath);
			try {
				File file = new File(mo_configuration_outputPath);
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				FileOutputStream outStr = new FileOutputStream(file);
				BufferedOutputStream buff = new BufferedOutputStream(outStr);
				String configure = "Moving Object Type=" + movingObjectTypeComboBox.getSelectedItem().toString() + "\n";
				configure = configure + "Initial Distribution="
						+ movObjDistributerTypeComboBox.getSelectedItem().toString() + "\n";
				configure = configure + "Maximum Object Number in a Partition=" + txtMaxMovObjNumInPart.getText()
						+ "\n";
				configure = configure + "Maximum Life Span(s)=" + txtMaximumLifeSpan.getText() + "\n";
				configure = configure + "Maximum Step Length(m)=" + txtMaxStepLength.getText() + "\n";
				configure = configure + "Move Rate(ms)=" + txtMoveRate.getText() + "\n";
				configure = configure + "Generation Period=" + txtStartTime.getText() + "-" + txtEndTime.getText()
						+ "\n";
				buff.write(configure.getBytes());
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void exportPositioningConfiguration(String outputPath) {

			System.out.println(outputPath);
			String position_configuration_outputPath = outputPath.substring(0, outputPath.lastIndexOf(File.separator))
					+ "//Positioning_Configuration_"
					+ outputPath.substring(outputPath.lastIndexOf(File.separator) + 1, outputPath.length()) + ".txt";
			System.out.println(position_configuration_outputPath);
			try {
				File file = new File(position_configuration_outputPath);
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				FileOutputStream outStr = new FileOutputStream(file);
				BufferedOutputStream buff = new BufferedOutputStream(outStr);
				String configure = positionPropertiesArea.getText();
				configure = configure + "\n" + "positioning_algorithm="
						+ positionAlgorithmComboBox.getSelectedItem().toString() + "\n";
				configure = configure + "\n" + "input_rssi_path=" + txtRssiInputPath.getText() + "\n";
				buff.write(configure.getBytes());
				buff.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void exportStations(String stationDir) {

			Date date = new Date(System.currentTimeMillis());
			String time = IdrObjsUtility.dir_sdf.format(date);

			// IdrObjsUtility.createOutputDir();

			String station_outputPath = stationDir + "//Devices_" + time + ".txt";
			String station_configuration_outputPath = stationDir + "//Device_Configuration_" + time + ".txt";
			try {
				File file = new File(station_outputPath);
				file.createNewFile();
				FileOutputStream outStr = new FileOutputStream(file);
				BufferedOutputStream buff = new BufferedOutputStream(outStr);
				String comments = "deviceId" + "\t" + "floorId" + "\t" + "partitionId" + "\t" + "location_x" + "\t"
						+ "location_y" + "\n";
				buff.write(comments.getBytes());
				boolean flag = false;
				Station sampled_station = null;
				IdrObjsUtility.allStations = new Hashtable<Integer, Station>();
				for (Floor floor : DB_WrapperLoad.floorT) {
					for (Station station : floor.getStations()) {
						IdrObjsUtility.allStations.put(station.getId(), station);
						if (!flag) {
							sampled_station = station;
							flag = true;
						}
						buff.write(station.toString().getBytes());
					}
				}
				buff.close();

				if (sampled_station != null) {
					file = new File(station_configuration_outputPath);
					file.createNewFile();
					outStr = new FileOutputStream(file);
					buff = new BufferedOutputStream(outStr);
					String configure = "Device Type=" + stationTypeComboBox.getSelectedItem().toString() + "\n";
					configure = configure + "Deployment Model=" + stationDistriTypeComboBox.getSelectedItem().toString()
							+ "\n";
					configure = configure + "Maximum Device Number in a Partition=" + txtStationMaxNumInPart.getText()
							+ "\n";
					configure = configure + "Maximum Device Number in 100 m^2=" + txtStationMaxNumInArea.getText()
							+ "\n";
					configure = configure + "Detection Range(m)=" + txtScanRange.getText() + "\n";
					configure = configure + "Detection Frequency(ms)=" + txtScanRate.getText() + "\n";
					buff.write(configure.getBytes());
					buff.close();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private String createEnvironmentOutputDir(String outputPath) {
			// TODO Auto-generated method stub

			File dir = new File(outputPath);

			File spaceDir = new File(outputPath + "//indoor enviroment");

			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			if (!spaceDir.exists() && !spaceDir.isDirectory()) {
				spaceDir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			return spaceDir.getPath();

		}

		private String createStationOutputDir(String outputPath) {
			// TODO Auto-generated method stub

			File dir = new File(outputPath);

			File stationDir = new File(outputPath + "//indoor devices");

			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			if (!stationDir.exists() && !stationDir.isDirectory()) {
				stationDir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			return stationDir.getPath();

		}

		private void createMovingObjectOutputDir(String outputPath) {
			// TODO Auto-generated method stub

			Date date = new Date(System.currentTimeMillis());
			String time = IdrObjsUtility.dir_sdf.format(date);

			File dir = new File(outputPath);

			File rssiDir = new File(outputPath + "//raw rssi");
			File trajDir = new File(outputPath + "//raw trajectory");

			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			IdrObjsUtility.outputDir = outputPath;
			IdrObjsUtility.rssiDir = rssiDir.getPath() + "//" + time;
			IdrObjsUtility.trajDir = trajDir.getPath() + "//" + time;
			exportMovingPatternsConfiguration(rssiDir.getPath(), time);

			if (!rssiDir.exists() && !rssiDir.isDirectory() && chckbxTracking.isSelected()) {
				rssiDir.mkdirs();
			}
			File cur_rssiDir = new File(IdrObjsUtility.rssiDir);
			if (!cur_rssiDir.exists() && !cur_rssiDir.isDirectory() && chckbxTracking.isSelected()) {
				cur_rssiDir.mkdirs();
			}

			if (!trajDir.exists() && !trajDir.isDirectory() && chckbxTrajectory.isSelected()) {
				trajDir.mkdirs();
				new File(IdrObjsUtility.trajDir).mkdirs();
			}
			File cur_trajDir = new File(IdrObjsUtility.trajDir);
			if (!cur_trajDir.exists() && !cur_trajDir.isDirectory() && chckbxTracking.isSelected()) {
				cur_trajDir.mkdirs();
			}

			return;

		}

		private String createPositioningOutputDir(String outputPath) {
			// TODO Auto-generated method stub

			Date date = new Date(System.currentTimeMillis());
			String time = IdrObjsUtility.dir_sdf.format(date);

			File dir = new File(outputPath);

			File positionDir = new File(outputPath + "//indoor positioning data");

			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			positionDir.mkdirs();

			if (!positionDir.exists() && !positionDir.isDirectory() && chckbxPositiongData.isSelected()) {
				positionDir.mkdirs();
			}
			File cur_positionDir = new File(positionDir.getPath() + "//" + time);
			if (!cur_positionDir.exists() && !cur_positionDir.isDirectory() && chckbxTracking.isSelected()) {
				cur_positionDir.mkdirs();
			}

			return cur_positionDir.getPath();

		}

		private void createSnapshotOutputDir(String outputPath) {
			// TODO Auto-generated method stub

			File dir = new File(outputPath);

			File snapshotDir = new File(outputPath + "//snapshot");

			if (!dir.exists() && !dir.isDirectory()) {
				dir.mkdirs();
			} else {
				System.out.println("Dir already exists");
			}

			snapshotDir.mkdirs();

			IdrObjsUtility.snapshotDir = snapshotDir.getPath();
			//
			// new File(IdrObjsUtility.snapshotDir).mkdirs();

			// positionDir.mkdirs();

			return;

		}

		private void generateMovingObjs() {
			movingObjs.clear();
			IndoorObjsFactory initlizer = new IndoorObjsFactory();
			for (Floor floor : DB_WrapperLoad.floorT) {
				initlizer.generateMovingObjsOnFloor(floor, movingObjs);
			}
			// PropLoader propLoader = new PropLoader();
			// propLoader.loadProp("conf/factory.properties");
			// movingObjs = (ArrayList)DB_WrapperLoad.floorT
			// .stream()
			// .map(floor ->
			// IndoorObjectFactory.createMovingObjectsOnFloor(floor,
			// propLoader.getMovingObjDistributerType()))
			// .flatMap(Collection::stream)
			// .collect(Collectors.toList());
			setMovingObjsInitTime();
		}

		private void setMovingObjsInitTime() {
			movingObjs.forEach(movingObj -> {
				movingObj.setInitMovingTime(calGaussianTime());
			});
		}

		private long calGaussianTime() {
			Random random = new Random();
			try {
				IdrObjsUtility.objectGenerateStartTime = startCalendar == null
						? IdrObjsUtility.sdf.parse(txtStartTime.getText()) : startCalendar.getTime();
				IdrObjsUtility.objectGenerateEndTime = endCalendar == null
						? IdrObjsUtility.sdf.parse(txtEndTime.getText()) : endCalendar.getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			long startTime = IdrObjsUtility.objectGenerateStartTime.getTime();
			long endTime = IdrObjsUtility.objectGenerateEndTime.getTime();
			long middle = (long) ((endTime - startTime) / 2.0 + startTime);
			long error = (long) ((endTime - startTime) * 0.5);
			long gaussianTime = (long) (middle + random.nextGaussian() * error);
			if (gaussianTime < startTime) {
				return startTime;
			} else if (gaussianTime > endTime) {
				return endTime;
			} else {
				return gaussianTime;
			}
		}

		private void snapShot(ArrayList<MovingObj> movingObjs) {

			try {

				String time = IdrObjsUtility.dir_sdf.format(new Date(IdrObjsUtility.objectGenerateStartTime.getTime()
						+ (System.currentTimeMillis() - IdrObjsUtility.startClickedTime.getTime())));

				// create snap shot file
				File file = new File(IdrObjsUtility.snapshotDir + "//snapshot_" + time + ".txt");
				file.createNewFile();
				FileOutputStream outStr = new FileOutputStream(file);
				BufferedOutputStream buff = new BufferedOutputStream(outStr);

				// record snap shot data
				for (MovingObj movingObj : movingObjs) {
					buff.write((movingObj.getId() + "\t").getBytes());
					buff.write((movingObj.getCurrentLocation().getX() + "\t" + movingObj.getCurrentLocation().getY()
							+ "\n").getBytes());
					int packIndex = 1;
					movingObj.calRSSI();
					ArrayList<Pack> packs = movingObj.getPackages();
					for (Pack pack : packs) {
						String packInfo = packIndex + "\t" + pack.toString() + "\n";
						buff.write(packInfo.getBytes());
						packIndex++;
					}
					buff.write("\n".getBytes());
				}

				buff.flush();
				buff.close();
				JOptionPane.showMessageDialog(mainFrame, "Extracting snapshot is done!", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void storePropFromGUI(String propName) {
			Properties props = new Properties();

			String stationTypeSimple = stationTypeComboBox.getSelectedItem().toString();
			String stationType = stationTypeMap.get(stationTypeSimple);
			props.setProperty("stationType", stationType);
			String stationDistributerTypeSimple = stationDistriTypeComboBox.getSelectedItem().toString();
			String stationDistributerType = stationInitMap.get(stationDistributerTypeSimple);
			props.setProperty("stationDistributerType", stationDistributerType);
			String stationMaxNumInPart = txtStationMaxNumInPart.getText();
			props.setProperty("stationMaxNumInPart", stationMaxNumInPart);

			String stationNumArea = txtStationMaxNumInArea.getText();
			props.setProperty("stationNumArea", stationNumArea);
			String scanRange = txtScanRange.getText();
			props.setProperty("stationScanRange", scanRange);
			String stationScanRate = txtScanRate.getText();
			props.setProperty("stationScanRate", stationScanRate);

			Station.setScanRange(Double.parseDouble(scanRange));
			Station.setScanRate(Integer.parseInt(stationScanRate));

			String movingObjTypeSimple = movingObjectTypeComboBox.getSelectedItem().toString();
			String movingObjType = movingObjTypeMap.get(movingObjTypeSimple);
			props.setProperty("movingObjType", movingObjType);
			String movObjDistriTypeSimple = movObjDistributerTypeComboBox.getSelectedItem().toString();
			String movObjDistriType = movObjInitMap.get(movObjDistriTypeSimple);
			props.setProperty("movingObjDistributerType", movObjDistriType);
			String maxMovingNumInPart = txtMaxMovObjNumInPart.getText();
			props.setProperty("movingObjMaxNumInPart", maxMovingNumInPart);
			String maxStepLength = txtMaxStepLength.getText();
			props.setProperty("movingObjMaxStepLength", maxStepLength);
			String moveRate = txtMoveRate.getText();
			props.setProperty("movingObjMoveRate", moveRate);
			String movingObjMaxLifeSpan = txtMaximumLifeSpan.getText();
			props.setProperty("movingObjMaxLifeSpan", movingObjMaxLifeSpan);

			String positionAlgorithm = positionAlgorithmComboBox.getSelectedItem().toString();
			String posAlgType = positionAlgorithmMap.get(positionAlgorithm);
			props.setProperty("positionAlgorithm", posAlgType);

			MovingObj.setScanRange(Double.parseDouble(scanRange));
			MovingObj.setMaxStepLength(Double.parseDouble(maxStepLength));
			MovingObj.setMoveRate(Integer.parseInt(moveRate));
			MovingObj.setMaxSpeed(Double.parseDouble(maxStepLength) / ((Integer.parseInt(moveRate) + 0.0) / 1000));

			try {
				FileOutputStream out = new FileOutputStream(propName);
				props.store(out, null);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		private void startIndoorObj() {
			// IdrObjsUtility.createOutputDir();
			IdrObjsUtility.movingObjsTest(movingObjs);

			destMovingObjs.clear();
			Floor floor1 = DB_WrapperLoad.floorT.get(2);
			Floor floor2 = DB_WrapperLoad.floorT.get(2);
			IdrObjsUtility.DestMovingObjTest2(floor1, floor2, destMovingObjs);
		}

		private void stopIndoorObj() {
			for (MovingObj movingObj : movingObjs) {
				movingObj.setArrived(true);
				if (movingObj instanceof RegularMultiDestCustomer) {
					((RegularMultiDestCustomer) movingObj).setFinished(true);
				}
			}
			for (MovingObj destMoving : destMovingObjs) {
				DstMovingObj destMovingObj = (DstMovingObj) destMoving;
				destMovingObj.setArrived(true);
			}
			movingObjs.clear();
			destMovingObjs.clear();

			for (Floor floor : DB_WrapperLoad.floorT) {
				floor.getStations().clear();
			}

			JOptionPane.showMessageDialog(mainFrame, "Generating Moving Object Data is done!", "Information",
					JOptionPane.INFORMATION_MESSAGE);

		}

		private void pauseIndoorObj() {
			for (MovingObj movingObj : movingObjs) {

				movingObj.changeFlag();
				// pauseBut.setText("resume");
				if (movingObj.getPauseFlag() == false) {
					movingObj.resumeThread();
					// pauseBut.setText("pause");
				}
			}

			for (MovingObj movingObj : destMovingObjs) {
				DstMovingObj destMovingObj = (DstMovingObj) movingObj;
				destMovingObj.changeFlag();
				if (destMovingObj.getPauseFlag() == false) {
					destMovingObj.resumeThread();
				}
			}
		}

		private void loadPropFromFile(String propName) {

			Properties props = new Properties();
			FileInputStream in;
			try {
				in = new FileInputStream(propName);
				props.load(in);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String stationMaxNumInPart = props.getProperty("stationMaxNumInPart");
			txtStationMaxNumInPart.setText(stationMaxNumInPart);
			String stationNumArea = props.getProperty("stationNumArea");
			txtStationMaxNumInArea.setText(stationNumArea);
			String scanRange = props.getProperty("stationScanRange");
			txtScanRange.setText(scanRange);
			String stationScanRate = props.getProperty("stationScanRate");
			txtScanRate.setText(stationScanRate);

			String maxMovingNumInPartition = props.getProperty("movingObjMaxNumInPart");
			txtMaxMovObjNumInPart.setText(maxMovingNumInPartition);
			String maxStepLength = props.getProperty("movingObjMaxStepLength");
			txtMaxStepLength.setText(maxStepLength);
			String moveRate = props.getProperty("movingObjMoveRate");
			txtMoveRate.setText(moveRate);
			String movObjMaxLifeSpan = props.getProperty("movingObjMaxLifeSpan");
			txtMaximumLifeSpan.setText(movObjMaxLifeSpan);

			txtStartTime.setText(IdrObjsUtility.sdf.format(System.currentTimeMillis()));
			txtEndTime.setText(IdrObjsUtility.sdf.format(System.currentTimeMillis() + 10 * 60 * 1000));
		}

		private class MovingAdapter extends MouseAdapter {
			private Point startDrag;

			public void mouseWheelMoved(MouseWheelEvent e) {
				previousX = e.getX();
				previousY = e.getY();
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					incrementZoom(1.0 * (double) e.getWheelRotation());
				}
			}

			public void mousePressed(MouseEvent e) {
				previousX = e.getX();
				previousY = e.getY();

				startDrag = new Point(e.getX(), e.getY()); // First point
				if (!empty) {
					txtselectedNameField.setText("");
					selectedPart = null;
					selectedAP = null;

					for (Entry<Path2D, Partition> mapping : partitionsMap.entrySet()) {
						if (mapping.getKey().contains(startDrag)) {
							selectedPart = mapping.getValue();
							selectedAP = null;
							selectedCon = null;
							break;
						}
					}

					for (Entry<Path2D, AccessPoint> mapping : accesspointsMap.entrySet()) {
						if (mapping.getKey().contains(startDrag)) {
							selectedAP = mapping.getValue();
							selectedPart = null;
							selectedCon = null;
							break;
						}
					}

					for (Entry<Path2D, Connector> mapping : connsMap.entrySet()) {
						if (mapping.getKey().contains(startDrag)) {
							selectedCon = mapping.getValue();
							selectedPart = null;
							selectedAP = null;
							break;
						}
					}

					connectedPartsModel.clear();
					updateSelectPartsList();

					// possibleConnectedPartsList.clear();
					// updatePossibleConnectedPartsList();
				}
				repaint();
			}

			public void mouseDragged(MouseEvent e) {

				Point2D adjPreviousPoint = getTranslatedPoint(previousX, previousY);
				Point2D adjNewPoint = getTranslatedPoint(e.getX(), e.getY());

				double newX = adjNewPoint.getX() - adjPreviousPoint.getX();
				double newY = adjNewPoint.getY() - adjPreviousPoint.getY();

				previousX = e.getX();
				previousY = e.getY();

				currentX += newX;
				currentY += newY;

				repaint();
			}

			public void mouseReleased(MouseEvent e) {
			}
		}
	}

	public static void main(String[] args) {

		VITA_Application vita = new VITA_Application();
		vita.getContentPane().setBackground(Color.WHITE);
		vita.init();

		mainFrame = new JFrame();
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.getContentPane().add(vita);
		mainFrame.setPreferredSize(size);
		mainFrame.setTitle("VITA - Verstile Indoor Mobility Data Generator");
		mainFrame.pack();
		mainFrame.setVisible(true);

		vita.start();
	}
}
