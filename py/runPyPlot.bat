@echo off

set ppath=%1
set game_idx=%2
set lvl=%3

c:\python27\python.exe %ppath% %game_idx% %lvl% > logs/output_pyplot.log 2> logs/err_pyplot.log