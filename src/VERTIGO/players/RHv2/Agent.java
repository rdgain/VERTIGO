package VERTIGO.players.RHv2;

import VERTIGO.players.RHv2.utils.DrawingAgent;
import VERTIGO.players.RHv2.utils.RHEAParams;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.EvoAnalyzer;

import java.awt.*;
import java.util.Arrays;
import java.util.Random;

import static VERTIGO.players.RHv2.utils.Constants.*;
import static tools.EvoAnalyzer.analysis;

@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions"})
public class Agent extends AbstractPlayer {

    RHEAParams parameters;

    private boolean experiment = true;
    boolean realTime = false;
    static boolean printTree = false;

    private RollingHorizonPlayer player;

    //Drawing
    public static boolean drawing = false;
    public static int drawingWhat = DRAW_ET;
    static DrawingAgent drawingAgent;
    public static Color goodAction = new Color(47, 214, 184);
    public static Color badAction = new Color(207, 64, 63);
    public static Color explorationColor = new Color(0, 0, 0);

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        if (!experiment) {
            // Set up parameters
            parameters = new RHEAParams();
        }

        // Set up random generator
        Random randomGenerator = new Random();

        // Set up drawing
        drawingAgent = new DrawingAgent(stateObs);

        // Set up algorithm to get actionc
        player = new RollingHorizonPlayer(stateObs, randomGenerator);

    }

    /**
     * Act method of agent, called at every game tick.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due (not used).
     * @return - action to play in the game
     */
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        // Set up for drawing in this game tick
        drawingAgent.init(stateObs);

        if (analysis) {
            EvoAnalyzer.initGameTick();
        }

        // Find action to play
        int nextAction = player.run(stateObs, this);

        // Printing pop stats; how many times each action was seen in each gene
        parameters = (RHEAParams) params;
        if (parameters.DEBUG) {
            Population pop = player.getPopulation();
            System.out.println(Arrays.toString(pop.getActionCountAllGen()));
        }

        Types.ACTIONS actionToPlay = player.getActionMapping(nextAction);

        // Write stats about evolution during this game tick to file
        //Analysis at end of game tick
        if (analysis) {
            EvoAnalyzer.endGameTick(player.getPopulation().getBestFitness(), nextAction);
            writeEvoFile(actionToPlay);
            logScore(stateObs.getGameScore());
        }

        if (printTree) {
            if (stateObs.getGameTick() == 100)
                System.out.println(player.getPopulation().getStatsTree().treeToString());
        }

        return actionToPlay;
    }

    public void draw(Graphics2D g) {
        if (drawing) {
            drawingAgent.draw(g, drawingWhat);
        }
    }

    //Evo, [AVG CONVERGENCE], [STAT SUMMARY FINAL BEST FITNESS], [PERC LEVEL EXPLORATION] [PERC LEVEL EXPLORATION FM]
    public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer)
    {
        if (analysis) {
            EvoAnalyzer.printFinalEvo(stateObs);
        }
    }

    //[CONVERGENCE], [ENTROPY_E] [ACTIONS EXPLORED], [REC ACTION PERC] [ENTROPY_I] [ACTIONS REC], [STAT SUMMARY FITNESS]
    private void writeEvoFile(Types.ACTIONS nextAction) {
        if (analysis) {
            EvoAnalyzer.writeEvo(evoWriter, nextAction, SHOULD_LOG);
        }
    }

}
