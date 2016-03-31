%% Initialization
close all;
clear all;
clc;
%% Set paths
topoPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\topo.csv';
demandPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\demand.csv';
resultPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\result.csv';
%% Read from csv files
topoData = csvread(topoPath);

fid = fopen(demandPath);
demandData = textscan(fid, '%s %*[^\n]');
demandData = char(demandData{1});
fclose(fid);

fid = fopen(resultPath);
resultData = textscan(fid, '%s %*[^\n]');
resultData = char(resultData{1});
fclose(fid);
%% Handle inputs
if (isequal(resultData, 'NA'))
    error('Result is NA.');
end

topo = topoData;

demandData = regexprep(demandData, '\|', ',');
demandData = regexp(demandData, ',', 'split');
demandData = str2num(char(demandData));
src = demandData(1) ;
dest = demandData(2);
includingSet = demandData(3: end)';

resultData = regexp(resultData, '\|', 'split');
pathId = str2num(char(resultData));
%% Check result
cost = 0;
lastPathDest = src;
for i = 1: size(pathId, 1)
    for j = 1: size(topo, 1)
        if (topo(j, 1) == pathId(i))
            nextPathSrc = topo(j, 2);
            if (nextPathSrc ~= lastPathDest)
                error(['Wrong path at path id ', pathId(i)]);
            else
                cost = cost + topo(j, 4);
                lastPathDest = topo(j, 3);
                break;
            end
        end
    end
    if (i == size(pathId, 1))
        if (lastPathDest == dest)
            disp(cost);
        else
            error('Last node wrong.');
        end
    end
end