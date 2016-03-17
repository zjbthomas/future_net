function [path] = findBasePath(src, dest, is)
    %% Clearence    
    close all;
    clc;

    %% Output initialization
    path = [];
    
    %% Modify input to the format of MATLAB
    src = src + 1;
    dest = dest + 1;
    is = is + 1;
    
    %% Constant
    SRC_INIT = 1;
    DEST_INIT = -1;
    INC_INIT = 0.2;
    NINC_INIT = 0;

    OUT_CEF = 0.05; % Can not be 1 or 0
    
    topo = [[1, 0, 0, 0, OUT_CEF, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, OUT_CEF];
            [OUT_CEF, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
            [0, 0, 1, OUT_CEF, 0, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0];
            [OUT_CEF, 0, 0, 1, 0, OUT_CEF, 0, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 0];
            [0, OUT_CEF, 0, 0, 1, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0];
            [0, 0, 0, 0, 0, 1, 0, OUT_CEF, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 0, 0, OUT_CEF];
            [0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, OUT_CEF, 0, 0];
            [0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, OUT_CEF, 0, OUT_CEF, 0, 0, OUT_CEF, 0, 0, 0];
            [0, 0, 0, 0, 0, 0, OUT_CEF, 0, 1, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0];
            [0, 0, 0, 0, OUT_CEF, 0, 0, OUT_CEF, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, OUT_CEF];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, OUT_CEF, 0, 0, 0, 0, 0];
            [0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, OUT_CEF, 0, 0];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, OUT_CEF];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, OUT_CEF, 0];
            [0, 0, 0, 0, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0];
            [0, 0, 0, 0, 0, OUT_CEF, 0, 0, 0, OUT_CEF, 0, OUT_CEF, 0, 0, 0, 0, 0, 1, OUT_CEF, OUT_CEF];
            [0, 0, 0, OUT_CEF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, OUT_CEF, 0, 0, 1, 0];
            [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, OUT_CEF, 1];];
    
    %% Handle input
    w = NINC_INIT * ones(20, 1);
    w(src, 1) = SRC_INIT;
    w(dest, 1) = DEST_INIT;
    w(is, 1) = INC_INIT;
    
    mask_topo = topo;
    
    mask_topo(:, src) = 0;
    
    mask_topo(src,:) = zeros(1, 20);
    mask_topo(src, src) = 1;
    mask_topo(dest,:) = zeros(1, 20);
    mask_topo(dest, dest) = 1;
    
    %% Recursion
    last_path = [];
    while (true)
        w = mask_topo * w;
        now_path = [];
        remain_is = is;
        now = src;
        while (true) 
            now_path = [now_path now];
            out = [];
            for i = 1 : 20
                if ((topo(now, i) == OUT_CEF) && (isempty(find(now_path == i))))
                    out = [out i];
                end
            end

            if (isempty(out))
                if (isequal(now_path, last_path))
                    return;
                else
                    last_path = now_path;
                    break;
                end
            end

            if (isempty(remain_is))
                if (~isempty(find(out == dest)))
                    path = [now_path dest];
                    path = path - 1; % Modify from the format of MATLAB
                    return;
                end
            end
            
            next = find(w == max(w(out)));
            if (size(next, 1) ~= 1)
                break;
            else
                if (~isempty(find(is == next)))
                    index = find(remain_is == next);
                    remain_is = [remain_is(1, 1:(index-1)), remain_is(1, (index+1):end)];
                end    
                now = next;
            end
        end
    end
end