package VERTIGO;

import VERTIGO.plots.LinePlot;
import controllers.singlePlayer.RHv2.utils.ParameterSet;
import core.ArcadeMachine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import static VERTIGO.HelperMethods.closeAllPlots;
import static VERTIGO.HelperMethods.parseResult;
import static VERTIGO.VertigoView.*;

public class GameRunner implements Runnable {

    JFrame frame;
    JPanel gamePanel;
    String game_file, level_file;
    boolean visuals;
    String agentNames;
    String actionFile, evoFile;
    int randomSeed;
    int playerID;
    ParameterSet params;

    JComboBox<String> gameOptions; JComboBox<Integer> levelOptions;
    DefaultTableModel evoResTableModel, gameResTableModel;

    LinePlot linePlot;

    public GameRunner(JFrame frame, JPanel gamePanel, String game_file, String level_file, boolean visuals,
                      String agentNames, String actionFile, String evoFile, int randomSeed, int playerID, ParameterSet params,
                      JComboBox<String> gameOptions, JComboBox<Integer> levelOptions, DefaultTableModel evoResTableModel, DefaultTableModel gameResTableModel,

                      LinePlot linePlot) {

        this.frame = frame;
        this.gamePanel = gamePanel;
        this.game_file = game_file;
        this.level_file = level_file;
        this.visuals = visuals;
        this.agentNames = agentNames;
        this.actionFile = actionFile;
        this.evoFile = evoFile;
        this.randomSeed = randomSeed;
        this.playerID = playerID;
        this.params = params;

        this.gameOptions = gameOptions;
        this.levelOptions = levelOptions;
        this.evoResTableModel = evoResTableModel;
        this.gameResTableModel = gameResTableModel;

        this.linePlot = linePlot;
    }

    @Override
    public void run() {
        linePlot.reset();

        //Play game with params
        try {
            ArcadeMachine.runOneExpGame(frame, gamePanel, game_file, level_file, visuals, agentNames, actionFile, evoFile, randomSeed, playerID, params);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //End of run, reset
        startGame = false;
        stopGame = false;
        pauseGame = false;
        readyGame = false;
        pauseB.setEnabled(false);
        stopB.setEnabled(false);

        // Display game result in frame
        parseResult(evoResTableModel, gameResTableModel);

        // Wait for all plots to be closed before proceeding
        closeAllPlots(evoFile, readyB, gameOptions, levelOptions);
    }
}
