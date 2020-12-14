package VERTIGO.players.sampleOLMCTSMacro;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.EvoAnalyzer;

import java.util.ArrayList;
import java.util.Random;

import static tools.EvoAnalyzer.analysis;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {

    public int num_actions;
    public Types.ACTIONS[] actions;

    private int m_actionsLeft;
    private int m_lastMacroAction;
    private boolean m_throwTree;

    protected SingleMCTSPlayer mctsPlayer;
    MCTSParams parameters;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        parameters = (MCTSParams) params;

        m_actionsLeft = 0;
        m_lastMacroAction = -1;
        m_throwTree = true;

        //Get the actions in a static array.
        ArrayList<Types.ACTIONS> act = so.getAvailableActions(true);
        actions = new Types.ACTIONS[act.size()];
        for(int i = 0; i < actions.length; ++i)
        {
            actions[i] = act.get(i);
        }
        num_actions = actions.length;

        //Create the player.

        mctsPlayer = getPlayer(so, elapsedTimer);
    }

    public SingleMCTSPlayer getPlayer(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        return new SingleMCTSPlayer(new Random(), num_actions, actions, parameters.MACRO_ACTION_LENGTH, parameters.ROLLOUT_DEPTH);
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        if (analysis) {
            EvoAnalyzer.initGameTick();

            //These are the positions explored in the game
            EvoAnalyzer.addPositionExploredGame(stateObs.getAvatarPosition());
        }

        //Set the state observation object as the new root of the tree.
        mctsPlayer.init(stateObs, true, parameters);

        //Determine the action using MCTS...
        int action = mctsPlayer.run(elapsedTimer, parameters);
        Types.ACTIONS actionToPlay = actions[action];

        // Write stats about evolution during this game tick to file
        //Analysis at end of game tick
        if (analysis) {
            EvoAnalyzer.endGameTick(mctsPlayer.m_root.children[mctsPlayer.m_root.bestAction()].uctValue(), action);
            EvoAnalyzer.writeEvo(evoWriter, actionToPlay, SHOULD_LOG);
            logScore(stateObs.getGameScore());
        }

        //... and return it.
        return actionToPlay;


//        MACRO_ACTION_LENGTH = params.MACRO_ACTION_LENGTH;
//
//        int nextAction;
//
//        int gameTick = stateObs.getGameTick();
//        //a_timeDue -=30;
//        if(gameTick == 0)
//        {
//            mctsPlayer.init(stateObs, m_throwTree, params);
//
//            //Game just started, determine a macro-action.
//            int action = mctsPlayer.run(elapsedTimer, params);
//
//            m_lastMacroAction = action;
//            m_throwTree = true;
//            nextAction = action;
//            m_actionsLeft = MACRO_ACTION_LENGTH-1;
//
//        }else{
//
//            prepareGameCopy(stateObs);
//
//            if(m_actionsLeft > 0) //In the middle of the macro action.
//            {
//                mctsPlayer.init(stateObs, m_throwTree, params);
//
//                mctsPlayer.run(elapsedTimer, params);
//                nextAction = m_lastMacroAction;
//                m_actionsLeft--;
//                m_throwTree = false;
//
//
//            }else if(m_actionsLeft == 0)        //Finishing a macro-action
//            {
//
//                int action = mctsPlayer.run(elapsedTimer, params);
//                nextAction = m_lastMacroAction;
//                m_lastMacroAction = action;
//                m_actionsLeft = MACRO_ACTION_LENGTH-1;
//                m_throwTree = true;
//
//            }else{
//                throw new RuntimeException("This should not be happening: " + m_actionsLeft);
//            }
//        }
//
//        return actions[nextAction];
    }


    public void prepareGameCopy(StateObservation stateObs)
    {
        if(m_lastMacroAction != -1)
        {
            int first = parameters.MACRO_ACTION_LENGTH - m_actionsLeft - 1;
            for(int i = first; i < parameters.MACRO_ACTION_LENGTH; ++i)
            {
                stateObs.advance(actions[m_lastMacroAction]);
            }
        }
    }

    //Evo, [AVG CONVERGENCE], [STAT SUMMARY FINAL BEST FITNESS], [PERC LEVEL EXPLORATION] [PERC LEVEL EXPLORATION FM]
    public void result(StateObservation stateObs, ElapsedCpuTimer elapsedCpuTimer)
    {
        if (analysis) {
            EvoAnalyzer.printFinalEvo(stateObs);
        }
    }

}
