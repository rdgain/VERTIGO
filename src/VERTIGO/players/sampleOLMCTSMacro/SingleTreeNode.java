package VERTIGO.players.sampleOLMCTSMacro;

import core.game.StateObservation;
import ontology.Types;
import tools.EvoAnalyzer;
import tools.Utils;

import java.util.ArrayList;
import java.util.Random;

import static tools.EvoAnalyzer.analysis;

public class SingleTreeNode
{
    private final double HUGE_NEGATIVE = -10000000.0;
    private final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-6;
    public double egreedyEpsilon = 0.05;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public int childIdx;

    public int ma_length;
    public int num_actions;
    Types.ACTIONS[] actions;
    public int m_rollDepth;
    int idx;

    public double K = Math.sqrt(2);

    public static StateObservation rootState;
    static int count, idxCount = 0;
    static String[] actColors = new String[]{"red", "blue", "black", "green", "orange", "purple"};
    static boolean include_rollout = true;

    public SingleTreeNode(Random rnd, int num_actions, Types.ACTIONS[] actions, int ma_length, int nrollDepth) {
        this(null, -1, rnd, num_actions, actions, ma_length, nrollDepth);
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd,
                          int num_actions, Types.ACTIONS[] actions, int ma_length, int nrollDepth) {
        this.parent = parent;
        this.m_rnd = rnd;
        this.num_actions = num_actions;
        this.actions = actions;
        children = new SingleTreeNode[num_actions];
        totValue = 0.0;
        this.childIdx = childIdx;
        this.ma_length = ma_length;
        this.m_rollDepth = nrollDepth;
        if(parent != null) {
            m_depth = parent.m_depth + 1;
            this.idx = idxCount++;
        }
        else {
            m_depth = 0;
            this.idx = 0;
        }
    }


//    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {
//
//        double avgTimeTaken = 0;
//        double acumTimeTaken = 0;
//        long remaining = elapsedTimer.remainingTimeMillis();
//        int numIters = 0;
//
//        int remainingLimit = 5;
//        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
//        //while(numIters < Agent.MCTS_ITERATIONS){
//
//            StateObservation state = rootState.copy();
//
//            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
//            SingleTreeNode selected = treePolicy(state);
//            double delta = selected.rollOut(state);
//            backUp(selected, delta);
//
//            numIters++;
//            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
//            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
//            avgTimeTaken  = acumTimeTaken/numIters;
//            remaining = elapsedTimer.remainingTimeMillis();
//        }
//        //System.out.println("-- " + numIters + " -- ( " + avgTimeTaken + ")");
//    }


    public void mctsSearchCalls(int numCalls, int windowSize) {

        int lastCount = -1;
        count = 0;
        int numIters = 0;
        idxCount = 1;

        while((count - lastCount) > 0 && (count + m_rollDepth) < numCalls){
            //while(numIters < Agent.MCTS_ITERATIONS){
            lastCount = count;
            StateObservation state = rootState.copy();

            SingleTreeNode selected = treePolicy(state, windowSize);
            double delta = selected.rollOut(state, numCalls);
            if (!include_rollout) {
                backUp(selected, delta);
            }

            numIters++;

            // Keep track of best action and best fitness for analysis
            if (analysis) {
                EvoAnalyzer.addActionBest(mostVisitedAction());
            }
        }
//        System.out.println("-- " + numIters + " -- ");
//        if (rootState.getGameTick() == 100)
//            System.out.println(this.treeToString());
    }

    public void advanceMacro(StateObservation state, int action)
    {
        int i = 0;
        boolean end = false;
        Types.ACTIONS act = actions[action];

        while(!end)
        {
            state.advance(act);
            if (analysis) {
                EvoAnalyzer.addPositionExploredFM(state.getAvatarPosition());
            }
            count++;
            end = (++i >= ma_length) || state.isGameOver();
        }

    }

    public SingleTreeNode treePolicy(StateObservation state, int windowSize) {

        SingleTreeNode cur = this;

        while (!state.isGameOver() && cur.m_depth < m_rollDepth)
        {
            if (analysis && cur.m_depth == 1) {
                EvoAnalyzer.addActionRecommended(actions[cur.childIdx], windowSize);
            }
            if (cur.notFullyExpanded()) {
                return cur.expand(state);

            } else {
                SingleTreeNode next = cur.uct(state);
                cur = next;
            }
        }

        return cur;
    }

    public SingleTreeNode randomTreePolicy(StateObservation state, int thisDepth, int numCalls) {

        SingleTreeNode cur = this;

        while (!finishRollout(state,thisDepth, numCalls))
        {
            int action = m_rnd.nextInt(num_actions);
            if (cur.children[action] != null) {
                cur = cur.children[action];
                state.advance(actions[action]);
                count++;
            } else {
                state.advance(actions[action]);
                count++;
                SingleTreeNode tn = new SingleTreeNode(cur,action,cur.m_rnd,cur.num_actions,cur.actions,cur.ma_length, cur.m_rollDepth);
                cur.children[action] = tn;
                cur = tn;
            }
            thisDepth++;
        }

        return cur;
    }


    public SingleTreeNode expand(StateObservation state) {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //Roll the state
        advanceMacro(state, bestAction);

        SingleTreeNode tn = new SingleTreeNode(this,bestAction,this.m_rnd,num_actions,actions,ma_length, m_rollDepth);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(StateObservation state) {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        for (SingleTreeNode child : this.children)
        {
            double uctValue = child.uctValue();

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:
        advanceMacro(state, selected.childIdx);

        return selected;
    }


    public double rollOut(StateObservation state, int numCalls)
    {
        int thisDepth = this.m_depth;
        SingleTreeNode last = null;

        if (include_rollout) {
            last = randomTreePolicy(state, thisDepth, numCalls);
        } else {
            while (!finishRollout(state, thisDepth, numCalls)) {
                int action = m_rnd.nextInt(num_actions);
                state.advance(actions[action]);
                count++;
                thisDepth++;
            }
        }

        double delta = value(state);

        // This is fitness landscape
        if (analysis) {
            int thisAct = childIdx;
            if (delta == HUGE_POSITIVE) {
                EvoAnalyzer.countWin(rootState.getGameTick(),thisAct);
                EvoAnalyzer.addFitness(state.getGameScore(),thisAct);
            } else if (delta == HUGE_NEGATIVE) {
                EvoAnalyzer.countLoss(rootState.getGameTick(),thisAct);
                EvoAnalyzer.addFitness(state.getGameScore(),thisAct);
            } else {
                EvoAnalyzer.addFitness(delta,thisAct);
            }
        }

        if(delta < bounds[0])
            bounds[0] = delta;
        if(delta > bounds[1])
            bounds[1] = delta;

        //double normDelta = Utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        if (include_rollout) {
            backUp(last, delta);
        }

        return delta;
    }

    public double value(StateObservation a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        Types.WINNER win = a_gameState.getGameWinner();
        double rawScore = a_gameState.getGameScore();

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            return HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            return HUGE_POSITIVE;

        return rawScore;
    }

    public boolean finishRollout(StateObservation rollerState, int depth, int numCalls)
    {
        if (count >= numCalls)
            return true;

        if(depth >= m_rollDepth)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public int mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }

                double childValue = children[i].nVisits;
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return selected;
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }

        return false;
    }

    double uctValue() {
        double hvVal = totValue;
        double childValue =  hvVal / (nVisits + this.epsilon);

        childValue = Utils.normalise(childValue, bounds[0], bounds[1]);
        //System.out.println("norm child value: " + childValue);

        double uctValue = childValue +
                K * Math.sqrt(Math.log(this.nVisits + 1) / (nVisits + this.epsilon));

        uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
        return uctValue;
    }

    @Override
    public String toString() {
        int idxp = parent == null? -1 : parent.idx;
//        return "[i=" + idx + ", q=" + uctValue() + ", n=" + nVisits + ", p=" + idxp + ", d=" + m_depth + "]";
//        return "[i=" + idx + ", q=" + uctValue() + ", n=" + nVisits + ", p=" + idxp + ", d=" + m_depth + "]";
//        return "[" + idx + "," + String.format("%.2f", uctValue()) + "," + nVisits + "," + childIdx + "]";
        return "[" + String.format("%.2f", uctValue()) + "]";
//        return ""+nVisits;
    }

    String treeToString() {
        ArrayList<String> treeLevels = new ArrayList<>();
        ArrayList<SingleTreeNode> treeNodes = traverse (this);

        String tree = "----" + treeNodes.size() + "-----\n";

        for (SingleTreeNode tn : treeNodes) {
            String hex = Integer.toHexString((int)Math.round(tn.nVisits*1.7));
            tree += "A.node_attr['fillcolor']='#000000" + hex + "'" + "\n";
            tree += "A.add_node(\"" + tn.idx + "\", label=\"" + tn.toString() + "\")" + "\n";
        }

        for (SingleTreeNode tn : treeNodes) {
            for (SingleTreeNode child : tn.children) {
                if (child != null) {
                    tree += "A.add_edge(\"" + tn.idx + "\", \"" + child.idx + "\", color=\"" + actColors[child.childIdx] + "\")" + "\n";
                }
            }
        }

//        for (SingleTreeNode node : treeNodes) {
//            if (node.m_depth <= 0) { //root node
//                treeLevels.add(node.toString());
//            } else {
//                if (node.m_depth < treeLevels.size()) {
//                    String news = treeLevels.get(node.m_depth);
//                    news += " " + node.toString();
//                    treeLevels.set(node.m_depth, news);
//                } else {
//                    treeLevels.add(node.toString());
//                }
//            }
//        }
//
//
//        for (String s : treeLevels)
//            tree += treeLevels.indexOf(s) + ": " + s + "\n";
//
        tree += "---------\n";

        return tree;
    }

    private ArrayList<SingleTreeNode> traverse(SingleTreeNode rootNode) {

        ArrayList<SingleTreeNode> nodes = new ArrayList<>();

        SingleTreeNode node = rootNode;

        while (node != null) {

            // do stuff with node
            nodes.add(node);

            // move to next node
            if (node.hasChildren()) {
                node = node.getFirstChild();
            }
            else {    // leaf
                // find the parent level
                while (node != null && !node.equals(rootNode) && node.getNextSibling() == null) {
                    // use child-parent link to get to the parent level
                    node = node.parent;
                }

                if (node != null) {
                    node = node.getNextSibling();
                }
            }
        }

        return nodes;
    }

    private boolean hasChildren() {
        for (SingleTreeNode aChildren : children) {
            if (aChildren != null) return true;
        }
        return false;
    }
    private SingleTreeNode getFirstChild() {
        for (int i = 0; i < children.length; i++) {
            if (children[i] != null) return children[i];
        }
        return null;
    }
    private SingleTreeNode getNextSibling() {
        if (parent != null) {
            int index = -1;
            for (int i = 0; i < parent.children.length; i++) {
                if (parent.children[i] == null) continue;
                if (parent.children[i].equals(this)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                for (int i = index + 1; i < parent.children.length; i++) {
                    if (parent.children[i] != null) return parent.children[i];
                }
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        SingleTreeNode node = (SingleTreeNode)obj;
        return node.idx == this.idx;
    }
}
