package VERTIGO.players.RHv2.utils;

import core.game.StateObservation;
import ontology.Types;
import tools.Pair;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static VERTIGO.players.RHv2.Agent.*;
import static VERTIGO.players.RHv2.utils.Constants.*;
import static tools.Utils.normalise;

/**
 * Created by rdgain on 6/28/2017.
 */
public class DrawingAgent {

    // Drawing
    protected int block_size, itype;
    public ArrayList<Vector2d> positions, positionsThinking;
    public ArrayList<Double> scoresThinking;
    public HashMap<Integer,ArrayList<Pair<Integer,Types.ACTIONS>>> actionsThinking;
    ArrayList<Vector2d> newpos;

    public int alphaThink = 25;
    public int heatmapAlpha = 10;

    public DrawingAgent(StateObservation stateObs) {
        block_size = stateObs.getBlockSize();
        itype = stateObs.getAvatarType();
        positions = new ArrayList<>();
        positionsThinking = new ArrayList<>();
        actionsThinking = new HashMap<>();
        scoresThinking = new ArrayList<>();
        newpos = new ArrayList<>();
    }

    public void init(StateObservation stateObs) {
        itype = stateObs.getAvatarType();
        positionsThinking = new ArrayList<>();
        actionsThinking = new HashMap<>();
        scoresThinking = new ArrayList<>();
    }

    public void updatePos(Vector2d position) {
        positions.add(position);
    }

    /**
     * @param gen - index of generation
     * @param idx - index of action in individual
     * @param action - action taken
     * @param position - position avatar ends up in
     * @param score - score for taking this action
     */
    public void updatePosThinking(int gen, int idx, Types.ACTIONS action, Vector2d position, double score) {
        positionsThinking.add(position);
        scoresThinking.add(score);
        if (!actionsThinking.containsKey(gen)) {
            actionsThinking.put(gen, new ArrayList<>());
        }
        actionsThinking.get(gen).add(new Pair<>(idx, action));
    }

    public void draw(Graphics2D g, int drawCode) {


        /*
         * Draw exploration
         */

        if (drawCode == DRAW_EXPLORATION || drawCode == DRAW_ET) {
            g.setColor(new Color(explorationColor.getRed(), explorationColor.getGreen(), explorationColor.getBlue(), heatmapAlpha));
            newpos.clear();
            newpos.addAll(positions);
            if (!newpos.isEmpty()) {
                for (Vector2d p : newpos) {
                    g.fillRect((int) p.x, (int) p.y, block_size, block_size);
                }
            }
        }

        /*
         * Draw thinking
         */

        if (drawCode == DRAW_THINKING || drawCode == DRAW_ET) {

            newpos.clear();
            newpos.addAll(positionsThinking);
            ArrayList<Double> newScores = new ArrayList<>(scoresThinking);

            if (!newpos.isEmpty() && !newScores.isEmpty()) {

                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                for (double s: newScores) {
                    if (s > max) max = s;
                    if (s < min) min = s;
                }

                for (int i = 0; i < newpos.size(); i++) {
                    Vector2d pos = newpos.get(i);
                    if (pos == null) continue;

                    double ratio = 0;
                    if (min < max) ratio = normalise(newScores.get(i), min, max);

                    int re = (int)( ratio * goodAction.getRed() + (1-ratio) * badAction.getRed() );
                    int gr = (int)( ratio * goodAction.getGreen() + (1-ratio) * badAction.getGreen() );
                    int bl = (int)( ratio * goodAction.getBlue() + (1-ratio) * badAction.getBlue() );
                    g.setColor(new Color(re, gr, bl, alphaThink));
                    double size = ratio * block_size/2;
                    g.fillOval((int) (pos.x + block_size / 2 - size/2),
                            (int) (pos.y + block_size / 2 - size/2),
                            (int) size,
                            (int) size);
                }
            }
        }
    }
}
