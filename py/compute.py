__author__ = 'raluca'

import matplotlib
import glob
import numpy as np
import pylab
import sys


def get_convergence(filecontents, tick):
    gameTickConvergence = []
    i = -1

    for f in filecontents:
        i += 1
        if i >= tick:
            try:
                gameTickConvergence.append(f[0])
            except:
                e = sys.exc_info()[0]
                print(e)
                break

    return gameTickConvergence, i


def get_events(acts, evos, tick):
    scores = []
    scoreEventsp = []
    scoreEventsm = []
    winData = []
    loseData = []
    i = -1

    try:
        for _ in range(len(acts)):
            i += 1
            if i >= tick:
                try:
                    s = acts[i]
                    w = evos[i][31]
                    l = evos[i][38]
                    if scores:
                        if s > scores[-1]:
                            scoreEventsp.append(i)
                        elif s < scores[-1]:
                            scoreEventsm.append(i)
                    scores.append(s)
                    if w != 0:
                        winData.append(i)
                    if l != 0:
                        loseData.append(i)
                except:
                    e = sys.exc_info()[0]
                    print("Error parsing score events " + str(e))
                    break
    except:
        e = sys.exc_info()[0]
        print("Error reading acts data " + str(e))

    return scoreEventsp, scoreEventsm, winData, loseData, i


def get_fitness(filecontents, tick):
    min = []
    max = []
    mean = []
    q1 = []
    q2 = []
    q3 = []
    sd = []
    stderr = []
    e = []
    i = -1

    for f in filecontents:
        i += 1
        if i >= tick:
            try:
                min.append(f[16])
                max.append(f[17])
                mean.append(f[18])
                q1.append(f[19])
                q2.append(f[20])
                q3.append(f[21])
                sd.append(f[22])
                e.append(f[24])
                stderr.append(f[23])
            except:
                e = sys.exc_info()[0]
                print(e)
                break

    return min, max, mean, q1, q2, q3, sd, stderr, e, i


def get_act_explored(filecontents, tick):
    p1 = []
    p2 = []
    p3 = []
    p4 = []
    p5 = []
    p6 = []

    d1 = []
    d2 = []
    d3 = []
    d4 = []
    d5 = []
    d6 = []

    i = -1

    actsUsed = {0, 1, 2, 3, 4, 5}
    count = [0] * 6

    for f in filecontents:
        i += 1
        try:
            if i >= tick:
                v1 = (0 if f[2] == -1 else f[2])
                v2 = (0 if f[3] == -1 else f[3])
                v3 = (0 if f[4] == -1 else f[4])
                v4 = (0 if f[5] == -1 else f[5])
                v5 = (0 if f[6] == -1 else f[6])
                v6 = (0 if f[7] == -1 else f[7])
                sum = v1 + v2 + v3 + v4 + v5 + v6
                p1.append(v1 / sum * 100)
                p2.append(v2 / sum * 100)
                p3.append(v3 / sum * 100)
                p4.append(v4 / sum * 100)
                p5.append(v5 / sum * 100)
                p6.append(v6 / sum * 100)

                v11 = (0 if f[10] == -1 else f[10])
                v21 = (0 if f[11] == -1 else f[11])
                v31 = (0 if f[12] == -1 else f[12])
                v41 = (0 if f[13] == -1 else f[13])
                v51 = (0 if f[14] == -1 else f[14])
                v61 = (0 if f[15] == -1 else f[15])
                sum = v11 + v21 + v31 + v41 + v51 + v61
                d1.append(v11 / sum * -100)
                d2.append(v21 / sum * -100)
                d3.append(v31 / sum * -100)
                d4.append(v41 / sum * -100)
                d5.append(v51 / sum * -100)
                d6.append(v61 / sum * -100)

            if f[2] == -1:
                count[0] += 1
            if f[3] == -1:
                count[1] += 1
            if f[4] == -1:
                count[2] += 1
            if f[5] == -1:
                count[3] += 1
            if f[6] == -1:
                count[4] += 1
            if f[7] == -1:
                count[5] += 1
        except:
            e = sys.exc_info()[0]
            print(e)
            break

    # check which actions are never explored in this game (likely not part of actions available, thus irrelevant)
    for idx in range(len(count)):
        c = count[idx]
        if c == i + 1:
            actsUsed.remove(idx)
        else:
            actsUsed.add(idx)

    return actsUsed, p1, p2, p3, p4, p5, p6, d1, d2, d3, d4, d5, d6, i

def get_fit_act(filecontents, tick):
    p1 = []
    p2 = []
    p3 = []
    p4 = []
    p5 = []
    p6 = []

    i = -1

    for f in filecontents:
        i += 1
        try:
            if i >= tick:
                p1.append(f[25])
                p2.append(f[26])
                p3.append(f[27])
                p4.append(f[28])
                p5.append(f[29])
                p6.append(f[30])
        except:
            e = sys.exc_info()[0]
            print(e)
            break

    return p1, p2, p3, p4, p5, p6, i
