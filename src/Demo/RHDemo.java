package Demo;

import controllers.singlePlayer.RHv2.Agent;
import controllers.singlePlayer.RHv2.utils.ParameterSet;
import core.ArcadeMachine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.Random;

import static Demo.HelperMethods.*;
import static controllers.singlePlayer.RHv2.Agent.*;
import static controllers.singlePlayer.RHv2.utils.Constants.DRAW_ET;
import static controllers.singlePlayer.RHv2.utils.Constants.DRAW_EXPLORATION;
import static controllers.singlePlayer.RHv2.utils.Constants.DRAW_THINKING;

public class RHDemo {
    public static boolean startGame = false, pauseGame = false, stopGame = false, readyGame = false, closePlots = false;
    public static boolean drawHM = false, drawTH = false;
    public static JButton pauseB;
    public static BufferedWriter resultWriter;
    static File resultFile;

    public static void main(String[] args) {

        // Set up scripts based on OS
        String winScript = "py/runPyPlot.bat";
        String unixScript = "py/runPyPlot.sh";
        boolean OS_WIN = System.getProperty("os.name").contains("Windows");

        // List of games
        String[] games = getGames();
        String gamespath = "examples/gridphysics/";

        // General styling
        styleUI();

        // Setting up the JFrame
        JFrame frame = new JFrame("RHEA DEMO");
        frame.setLayout(new GridBagLayout());
        JTabbedPane mainPanel = new JTabbedPane();
        frame.getContentPane().setBackground(Color.black);

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

        // Create things for game panel
        JButton startB = new JButton("Step 2: Alg ready - Start!");
        startB.addActionListener(e -> {
            if (readyGame) {
                startGame = true;
            }
        });
        pauseB = new JButton("Pause");
        pauseB.addActionListener(e -> pauseGame = !pauseGame);
        JButton stopB = new JButton("Stop");
        stopB.addActionListener(e -> stopGame = true);
        JButton closeB = new JButton("Close Plots");
        closeB.setToolTipText("Close all plot windows, ready for next run.");
        closeB.addActionListener(e -> closePlots = true);
        JButton readyB = new JButton("Step 1: Game ready");
        readyB.addActionListener(e -> readyGame = true);
        readyB.setToolTipText("Lock game and level and start graph display (can still adjust other parameters).");

        JComboBox gameOptions = new JComboBox<>(games);
        Integer[] levels = new Integer[]{0, 1, 2, 3, 4};
        JComboBox levelOptions = new JComboBox<>(levels);

        // Create result evo table
        DefaultTableModel evoResTableModel = createEvoResTableModel();
        JScrollPane evoResPanel = createEvoResPanel(evoResTableModel);

        // Create result game table
        DefaultTableModel gameResTableModel = createGameResTableModel();
        JScrollPane gameResPanel = createGameResPanel(gameResTableModel);

        // Toggle buttons for game overlay drawings
        JToggleButton jtb1 = new JToggleButton("Heatmap On/Off");
        jtb1.addItemListener(ev -> {
            if(ev.getStateChange()==ItemEvent.SELECTED){
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

        // Add all the things to the game panel
        JPanel gamePanel = getGamePanel(gameOptions, levelOptions, readyB, startB, pauseB, stopB, closeB, evoResPanel,
                gameResPanel, jtb1, jtb2);

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

        // Add tabs to main panel and panel to frame
        mainPanel.addTab("Game", gamePanel);
        mainPanel.addTab("Parameters", params);
        mainPanel.addTab("Instructions", instructions);

        frame.add(mainPanel);
        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Main loop to keep window open and playing games
        while (true) {

            startB.setEnabled(false);
            pauseB.setEnabled(false);
            stopB.setEnabled(false);
            closeB.setEnabled(false);
            while (!readyGame) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
            startB.setEnabled(true);
            readyB.setEnabled(false);
            gameOptions.setEnabled(false);
            levelOptions.setEnabled(false);

            // Game is ready, get game params and start Python to avoid delays in plot display

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

            try {
                new FileWriter(new File(actionFile));
                new FileWriter(new File(evoFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Call python to draw the plot
            String cmdpath = new File("py/main.py").getAbsolutePath();
            String runpath = new File(OS_WIN ? winScript : unixScript).getAbsolutePath();
            if (OS_WIN) {
                cmdpath = "\"" + cmdpath + "\"";
                runpath = "\"" + runpath + "\"";
            }
            String pycommand = cmdpath + " " + game_idx + " " + lvl_idx;
            String command = runpath + " " + pycommand;

            Process pyProcess = null;

            try {
                pyProcess = Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (!startGame) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }

            // Algorithm is ready, start game
            startB.setEnabled(false);
            pauseB.setEnabled(true);
            stopB.setEnabled(true);

            // Get params
            paramSet = getParamSet(paramInputs);

            // Reset frame contents
            frame.getContentPane().removeAll();
            mainPanel.removeAll();
            gamePanel = getGamePanel(gameOptions, levelOptions, readyB, startB, pauseB, stopB, closeB, evoResPanel,
                    gameResPanel, jtb1, jtb2);
            mainPanel.addTab("Game", gamePanel);
            mainPanel.addTab("Parameters", params);
            mainPanel.addTab("Instructions", instructions);

            // Add all to frame
            frame.add(mainPanel);
            frame.getContentPane().revalidate();
            frame.getContentPane().repaint();
            frame.pack();

            //Play game with params
            try {
                ArcadeMachine.runOneExpGame(frame, gamePanel, map, level, true, RHEA,
                        actionFile, evoFile, seed, 0, paramSet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //End of run, reset and kill process
            startGame = false;
            stopGame = false;
            pauseGame = false;
            readyGame = false;
            pauseB.setEnabled(false);
            stopB.setEnabled(false);

            if (pyProcess != null && pyProcess.isAlive()) {
                pyProcess.destroy();
            }

            // Display game result in frame
            parseResult(evoResTableModel, gameResTableModel);

            // Wait for all plots to be closed before proceeding
            closeAllPlots(evoFile, readyB, closeB, gameOptions, levelOptions);
        }
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

}