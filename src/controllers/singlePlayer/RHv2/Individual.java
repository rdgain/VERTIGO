package controllers.singlePlayer.RHv2;

import controllers.singlePlayer.RHv2.bandits.BanditArray;
import controllers.singlePlayer.RHv2.bandits.BanditGene;
import controllers.singlePlayer.RHv2.utils.ParameterSet;
import core.game.StateObservation;
import core.player.Player;
import ontology.Types;
import tools.EvoAnalyzer;
import tools.StatSummary;
import tools.Utils;
import tools.Vector2d;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static controllers.singlePlayer.RHv2.Population.getGridCell;
import static controllers.singlePlayer.RHv2.utils.Constants.*;
import static tools.EvoAnalyzer.analysis;

//TODO: Bandits working with inner macro-actions?

/**
 * Created by Raluca on 27-Jun-17.
 */
public class Individual implements Comparable {

    private Gene[] genes; // actions in individual. length of individual = actions.length
    private int nActions; // number of legal actions
    private StatSummary value;
    private double diversityScore;
    private boolean canMut;
    private Random randomGenerator;
    private StateHeuristic heuristic;
    private RollingHorizonPlayer player;
    private Player agent;

    int nCalls;

    static protected double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

    Individual(int nActions, Random gen, StateHeuristic heuristic, RollingHorizonPlayer player, Player agent) {
        this.heuristic = heuristic;
        this.randomGenerator = gen;
        this.nActions = nActions;
        this.player = player;
        this.agent = agent;
        this.value = new StatSummary();
        this.diversityScore = 0;

        genes = new Gene[agent.getParameters().SIMULATION_DEPTH];
        canMut = true;
        for (int i = 0; i < genes.length; i++) {
            if (nActions <= 1)  {nActions = 1; canMut = false;}
            genes[i] = new Gene(randomGenerator, player.params.INNER_MACRO_ACTION_LENGTH);
        }
    }

    /**
     * Evaluates an individual by rolling the current state with the actions in the individual
     * and returning the value of the resulting state; random action chosen for the opponent
     * @param state - current state, root of rollouts
     * @return - number of FM calls used during this call
     */
    int evaluate(StateObservation state, ParameterSet params, TreeNode statsTree, BanditArray bandits, int[] actionDist) {
        nCalls = 0;
        StateObservation end = rollout(state, actionDist, params);

        double reward;
        if (params.ROLLOUTS) {
            reward = MCrollouts(end,params,state);
        } else {
            reward = heuristic.evaluateState(end);

            // This is fitness landscape
            if (analysis) {
                int thisAct = getActions()[0];
                if (reward == HUGE_POSITIVE) {
                    EvoAnalyzer.countWin(state.getGameTick(),thisAct);
                    EvoAnalyzer.addFitness(end.getGameScore(),thisAct);
                } else if (reward == HUGE_NEGATIVE) {
                    EvoAnalyzer.countLoss(state.getGameTick(),thisAct);
                    EvoAnalyzer.addFitness(end.getGameScore(),thisAct);
                } else {
                    EvoAnalyzer.addFitness(reward,thisAct);
                }
            }
        }

        // Apply discount factor
//        reward *= Math.pow(params.DISCOUNT,params.SIMULATION_DEPTH*params.INNER_MACRO_ACTION_LENGTH);

        // bounds
        if(reward < bounds[0])
            bounds[0] = reward;
        if(reward > bounds[1])
            bounds[1] = reward;

        double delta = updateReward(reward);

        // Update bandits; No bandits for inner macro-actions
        if (params.BANDIT_MUTATION) {
            for (BanditGene bg: bandits.genome) {
                bg.applyReward(delta);
                if (bg.revertOrKeep(delta)) {
                    genes[bg.index].setAction(bg.x, null, 0);
                }
            }
        }

        // Update tree
        int[] actions = new int[params.SIMULATION_DEPTH*params.INNER_MACRO_ACTION_LENGTH];
        for (int k = 0; k < params.SIMULATION_DEPTH; k++) {
            System.arraycopy(genes[k].getMacroAction(), 0, actions, k * params.INNER_MACRO_ACTION_LENGTH, params.INNER_MACRO_ACTION_LENGTH);
        }
        if (params.TREE)
            statsTree.rollout(actions, getValue());

        return nCalls;
    }

    // Returns delta (diff between previous value and new one)
    double updateReward(double reward) {
        // Update individual value
        this.value.add(reward);
        double avgValue = getValue();

        return reward - avgValue;
    }

    double updateDiversityScore(double score) {
        // Update individual value
        diversityScore = score;
        return score;
    }

    private StateObservation rollout(StateObservation start, int[] actionDist, ParameterSet params) {
        // State roll through individual action sequence
        StateObservation st = start.copy();

        Vector2d lastPos = null; //keep track of last pos; if game over, state would not return a pos anymore, but we need it in vector

        for (int i = 0; i < params.SIMULATION_DEPTH; i++) {
            for (int m = 0; m < params.INNER_MACRO_ACTION_LENGTH; m++) {
                if (!st.isGameOver()) {
                    int action = genes[i].getMacroAction()[m];
                    actionDist[action]++; //add action count
                    lastPos = st.getAvatarPosition();
                    player.advanceState(st, player.getActionMapping(action), agent);
                    nCalls += params.MACRO_ACTION_LENGTH;
                }
//               else {
//                    break;
//               }

                genes[i].setPosition(m, lastPos); // Set current position in the gene
            }
        }

        return st;
    }

    // Monte Carlo rollouts
    private double MCrollouts(StateObservation start, ParameterSet params, StateObservation current) {
        double reward = 0;
        boolean ok = true;
        for (int k = 0; k < params.REPEAT_ROLLOUT; k++) {
            StateObservation first = start.copy();
            for (int j = 0; j < params.ROLLOUT_LENGTH; j++) {
                if (ok) {
                    ArrayList<Types.ACTIONS> acts = first.getAvailableActions();
                    int bound = acts.size();
                    Types.ACTIONS action = null;
                    if (bound > 0) {
                        action = acts.get(randomGenerator.nextInt(bound));
                    }
                    ok = player.advanceState(first, action, agent);
                    nCalls += params.MACRO_ACTION_LENGTH;
                } else {
                    break;
                }
            }
            double thisReward = heuristic.evaluateState(first);

            // This is fitness landscape
            if (analysis) {
                int thisAct = getActions()[0];
                if (thisReward == HUGE_POSITIVE) {
                    EvoAnalyzer.countWin(current.getGameTick(),thisAct);
                    EvoAnalyzer.addFitness(first.getGameScore(),thisAct);
                } else if (thisReward == HUGE_NEGATIVE) {
                    EvoAnalyzer.countLoss(current.getGameTick(),thisAct);
                    EvoAnalyzer.addFitness(first.getGameScore(),thisAct);
                } else {
                    EvoAnalyzer.addFitness(thisReward,thisAct);
                }
            }

            reward += thisReward;
        }
        reward /= params.REPEAT_ROLLOUT;

        return reward;
    }

    double diversityDiff(Population population) {
        double diff = 0;

        int[] actionSequence = getActions();
        double maxCount = player.params.POPULATION_SIZE +
                (population.numGenerations-1) * (player.params.POPULATION_SIZE - player.params.ELITISM);

        HashMap<Integer,Integer>[] actionCountAllGen = population.getActionCountAllGen();
        HashMap<Integer,Integer> posCellCountAllGen = population.getPosCellCountAllGen();

        for (int i = 0; i < actionSequence.length; i++) {
            if (player.params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {

                // Compare actions
                diff += actionCountAllGen[i].get(actionSequence[i]) / (maxCount);

            } else if (player.params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {

                // Compare positions traversed
                int idxGene = i / player.params.INNER_MACRO_ACTION_LENGTH;
                int idxAction = i % player.params.INNER_MACRO_ACTION_LENGTH;
                Vector2d pos = genes[idxGene].positions[idxAction];
                if (posCellCountAllGen.containsKey(getGridCell(pos))) {
                    diff += posCellCountAllGen.get(getGridCell(pos)) / maxCount;
                }

            }
        }

        diff /= (player.params.SIMULATION_DEPTH * player.params.INNER_MACRO_ACTION_LENGTH);

        return 1-diff;
    }

    boolean hasPositions() {
        for (int i = 0; i < genes.length; i++) {
            for (int m = 0; m < genes[i].positions.length; m++) {
                if (genes[i].positions[m] == null)
                    return false;
            }
        }
        return true;
    }

    /**
     * Return the value of this individual from the StatSummary object
     * @return - the mean value by default
     */
    double getValue() {
//        return value.mean();
        return Utils.normalise(value.getLastAdded(), bounds[0], bounds[1]);
    }

    /**
     * Reset the value of this individual
     */
    void resetValue() { value.reset(); }


    /**
     * Return the value of this individual from the StatSummary object
     * @return - the mean value by default
     */
    double getDiversityScore() {
        return diversityScore;
    }

    void setGene(int idx, int singleAction, int idxMacro) {
        genes[idx].setAction(singleAction, null, idxMacro);
    }

    void setGene (int idx, Gene g) {
        genes[idx].setGene(g);
    }

    void setGene (int idx) {
        genes[idx].randomActions(player.params.INNER_MACRO_ACTION_LENGTH);
    }

    public Gene getGene(int idx) {
        return genes[idx];
    }

    public int[] getActions() {
        int[] actionSequence = new int[player.params.SIMULATION_DEPTH * player.params.INNER_MACRO_ACTION_LENGTH];
        int i = 0;
        for (Gene g : genes) {
            for (int a : g.getMacroAction()) {
                actionSequence[i] = a;
                i++;
            }
        }
        return actionSequence;
    }

    /**
     * Mutate this individual (no new individual is created)
     * Select which gene to mutate and let the gene decide what to mutate to.
     * Use default mutation (one random gene mutated uniformly at random)
     */
    void mutate(Population population) {
        int no_mutations = player.params.MUTATION;
        if (canMut) {
            int count = 0;
            while (count < no_mutations) {

                int idxGene; // index of gene to mutate
                int idxActionToMutate = -1; //index of action to mutate

                if (player.params.MUT_BIAS) {
                    // bias mutations towards the beginning of the array of individuals, softmax
                    int L = genes.length;
                    double[] p = new double[L];
                    double sum = 0, psum = 0;
                    for (int i = 0; i < L; i++) {
                        sum += Math.pow(Math.E, -(i + 1));
                    }
                    double prob = Math.random();
                    idxGene = 0;
                    for (int i = 0; i < L; i++) {
                        p[i] = Math.pow(Math.E, -(i + 1)) / sum;
                        psum += p[i];
                        if (psum > prob) {
                            idxGene = i;
                            break;
                        }
                    }
                } else if (player.params.MUT_DIVERSITY && player.params.DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
                    // find action most similar to the others
                    int actionIdx = 0;
                    int max = 0;
                    HashMap<Integer, Integer>[] actionCountAllGen = population.getActionCountAllGen();
                    int[] actionSequence = getActions();
                    for (int i = 0; i < actionSequence.length; i++) {
                        int actionCount = actionCountAllGen[i].get(actionSequence[i]);
                        if (actionCount > max) {
                            max = actionCount;
                            actionIdx = i;
                        }
                    }

                    // find gene with actionIdx
                    idxGene = actionIdx / player.params.INNER_MACRO_ACTION_LENGTH;
                    idxActionToMutate = actionIdx % player.params.INNER_MACRO_ACTION_LENGTH;

                } else if (player.params.MUT_DIVERSITY && player.params.DIVERSITY_TYPE == DIVERSITY_PHENOTYPE) {
                    // find pos most similar to the others
                    int actionIdx = 0;
                    int max = 0;
                    HashMap<Integer, Integer> posCellCountAllGen = population.getPosCellCountAllGen();
                    int[] actionSequence = getActions();
                    for (int i = 0; i < actionSequence.length; i++) {
                        int idxG = i / player.params.INNER_MACRO_ACTION_LENGTH;
                        int idxAction = i % player.params.INNER_MACRO_ACTION_LENGTH;
                        Vector2d pos = genes[idxG].positions[idxAction];
                        int cellNo = getGridCell(pos);
                        int posCount = posCellCountAllGen.get(cellNo);
                        if (posCount > max) {
                            max = posCount;
                            actionIdx = i;
                        }
                    }

                    // find gene with actionIdx
                    idxGene = actionIdx / player.params.INNER_MACRO_ACTION_LENGTH;
                    idxActionToMutate = actionIdx % player.params.INNER_MACRO_ACTION_LENGTH;

                } else {
                    // random mutation of one gene
                    idxGene = randomGenerator.nextInt(genes.length);
                }

                genes[idxGene].mutate(population, player.params.MUT_DIVERSITY, player.params.DIVERSITY_TYPE, idxGene, idxActionToMutate); // gene decides what the new value is
                count++;
            }
        }
    }

    /**
     * Mutate this individual (no new individual is created)
     * Use bandit mutation
     */
    void banditMutate(BanditArray bandits) {
        if (canMut) {
            BanditGene g = bandits.selectGeneToMutate();
            g.banditMutate();
            genes[g.index].setAction(g.x, null, 0);
        }
    }

    /**
     * Sets the actions of this individual to a new array of actions.
     * @param a - new array of actions.
     */
    private void setGenes(Gene[] a) {
        for (int i = 0; i < a.length; i++) {
            genes[i] = a[i].copy();
        }

    }

    @Override
    public int compareTo(Object o) {
        Individual a = this;
        Individual b = (Individual)o;

//        double valueA = a.getValue();
//        double valueB = b.getValue();
        double valueA = Utils.normalise(a.getValue(), bounds[0], bounds[1]);
        double valueB = Utils.normalise(b.getValue(), bounds[0], bounds[1]);
        double diversityA = a.getDiversityScore();
        double diversityB = b.getDiversityScore();

        double fitnessA, fitnessB;

        fitnessA = valueA * (1-a.player.params.D) + diversityA * a.player.params.D;
        fitnessB = valueB * (1-b.player.params.D) + diversityB * b.player.params.D;

        if (fitnessA < fitnessB) return 1;
        else if (fitnessA > fitnessB) return -1;
        else return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Individual)) return false;

        Individual a = this;
        Individual b = (Individual)o;

        for (int i = 0; i < genes.length; i++) {
            if (a.genes[i] != b.genes[i]) return false;
        }
        return true;
    }

    public Individual copy () {
        Individual a = new Individual(this.nActions, this.randomGenerator, this.heuristic, this.player, this.agent);
        a.value = this.value;
        a.diversityScore = this.diversityScore;
        a.setGenes(this.genes);
        a.canMut = this.canMut;

        return a;
    }

    @Override
    public String toString() {
        double value = getValue();
        String s = "Value = " + String.format("%.2f", value) + ": NormValue = " + String.format("%.2f", Utils.normalise(value, bounds[0], bounds[1])) + ": DiversityScore = " +
                String.format("%.2f", diversityScore) + ": Actions = ";


        for (Gene action : genes) s += action + " ";
        return s;
    }

}
