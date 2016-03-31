%% Initialization
close all;
clear all;
clc;
%% Check case
% Set paths
topoPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\checkCases\topo.csv';
demandPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\checkCases\demand.csv';
resultPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\checkCases\result.csv';
% Call subroutine
[valid, message] = checkResult(topoPath, demandPath, resultPath, true);
if (valid)
    disp(['Given case', message]);
else
    warning(['Given case', message]);
end