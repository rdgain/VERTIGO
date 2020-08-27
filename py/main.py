import os

__author__ = 'dperez'

from compute import *
from graphs import drawLivePlot
import sys
import time

def printLivePlot(algFile, actFile):
    drawLivePlot(algFile, actFile)

if __name__ == "__main__":
    #get args to find files
    path = os.path.dirname(sys.argv[0])
    game_idx = sys.argv[1]
    lvl = sys.argv[2]
    algFile = path + "\\files\\evo_" + game_idx + "_" + lvl + ".log"
    actFile = path + "\\files\\actions_" + game_idx + "_" + lvl + ".log"

    #draw plots
    printLivePlot(algFile, actFile)
