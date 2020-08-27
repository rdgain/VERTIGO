package tools;

import core.game.Observation;
import core.game.StateObservation;
import core.player.Player;
import ontology.Types;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static Demo.RHDemo.resultWriter;

public class EvoAnalyzer {
    public static boolean analysis = true; // if false, no analysis will be recorded during the play

    private static StatSummary2 bestFitness; // SS of best fitness at each game tick
    private static Set<Vector2d> exploredG; // positions explored while playing the game
    private static Set<Vector2d> exploredFM; // positions explored in Forward Model simulations
    public static ArrayEntropy entropyC; // Shannon entropy over the actions chosen to play in the game

    //Resets on game tick
    private static ArrayList<Integer> chosenAction; // best action at each generation during evolution, all generations
    static StatSummary2 fitnessLandscape; // all fitness values seen during evolution
    static StatSummary2[] fitnessLandscapePerAct; // all fitness values seen during evolution, separated per action
    static ArrayList<Types.ACTIONS> actionsExplored; // 'next' actions explored during evolution, all generations
    static ArrayList<Types.ACTIONS> actionsRecommended; // 'next' actions recommended during evolution, final generation
    private static int convergence; // convergence at one game tick (number of generation when final action was decided)
    private static int countWin, countLoss; // counts wins and losses seen during evolution
    private static int[] countWinPerAct, countLossPerAct; // counts wins and losses seen during evolution, separated per action

    //Keep things that reset on game tick
    static ArrayList<ArrayList<Integer>> chosenActionAllTicks;
    static ArrayList<StatSummary2> fitnessLandscapeAllTicks;
    static ArrayList<ArrayList<Types.ACTIONS>> actionsExploredAllTicks;
    static ArrayList<ArrayList<Types.ACTIONS>> actionsRecommendedAllTicks;
    private static double sumConvergence; // sum convergence over all game ticks
    private static int gameTickFirstWin, gameTickFirstLoss; // keeps track of first game tick when win and loss were seen

    private static int tick = -1;



    //---------- Methods to be called from the agent

    /**
     * Called once at the start of each game.
     */
    public static void setup() {
        sumConvergence = 0;
        gameTickFirstWin = -1;
        gameTickFirstLoss = -1;
        countWin = 0;
        countWinPerAct = new int[Types.AGENT_ACTION_COUNT];
        countLoss = 0;
        countLossPerAct = new int[Types.AGENT_ACTION_COUNT];
        bestFitness = new StatSummary2();
        exploredG = new HashSet<>();
        exploredFM = new HashSet<>();
        actionsRecommended = new ArrayList<>();
        actionsExplored = new ArrayList<>();
        chosenAction = new ArrayList<>();
        fitnessLandscape = new StatSummary2();
        fitnessLandscapePerAct = new StatSummary2[Types.AGENT_ACTION_COUNT];
        for (int i = 0; i < fitnessLandscapePerAct.length; i++) {
            fitnessLandscapePerAct[i] = new StatSummary2();
        }
        fitnessLandscapeAllTicks = new ArrayList<>();
        chosenActionAllTicks = new ArrayList<>();
        actionsExploredAllTicks = new ArrayList<>();
        actionsRecommendedAllTicks = new ArrayList<>();
    }

    /**
     * Called once at the start of each game tick.
     */
    public static void initGameTick() {
        tick++;
        if (tick > 0) {
            chosenActionAllTicks.add(chosenAction);
            fitnessLandscapeAllTicks.add(fitnessLandscape);
            actionsExploredAllTicks.add(actionsExplored);
            actionsRecommendedAllTicks.add(actionsRecommended);
        }
        actionsRecommended.clear();
        actionsExplored.clear();
        fitnessLandscape = new StatSummary2();
        fitnessLandscapePerAct = new StatSummary2[Types.AGENT_ACTION_COUNT];
        for (int i = 0; i < fitnessLandscapePerAct.length; i++) {
            fitnessLandscapePerAct[i] = new StatSummary2();
        }
        chosenAction.clear();
        countWin = 0;
        countWinPerAct = new int[Types.AGENT_ACTION_COUNT];
        countLoss = 0;
        countLossPerAct = new int[Types.AGENT_ACTION_COUNT];
    }

    /**
     * Called once at the end of each game tick
     * @param bestFitness - best fitness found during the game tick
     * @param nextAction - action to play decided during the game tick
     */
    public static void endGameTick(double bestFitness, int nextAction) {
        convergence = checkConvergence(nextAction);
        sumConvergence += convergence;
        EvoAnalyzer.bestFitness.add(bestFitness);
    }

    /**
     * Adds one position explored in the game (call at start of each game tick)
     */
    public static void addPositionExploredGame(Vector2d pos) {
        exploredG.add(pos);
    }

    /**
     * Adds one position explored in simulations (call at every advance() method call)
     */
    public static void addPositionExploredFM(Vector2d pos) {
        exploredFM.add(pos);
    }

    /**
     * Adds one action recommended (call at the end of the game tick
     * adding the first actions of individuals in the final pop)
     */
    public static void addActionRecommended(Types.ACTIONS act) {
        actionsRecommended.add(act);
    }

    /**
     * Adds one action recommended (call at the end of the game tick
     * adding the first actions of individuals in the final pop)
     * Keep size of list no bigger than ${window size}
     */
    public static void addActionRecommended(Types.ACTIONS act, int windowSize) {
        if (actionsRecommended.size() >= windowSize) {
            actionsRecommended.remove(0);
        }
        actionsRecommended.add(act);
    }

    /**
     * Adds one action explored during the game tick (call at every iteration
     * adding the first actions of all individuals)
     */
    public static void addActionExplored(Types.ACTIONS act) {
        actionsExplored.add(act);
    }

    /**
     * Keep track of the action recommended at each iteration
     */
    public static void addActionBest(int act) {
        chosenAction.add(act);
    }

    /**
     * Keep track of all fitness values seen during one game tick
     */
    public static void addFitness(double fitness, int act) {
        fitnessLandscape.add(fitness);
        fitnessLandscapePerAct[act].add(fitness);
    }

    /**
     * Count one win during the game tick
     */
    public static void countWin(int gameTick, int act) {
        if (gameTickFirstWin == -1) {
            gameTickFirstWin = gameTick;
        }
        countWin++;
        countWinPerAct[act]++;
    }

    /**
     * Count one loss during the game tick
     */
    public static void countLoss(int gameTick, int act) {
        if (gameTickFirstLoss == -1) {
            gameTickFirstLoss = gameTick;
        }
        countLoss++;
        countLossPerAct[act]++;
    }


    /**
     * Writing result, called once at the end of each game. Format:
     * Evo, [AVG CONVERGENCE], [STAT SUMMARY FINAL BEST FITNESS 9], [PERC LEVEL EXPLORATION] [PERC LEVEL EXPLORATION FM], [FIRST_TICK_WIN] [FIRST_TICK_LOSS]
     * @param stateObs - state observation at the end of the game
     */
    public static void printFinalEvo(StateObservation stateObs) {
        ArrayList<Observation>[][] obs = stateObs.getObservationGrid();
        double levelSize = obs.length * obs[0].length;
        String res = "Evo " + String.format("%.2f", avgConvergence(stateObs)) + " "
                + bestFitness.toString() + " " + String.format("%.2f",exploredG.size() / levelSize) + " "
                + String.format("%.2f",exploredFM.size() / levelSize) + " " + gameTickFirstWin + " " + gameTickFirstLoss;
        try {
            resultWriter.write(res + "\n");
            resultWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(res);
    }

    /**
     * Writing result to separate log file, called once at the end of each game tick. Format:
     * [CONVERGENCE], [ENTROPY_E] [ACTIONS EXPLORED 6], [REC ACTION PERC] [ENTROPY_I] [ACTIONS REC 6],
     * [STAT SUMMARY FITNESS 9], [AVG FITNESS PER ACT 6], [COUNT_WIN] [COUNT WIN PER ACT 6], [COUNT_LOSS] [COUNT LOSS PER ACT 6]
     * @param writer - BufferedWriter object to write to file
     * @param nextAction - action to play decided during the game tick
     * @param SHOULD_LOG - if true, logs
     */
    public static void writeEvo(BufferedWriter writer, Types.ACTIONS nextAction, boolean SHOULD_LOG) {
        if (writer != null && SHOULD_LOG) {
            ArrayEntropy entropyE = new ArrayEntropy(actionsExplored);
            ArrayEntropy entropyI = new ArrayEntropy(actionsRecommended);
            try {
                writer.write("" + convergence + " " + actionsToString(entropyE) + " "
                        + String.format("%.2f", entropyI.getPercentage(nextAction)) + " " + actionsToString(entropyI) + " "
                        + fitnessLandscape.toString() + " " + getAvgFitnessPerAct() + " " + countWin + " " +
                        getCountWinPerAct() + " " + countLoss + " " + getCountLossPerAct() + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //---------- Methods called from the Game class, don't call from agent.

    /**
     * Calculates the entropy of chosen actions at the end of the game.
     * @param player - player to get entropy of.
     */
    public static void calcEntropyChosenActions(Player player) {
        entropyC = new ArrayEntropy(player.getAllActions());
    }


    //---------- Helper methods, don't call from agent.

    /**
     * Gets average convergence at the end of the game.
     */
    private static double avgConvergence(StateObservation stateObs) {
        return 1.0 * sumConvergence / stateObs.getGameTick();
    }

    /**
     * Returns a nicely formatted ArrayEntropy object containing actions
     */
    public static String actionsToString(ArrayEntropy e) {
        StringBuilder s = new StringBuilder("");
        if (e != null) {
             s.append(String.format("%.2f", e.entropy)).append(" ");
            for (Types.ACTIONS action : Types.ACTIONS.values()) {
                if (action != Types.ACTIONS.ACTION_ESCAPE && action != Types.ACTIONS.ACTION_PAUSE) {
                    double p = e.getPercentage(action);
                    s.append(p == -1 ? -1 : String.format("%.2f", p)).append(" ");
                }
            }
            return s.substring(0, s.length() - 1);
        }
        s.append("-1");
        return s.toString();
    }

    /**
     * Checks convergence of algorithm (what generation the final action was found)
     * @param finalAction - the action chosen to be played in the game at the end of the evolution process
     */
    public static int checkConvergence(int finalAction) {
        int found = -1;
        int i;
        boolean ok;
        for (i = 0; i < chosenAction.size(); i++) {
            if (chosenAction.get(i) == finalAction) {
                ok = true;
                for (int j = i + 1; j < chosenAction.size(); j++) {
                    if (chosenAction.get(j) != finalAction) {
                        ok = false; break;
                    }
                }
                if (ok) {
                    found = i + 1; break;
                }
            }
        }

        if (found == -1) found = chosenAction.size();

        return found;
    }

    /**
     * Returns nicely formatted AVERAGE values for FITNESS per ACT
     */
    private static String getAvgFitnessPerAct() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < fitnessLandscapePerAct.length; i++) {
            s.append(String.format("%.2f", fitnessLandscapePerAct[i].mean()));
            if (i < fitnessLandscapePerAct.length - 1)
                s.append(" ");
        }
        return s.toString();
    }

    /**
     * Returns nicely formatted COUNT values for WIN per ACT
     */
    private static String getCountWinPerAct() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < countWinPerAct.length; i++) {
            s.append(countWinPerAct[i]);
            if (i < countWinPerAct.length - 1)
                s.append(" ");
        }
        return s.toString();
    }

    /**
     * Returns nicely formatted COUNT values for LOSS per ACT
     */
    private static String getCountLossPerAct() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < countLossPerAct.length; i++) {
            s.append(countLossPerAct[i]);
            if (i < countLossPerAct.length - 1)
                s.append(" ");
        }
        return s.toString();
    }
}
