package controllers.singlePlayer.sampleOLMCTSMacro;

import controllers.singlePlayer.RHv2.utils.ParameterSet;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.EvoAnalyzer;

import java.util.Random;

import static tools.EvoAnalyzer.analysis;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer
{
    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;

    public int num_actions;
    public Types.ACTIONS[] actions;
    public int ma_length;
    public int m_rollDepth;

    public SingleMCTSPlayer(Random a_rnd, int num_actions, Types.ACTIONS[] actions, int ma_length, int nrollDepth)
    {
        this.num_actions = num_actions;
        this.actions = actions;
        this.ma_length = ma_length;
        this.m_rollDepth = nrollDepth;
        m_rnd = a_rnd;
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     */
    public void init(StateObservation a_gameState, boolean a_throwTree, ParameterSet params)
    {
        m_rollDepth = params.SIMULATION_DEPTH;
        ma_length = params.MACRO_ACTION_LENGTH;

        //Set the game observation to a newly root node.
        if(a_throwTree) {
            m_root = new SingleTreeNode(m_rnd, num_actions, actions, ma_length, m_rollDepth);
        }

        m_root.rootState = a_gameState;
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public int run(ElapsedCpuTimer elapsedTimer, ParameterSet params)
    {
        //This is possible (the end of the current macro-action reaches game end).
        if(m_root.rootState.isGameOver())
            return 0; //Nothing to do.

        //Do the search within the available time.
//        m_root.mctsSearch(elapsedTimer);
        m_root.mctsSearchCalls(params.MAX_FM_CALLS, params.POPULATION_SIZE);

        if (analysis) {
            for (SingleTreeNode child : m_root.children) {
                for (int i = 0; i < child.nVisits; i++) {
                    //These are actions explored
                    EvoAnalyzer.addActionExplored(actions[child.childIdx]);
                }
            }
        }

        int action = m_root.mostVisitedAction();
        //int action = m_root.bestAction();

        return action;
    }

}
