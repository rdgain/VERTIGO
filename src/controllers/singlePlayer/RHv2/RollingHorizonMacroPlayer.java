package controllers.singlePlayer.RHv2;

import controllers.singlePlayer.RHv2.utils.ParameterSet;
import core.game.StateObservation;
import core.player.Player;

import java.util.Random;

/**
 * Created by rdgain on 6/28/2017.
 */
class RollingHorizonMacroPlayer extends RollingHorizonPlayer {

    // Macro actions
    private int m_actionsLeft;
    private int m_lastMacroAction;
    private boolean m_throwPop;

    RollingHorizonMacroPlayer(StateObservation stateobs, Random random) {
        super(stateobs, random);

        // Set up for macro actions
        m_actionsLeft = 0;
        m_lastMacroAction = -1;
        m_throwPop = true;
    }

    /**
     * Run the algorithm with macro action (expanding evolution over several game ticks, during which we execute
     * the same action several times)
     * @param stateObs - StateObservation of current game tick
     * @return - next action to play as integer
     */
    int run(StateObservation stateObs, Player agent) {
        params = agent.getParameters();
        int budget = params.MAX_FM_CALLS;

        int nextAction;
        if (stateObs.getGameTick() == 0) {
            if (stateObs.getAvailableActions().size() + 1 != nActions || m_throwPop)
                init(stateObs,agent);

            //Game just started, determine a macro-action.
            int best = evolve(stateObs, agent, budget);

            m_lastMacroAction = best;
            m_throwPop = true;
            nextAction = best;
            m_actionsLeft = params.MACRO_ACTION_LENGTH-1;

        } else {

            if(m_actionsLeft > 0) { //In the middle of the macro action.

                if (stateObs.getAvailableActions().size() + 1 != nActions || m_throwPop)
                    init(stateObs,agent);
                prepareGameCopy(stateObs);

                evolve(stateObs, agent, budget);

                nextAction = m_lastMacroAction;
                m_actionsLeft--;
                m_throwPop = false;

            } else if(m_actionsLeft == 0) { //Finishing a macro-action
                prepareGameCopy(stateObs);

                int best = evolve(stateObs, agent, budget);
                nextAction = m_lastMacroAction;
                m_lastMacroAction = best;
                m_actionsLeft = params.MACRO_ACTION_LENGTH-1;
                m_throwPop = true;

            } else{
                throw new RuntimeException("This should not be happening: " + m_actionsLeft);
            }
        }
        return nextAction;
    }

    /**
     * Prepare the game copy for the macro action code
     * @param stateObs - current StateObservation
     */
    private void prepareGameCopy(StateObservation stateObs)
    {
        if(m_lastMacroAction != -1)
        {
            int first = params.MACRO_ACTION_LENGTH - m_actionsLeft - 1;
            for(int i = first; i < params.MACRO_ACTION_LENGTH; ++i)
            {
                if (!stateObs.isGameOver()) {
                    stateObs.advance(getActionMapping(m_lastMacroAction));
                    numCalls++;
                } else break;
            }
        }
    }
}
