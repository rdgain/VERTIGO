package VERTIGO.players.RHv2;

import ontology.Types;
import tools.Vector2d;

import java.util.*;

import static VERTIGO.players.RHv2.utils.Constants.DIVERSITY_GENOTYPE;
import static VERTIGO.players.RHv2.utils.Constants.HUGE_POSITIVE;

/**
 * Created by rdgain on 6/29/2017.
 */
public class Gene {

    private int[] macroAction;
    private Random gen;

    Vector2d[] positions;

    private static ArrayList<Integer>[][][] validMutation;
    static int nActions;
    static HashMap<Integer, Types.ACTIONS> actionMapping;

    Gene(Random randomGenerator, int macroLength) {
        gen = randomGenerator;
        macroAction = new int[macroLength];
        positions = new Vector2d[macroLength];

        // Random action initialization
        randomActions(macroLength);
    }

    /**
     * Initialize to valid random actions
     * @param macroLength - length of one macro-action
     */
    void randomActions(int macroLength) {
        for (int j = 0; j < macroLength; j++) {
            macroAction[j] = -1;
            setNewMacroValidRandomValue(j);
        }
    }

    static void initValidMutation(HashMap<Integer, Types.ACTIONS> actionMap) {
        nActions = actionMap.size();
        actionMapping = actionMap;
        validMutation = new ArrayList[nActions][][];
        for (int k = 0; k < nActions; k++) {
            validMutation[k] = new ArrayList[nActions][];
            for (int i = 0; i < nActions; i++) {
                validMutation[k][i] = new ArrayList[nActions];
                for (int j = 0; j < nActions; j++) {
                    ArrayList validkij = new ArrayList();
                    for (int a = 0; a < nActions; a++)
                        if (a != k && valid(a,i,actionMap) && valid(a,j,actionMap))
                            validkij.add(a);
                    validMutation[k][i][j] = validkij;
                }
            }
        }
    }

    // Is this action pair valid? Returns false if LR or UD
    private static boolean valid(int action, int neighbour, HashMap<Integer, Types.ACTIONS> actionMap) {
        Types.ACTIONS a1 = actionMap.get(action);
        Types.ACTIONS a2 = actionMap.get(neighbour);

        return !(a1 == Types.ACTIONS.ACTION_DOWN && a2 == Types.ACTIONS.ACTION_UP || a1 == Types.ACTIONS.ACTION_UP && a2 == Types.ACTIONS.ACTION_DOWN)
                && !(a1 == Types.ACTIONS.ACTION_LEFT && a2 == Types.ACTIONS.ACTION_RIGHT || a1 == Types.ACTIONS.ACTION_RIGHT && a2 == Types.ACTIONS.ACTION_LEFT);
    }

    void setAction(int singleAction, int[] macroGene, int macroIdx) {
        if (macroGene != null)
            setMacroAction(macroGene);
        else if (macroIdx >= 0 && macroIdx < macroAction.length)
            macroAction[macroIdx] = singleAction;
        else {
            System.out.println("Gene.setAction() call. IndexOutOfBounds for setting individual action in macro-action gene.");
        }
    }

    void setPosition(int m, Vector2d pos) {
        positions[m] = pos.copy();
    }

    void setGene(Gene gene) {
        macroAction = gene.macroAction.clone();
        positions = gene.positions.clone();
        gen = gene.gen;
    }

    /**
     * Returns action of this gene. If a macro action, returns first action of the macro action.
     * @return
     */
    public int getFirstAction() {
        return macroAction[0];
    }

    int[] getMacroAction() {
        return macroAction;
    }

    /**
     * Uniformly random mutation by default
     * @param idxGene - the index this gene has in the individual
     * @param MUT_DIVERSITY - whether we use diversity mutation operator or not
     * @param population - the population where this gene comes from
     */
    public void mutate(Population population, boolean MUT_DIVERSITY, int DIVERSITY_TYPE, int idxGene, int idxActionToMutate) {
        mutateMacroAction(population, MUT_DIVERSITY, DIVERSITY_TYPE, idxGene, idxActionToMutate);
    }

    private void setMacroAction(int[] a) {
        macroAction = a.clone();
    }

    /**
     * Uniformly random mutation of macro action
     * Avoid LR and UD action blocks
     * @param idxGene - the index this gene has in the individual
     * @param MUT_DIVERSITY - whether we use diversity mutation operator or not
     * @param pop - the population where this gene comes from
     */
    private void mutateMacroAction(Population pop, boolean MUT_DIVERSITY, int DIVERSITY_TYPE, int idxGene, int idxActionToMutate) {
        if (MUT_DIVERSITY && DIVERSITY_TYPE == DIVERSITY_GENOTYPE) {
            //get the valid values this action can take
            HashSet<Integer> validValues = getValidActionSpace(idxActionToMutate);
            int newValue = 0;

            //pick new value that appears the least in the population
            HashMap<Integer, Integer>[] popStats = pop.getActionCountAllGen();
            int minCount = (int) HUGE_POSITIVE;
            for (int i : validValues) {
                int countValue = popStats[idxGene].get(i);
                if (countValue < minCount) {
                    minCount = countValue;
                    newValue = i;
                }
            }

            //set action to new value
            macroAction[idxActionToMutate] = newValue;
        } else {
            // Choose one random action in the macro-action to mutate and change it to a new valid random action
            idxActionToMutate = gen.nextInt(macroAction.length);
            setNewMacroValidRandomValue(idxActionToMutate);
        }
    }

    private HashSet<Integer> getValidActionSpace(int idx) {
        // Get list of valid values
        HashSet<Integer> validValues = new HashSet<>();

        int nextIdx = idx + 1;
        int prevIdx = idx - 1;
        if (nextIdx >= macroAction.length)
            nextIdx = macroAction.length - 1;
        if (prevIdx < 0)
            prevIdx = 0;

        if (macroAction[idx] == -1)
            validValues.addAll(actionMapping.keySet()); // All actions okay
        else {
            // Handle edge cases separately
            if (idx == 0)
                for (int i = 0; i < validMutation.length; i++) {
                    validValues.addAll(validMutation[macroAction[idx]][i][macroAction[nextIdx]]);
                }
            else if (idx == macroAction.length - 1)
                for (int i = 0; i < validMutation.length; i++) {
                    validValues.addAll(validMutation[macroAction[idx]][macroAction[prevIdx]][i]);
                }
            else
                validValues.addAll(validMutation[macroAction[idx]][macroAction[prevIdx]][macroAction[nextIdx]]);
        }
        return validValues;
    }

    private void setNewMacroValidRandomValue(int idx) {
        HashSet<Integer> validValues = getValidActionSpace(idx);

        // Get random new value from the list of valid values
        int newValueIdx = gen.nextInt(validValues.size());
        int i = 0;
        for(Integer k : validValues)
        {
            if (i == newValueIdx) {
                // Assign new value
                macroAction[idx] = k;
                break;
            }
            i++;
        }


    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Gene))
            return false;

        for (int i = 0; i < macroAction.length; i++) {
            if (macroAction[i] != ((Gene) o).macroAction[i]) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(macroAction);
    }

    public Gene copy() {
        Gene g = new Gene(this.gen, this.macroAction.length);
        g.macroAction = macroAction.clone();
        g.gen = gen;
        return g;
    }
}
