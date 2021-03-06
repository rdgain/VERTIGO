package VERTIGO;

import VERTIGO.players.RHv2.utils.RHEAParams;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;

import static VERTIGO.VertigoView.resultFile;

@SuppressWarnings("UnnecessaryLocalVariable")
public class HelperMethods {

    static String[] getGames() {
        String[] games = new String[]{"aliens", "angelsdemons", "assemblyline", "avoidgeorge", "bait", //0-4
                "beltmanager", "blacksmoke", "boloadventures", "bomber", "bomberman",                  //5-9
                "boulderchase", "boulderdash", "brainman", "butterflies", "cakybaky",                  //10-14
                "camelRace", "catapults", "chainreaction", "chase", "chipschallenge",                  //15-19
                "clusters", "colourescape", "chopper", "cookmepasta", "cops",                          //20-24
                "crossfire", "defem",  "defender", "deflection", "digdug",                             //25-29
                "donkeykong", "doorkoban", "dungeon", "eighthpassenger", "eggomania",                  //30-34
                "enemycitadel", "escape", "factorymanager", "firecaster",  "fireman",                  //35-39
                "firestorms", "freeway", "frogs", "garbagecollector", "ghostbuster",                   //40-44
                "gymkhana", "hungrybirds", "iceandfire", "ikaruga", "infection",                       //45-49
                "intersection", "islands", "jaws", "killbillVol1", "labyrinth",                        //50-54
                "labyrinthdual", "lasers", "lasers2", "lemmings", "mirrors",                           //55-59
                "missilecommand", "modality", "overload", "pacman", "painter",                         //60-64
                "pokemon", "plants", "plaqueattack", "portals", "racebet",                             //65-69
                "raceBet2", "realportals", "realsokoban", "rivers", "roadfighter",                     //70-74
                "roguelike", "run", "seaquest", "sheriff", "shipwreck",                                //75-79
                "sokoban", "solarfox" ,"superman", "surround", "survivezombies",                       //80-84
                "tercio", "thecitadel", "themole", "theshepherd", "thesnowman",                        //85-89
                "vortex",  "waitforbreakfast", "watergame", "waves", "whackamole",                     //90-94
                "wildgunman", "witnessprotected", "witnessprotection", "wrapsokoban", "x-racer",       //95-99
                "zelda", "zenpuzzle" };                                                                //100,101

//        games = new String[]{"digdug", "lemmings", "roguelike", "chopper", "crossfire",
//                "chase", "camelRace", "escape", "hungrybirds", "bait", "waitforbreakfast",
//                "survivezombies", "modality", "missilecommand", "plaqueattack",
//                "seaquest", "infection", "aliens", "butterflies", "intersection"};

        return games;
    }

    static void parseResult(DefaultTableModel evoResTableModel, DefaultTableModel gameResTableModel) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(resultFile));
            String line = br.readLine();
            String evoRes = null;
            String gameRes = null;

            while (line != null) {
                if (line.contains("Evo")) {
                    evoRes = line;
                }
                if (line.contains("Result")) {
                    gameRes = line;
                }
                line = br.readLine();
            }

            // Parse evo result
            if (evoRes != null) {
                String[] evo = evoRes.split(" ");
                // add row dynamically into the table
                evoResTableModel.addRow(new Object[]{evo[1], evo[4], evo[11],
                        evo[12], evo[13], evo[14]});
            }

            // Parse game result
            if (gameRes != null) {
                String[] game = gameRes.split(" ");
                int win = Integer.parseInt(game[1]);
                String res = "";
                if (win == 0) {
                    res = "LOST";
                } else if (win == 1) {
                    res = "WON";
                } else if (win == -1) {
                    res = "INTERRUPTED";
                } else if (win == -100) {
                    res = "DISQUALIFIED";
                }

                gameResTableModel.addRow(new Object[]{res, game[2], game[3],
                        game[4]});

                //game[5] - game[10]: [ACTIONS CHOSEN]
            }

//                resEvoText.setText(resEvo);
//                resGameText.setText(resGame);

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static DefaultTableModel createEvoResTableModel() {
        DefaultTableModel evoResTableModel = new DefaultTableModel(0, 0);
        // Evo, [AVG CONVERGENCE], [STAT SUMMARY FINAL BEST FITNESS 9], [PERC LEVEL EXPLORATION] [PERC LEVEL EXPLORATION FM], [FIRST_TICK_WIN] [FIRST_TICK_LOSS]
        String[] header = new String[]{"Avg Convergence", "Mean Fitness", "% Lvl Explored",
                "% Lvl Explored FM", "First Tick Win", "First Tick Loss"};
        evoResTableModel.setColumnIdentifiers(header);
        return evoResTableModel;
    }

    static JScrollPane createEvoResPanel(DefaultTableModel evoResTableModel) {
        JTable evoResTable = new JTable();
        evoResTable.setModel(evoResTableModel);
        JScrollPane evoResPanel = new JScrollPane(evoResTable);
        evoResPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Result Evo",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        evoResPanel.setPreferredSize(new Dimension(700, 70));
        return evoResPanel;
    }

    static DefaultTableModel createGameResTableModel() {
        DefaultTableModel gameResTableModel = new DefaultTableModel(0, 0);
        // Result, [WIN] [SCORE] [TICK], [ENTROPY_C] [ACTIONS CHOSEN]
        String[] header = new String[]{"Win", "Score", "Ticks", "Action Entropy"};
        gameResTableModel.setColumnIdentifiers(header);
        return gameResTableModel;
    }

    static JScrollPane createGameResPanel(DefaultTableModel gameResTableModel) {
        JTable gameResTable = new JTable();
        gameResTable.setModel(gameResTableModel);
        JScrollPane gameResPanel = new JScrollPane(gameResTable);
        gameResPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                "Result Game",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        gameResPanel.setPreferredSize(new Dimension(700, 70));
        return gameResPanel;
    }

    static JPanel getGamePanel(JComboBox<String> agentOptions, JComboBox<String> gameOptions, JComboBox<Integer> levelOptions, JButton startB,
                               JButton pauseB, JButton stopB, JButton clearFilesBut, JScrollPane resEvo, JScrollPane resGame,
                               JToggleButton jtb1, JButton heatmapColorChooser, JToggleButton jtb2,
                               JButton simGoodColorChooser, JButton simBadColorChooser) {
        // Add things to game panel
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,2,2,2);
        c.gridy = 0;
        c.gridx = 1;
        gamePanel.add(new JLabel("Agent: "),c);
        c.gridx = 2;
        gamePanel.add(agentOptions,c);
        c.gridy = 1;
        c.gridx = 1;
        gamePanel.add(new JLabel("Game: "),c);
        c.gridx = 2;
        gamePanel.add(gameOptions,c);
        c.gridy = 2;
        c.gridx = 1;
        gamePanel.add(new JLabel("Level: "),c);
        c.gridx = 2;
        gamePanel.add(levelOptions,c);

        JPanel buttonpanel = new JPanel();
        buttonpanel.add(jtb1);
        buttonpanel.add(heatmapColorChooser);
        buttonpanel.add(jtb2);
        buttonpanel.add(simGoodColorChooser);
        buttonpanel.add(simBadColorChooser);
        gamePanel.add(buttonpanel);

        c.gridy = 4;
        c.gridx = 2;
        gamePanel.add(startB,c);
        c.gridy = 5;
        c.gridx = 1;
        gamePanel.add(pauseB,c);
        c.gridx = 2;
        gamePanel.add(stopB,c);
        c.gridx = 3;
        gamePanel.add(clearFilesBut, c);
        c.gridy = 9;
        c.gridx = 0;
        c.gridwidth = 4;
        gamePanel.add(resEvo,c);
        c.gridy = 10;
        c.gridx = 0;
        c.gridwidth = 4;
        gamePanel.add(resGame,c);
        return gamePanel;
    }

    public static void styleUI() {
        UIManager.put("TabbedPane.selected", new Color(40, 42, 58));
        UIManager.put("TabbedPane.foreground", Color.white);
        UIManager.put("TabbedPane.focus", new Color(10, 17, 33));
        UIManager.put("TabbedPane.background", new Color(10, 17, 33));
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0,0,0,0));
        UIManager.getDefaults().put("TabbedPane.tabAreaInsets", new Insets(0,0,1,0));

        UIManager.put("Panel.background", new Color(40, 42, 58));
        UIManager.put("Label.foreground", Color.white);

        UIManager.put("ScrollPane.background", new Color(10, 17, 33));
        UIManager.put("TitledBorder.titleColor", Color.white);

    }

}
