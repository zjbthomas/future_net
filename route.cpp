#include "route.h"
#include "lib_record.h"
#include <stdio.h>

#include "lp_lib.h"

#define INFONUM 4
#define COMMANUM 2
#define TOPOID 0
#define TOPOSRC 1
#define TOPODEST 2
#define TOPOCOST 3

//你要完成的功能总入口
void search_route(char *topo[5000], int edge_num, char *demand)
{
    // Find source and destination
    int source;
    int destination;

    int beginPos = 0;
    int commaCnt = 0;
    for (int i = 0; i < strlen(demand); i++) {
        if (demand[i] == ',') {
            char * numStr = new char[i - beginPos];
            for (int j = 0; j < i - beginPos; j++) {
                numStr[j] = demand[j + beginPos];
            }
            int num = atoi((const char *) numStr);
            delete [] numStr;

            switch (commaCnt) {
                case 0:
                    source = num;
                    break;
                case 1:
                    destination = num;
            }

            beginPos = i + 1;
            commaCnt++;
        }
        if (commaCnt == COMMANUM) {
            break;
        }
    }

    // Create topology information matrix, plus 1 for the fade route from destination to source
    int * pathIds = new int[edge_num];
    int * pathSrcs = new int[edge_num + 1];
    int * pathDests = new int[edge_num + 1];
    int * pathCosts = new int[edge_num + 1];

    // Convert from topo
    int n = 0; // Number of nodes
    int delPath = 0;
    for (int i = 0; i < edge_num; i++) {
        int beginPos = 0;
        int infoCnt = 0;

        int id;
        int src;
        int dest;
        int cost;
        for (int j = 0; j < strlen(topo[i]); j++) {
            if (topo[i][j] == ',') {
                char * numStr = new char[j - beginPos];
                for (int k = 0; k < j - beginPos; k++) {
                    numStr[k] = topo[i][k + beginPos];
                }
                int num = atoi((const char *) numStr);
                delete [] numStr;

                switch (infoCnt) {
                    case TOPOID:
                        id = num;
                        break;
                    case TOPOSRC:
                        src = num;
                        break;
                    case TOPODEST:
                        dest = num;
                        break;
                }

                beginPos = j + 1;
                infoCnt++;
                if (infoCnt == INFONUM - 1) {
                    // Get cost
                    char * numStr = new char[strlen(topo[i]) - beginPos];
                    for (int k = 0; k < strlen(topo[i]) - beginPos; k++) {
                        numStr[k] = topo[i][k + beginPos];
                    }
                    int num = atoi((const char *) numStr);
                    delete [] numStr;

                    cost = num;

                    break;
                }
            }
        }

        if (dest == source) {
            delPath++;
            continue;
        }

        if (src == destination) {
            delPath++;
            continue;
        }

        bool flag = true;
        for (int j = 0; j < i; j++) {
            if ((pathSrcs[j] == src) && (pathDests[j] == dest)) {
                if (pathCosts[j] > cost) {
                    pathIds[j] = id;
                    pathCosts[j] = cost;
                }

                delPath++;

                flag = false;

                break;
            }
        }
        if (flag) {
            pathIds[i - delPath] = id;
            pathSrcs[i - delPath] = src;
            pathDests[i - delPath] = dest;
            pathCosts[i - delPath] = cost;

            if (src > n) n = src;
            if (dest > n) n = dest;
        }
    }
    n++; // The index of node starts at 0
    edge_num -= delPath;

    // Fade connection from destination to source (with 0 cost)
    pathSrcs[edge_num] = destination;
    pathDests[edge_num] = source;
    pathCosts[edge_num] = 0;
    edge_num++;

    // Find nodes in the includsing set plus source and destination
    int * coreNodes = new int[n]; // Nodes in the includsing set plus source and destination
    coreNodes[0] = source;
    coreNodes[1] = destination;
    int coreNodesCnt = 2;

    for (int i = beginPos; i < strlen(demand); i++) {
        if (demand[i] == '|') {
            char * numStr = new char[i - beginPos];
            for (int j = 0; j < i - beginPos; j++) {
                numStr[j] = demand[j + beginPos];
            }
            int num = atoi((const char *) numStr);
            delete [] numStr;

            coreNodes[coreNodesCnt] = num;
            coreNodesCnt++;

            beginPos = i + 1;
        }
    }
    // Get last core node
    char * numStr = new char[strlen(demand) - beginPos];
    for (int i = 0; i < strlen(demand) - beginPos; i++) {
        numStr[i] = demand[i + beginPos];
    }
    int num = atoi((const char *) numStr);
    delete [] numStr;

    coreNodes[coreNodesCnt] = num;
    coreNodesCnt++;

    // Create lp
	lprec * lp;

    int xcol = edge_num; // x : edge_num
	int Ncol = xcol + n; // u : n
	REAL * row = new REAL[Ncol];
	int cnt;
	int * colno = new int[Ncol];

	lp = make_lp(0, Ncol);

	// Set objective
	cnt = 0;
	for (int i = 0; i < xcol - 1; i++) {
        colno[cnt] = i + 1;
        row[cnt++] = pathCosts[i];
	}
    set_obj_fnex(lp, cnt, row, colno);

    // Set x to be binary
    for (int i = 0; i < xcol; i++) {
        set_binary(lp, i + 1, TRUE);
    }

    // Set u to be integer, no need to set u >= 0 since it is inherited in lpsolve
    for (int i = xcol; i < Ncol; i++) {
        set_int(lp, i + 1, TRUE);
    }

	// Estimate the number of constraints
	resize_lp(lp, 4 * n + xcol - 1, get_Ncolumns(lp));

	set_add_rowmode(lp, TRUE);

    // Every nodes should have same number of input and output (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;

        bool flag = false;
        for (int j = 0; j < xcol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
                flag = true;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = -1;
                flag = true;
            }
        }

        if (flag) {
            add_constraintex(lp, cnt, row, colno, EQ, 0);
        }
    }

    // Pass every node no more than one time, for core nores, must pass (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;

        bool flag = false;
        for (int j = 0; j < xcol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
                flag = true;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
                flag = true;
            }
        }

        if (flag) {
            for (int j = 0; j < coreNodesCnt; j++) {
                if (coreNodes[j] == i) {
                    add_constraintex(lp, cnt, row, colno, EQ, 2);
                    break;
                }
                if (j == coreNodesCnt - 1) {
                    add_constraintex(lp, cnt, row, colno, LE, 2);
                }
            }
        }
    }

    // Get rid of loop - TSP constraints (xcol - 1)
    for (int i = 0; i < xcol - 1; i++) {
        cnt = 0;
        // +u_i
        colno[cnt] = xcol + pathSrcs[i] + 1;
        row[cnt++] = 1;
        // -u_j
        colno[cnt] = xcol + pathDests[i] + 1;
        row[cnt++] = -1;
        // +n * x_ij
        colno[cnt] = i + 1;
        row[cnt++] = n;

        add_constraintex(lp, cnt, row, colno, LE, n - 1);
    }

    // Get rid of loop - lower bound of u (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        // Set u
        colno[cnt] = xcol + i + 1;
        row[cnt++] = 1;
        // Set x
        for (int j = 0; j < xcol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = -1;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = -1;
            }
        }
        add_constraintex(lp, cnt, row, colno, GE, -1);
    }

    // Get rid of loop - Upper bound of u (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        // Set u
        colno[cnt] = xcol + i + 1;
        row[cnt++] = -1;
        // Set x
        for (int j = 0; j < xcol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = n;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = n;
            }
        }
        add_constraintex(lp, cnt, row, colno, GE, 0);
    }

    set_add_rowmode(lp, FALSE);

    // Set to minimize
    set_minim(lp);

    // Decrease report
    set_verbose(lp, NEUTRAL);

    // Set timeout
    set_timeout(lp, 7);

    // Solve
    int ret = solve(lp);
    if ((ret == OPTIMAL) || (ret == SUBOPTIMAL)) {
        get_variables(lp, row);

        int * path = new int[n];
        int pathCnt = 0;

        int now = row[xcol + source];
        for (int i = 0; i < n; i++) {
            for (int i = xcol; i < Ncol; i++) {
                if (row[i] == now) {
                    path[pathCnt] = i - xcol;
                    pathCnt++;
                    break;
                }
            }
            now++;
        }

        if (path[pathCnt - 1] == destination) {
            for (int i = 0; i < pathCnt - 1; i++) {
                for (int j = 0; j < xcol; j++) {
                    if ((pathSrcs[j] == path[i]) && (pathDests[j] == path[i + 1])) {
                        record_result(pathIds[j]);
                    }
                }
            }
        }

        delete [] path;
    }

    // Delete lp
    delete_lp(lp);
    delete [] colno;
    delete [] row;

	// Delete memory
	delete [] pathIds;
	delete [] pathSrcs;
	delete [] pathDests;
	delete [] pathCosts;
	delete [] coreNodes;
}
