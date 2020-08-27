package VERTIGO;

import VERTIGO.plots.ConvergencePlot;
import controllers.singlePlayer.RHv2.utils.ParameterSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import static VERTIGO.HelperMethods.*;
import static controllers.singlePlayer.RHv2.Agent.drawing;
import static controllers.singlePlayer.RHv2.Agent.drawingWhat;
import static controllers.singlePlayer.RHv2.utils.Constants.*;
import static controllers.singlePlayer.RHv2.utils.Constants.DRAW_EXPLORATION;

public class VertigoView extends JFrame {

    public static boolean startGame = false, pauseGame = false, stopGame = false, readyGame = false;
    public static boolean drawHM = false, drawTH = false;
    public static JButton pauseB, startB, stopB, readyB;
    public static BufferedWriter resultWriter;
    static File resultFile;
    String gamespath = "examples/gridphysics/";
    String[] games;
    Integer[] levels = new Integer[]{0, 1, 2, 3, 4};

    ConvergencePlot covPlot;

    public VertigoView() {

        // List of games
        games = getGames();

        // General styling
        styleUI();

        // Setting up the JFrame
        setTitle("VERTIGO 1.1");
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.black);

        // Create result dir, log dir and result writer
        File dir = new File("py/files");
        createDirs(dir);
        File logDir = new File("logs");
        createDirs(logDir);
        try {
            resultFile = new File("py/files/results.log");
            resultWriter = new BufferedWriter(new FileWriter(resultFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Main panel
        JTabbedPane mainPanel = new JTabbedPane();

        // Creating things for parameter panel
        JPanel params = new JPanel();
        params.setLayout(new GridLayout(15, 4, 20, 0));
        ParameterSet paramSet = new ParameterSet();

        String[] p = paramSet.getParams();
        JLabel[] paramLabels = new JLabel[p.length];
        for (int i = 0; i < paramLabels.length; i++) {
            paramLabels[i] = new JLabel(p[i]);
            paramLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
        }

        String[] v = paramSet.getDefaultValues();
        Object[][] valueOptions = paramSet.getValueOptions();
        JComponent[] paramInputs = new JComponent[v.length];
        for (int i = 0; i < paramInputs.length; i++) {
            if (valueOptions[i].length == 0) {
                //it's a text field with default value in v
                paramInputs[i] = new JTextField(v[i], 10);
            } else {
                paramInputs[i] = new JComboBox<>(valueOptions[i]);
            }
        }

        // Adding things to parameter panel
        for (int i = 0; i < paramLabels.length; i++) {
            params.add(paramLabels[i]);
            params.add(paramInputs[i]);
        }

        JComboBox<String> gameOptions = new JComboBox<>(games);
        JComboBox<Integer> levelOptions = new JComboBox<>(levels);

        // Create result evo table
        DefaultTableModel evoResTableModel = createEvoResTableModel();
        JScrollPane evoResPanel = createEvoResPanel(evoResTableModel);

        // Create result game table
        DefaultTableModel gameResTableModel = createGameResTableModel();
        JScrollPane gameResPanel = createGameResPanel(gameResTableModel);

        // Toggle buttons for game overlay drawings
        JToggleButton jtb1 = new JToggleButton("Heatmap On/Off");
        jtb1.addItemListener(ev -> {
            if(ev.getStateChange()== ItemEvent.SELECTED){
                drawHM = true;
                drawing = true;
                if (drawTH) drawingWhat = DRAW_ET;
                else drawingWhat = DRAW_EXPLORATION;
            } else if(ev.getStateChange()==ItemEvent.DESELECTED){
                drawHM = false;
                if (!drawTH)
                    drawing = false;
                else
                    drawingWhat = DRAW_THINKING;
            }
        });

        JToggleButton jtb2 = new JToggleButton("Simulations On/Off");
        jtb2.addItemListener(ev -> {
            if(ev.getStateChange()==ItemEvent.SELECTED){
                drawTH = true;
                drawing = true;
                if (drawHM) drawingWhat = DRAW_ET;
                else drawingWhat = DRAW_THINKING;
            } else if(ev.getStateChange()==ItemEvent.DESELECTED){
                drawTH = false;
                if (!drawHM) {
                    drawing = false;
                }
                else
                    drawingWhat = DRAW_EXPLORATION;
            }
        });

        // Create instructions panel
        JPanel instructions = new JPanel();
        JLabel insText = new JLabel("<html><div width=500>" +
                "<center><h1>How to use</h1></center></br><hr>" +
                "<ul><li>Step 1: Adjust game to play and level in \"Game\" tab</li>" +
                "<li>Step 2: Click the \"Game ready\" button</li>" +
                "<li>Step 3: Wait for plot windows to load, then continue.</li>" +
                "<li>Step 4: Adjust algorithm parameters in \"Parameters\" tab</li>" +
                "<li>Step 5: Click the \"Start\" button</li>" +
                "<li>Step 6: Watch the game play out and the different plots generated</li>" +
                "<li>Step 7: Pause/Resume the game using the \"Pause\" button</li>" +
                "<li>Step 8: Interrupt the game using the \"Stop\" button</li>" +
                "<li>Step 9: Analyze the final results in the tables and save plots</li>" +
                "<li>Step 10: Click the \"Close Plots\" button at the bottom of the screen</li>" +
                "<li>Repeat from step 1 with different settings. " +
                "</div></html>");
        instructions.add(insText);

        // Make side panel for plots
        JPanel analysis = new JPanel();
        covPlot = new ConvergencePlot();
        analysis.add(covPlot);

        // Put all together
        JPanel comboPanel = new JPanel();
        comboPanel.add(mainPanel);
        comboPanel.add(analysis);

        // Buttons
        // Create things for game panel
        startB = new JButton("Step 2: Alg ready - Start!");
        startB.addActionListener(e -> {
            if (readyGame) {
                startGame = true;
                startB.setEnabled(false);
                pauseB.setEnabled(true);
                stopB.setEnabled(true);

                // Get game and level to play
                int game_idx = gameOptions.getSelectedIndex(); // game index
                int lvl_idx = levelOptions.getSelectedIndex();

                // Settings for game
                int seed = new Random().nextInt();
                String RHEA = "controllers.singlePlayer.RHv2.Agent";
                String map = gamespath + games[game_idx] + ".txt";
                String level = gamespath + games[game_idx] + "_lvl" + lvl_idx + ".txt";

                // Log files
                String actionFile = "py/files/actions_" + game_idx + "_" + lvl_idx + ".log";
                String evoFile = "py/files/evo_" + game_idx + "_" + lvl_idx + ".log";
                covPlot.setDataFiles(actionFile, evoFile);

                try {
                    new FileWriter(new File(actionFile));
                    new FileWriter(new File(evoFile));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Get params
                ParameterSet ps = getParamSet(paramInputs);

                // Reset frame contents
                mainPanel.removeAll();
                JPanel gp = getGamePanel(gameOptions, levelOptions, readyB, startB, pauseB, stopB, evoResPanel,
                        gameResPanel, jtb1, jtb2);
                mainPanel.addTab("Game", gp);
                mainPanel.addTab("Parameters", params);
                mainPanel.addTab("Instructions", instructions);

                // Add all to frame
                getContentPane().revalidate();
                getContentPane().repaint();
                pack();

                // Start game on separate thread
                GameRunner gr = new GameRunner(this, gp, map, level, true, RHEA,
                        actionFile, evoFile, seed, 0, ps, gameOptions, levelOptions, evoResTableModel, gameResTableModel,
                        covPlot);
                Thread t = new Thread(gr);
                t.start();
            }
        });

        pauseB = new JButton("Pause");
        pauseB.addActionListener(e -> pauseGame = !pauseGame);

        stopB = new JButton("Stop");
        stopB.addActionListener(e -> stopGame = true);

        readyB = new JButton("Step 1: Game ready");
        readyB.addActionListener(e -> {
            readyGame = true;
            startB.setEnabled(true);
            readyB.setEnabled(false);
            gameOptions.setEnabled(false);
            levelOptions.setEnabled(false);
        });
        readyB.setToolTipText("Lock game and level and start graph display (can still adjust other parameters).");

        // Add all the things to the game panel
        JPanel gamePanel = getGamePanel(gameOptions, levelOptions, readyB, startB, pauseB, stopB, evoResPanel,
                gameResPanel, jtb1, jtb2);

        // Add tabs to main panel and panel to frame
        mainPanel.addTab("Game", gamePanel);
        mainPanel.addTab("Parameters", params);
        mainPanel.addTab("Instructions", instructions);

        add(comboPanel);
        pack();

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        startB.setEnabled(false);
        pauseB.setEnabled(false);
        stopB.setEnabled(false);
    }

    private static void createDirs(File dir) {
        if (!dir.exists()) {
            boolean createdDirectory = dir.mkdir();
            if (!createdDirectory) {
                System.err.println("Failed in creating dir + " + dir.toString() + ". System exit.");
                System.exit(0);
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (covPlot != null) {
            covPlot.paintComponent(g);
        }
    }
}
