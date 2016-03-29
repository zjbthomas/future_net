%% Initialization
close all;
clear all;
clc;
%% Set paths
topoPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\topo.csv';
demandPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\demand.csv';
resultPath = 'D:\Users\Thomas Zhang\Desktop\Data\GIT\future_net\test-case\randomCase\result.csv';
%% Constant
MAXCOST = 12001;
%% Read from csv files
topoData = csvread(topoPath);

fid = fopen(demandPath);
demandData = textscan(fid, '%s %*[^\n]');
demandData = char(demandData{1});
fclose(fid);
%% Handle inputs
n = max(max(topoData(:, 2:3))) + 1;
c = ones(n, n) * MAXCOST;
id = zeros(n, n);
for i = 1 : size(topoData, 1)
    c(topoData(i, 3) + 1, topoData(i, 2) + 1) = topoData(i, 4);
    id(topoData(i, 3) + 1, topoData(i, 2) + 1) = topoData(i, 1);
end

demandData = regexprep(demandData, '\|', ',');
demandData = regexp(demandData, ',', 'split');
demandData = str2num(char(demandData));
src = demandData(1) + 1;
dest = demandData(2) + 1;
includingSet = demandData(3: end)' + 1;
coreNodes = [src, includingSet, dest];
% Create a fade route from destination to src
c(src, dest) = 0;
%% LP
x = binvar(n, n, 'full');
u = intvar(n, 1);
objective = sum(sum(c .* x));
constraints = [];
% Constraints on u
constraints = [constraints, u >= 0];
% Connect source and destination
constraints = [constraints, x(src, dest) == 1];
% Cost control
constraints = [constraints, c .* x <= MAXCOST - 1];
% Nodes control
constraints = [constraints, x' * ones(n, 1) - x * ones(n, 1) == 0];
% Pass every node no more than one time
constraints = [constraints, x * ones(n, 1) <= 1];
constraints = [constraints, x' * ones(n, 1) <= 1];
% Must pass all core nodes
constraints = [constraints, x(:, coreNodes)' * ones(n, 1) == 1];
constraints = [constraints, x(coreNodes, :) * ones(n, 1) == 1];
% Get rid of loop
for i = 1: n
    for j = 1: n
        if ((j ~= src) && (i ~= j))
            constraints = [constraints, u(i) - u(j) + n * x(j, i) <= n - 1];
        end
    end 
end
constraints = [constraints, (n + 1) * (x' * ones(n, 1) + x * ones(n ,1)) >= u >= x' * ones(n, 1) + x * ones(n ,1)];
% Solve
options = sdpsettings('solver','lpsolve');
sol = optimize(constraints, objective, options);

if sol.problem == 0
    fTopo = value(x);
    fOrder = value(u);
    
    path = [];
    now = max(fOrder);
    for i = 1: n
        if ((fOrder(i) ~= 0) && (fOrder(i) < now))
            now = fOrder(i);
        end
    end
    while (now <= max(fOrder))
        for i = 1: n
            if (fOrder(i) == now)
                path = [path, i];
                now = now + 1;
                break;
            end
        end
    end
    
    out = '';
    cost = 0;
    for i = 1: size(path, 2) - 1
        cost = cost + c(path(i + 1), path(i));
        out = [out, num2str(id(path(i + 1), path(i)))];
        if (i ~= size(path, 2) - 1)
            out = [out, '|'];
        end
    end
    fid = fopen(resultPath, 'w');
    fprintf(fid, '%s', out);
    fclose(fid);
    disp(path - 1);
    disp(cost);
else
    fid = fopen(resultPath, 'w');
    fprintf(fid, '%s', 'NA');
    fclose(fid);
    disp('NA');
end