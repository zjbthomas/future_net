function [valid, message] = checkResult(topoPath, demandPath, resultPath, display)
    %% Initialize output
    valid = false;
    message = [];
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
        message = ' with result NA';
        return;
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
                    message = [' with wrong path at path id ', num2str(pathId(i))];
                    return;
                else
                    cost = cost + topo(j, 4);
                    if (display)
                        disp(lastPathDest);
                    end
                    lastPathDest = topo(j, 3);
                    break;
                end
            end
        end
        if (i == size(pathId, 1))
            if (display)
                disp(lastPathDest);
            end
            if (lastPathDest == dest)
                valid = true;
                message = [' correct with cost ', num2str(cost)];
                return;
            else
                message = ' with last node wrong';
                return;
            end
        end
    end
end