package VERTIGO.players.sampleOLMCTSMacro;

import VERTIGO.players.ParameterSet;

import javax.swing.*;

public class MCTSParams implements ParameterSet {

    public int ROLLOUT_DEPTH = 10; //NUMBER OF MACRO-ACTIONS
    public int MACRO_ACTION_LENGTH = 1; //LENGTH OF EACH MACRO-ACTION
    public int WINDOW_SIZE = 5; //try 1,2,5
    public int MAX_FM_CALLS = 900;

    @Override
    public String[] getParams() {
        return new String[0];
    }

    @Override
    public String[] getParamDescriptions() {
        return new String[0];
    }

    @Override
    public String[] getDefaultValues() {
        return new String[0];
    }

    @Override
    public Object[][] getValueOptions() {
        return new Object[0][];
    }

    @Override
    public ParameterSet getParamSet(JComboBox[] paramInputs) {
        return null;
    }
}
