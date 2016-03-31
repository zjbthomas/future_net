%% Initialization
close all;
clear all;
clc;
%% Check cases
baseDir = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\checkCases\';
list = dir(baseDir);

for l = 3: length(list)
    if (list(l).isdir)
        % Set paths
        topoPath = [baseDir, list(l).name, '\topo.csv'];
        demandPath = [baseDir, list(l).name, '\demand.csv'];
        resultPath = [baseDir, list(l).name, '\result.csv'];
        % Call subroutine
        [valid, message] = checkResult(topoPath, demandPath, resultPath);
        if (valid)
            disp([list(l).name, message]);
        else
            warning([list(l).name, message]);
        end
    end
end