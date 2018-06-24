from compute import *

__author__ = 'raluca'

import matplotlib
matplotlib.use('TkAgg')
import matplotlib.pyplot as plt
import pylab
import numpy as np
import os
import sys
import time
#import seaborn as sns

c = ['blue', 'green', 'pink', 'orange']  # colors
actcol = ["#588C7E", "#F2E394", "#F2AE72", "#D96459", "#8C4646", "#6E5160"]
actnames = ['NIL', 'UP', 'LEFT', 'DOWN', 'RIGHT', 'USE']
no_graphs = 4
no_acts = 6

def drawLivePlot(algFile, actFile) :
    plt.style.use('ggplot')

    # plot game tick convergence over time
    axes = []  # axes
    ydata = []  # y data
    xdata = []  # x data
    errdata = []  # error data
    scoreDatap = []  # score event data
    scoreDatam = []  # score event data
    winData = []  # win event data
    loseData = []  # lose event data

    # action plots
    a0 = []
    a1 = []
    a2 = []
    a3 = []
    a4 = []
    a5 = []

    r0 = []
    r1 = []
    r2 = []
    r3 = []
    r4 = []
    r5 = []

    v0 = []
    v1 = []
    v2 = []
    v3 = []
    v4 = []
    v5 = []

    hl = [None] * no_graphs  # plots
    tl = [None] * no_graphs  # plots

    al0 = [None] * no_graphs  # plots
    al1 = [None] * no_graphs  # plots
    al2 = [None] * no_graphs  # plots
    al3 = [None] * no_graphs  # plots
    al4 = [None] * no_graphs  # plots
    al5 = [None] * no_graphs  # plots

    rl0 = [None] * no_graphs  # plots
    rl1 = [None] * no_graphs  # plots
    rl2 = [None] * no_graphs  # plots
    rl3 = [None] * no_graphs  # plots
    rl4 = [None] * no_graphs  # plots
    rl5 = [None] * no_graphs  # plots

    vl0 = [None] * no_graphs  # plots
    vl1 = [None] * no_graphs  # plots
    vl2 = [None] * no_graphs  # plots
    vl3 = [None] * no_graphs  # plots
    vl4 = [None] * no_graphs  # plots
    vl5 = [None] * no_graphs  # plots

    for i in range(no_graphs):
        fignum = i+1
        fig = plt.figure(fignum)  # Create figure
        axes.append(fig.add_axes([0.15, 0.2, 0.7, 0.7]))  # Add subplot (dont worry only one plot appears)
        axes[i].set_autoscale_on(True)  # enable autoscale
        axes[i].autoscale_view(True, True, True)
        ydata.append([])
        xdata.append([])
        errdata.append([])
        scoreDatap.append([])
        scoreDatam.append([])
        winData.append([])
        loseData.append([])

        hl[i], = plt.plot(xdata[i], ydata[i], color=c[i], alpha=0.5)

        a0.append([])
        a1.append([])
        a2.append([])
        a3.append([])
        a4.append([])
        a5.append([])

        r0.append([])
        r1.append([])
        r2.append([])
        r3.append([])
        r4.append([])
        r5.append([])

        v0.append([])
        v1.append([])
        v2.append([])
        v3.append([])
        v4.append([])
        v5.append([])

        # calc the trendline
        tl[i], = plt.plot(xdata[i], [], color='red')

        handleList = []
        al0[i], = plt.plot([], [], color=actcol[0], label=actnames[0])
        rl0[i], = plt.plot([], [], color=actcol[0], alpha=0)
        vl0[i], = plt.plot([], [], color=actcol[0], label=actnames[0])
        handleList.append(al0[i])

        al1[i], = plt.plot(xdata[i], [], color=actcol[1], label=actnames[1])
        rl1[i], = plt.plot([], [], color=actcol[1], alpha=0)
        vl1[i], = plt.plot([], [], color=actcol[1], label=actnames[1])
        handleList.append(al1[i])

        al2[i], = plt.plot(xdata[i], [], color=actcol[2], label=actnames[2])
        rl2[i], = plt.plot([], [], color=actcol[2], alpha=0)
        vl2[i], = plt.plot([], [], color=actcol[2], label=actnames[2])
        handleList.append(al2[i])

        al3[i], = plt.plot(xdata[i], [], color=actcol[3], label=actnames[3])
        rl3[i], = plt.plot([], [], color=actcol[3], alpha=0)
        vl3[i], = plt.plot([], [], color=actcol[3], label=actnames[3])
        handleList.append(al3[i])

        al4[i], = plt.plot(xdata[i], [], color=actcol[4], label=actnames[4])
        rl4[i], = plt.plot([], [], color=actcol[4], alpha=0)
        vl4[i], = plt.plot([], [], color=actcol[4], label=actnames[4])
        handleList.append(al4[i])

        al5[i], = plt.plot(xdata[i], [], color=actcol[5], label=actnames[5])
        rl5[i], = plt.plot([], [], color=actcol[5], alpha=0)
        vl5[i], = plt.plot([], [], color=actcol[5], label=actnames[5])
        handleList.append(al5[i])

        if i == 0 :
            plt.xlabel('game tick')  # Set up axes
            plt.ylabel('convergence')  # Set up axes
            plt.title('Convergence Graph')
        elif i == 1 :
            plt.xlabel('game tick')  # Set up axes
            plt.ylabel('fitness')  # Set up axes
            plt.title('Fitness Graph')
        elif i == 2 :
            plt.xlabel('game tick')  # Set up axes
            plt.ylabel('action %')  # Set up axes
            plt.title('Actions Explored (top)\nActions Recommended (bottom)')
            leg = plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.15), fancybox=True, shadow=True, ncol=6,
                             handles=handleList, fontsize='xx-small')
            for legobj in leg.legendHandles:
                legobj.set_linewidth(10.0)
            plt.axhline(0, color='black')
        elif i == 3 :
            plt.xlabel('game tick')  # Set up axes
            plt.ylabel('fitness')  # Set up axes
            plt.title('Fitness per Action Graph')
            leg = plt.legend(loc='upper center', bbox_to_anchor=(0.5, -0.15), fancybox=True, shadow=True, ncol=6,
                       handles=handleList, fontsize='xx-small')
            for legobj in leg.legendHandles:
                legobj.set_linewidth(3.0)

    tick = 0

    plt.draw()
    plt.pause(0.01)

    while os.stat(algFile).st_size == 0:  # wait to have something in files before getting data
        time.sleep(2)

    while running(no_graphs):
        try:
            actcontents = pylab.loadtxt(actFile, comments='*', delimiter=' ', usecols=1)
        except:
            e = sys.exc_info()[0]
            print("Error reading action file " + str(e))

        try: #update file contents
            filecontents = pylab.loadtxt(algFile, comments='*', delimiter=' ', usecols=range(45))
        except:  # error reading file
            e = sys.exc_info()[0]
            print("Error reading evo file " + str(e))
            sys.exit()

        for i in range(no_graphs):
            if i == 0:
                tick, xdata[i], ydata[i], scoreDatap[i], \
                    scoreDatam[i], winData[i], loseData[i] = convPlot(filecontents, actcontents, tick, xdata[i],
                                                                      ydata[i], hl[i], scoreDatap[i], scoreDatam[i],
                                                                      winData[i], loseData[i], axes[i])
            elif i == 1:
                tick, xdata[i], ydata[i], errdata[i] = fitnessPlot(filecontents, tick, xdata[i], ydata[i], errdata[i],
                                                                   hl[i], tl[i], axes[i])
            elif i == 2:
                tick, xdata[i], a0, a1, a2, a3, a4, a5, \
                    r0, r1, r2, r3, r4, r5 = actExplored(filecontents, tick, axes[i], hl[i], xdata[i], a0, a1, a2, a3,
                                                         a4, a5, r0, r1, r2, r3, r4, r5, al0, al1, al2, al3, al4, al5,
                                                         rl0, rl1, rl2, rl3, rl4, rl5, i)
            elif i == 3:
                tick, xdata[i], v0, v1, v2, v3, v4, v5 = fitPerAct(filecontents, tick, axes[i], xdata[i], v0, v1, v2,
                                                                   v3, v4, v5, vl0, vl1, vl2, vl3, vl4, vl5, i)
            axes[i].relim()  # Recalculate limits
            axes[i].autoscale_view(True, True, True)  # Autoscale

        plt.draw()
        plt.pause(0.03)


def running(no_graphs):
    count = 0
    for i in range(no_graphs):
        if plt.fignum_exists(i):
            count += 1
    if count == 0:
        return False
    return True


def convPlot(filecontents, actcontents, tick, xdata, ydata, hl, scoreDatap, scoreDatam, winData, loseData, axes):
    oldtick = tick
    newdata, tick = get_convergence(filecontents, tick)
    ydata.extend(newdata)
    xdata.extend(range(oldtick, tick + 1))
    hl.set_data(np.array(xdata), np.array(ydata))

    scoreEventsp, scoreEventsm, winEvents, loseEvents, tick = get_events(actcontents, filecontents, oldtick)
    scoreDatap.extend(scoreEventsp)
    scoreDatam.extend(scoreEventsm)
    winData.extend(winEvents)
    loseData.extend(loseEvents)

    yscoreData1 = []
    yscoreData2 = []
    yscoreData3 = []
    yscoreData4 = []
    for _ in range(len(scoreDatap)):
        yscoreData1.append(2)
    for _ in range(len(scoreDatam)):
        yscoreData2.append(3)
    for _ in range(len(winData)):
        yscoreData3.append(4)
    for _ in range(len(loseData)):
        yscoreData4.append(5)

    a1 = axes.scatter(np.array(scoreDatap), yscoreData1, c="#FFFF00", marker=(5, 1), label="Score Events +", zorder=10, s=50,
                     edgecolor='black', linewidth='1', alpha=1)
    a2 = axes.scatter(np.array(scoreDatam), yscoreData2, c="#FFFF00", marker=(5, 1), label="Score Events -", zorder=10, s=50,
                     edgecolor='red', linewidth='1', alpha=1)
    a3 = axes.scatter(np.array(winData), yscoreData3, c="g", marker='D', label="Win Events", zorder=10, s=50,
                     edgecolor='black', linewidth='1', alpha=1)
    a4 = axes.scatter(np.array(loseData), yscoreData4, c="#9932CC", marker='x', label="Lose Events", zorder=10, s=50,
                     edgecolor='black', linewidth='1', alpha=1)
    axes.legend(loc='upper center', bbox_to_anchor=(0.5, -0.15), fancybox=True, shadow=True, ncol=6,
                handles=[a1, a2, a3, a4, hl], labels=["Score Events +", "Score Events -", "Win Events", "Lose Events",
                                                      "Convergence"], fontsize='xx-small')

    return tick, xdata, ydata, scoreDatap, scoreDatam, winData, loseData


def fitnessPlot(filecontents, tick, xdata, ydata, errdata, hl, tl, axes):
    oldtick = tick
    min, max, mean, q1, q2, q3, sd, stderr, e, tick = get_fitness(filecontents, tick)
    xdata.extend(range(oldtick,tick+1))
    errdata.extend(stderr)
    ydata.extend(mean)

    ymin = []
    ymax = []
    for i in range(len(ydata)):
        ymin.append(ydata[i] - errdata[i])
        ymax.append(ydata[i] + errdata[i])

    axes.fill_between(np.array(xdata), np.array(ymax), np.array(ymin), color=c[1], alpha=0.1)
    # axes.errorbar(np.array(xdata), np.array(ydata), yerr=np.array(errdata))

    # calc the trendline
    z = np.polyfit(xdata, ydata, 1)
    p = np.poly1d(z)
    tl.set_data(xdata, p(xdata))

    hl.set_data(np.array(xdata), np.array(ydata))

    return tick, xdata, ydata, errdata


def actExplored(filecontents, tick, axes, hl, xdata, a0, a1, a2, a3, a4, a5, r0, r1, r2, r3, r4, r5, al0, al1, al2, al3,
                al4, al5, rl0, rl1, rl2, rl3, rl4, rl5, i):
    oldtick = tick
    actsUsed, p1, p2, p3, p4, p5, p6, d1, d2, d3, d4, d5, d6, tick = get_act_explored(filecontents, tick)
    xdata.extend(range(oldtick,tick+1))
    a0[i].extend(p1)
    a1[i].extend(p2)
    a2[i].extend(p3)
    a3[i].extend(p4)
    a4[i].extend(p5)
    a5[i].extend(p6)

    r0[i].extend(d1)
    r1[i].extend(d2)
    r2[i].extend(d3)
    r3[i].extend(d4)
    r4[i].extend(d5)
    r5[i].extend(d6)

    ydata = np.row_stack((a0[i], a1[i], a2[i], a3[i], a4[i], a5[i]))
    ydata2 = np.row_stack((r0[i], r1[i], r2[i], r3[i], r4[i], r5[i]))
    y_stack = np.cumsum(ydata, axis=0)
    y_stack2 = np.cumsum(ydata2, axis=0)

    for idx in range(no_acts):
        x = xdata

        # acts explored
        y1 = (0 if idx == 0 else y_stack[(idx-1), :])
        y2 = y_stack[idx, :]
        axes.fill_between(x, y1, y2, facecolor=actcol[idx], alpha=.7)

        # acts recommended
        y3 = (0 if idx == 0 else y_stack2[(idx-1), :])
        y4 = y_stack2[idx, :]
        axes.fill_between(x, y3, y4, facecolor=actcol[idx], alpha=.7)

    # update legend with only actions available in this game
    handleList = []
    if 0 in actsUsed:
        handleList.append(al0[i])
    if 1 in actsUsed:
        handleList.append(al1[i])
    if 2 in actsUsed:
        handleList.append(al2[i])
    if 3 in actsUsed:
        handleList.append(al3[i])
    if 4 in actsUsed:
        handleList.append(al4[i])
    if 5 in actsUsed:
        handleList.append(al5[i])
    leg = axes.legend(loc='upper center', bbox_to_anchor=(0.5, -0.15), fancybox=True, shadow=True, ncol=6,
                      handles=handleList, fontsize='xx-small')
    for legobj in leg.legendHandles:
        legobj.set_linewidth(10.0)

    al0[i].set_data(xdata, y_stack[0, :])
    al1[i].set_data(xdata, y_stack[1, :])
    al2[i].set_data(xdata, y_stack[2, :])
    al3[i].set_data(xdata, y_stack[3, :])
    al4[i].set_data(xdata, y_stack[4, :])
    al5[i].set_data(xdata, y_stack[5, :])

    rl0[i].set_data(xdata, y_stack2[0, :])
    rl1[i].set_data(xdata, y_stack2[1, :])
    rl2[i].set_data(xdata, y_stack2[2, :])
    rl3[i].set_data(xdata, y_stack2[3, :])
    rl4[i].set_data(xdata, y_stack2[4, :])
    rl5[i].set_data(xdata, y_stack2[5, :])

    return tick, xdata, a0, a1, a2, a3, a4, a5, r0, r1, r2, r3, r4, r5

def fitPerAct(filecontents, tick, axes, xdata, v0, v1, v2, v3, v4, v5, vl0, vl1, vl2, vl3, vl4, vl5, i):
    oldtick = tick
    p1, p2, p3, p4, p5, p6, tick = get_fit_act(filecontents, tick)
    xdata.extend(range(oldtick, tick + 1))
    v0[i].extend(p1)
    v1[i].extend(p2)
    v2[i].extend(p3)
    v3[i].extend(p4)
    v4[i].extend(p5)
    v5[i].extend(p6)

    # update legend with only actions available in this game
    handleList = []
    actsUsed, p1, p2, p3, p4, p5, p6, d1, d2, d3, d4, d5, d6, tick = get_act_explored(filecontents, tick)
    if 0 in actsUsed:
        handleList.append(vl0[i])
    if 1 in actsUsed:
        handleList.append(vl1[i])
    if 2 in actsUsed:
        handleList.append(vl2[i])
    if 3 in actsUsed:
        handleList.append(vl3[i])
    if 4 in actsUsed:
        handleList.append(vl4[i])
    if 5 in actsUsed:
        handleList.append(vl5[i])
    leg = axes.legend(loc='upper center', bbox_to_anchor=(0.5, -0.15), fancybox=True, shadow=True, ncol=6,
                     handles=handleList, fontsize='xx-small')
    for legobj in leg.legendHandles:
        legobj.set_linewidth(3.0)

    vl0[i].set_data(xdata, v0[i])
    vl1[i].set_data(xdata, v1[i])
    vl2[i].set_data(xdata, v2[i])
    vl3[i].set_data(xdata, v3[i])
    vl4[i].set_data(xdata, v4[i])
    vl5[i].set_data(xdata, v5[i])

    return tick, xdata, v0, v1, v2, v3, v4, v5

