package controllers.singlePlayer.RHv2.utils;

import core.game.Observation;
import core.game.StateObservation;
import tools.Vector2d;

import java.awt.*;
import java.util.ArrayList;

import static controllers.singlePlayer.RHv2.utils.Constants.*;

/**
 * Created by rdgain on 6/28/2017.
 */
public class DrawingAgent {

    // Drawing
    protected ArrayList<Observation> grid[][];
    protected int block_size, itype;
    public ArrayList<Vector2d> positions, positionsThinking;
    ArrayList<Vector2d> newpos;

    public DrawingAgent(StateObservation stateObs) {
        grid = stateObs.getObservationGrid();
        block_size = stateObs.getBlockSize();
        itype = stateObs.getAvatarType();
        positions = new ArrayList<>();
        positionsThinking = new ArrayList<>();
        newpos = new ArrayList<>();
    }

    public void init(StateObservation stateObs) {
        grid = stateObs.getObservationGrid();
        itype = stateObs.getAvatarType();
        positionsThinking = new ArrayList<>();
    }

    public void updatePos(Vector2d position) {
        positions.add(position);
    }


    public void updatePosThinking(Vector2d position) {
        positionsThinking.add(position);
    }

    public void draw(Graphics2D g, int drawCode) {


        /**
         * Draw exploration
         */

        if (drawCode == DRAW_EXPLORATION || drawCode == DRAW_ET) {

            g.setColor(new Color(0, 0, 0, 10));

            newpos.clear();
            newpos.addAll(positions);
            if (!newpos.isEmpty()) {
                for (Vector2d p : newpos) {
                    g.fillRect((int) p.x, (int) p.y, block_size, block_size);
                }
            }
        }

        /**
         * Draw thinking
         */

        if (drawCode == DRAW_THINKING || drawCode == DRAW_ET) {

            g.setColor(new Color(255, 255, 255, 25));
            newpos.clear();
            newpos.addAll(positionsThinking);
            if (!newpos.isEmpty()) {
                for (Vector2d pos : newpos) {
                    g.fillOval((int) pos.x + block_size / 2, (int) pos.y + block_size / 2, block_size / 2, block_size / 2);
                }
            }
        }
    }
}
