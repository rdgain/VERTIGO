package VERTIGO.players;

import javax.swing.*;

public interface ParameterSet {
    String[] getParams();
    String[] getParamDescriptions();
    String[] getDefaultValues();
    Object[][] getValueOptions();
    ParameterSet getParamSet(JComboBox[] paramInputs);
}
