package VERTIGO.players.RHv2;

import VERTIGO.players.RHv2.bandits.BanditArray;
import VERTIGO.players.RHv2.sampleOLMCTS.SingleTreeNode;
import VERTIGO.players.RHv2.utils.RHEAParams;
import core.game.Observation;
import core.game.StateObservation;
import core.player.Player;
import ontology.Types;
import tools.EvoAnalyzer;
import tools.Utils;
import tools.Vector2d;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

import java.util.*;

import static VERTIGO.players.RHv2.utils.Constants.*;
import static tools.EvoAnalyzer.analysis;

/**
 * Created by Raluca on 27-Jun-17.
 */
public class Population {
    private Individual[] population;
    private StateHeuristic heuristic;
    public int numGenerations;

    private RollingHorizonPlayer player;
    private Player agent;
    private RHEAParams params;

    //Bandits
    private BanditArray bandits; // bandits for each gene

    private TreeNode statsTree;

    private HashMap<Integer, Integer>[] actionCountAllGen; //action:count array for all generations
    private HashMap<Integer, Integer> posCellCountAllGen; //pos/cell:count array for all generations
    private static int noCells, noCellsW, noCellsH, gridCellSize;
    static double noGridCellW, noGridCellH; //number grid cells per pos cell (smallest unit)

    /**
     * Constructor to initialize population
     * @param stateObs - StateObservation of current game tick
     */
    Population(StateObservation stateObs, Player agent, RollingHorizonPlayer player, int budget) {
        this.agent = agent;
        params = (RHEAParams) agent.getParameters();
        heuristic = new WinScoreHeuristic(stateObs);
        this.player = player;
        numGenerations = 0;

        // New tree
        if (params.TREE) {
            statsTree = new TreeNode(null, MAX_ACTIONS, 0, -1, player.random);
        }

        // New action count for all generations
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
            actionCountAllGen = new HashMap[params.SIMULATION_DEPTH * params.INNER_MACRO_ACTION_LENGTH];
            for (int i = 0; i < actionCountAllGen.length; i++) {
                actionCountAllGen[i] = new HashMap<>();
                Set<Integer> actionSpace = player.getActionMapping().keySet();
                for (int a = 0; a < actionSpace.size(); a++) {
                    actionCountAllGen[i].put(a, 0);
                }
            }
        }

        // New position cell count for all generations
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
            ArrayList<Observation>[][] obsGrid = stateObs.getObservationGrid();
            if (params.SIMULATION_DEPTH > 0) {
                noCellsH = (int) Math.ceil(1.0 * obsGrid.length / (params.SIMULATION_DEPTH / 2));
                noCellsW = (int) Math.ceil(1.0 * obsGrid[0].length / (params.SIMULATION_DEPTH / 2));
                noGridCellH = 1.0 * obsGrid.length / noCellsH;
                noGridCellW = 1.0 * obsGrid[0].length / noCellsW;
                gridCellSize = stateObs.getBlockSize();
                noCells = noCellsH * noCellsW;

                posCellCountAllGen = new HashMap<>();
                for (int i = 0; i < noCells; i++) {
                    posCellCountAllGen.put(i, 0);
                }
            }
        }

        // New population
        population = new Individual[params.POPULATION_SIZE];
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            population[i] = new Individual(player.nActions, player.random, heuristic, player, agent);
            if (params.INIT_TYPE == INIT_RANDOM) {
                addToActionCountAllGen(population[i]);
            }
        }

        // New bandits
        if (params.BANDIT_MUTATION) {
            bandits = new BanditArray(population, player.nActions, params.SIMULATION_DEPTH);
        }

    }

    private void addToActionCountAllGen(Individual ind) {
        int[] actionSequence = ind.getActions();
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
            for (int i = 0; i < actionSequence.length; i++) {
                int act = actionSequence[i];
                actionCountAllGen[i].put(act, actionCountAllGen[i].get(act) + 1);
            }
        }

        //These are actions explored
        if (analysis) {
            EvoAnalyzer.addActionExplored(player.getActionMapping(actionSequence[0]));
        }
    }

    void addAllToPosCellCountAllGen() {
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
            for (Individual i : population) {
                addToPosCellCountAllGen(i);
            }
        }
    }

    private void addToPosCellCountAllGen(Individual ind) {
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
            int[] actionSequence = ind.getActions();
            for (int i = 0; i < actionSequence.length; i++) {
                int idxGene = i / player.params.INNER_MACRO_ACTION_LENGTH;
                int idxAction = i % player.params.INNER_MACRO_ACTION_LENGTH;
                Vector2d pos = ind.getGene(idxGene).positions[idxAction];

                // which cell does this pos fit in?
                int cellNo = getGridCell(pos);

                if (posCellCountAllGen.containsKey(cellNo)) {
                    posCellCountAllGen.put(cellNo, posCellCountAllGen.get(cellNo) + 1);
                } else {
                    posCellCountAllGen.put(cellNo, 1);
                }
            }
        }
    }

    public HashMap<Integer, Integer>[] getActionCountAllGen() {
        return actionCountAllGen;
    }

    public HashMap<Integer, Integer> getPosCellCountAllGen() {
        return posCellCountAllGen;
    }

    public TreeNode getStatsTree() {
        return statsTree;
    }

    static int getGridCell(Vector2d pos) {
        int cellNo;
        int x = (int) Math.ceil(pos.x / (noGridCellW*gridCellSize));
        int y = (int) Math.ceil(pos.y / (noGridCellH*gridCellSize));

        cellNo = y*noCellsW + x;

        return cellNo;
    }

    /**
     * One Step Look Ahead initialization. For the first individual, roll the state with best action at each step
     * Rest of the individuals are mutations of the first
     * @param stateObs - current StateObservation
     * @param heuristic - Heuristic used by the 1SLA
     * @return - number of FM calls used in this method
     */
    int initOneStep (StateObservation stateObs, StateHeuristic heuristic) {
        int nCalls = 0;
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            if (i > 0) {
                population[i] = population[0].copy();
                mutation(population[i]);
            } else {
                Individual ind = population[i];
                StateObservation so = stateObs.copy();
                Types.ACTIONS bestAction;
                double maxQ;

                for (int k = 0; k < params.SIMULATION_DEPTH; k++) {
                    for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) {
                        bestAction = null;
                        maxQ = Double.NEGATIVE_INFINITY;

                        if (!so.isGameOver()) {
                            for (Types.ACTIONS action : so.getAvailableActions()) {

                                StateObservation stCopy = so.copy();
                                stCopy.advance(action);
                                double Q = heuristic.evaluateState(stCopy);
                                Q = Utils.noise(Q, epsilon, player.random.nextDouble());

                                //System.out.println("Action:" + action + " score:" + Q);
                                if (Q > maxQ) {
                                    maxQ = Q;
                                    bestAction = action;
                                }
                            }

                            ind.setGene(k, player.getReversedActionMapping(bestAction), m);
                            so.advance(bestAction);
                        }
                    }
                }

                nCalls += params.SIMULATION_DEPTH;
            }
            addToActionCountAllGen(population[i]);
        }
        return nCalls;
    }

    /**
     * Monte Carlo Tree Search initialization. For the first individual, use MCTS with half budget to find solution
     * Rest of the individuals are mutations of the first
     * @param stateObs - current StateObservation
     * @return - number of FM calls used in this method
     */
    int initMCTS (StateObservation stateObs) {
        int nCalls = 0;
        int MCTS_BUDGET = (int) (params.MAX_FM_CALLS * 0.5);

        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            if (i > 0) {
                population[i] = population[0].copy();
                mutation(population[i]);
            } else {
                Types.ACTIONS[] actions = new Types.ACTIONS[player.nActions];
                for (int j = 0; j < player.nActions; j++)
                    actions[j] = player.getActionMapping(j);
                SingleTreeNode m_root = new SingleTreeNode(player.random, player.nActions, actions);
                m_root.rootState = stateObs;//Do the search within the available time.
                m_root.mctsSearchCalls(MCTS_BUDGET, 10);

                // Seed only first gene
//               population[i].actions[0] = m_root.mostVisitedAction();

                // Seed N relevant genes
                ArrayList<Integer> ind = m_root.mostVisitedActions(m_root);
                int limit = ind.size() < params.SIMULATION_DEPTH ? ind.size() : params.SIMULATION_DEPTH;
                for (int j = 0; j < limit / params.INNER_MACRO_ACTION_LENGTH; j++) {
                    for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) {
                        population[i].setGene(j, ind.get(j), m);
                    }
                }

                nCalls += MCTS_BUDGET;
            }
            addToActionCountAllGen(population[i]);

        }
        return nCalls;
    }

    /**
     * Shift buffer. Shift population to the left (and trees and bandits), add random action at the end of all individuals
     * @param lastAct - the action that was played in previous game tick
     */
    void shiftLeft(int lastAct) {
        numGenerations = 0; // reset gens

        // Remove first action of all individuals and add a new random one at the end
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            for (int j = 0; j < params.SIMULATION_DEPTH - 1; j++) {
                    if (params.isInnerMacro()) {
                        for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) { // shift macros
                            Gene next = population[i].getGene(j + 1);
                            population[i].setGene(j, next);
                        }
                    } else {
                        int next = population[i].getGene(j + 1).getFirstAction();
                        population[i].setGene(j, (next < player.nActions) ? next : player.random.nextInt(player.nActions), 0);
                    }
            }
            population[i].setGene(params.SIMULATION_DEPTH - 1); // set last action as new random one
            population[i].resetValue();

            //These are actions explored
            if (analysis) {
                EvoAnalyzer.addActionExplored(player.getActionMapping(population[i].getGene(0).getFirstAction()));
            }
        }
        if (params.TREE) {
            // Cut the tree to the node that was chosen
            statsTree.shiftTree(lastAct, params.SHIFT_DISCOUNT);
        }
        if (params.BANDIT_MUTATION) {
            bandits.shiftArray(population, player.nActions, params.SHIFT_DISCOUNT);
        }

        // Shift overall pop stats
        if (params.POP_DIVERSITY && params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
            for (int i = 0; i < actionCountAllGen.length; i++) {
                if (i == actionCountAllGen.length - 1) {
                    actionCountAllGen[i] = new HashMap<>();
                    Set<Integer> actionSpace = player.getActionMapping().keySet();
                    for (int a = 0; a < actionSpace.size(); a++) {
                        actionCountAllGen[i].put(a, 0);
                    }
                } else {
                    actionCountAllGen[i] = actionCountAllGen[i + 1];
                }
            }
        }
    }

    /**
     * Returns the next action to play in the game
     * @return - by default, first action of best (first) individual
     */
    int getNextAction() {
        if (params.CHOOSE_TREE) {
//            return statsTree.getBestChild();
            return statsTree.getMostVisitedChild();
        }

        // remove diversity measure for recommendation policy
        double aux = params.D;
        params.D = 0;
        Arrays.sort(population);
        params.D = aux;

        return population[0].getGene(0).getFirstAction();
    }

    /**
     * Returns the best fitness value in the population
     * @return - fitness value of best (first) individual in the population
     */
    double getBestFitness() {
        return population[0].getValue();
    }

    Individual[] getPopulation() { return population;}

    static int n = 0;
    static double totalDiversity = 0;

    /**
     * Move to the next generation through crossover and mutation
     * @param stateObs - StateObservation of current game tick
     * @param budget - budget of FM calls left for execution
     * @return - number of FM calls during this call of the method
     */
    int nextGeneration(StateObservation stateObs, int budget) {
        numGenerations++;

        int nCalls = 0;
        Individual[] nextGenome = new Individual[params.POPULATION_SIZE];

        for (int i = 0; i < population.length; i++) {
            if (i < params.ELITISM && !params.isRMHC()) { // Individuals promoted through elitism are copied directly
                nextGenome[i] = population[i].copy();
            }
            else {
                Individual newInd = population[0].copy();

                // Crossover
                if (params.canCrossover()) {
                    newInd = crossover();
                }

                // Mutation
                mutation(newInd);

                // Evaluate new individual
                nCalls += evaluate(newInd, stateObs, false);

                // Insert new individual into population
                if (!params.isRMHC())
                    nextGenome[i] = newInd;
                else {
                    // Only 1 individual in the population, replace only if better
                    if (population[i].getValue() < newInd.getValue()) {
                        nextGenome[i] = newInd.copy();
                    }
                }

                // Add newInd actions to record of actions over all generations
                addToActionCountAllGen(newInd);
                addToPosCellCountAllGen(newInd);
            }
        }

        // Assign new population
        population = nextGenome;

        // Evaluate diversity for all individuals in new population
        evaluateDiversity(stateObs);

        // Sort the new population according to individual fitness values
        try {
            Arrays.sort(population);
        } catch (NullPointerException e) {
            System.out.println("Population.nextGeneration() call. Null values in population. This should not be happening.");
            e.printStackTrace();
        }

        // Debug print of evolutionary process during one game tick
//            double overallDiversity = 0;
//            for (Individual g : population) {
//                overallDiversity += g.getDiversityScore();
//            }
//            totalDiversity += overallDiversity;
//            n++;
//            System.out.println(String.format("%.2f", overallDiversity) + ": " + population[0]);


        return nCalls;
    }

    /**
     * Evaluate a certain individual in the population, passed through index
     * @param idx - index of individual to be evaluated
     * @param stateObs - StateObservation of current game tick
     * @return - FM calls used up in this method call
     */
    int evaluate(int idx, StateObservation stateObs, boolean evaluateAll) {
        return evaluate(population[idx], stateObs, evaluateAll);
    }

    private int evaluate(Individual newind, StateObservation stateObs, boolean evaluateAll) {
        return newind.evaluate(stateObs, params, statsTree, bandits, player.actionDist);
    }

    private int evaluateDiversity(StateObservation stateObs) {
        int nCalls = 0;

        if (params.POP_DIVERSITY) {
            //compute diversity score for all individuals in the population
            for (int i = 0; i < population.length; i++) {
                Individual aGenome = population[i];
                double diversityScore = 0;
                if (!aGenome.hasPositions()) { //if not already evaluated, evaluate
                    nCalls += aGenome.evaluate(stateObs, params, statsTree, bandits, player.actionDist);
                }
                //compare this individual to all the others in the population
                diversityScore += aGenome.diversityDiff(this);
                //update diversity score
                diversityScore /= population.length-1;
                aGenome.updateDiversityScore(diversityScore);
            }
        }

        return nCalls;
    }


    /**
     * Evaluate all individuals in the population
     * @param stateObs - StateObservation of current game tick
     * @return - FM calls used up in this method call
     */
    int evaluateAll(StateObservation stateObs, int budget) {
        int nCalls = 0;

        // Evaluate all individuals in the population
        for (int i = 0; i < params.POPULATION_SIZE; i++) {
            if (nCalls + params.SIMULATION_DEPTH <= budget) {
                nCalls += evaluate(i, stateObs, true);
            }
        }

        // Sort the new population according to individual fitness values
        if (params.POPULATION_SIZE > 1) {
            try {
                Arrays.sort(population);
            } catch (NullPointerException e) {
                System.out.println("Population.evaluateAll() call. Null values in population. This should not be happening.");
                e.printStackTrace();
            }
        }

        return nCalls;
    }

        /**
         * Mutates an individual using the correct mutation oeprator
         * @param newInd - individual to be mutated (if null, the first in the population)
         * @return - budget left after the mutation and evaluation of new individual
         */
    private void mutation(Individual newInd) {
        if (params.BANDIT_MUTATION) {
            newInd.banditMutate(bandits);
        } else {
            newInd.mutate(this);
        }
    }

    /**
     * Performs crossover throug tournament between individuals in the population
     * @return - new individual resulting from crossover
     */
    private Individual crossover() {
        Individual newInd = new Individual(player.nActions, player.random, heuristic, player, agent);
        Individual[] parents = new Individual[params.NO_PARENTS];

        // Get parents for crossover. Tournament if possible.
        if (params.canTournament()) {
            Individual[] tournament = new Individual[params.TOURNAMENT_SIZE];
            ArrayList<Individual> list = new ArrayList<>();
            list.addAll(Arrays.asList(population).subList(0, params.POPULATION_SIZE));
            Collections.shuffle(list);
            for (int i = 0; i < params.TOURNAMENT_SIZE; i++) {
                tournament[i] = list.get(i);
            }
            try {
                Arrays.sort(tournament);
            } catch (NullPointerException e) {
                System.out.println("Population.crossover() call. Null values in population. This should not be happening.");
                e.printStackTrace();
            }
            parents[0] = tournament[0];
            parents[1] = tournament[1];
        } else {
            parents[0] = population[0];
            parents[1] = population[1];
        }

        // Perform crossover, return resulting individual
        if (params.CROSSOVER_TYPE == POINT1_CROSS) {
            // 1-point
            int p = player.random.nextInt(params.SIMULATION_DEPTH - 3) + 1;
            for ( int i = 0; i < params.SIMULATION_DEPTH; i++) {
                if (i < p)
                    newInd.setGene(i, parents[0].getGene(i));
                else
                    newInd.setGene(i, parents[1].getGene(i));
            }
        } else if (params.CROSSOVER_TYPE == UNIFORM_CROSS) {
            // uniform
            for (int i = 0; i < params.SIMULATION_DEPTH; i++) {
                newInd.setGene(i, parents[player.random.nextInt(params.NO_PARENTS)].getGene(i));
            }
        }

        return newInd;
    }

    @Override
    public String toString() {
        return "Pop: " + Arrays.toString(population) + "\n";
    }
}
