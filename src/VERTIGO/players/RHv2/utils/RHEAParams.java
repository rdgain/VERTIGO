package VERTIGO.players.RHv2.utils;

import VERTIGO.players.ParameterSet;

import javax.swing.*;

import static VERTIGO.players.RHv2.utils.Constants.*;

/**
 * Created by rdgain on 3/20/2017.
 */
public class RHEAParams implements ParameterSet {

    public boolean DEBUG = false;
    public boolean EVOLVE = true; // set to false if no evolution required (ie Random Search)

    // variable
    public int POPULATION_SIZE = 5; //try 1,2,5
    public int SIMULATION_DEPTH = 10; //try 6,8,10
    public int INIT_TYPE = INIT_RANDOM;
    public int BUDGET_TYPE = HALF_BUDGET;
    public int MAX_FM_CALLS = 900;
    public int HEURISTIC_TYPE = HEURISTIC_WINSCORE;
    public int MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH MACRO-ACTION
    public int INNER_MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH INNER MACRO-ACTION

    public boolean BANDIT_MUTATION = false; //if false - random; if true - bandit
    public boolean MUT_BIAS = false;
    public boolean MUT_DIVERSITY = false;
    public int CROSSOVER_TYPE = UNIFORM_CROSS; // 0 - 1point; 1 - uniform

    public boolean TREE = false;
    public boolean CHOOSE_TREE = false;
    public boolean SHIFT_BUFFER = false;

    public boolean ROLLOUTS = false;
    public int ROLLOUT_LENGTH = SIMULATION_DEPTH / 2;
    public int REPEAT_ROLLOUT = 5;

    public boolean POP_DIVERSITY = false;
    public int DIVERSITY_TYPE = DIVERSITY_PHENOTYPE;
    public double D = 1; // weight given to diversity in fitness, values in range [0,1]

    // set
    public boolean REEVALUATE = false;
    public int MUTATION = 1;
    public int TOURNAMENT_SIZE = 2;
    public int NO_PARENTS = 2;
    public int RESAMPLE = 1; //try 1,2,3
    public int ELITISM = 1;
    public double DISCOUNT = 1; //0.99;
    public double SHIFT_DISCOUNT = 0.99;


    public boolean canCrossover() {
        return POPULATION_SIZE > 1;
    }

    public boolean canTournament() {
        return POPULATION_SIZE > TOURNAMENT_SIZE;
    }

    public boolean isRMHC() { return POPULATION_SIZE == 1; }

    public boolean isInnerMacro() { return INNER_MACRO_ACTION_LENGTH > 1; }

    public String[] getParams() {
        String[] s = new String[]{"Population size", "Simulation depth", "Init type", "Budget type", "Budget (FM calls)",
        "Heuristic type", "Macro-action length", "Inner macro-action length", "Bandit mutation", "Softmax bias in mutation",
        "Diversity in mutation", "Crossover type", "Statistical tree", "Stat tree recommend", "Shift buffer", "Rollouts",
        "Rollout length", "Repeat rollout N", "Population diversity", "Diversity type", "Diversity weight", "Reevaluate individuals",
        "N genes mutated", "Tournament size", "N parents", "Resample rate", "Elitism", "Discount reward", "Discount shift buffer"};
        return s;
    }

    @Override
    public String[] getParamDescriptions() {
        return new String[] {
                "How many individuals are evolved at a time.",
                "How long is an individual (= sequence of actions).",
                "Initialisation type",
                "Type of budget",
                "Number of forward model calls allowed for the agent",
                "Type of game state evaluation heuristic",
                "Number of game ticks action chosen is repeated before a new one is decided",
                "Number of actions that make up a gene",
                "If true, mutation type is bandit-based (UCB1)",
                "If true, mutation type is softmax, biased towards front of individual",
                "If true, mutation encourages diversity in the individual.",
                "Type of crossover used",
                "If true, a tree of statistics on each action is stored",
                "If true, the statistics tree is used to recommend the action to play (instead of first action of best individual)",
                "If true, population is kept between game ticks and only first action is removed, all individuals shifted forward and new random action added at the end",
                "If true, Monte Carlo rollouts are added at the end of individual evaluation",
                "How long is a Monte Carlo rollout",
                "How many times MC rollouts are repeated to obtain fitness value (average)",
                "If true, diversity is used in fitness",
                "Type of diversity to use",
                "Weight given to diversity in fitness",
                "If true, individuals are reevaluated at each generation, even if previously evaluated already",
                "Number of genes to mutate",
                "How many individuals take part in selection tournament",
                "Number of parents used in crossover",
                "Individuals are evaluated this many times, and fitness is average of all values",
                "Number of individuals promoted directly to the next generation",
                "Discount used in fitness evaluation",
                "Discount used with shift buffer to reduce previous values of individuals",
        };
    }

    public String[] getDefaultValues() {
        String[] s = new String[]{""+POPULATION_SIZE, ""+SIMULATION_DEPTH, ""+INIT_TYPE, ""+BUDGET_TYPE, ""+MAX_FM_CALLS,
        ""+HEURISTIC_TYPE, ""+MACRO_ACTION_LENGTH, ""+INNER_MACRO_ACTION_LENGTH, ""+BANDIT_MUTATION, ""+MUT_BIAS, ""+MUT_DIVERSITY,
        ""+CROSSOVER_TYPE, ""+TREE, ""+CHOOSE_TREE, ""+SHIFT_BUFFER, ""+ROLLOUTS, ""+ROLLOUT_LENGTH, ""+REPEAT_ROLLOUT,
        ""+POP_DIVERSITY, ""+DIVERSITY_TYPE, ""+D, ""+REEVALUATE, ""+MUTATION, ""+TOURNAMENT_SIZE, ""+NO_PARENTS, ""+RESAMPLE, ""+ELITISM,
        ""+DISCOUNT, ""+SHIFT_DISCOUNT};
        return s;
    }

    public Object[][] getValueOptions() {
        return new Object[][] {new Integer[]{1, 2, 5, 10, 15, 20, 50},
                new Integer[]{5, 10, 12, 15, 20, 25, 50},
                new String[]{"Random", "OneStep", "MCTS"},
                new String[]{"Full budget evo", "Budget include init"},
                new Integer[]{200, 500, 900, 1000, 3000, 5000},
                new String[]{"WinScore", "SimpleState"},
                new Integer[]{1, 5, 10},
                new Integer[]{1, 2, 3},
                new Boolean[]{false, true},
                new Boolean[]{false, true},
                new Boolean[]{false, true},
                new String[]{"1-Point", "Uniform"},
                new Boolean[]{false, true},
                new Boolean[]{false, true},
                new Boolean[]{false, true},
                new Boolean[]{false, true},
                new Integer[]{5, 10, 12, 15, 20, 25, 50},
                new Integer[]{1, 5, 10},
                new Boolean[]{false, true},
                new String[]{"Genotypic", "Phenotypic"},
                new Double[]{0.0, 0.2, 0.5, 0.7, 1.0},
                new Boolean[]{false, true},
                new Integer[]{1, 2, 5},
                new Integer[]{1, 2, 5},
                new Integer[]{2},
                new Integer[]{1, 2, 3},
                new Integer[]{0, 1, 2},
                new Double[]{0.5, 0.7, 0.9, 0.99, 1.0},
                new Double[]{0.5, 0.7, 0.9, 0.99, 1.0}};
    }

    @Override
    public String toString() {
        String s = "";

        String init = "none";
        if (INIT_TYPE == INIT_RANDOM) init = "random";
        else if (INIT_TYPE == INIT_ONESTEP) init = "OneStep";
        else if (INIT_TYPE == INIT_MCTS) init = "MCTS";

        String bud = "none";
        if (BUDGET_TYPE == FULL_BUDGET) bud = "full budget";
        else if (BUDGET_TYPE == HALF_BUDGET) bud = "half budget";

        String heur = "none";
        if (HEURISTIC_TYPE == HEURISTIC_WINSCORE) heur = "WinScore";
        else if (HEURISTIC_TYPE == HEURISTIC_SIMPLESTATE) heur = "SimpleState";

        String cross = "none";
        if (CROSSOVER_TYPE == UNIFORM_CROSS) cross = "uniform";
        else if (CROSSOVER_TYPE == POINT1_CROSS) cross = "1-Point";

        s += "---------- PARAMETER SET ----------\n";
        s += String.format("%1$-20s", "Population size") + ": " + POPULATION_SIZE + "\n";
        s += String.format("%1$-20s", "Individual length") + ": " + SIMULATION_DEPTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Initialization type") + ": " + init + "\n";
        s += String.format("%1$-20s", "Budget type") + ": " + bud + "\n";
        s += String.format("%1$-20s", "Budget") + ": " + MAX_FM_CALLS + "\n";
        s += String.format("%1$-20s", "Resampling") + ": " + RESAMPLE + "\n";
        s += String.format("%1$-20s", "Heuristic") + ": " + heur + "\n";
        s += String.format("%1$-20s", "Value discount") + ": " + DISCOUNT + "\n";
        s += String.format("%1$-20s", "Elitism") + ": " + ELITISM + "\n";
        s += String.format("%1$-20s", "Reevaluate?") + ": " + REEVALUATE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Macro Action Length") + ": " + MACRO_ACTION_LENGTH + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Bandit mutation?") + ": " + BANDIT_MUTATION + "\n";
        s += String.format("%1$-20s", "Genes mutated") + ": " + MUTATION + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Tournament size") + ": " + TOURNAMENT_SIZE + "\n";
        s += String.format("%1$-20s", "Crossover type") + ": " + cross + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Stats tree?") + ": " + TREE + "\n";
        s += String.format("%1$-20s", "Choose tree?") + ": " + CHOOSE_TREE + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Shift buffer?") + ": " + SHIFT_BUFFER + "\n";
        s += String.format("%1$-20s", "Shift discount?") + ": " + SHIFT_DISCOUNT + "\n";
        s += "\n";
        s += String.format("%1$-20s", "Rollouts?") + ": " + ROLLOUTS + "\n";
        s += String.format("%1$-20s", "Rollout length") + ": " + ROLLOUT_LENGTH + "\n";
        s += String.format("%1$-20s", "Repeat rollouts") + ": " + REPEAT_ROLLOUT + "\n";
        s += "---------- ------------- ----------\n";

        return s;
    }

    public ParameterSet getParamSet(JComboBox[] paramInputs) {
        RHEAParams paramSet = new RHEAParams();
        paramSet.POPULATION_SIZE = (int) paramInputs[0].getSelectedItem(); // population size
        paramSet.SIMULATION_DEPTH = (int) paramInputs[1].getSelectedItem(); // individual length
        paramSet.INIT_TYPE = paramInputs[2].getSelectedIndex();
        paramSet.BUDGET_TYPE = paramInputs[3].getSelectedIndex();
        paramSet.MAX_FM_CALLS = (int) paramInputs[4].getSelectedItem(); // number of FM calls
        paramSet.HEURISTIC_TYPE = paramInputs[5].getSelectedIndex();
        paramSet.MACRO_ACTION_LENGTH = (int) paramInputs[6].getSelectedItem();
        paramSet.INNER_MACRO_ACTION_LENGTH = (int) paramInputs[7].getSelectedItem();
        paramSet.BANDIT_MUTATION = (boolean) paramInputs[8].getSelectedItem();
        paramSet.MUT_BIAS = (boolean) paramInputs[9].getSelectedItem();
        paramSet.MUT_DIVERSITY = (boolean) paramInputs[10].getSelectedItem();
        paramSet.CROSSOVER_TYPE = paramInputs[11].getSelectedIndex();
        paramSet.TREE = (boolean) paramInputs[12].getSelectedItem();
        paramSet.CHOOSE_TREE = (boolean) paramInputs[13].getSelectedItem();
        paramSet.SHIFT_BUFFER = (boolean) paramInputs[14].getSelectedItem();
        paramSet.ROLLOUTS = (boolean) paramInputs[15].getSelectedItem();
        paramSet.ROLLOUT_LENGTH = (int) paramInputs[16].getSelectedItem();
        paramSet.REPEAT_ROLLOUT = (int) paramInputs[17].getSelectedItem();
        paramSet.POP_DIVERSITY = (boolean) paramInputs[18].getSelectedItem();
        paramSet.DIVERSITY_TYPE = paramInputs[19].getSelectedIndex();
        paramSet.D = (double) paramInputs[20].getSelectedItem();
        paramSet.REEVALUATE = (boolean) paramInputs[21].getSelectedItem();
        paramSet.MUTATION = (int) paramInputs[22].getSelectedItem();
        paramSet.TOURNAMENT_SIZE = (int) paramInputs[23].getSelectedItem();
        paramSet.NO_PARENTS = (int) paramInputs[24].getSelectedItem();
        paramSet.RESAMPLE = (int) paramInputs[25].getSelectedItem();
        paramSet.ELITISM = (int) paramInputs[26].getSelectedItem();
        paramSet.DISCOUNT = (double) paramInputs[27].getSelectedItem();
        paramSet.SHIFT_DISCOUNT = (double) paramInputs[28].getSelectedItem();
        return paramSet;
    }
}
