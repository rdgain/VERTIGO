package VERTIGO.players.RHv2;

import VERTIGO.players.RHv2.utils.RHEAParams;
import core.game.StateObservation;
import core.player.Player;
import ontology.Types;
import tools.EvoAnalyzer;
import tools.Vector2d;
import tracks.singlePlayer.tools.Heuristics.SimpleStateHeuristic;

import java.util.HashMap;
import java.util.Random;

import static VERTIGO.players.RHv2.Agent.drawingAgent;
import static VERTIGO.players.RHv2.Agent.printTree;
import static VERTIGO.players.RHv2.utils.Constants.INIT_MCTS;
import static VERTIGO.players.RHv2.utils.Constants.INIT_ONESTEP;
import static tools.EvoAnalyzer.analysis;

/**
 * Created by rdgain on 6/28/2017.
 */
class RollingHorizonPlayer {

    RHEAParams params;
    private Population population;
    int numCalls;

    Random random;

    // Action mapping
    int nActions;
    private HashMap<Integer, Types.ACTIONS> action_mapping;
    private HashMap<Types.ACTIONS, Integer> action_mapping_r;
    int[] actionDist; // action distribution
    private int lastAct = -1;
    public int MAX_ACTIONS = 6;

    RollingHorizonPlayer(StateObservation stateObs, Random randomGen) {
        this.random = randomGen;
        initStateInfo(stateObs);
        Gene.initValidMutation(action_mapping);
    }

    private void initStateInfo(StateObservation stateObs) {
        nActions = stateObs.getAvailableActions().size() + 1;
        actionDist = new int[MAX_ACTIONS];
        action_mapping = new HashMap<>();
        action_mapping_r = new HashMap<>();
        int k = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(k, action);
            action_mapping_r.put(action, k);
            k++;
        }
        action_mapping.put(k, Types.ACTIONS.ACTION_NIL);
        action_mapping_r.put(Types.ACTIONS.ACTION_NIL, k);
    }


    /**
     * Initialize the player for a new game tick to update information.
     * @param stateObs - current StateObservation
     * @param agent - agent calling the player
     */
    void init(StateObservation stateObs, Player agent) {
        initStateInfo(stateObs);

        numCalls = 0;

        if (params.SHIFT_BUFFER && lastAct != -1) {
            population.shiftLeft(lastAct);
        } else {
            population = new Population(stateObs, agent, this, params.MAX_FM_CALLS);
            if (params.INIT_TYPE == INIT_ONESTEP) {
                numCalls += population.initOneStep(stateObs,new SimpleStateHeuristic(stateObs));
            }
            if (params.INIT_TYPE == INIT_MCTS) {
                numCalls += population.initMCTS(stateObs);
            }
        }

        numCalls += population.evaluateAll(stateObs, params.MAX_FM_CALLS - numCalls);

        population.addAllToPosCellCountAllGen();

    }

    /**
     * Run this agent. Initialize then evolve.
     * @param stateObs - current StateObservation
     * @param agent - the agent calling the player
     * @return - action to be played in the game
     */
    int run(StateObservation stateObs, Player agent) {
        params = (RHEAParams) agent.getParameters();
        int budget = params.MAX_FM_CALLS;

        init(stateObs, agent);

        drawingAgent.updatePos(stateObs.getAvatarPosition());

        //These are the positions explored in the game
        if (analysis) {
            EvoAnalyzer.addPositionExploredGame(stateObs.getAvatarPosition());
        }

        int nextAction = evolve(stateObs, agent, budget - numCalls);

        //These are the actions recommended by the final population obtained after evolution
        if (analysis) {
            for (Individual i : population.getPopulation()) {
                EvoAnalyzer.addActionRecommended(getActionMapping(i.getGene(0).getFirstAction()));
            }
        }

        lastAct = nextAction;
        return nextAction;
    }

    /**
     * Run evolution
     * @param stateObs - StateObservation of current game tick
     * @param agent - Agent calling this player
     * @param budget - Budget for evolution in FM calls
     * @return - action to play in the game
     */
    int evolve(StateObservation stateObs, Player agent, int budget) {
        params = (RHEAParams) agent.getParameters();

        // Keep track of algorithm inner workings during this game tick
        population.numGenerations = 0;
        double budgetOneGen = params.SIMULATION_DEPTH*(params.POPULATION_SIZE-params.ELITISM);

        // Keep track of best action and best fitness for analysis
        if (analysis) {
            EvoAnalyzer.addActionBest(population.getNextAction());
        }

        if (!stateObs.isGameOver() && (numCalls + budgetOneGen) <= budget && params.EVOLVE) {
            do {
                // If we should reevaluate individuals promoted through elitism, average fitness
                int numCallsReeval = 0;
                if (params.REEVALUATE) {
                    for (int i = 0; i < params.ELITISM; i++) {
                        numCallsReeval += population.evaluate(i, stateObs, false);
                    }
                }

                // Move to next generation
                int numCallsNextGen = population.nextGeneration(stateObs, budget - numCalls);

                // Calculate budget
                int budgetThisGen = numCallsReeval + numCallsNextGen;
                budgetOneGen = budgetOneGen + (budgetThisGen - budgetOneGen)/population.numGenerations;
                numCalls += budgetOneGen;

                // Keep track of best action and best fitness for analysis
                if (analysis) {
                    EvoAnalyzer.addActionBest(population.getNextAction());
                }

            } while ((numCalls + budgetOneGen) <= budget); // While we can still evaluate one more generation
        }

//        System.out.println(population.numGenerations + " " + numCalls);

        if (params.DEBUG)
            debugPrints();

        int nextAction = population.getNextAction();

        if (printTree) {
            population.getStatsTree().markChosen(population.getPopulation()[0].getActions());
        }

        return nextAction;
    }

    // Access to action mapping
    Types.ACTIONS getActionMapping(int action) {return action_mapping.get(action);}
    HashMap<Integer, Types.ACTIONS> getActionMapping() {return action_mapping;}
    int getReversedActionMapping(Types.ACTIONS action) {return action_mapping_r.get(action);}
    HashMap<Types.ACTIONS, Integer> getReversedActionMapping() {return action_mapping_r;}

    /**
     * Helper method to advance state, used by both the simple and macro agent
     * @param state - starting StateObservation
     * @param idx - index of action to execute
     * @param act - action to advance the state with
     * @param agent - agent calling this method
     */
     boolean advanceState(StateObservation state, int idx, Types.ACTIONS act, Player agent) {
        boolean ok = !state.isGameOver();
        Vector2d prePos = state.getAvatarPosition();
        if(ok)
        {
            if (act != null && !state.getAvailableActions().contains(act)) act = Types.ACTIONS.ACTION_NIL;
            if (act == null) act = Types.ACTIONS.ACTION_NIL;
            state.advance(act);
            Vector2d pos = state.getAvatarPosition();

            if (state.isGameOver()) {
                pos = prePos;
                pos.x += act.getKey()[1];
                pos.y += act.getKey()[0];
            }

            if (agent.viewer != null) {
                double score = Individual.heuristic.evaluateState(state);

                // bounds
                if(score < Individual.bounds[0])
                    Individual.bounds[0] = score;
                if(score > Individual.bounds[1])
                    Individual.bounds[1] = score;

                drawingAgent.updatePosThinking(getPopulation().numGenerations, idx, act, pos, score);
            }
            if (analysis) {
                EvoAnalyzer.addPositionExploredFM(pos);
            }
        }
        return ok;
    }

    public Population getPopulation() {return population;}

    /**
     * Print debugging messages
     */
    private void debugPrints() {
        System.out.println("Debug true. Printing debug messages after evolution: ");
        System.out.println("Number of generations: " + population.numGenerations);
        System.out.println("Pop: ");
        for (Individual i : population.getPopulation()) {
            System.out.println(i);
        }
        System.out.println();
    }
}
