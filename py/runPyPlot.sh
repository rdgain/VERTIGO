#!/bin/bash

ppath=$1
game_idx=$2
lvl=$3

python ${ppath} ${game_idx} ${lvl} > logs/output_pyplot.log 2> logs/err_pyplot.log