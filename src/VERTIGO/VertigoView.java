package VERTIGO;

import VERTIGO.players.ParameterSet;
import VERTIGO.players.sampleOLMCTSMacro.MCTSParams;
import VERTIGO.plots.LinePlot;
import VERTIGO.players.RHv2.utils.RHEAParams;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static VERTIGO.HelperMethods.*;
import static VERTIGO.players.RHv2.Agent.*;
import static VERTIGO.players.RHv2.utils.Constants.*;
import static VERTIGO.players.RHv2.utils.Constants.DRAW_EXPLORATION;

public class VertigoView extends JFrame {

    public static boolean startGame = false, pauseGame = false, stopGame = false;
    public static boolean drawHM = false, drawTH = false;
    public static JButton pauseB, startB, stopB;
    public static BufferedWriter resultWriter;
    static File resultFile;
    String gamespath = "examples/gridphysics/";
    String[] games;
    Integer[] levels = new Integer[]{0, 1, 2, 3, 4};
    String[] agents = new String[]{
        "VERTIGO.players.RHv2.Agent",
        "VERTIGO.players.sampleOLMCTSMacro.Agent",
    };
    ParameterSet[] parameterSets = new ParameterSet[] {
            new RHEAParams(),
            new MCTSParams()
    };

    LinePlot linePlot;
    int colorButSize = 20;
    int maxParams = 40;

    public VertigoView() {

        // List of games
        games = getGames();

        // General styling
        styleUI();

        // Setting up the JFrame
        setTitle("VERTIGO 1.2");
        setLayout(new GridBagLayout());
        getContentPane().setBackground(Color.black);

        // Create result dir, log dir and result writer
        File dir = new File("logs");
        createDirs(dir);
        try {
            resultFile = new File("logs/results.log");
            resultWriter = new BufferedWriter(new FileWriter(resultFile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JButton clearFilesBut = new JButton("Clear logs");
        clearFilesBut.addActionListener(e -> {
            for(File file: Objects.requireNonNull(dir.listFiles())) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        });

        // Main panel
        JTabbedPane mainPanel = new JTabbedPane();

        // Creating things for parameter panel
        JPanel params = new JPanel();
        params.setLayout(new GridLayout(20, 4, 20, 0));

        JComboBox[] paramInputs = new JComboBox[maxParams];
        JLabel[] paramLabels = new JLabel[maxParams];
        // Adding things to parameter panel
        for (int i = 0; i < maxParams; i++) {
            paramLabels[i] = new JLabel("");
            paramLabels[i].setVisible(false);
            paramInputs[i] = new JComboBox<>(new Object[0]);
            paramInputs[i].setVisible(false);
            params.add(paramLabels[i]);
            params.add(paramInputs[i]);
        }

        JComboBox<String> agentOptions = new JComboBox<>(agents);
        JComboBox<String> gameOptions = new JComboBox<>(games);
        JComboBox<Integer> levelOptions = new JComboBox<>(levels);

        agentOptions.addActionListener(e -> {
            ParameterSet paramSet = parameterSets[agentOptions.getSelectedIndex()];
            String[] p = paramSet.getParams();
            String[] desc = paramSet.getParamDescriptions();

            for (int i = 0; i < p.length; i++) {
                paramLabels[i].setText(p[i]);
                paramLabels[i].setToolTipText(desc[i]);
                paramLabels[i].setHorizontalAlignment(SwingConstants.RIGHT);
                paramLabels[i].setVisible(true);
            }
            String[] v = paramSet.getDefaultValues();
            Object[][] valueOptions = paramSet.getValueOptions();
            for (int i = 0; i < p.length; i++) {
                paramInputs[i].setModel(new DefaultComboBoxModel(valueOptions[i]));
                paramInputs[i].setVisible(true);
                for (int j = 0; j < valueOptions[i].length; j++) {
                    if (("" + valueOptions[i][j].toString()).equals(v[i])) {
                        paramInputs[i].setSelectedIndex(j);
                        break;
                    }
                }
            }
            for (int i = p.length; i < maxParams; i++) {
                paramInputs[i].setVisible(false);
                paramLabels[i].setVisible(false);
                paramLabels[i].setToolTipText("");
            }
        });
        agentOptions.setSelectedIndex(0);

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
        JButton heatmapColorChooser = new JButton();
        heatmapColorChooser.setPreferredSize(new Dimension(colorButSize, colorButSize));
        heatmapColorChooser.setBackground(explorationColor);
        heatmapColorChooser.addActionListener(e -> {
            Color background = JColorChooser.showDialog(null, "Change Exploration Color",
                    explorationColor);
            explorationColor = background;
            heatmapColorChooser.setBackground(background);
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
        JButton simGoodColorChooser = new JButton();
        simGoodColorChooser.setPreferredSize(new Dimension(colorButSize, colorButSize));
        simGoodColorChooser.setBackground(goodAction);
        simGoodColorChooser.addActionListener(e -> {
            Color background = JColorChooser.showDialog(null, "Change Good Simulation Color",
                    goodAction);
            goodAction = background;
            simGoodColorChooser.setBackground(background);
        });
        JButton simBadColorChooser = new JButton();
        simBadColorChooser.setPreferredSize(new Dimension(colorButSize, colorButSize));
        simBadColorChooser.setBackground(badAction);
        simBadColorChooser.addActionListener(e -> {
            Color background = JColorChooser.showDialog(null, "Change Bad Simulation Color",
                    badAction);
            badAction = background;
            simBadColorChooser.setBackground(background);
        });

        // Create instructions panel
        JPanel instructions = new JPanel();
        JLabel insText = new JLabel("<html><div width=500>" +
                "<center><h1>How to use</h1></center></br><hr>" +
                "<ul><li>Step 1: Adjust agent, game to play and level in \"Game\" tab</li>" +
                "<li>Step 2: Adjust algorithm parameters in \"Parameters\" tab. Hover over parameter name to see description</li>" +
                "<li>Step 3: Click the \"Start\" button</li>" +
                "<li>Step 4: Click the \"Analysis\" button on the right and different toggles to observe plots</li>" +
                "<li>Step 5: Click the \"Heatmap\" and \"Simulations\" toggles for position history / agent thinking plots</li>" +
                "<li>Step 6: Pause/Resume the game using the \"Pause\" button</li>" +
                "<li>Step 7: Interrupt the game using the \"Stop\" button</li>" +
                "<li>Step 8: Analyze the final results in the tables and save plots</li>" +
                "<li>Step 9: Click the \"Clear logs\" button to clean up the logs directory</li>" +
                "<li>Repeat from step 1 with different settings. " +
                "</div></html>");
        insText.setFont(new Font(insText.getFont().getName(), Font.PLAIN, 18));

        instructions.add(insText);

        // Make side panel for plots
        JPanel analysis = new JPanel();
        analysis.setLayout(new BoxLayout(analysis, BoxLayout.Y_AXIS));
        linePlot = new LinePlot();
        JButton convToggle = new JButton("Convergence");
        convToggle.addActionListener(e -> {
            linePlot.toggleConvergence();
            repaint();
        });
        JButton scoreToggle = new JButton("Score");
        scoreToggle.addActionListener(e -> {
            linePlot.toggleScore();
            repaint();
        });
        JButton scorePlusToggle = new JButton("Score+");
        scorePlusToggle.addActionListener(e -> {
            linePlot.toggleScorePlus();
            repaint();
        });
        JButton scoreMinusToggle = new JButton("Score-");
        scoreMinusToggle.addActionListener(e -> {
            linePlot.toggleScoreMinus();
            repaint();
        });
        JButton winToggle = new JButton("Win");
        scoreMinusToggle.addActionListener(e -> {
            linePlot.toggleWin();
            repaint();
        });
        JButton loseToggle = new JButton("Lose");
        scoreMinusToggle.addActionListener(e -> {
            linePlot.toggleLose();
            repaint();
        });
        analysis.add(linePlot);

        JPanel buttons = new JPanel();
        buttons.add(convToggle);
        buttons.add(scoreToggle);
        buttons.add(scorePlusToggle);
        buttons.add(scoreMinusToggle);
        buttons.add(winToggle);
        buttons.add(loseToggle);

        analysis.add(buttons);
        analysis.setVisible(false);

        // Put all together
        JPanel comboPanel = new JPanel();
        comboPanel.add(mainPanel);
        comboPanel.add(analysis);

        JButton button = new JButton("Analysis");
        button.addActionListener(e -> {
            analysis.setVisible(!analysis.isVisible());
            pack();
            repaint();
        });
        comboPanel.add(button);

        // Buttons
        // Create things for game panel
        startB = new JButton("Start");
        startB.addActionListener(e -> {
            startGame = true;
            startB.setEnabled(false);
            pauseB.setEnabled(true);
            stopB.setEnabled(true);
            agentOptions.setEnabled(false);
            gameOptions.setEnabled(false);
            levelOptions.setEnabled(false);

            // Get game and level to play
            int agent_idx = agentOptions.getSelectedIndex();  // agent index
            int game_idx = gameOptions.getSelectedIndex(); // game index
            int lvl_idx = levelOptions.getSelectedIndex();

            // Settings for game
            int seed = new Random().nextInt();
            String agent = agents[agent_idx];
            String map = gamespath + games[game_idx] + ".txt";
            String level = gamespath + games[game_idx] + "_lvl" + lvl_idx + ".txt";

            // Log files
            long time = System.currentTimeMillis();
            String actionFile = "logs/actions_" + game_idx + "_" + lvl_idx + "_" + time + ".log";
            String evoFile = "logs/evo_" + game_idx + "_" + lvl_idx + "_" + time + ".log";
            linePlot.setDataFiles(actionFile, evoFile);

            try {
                new FileWriter(new File(actionFile));
                new FileWriter(new File(evoFile));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Get params
            ParameterSet ps = parameterSets[agent_idx].getParamSet(paramInputs);

            // Reset frame contents
            mainPanel.removeAll();
            JPanel gp = getGamePanel(agentOptions, gameOptions, levelOptions, startB, pauseB, stopB, clearFilesBut, evoResPanel,
                    gameResPanel, jtb1, heatmapColorChooser, jtb2, simGoodColorChooser, simBadColorChooser);
            mainPanel.addTab("Game", gp);
            mainPanel.addTab("Parameters", params);
            mainPanel.addTab("Instructions", instructions);

            // Add all to frame
            getContentPane().revalidate();
            getContentPane().repaint();
            pack();

            // Start game on separate thread
            GameRunner gr = new GameRunner(this, gp, map, level, true, agent,
                    actionFile, evoFile, seed, 0, ps, agentOptions, gameOptions, levelOptions, evoResTableModel, gameResTableModel,
                    linePlot);
            Thread t = new Thread(gr);
            t.start();
        });

        pauseB = new JButton("Pause");
        pauseB.addActionListener(e -> pauseGame = !pauseGame);

        stopB = new JButton("Stop");
        stopB.addActionListener(e -> stopGame = true);

        // Add all the things to the game panel
        JPanel gamePanel = getGamePanel(agentOptions, gameOptions, levelOptions, startB, pauseB, stopB, clearFilesBut, evoResPanel,
                gameResPanel, jtb1, heatmapColorChooser, jtb2, simGoodColorChooser, simBadColorChooser);

        // Add tabs to main panel and panel to frame
        mainPanel.addTab("Game", gamePanel);
        mainPanel.addTab("Parameters", params);
        mainPanel.addTab("Instructions", instructions);

        add(comboPanel);
        pack();

        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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
        if (linePlot != null) {
            linePlot.paintComponent(g);
        }
    }
}
