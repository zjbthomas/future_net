#include "route.h"
#include "lib_record.h"
#include <stdio.h>

#define INFONUM 4
#define COMMANUM 2
#define TOPOID 0
#define TOPOSRC 1
#define TOPODEST 2
#define TOPOCOST 3
#define MAXROUNDS 10

//你要完成的功能总入口
void search_route(char *topo[5000], int edge_num, char *demand)
{
    /** Handle inputs */
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

    /** Create lp model */
	lprec * lp;

    int Ncol = edge_num; // x: edge_num
	REAL * row = new REAL[Ncol];
	int cnt;
	int * colno = new int[Ncol];

    REAL * results = new REAL[Ncol];

	lp = make_lp(0, Ncol);

	// Estimate the number of constraints
	resize_lp(lp, 4 * n + Ncol + 50, get_Ncolumns(lp));

    // Decrease report
    set_verbose(lp, NEUTRAL);

	// Set objective
	cnt = 0;
	for (int i = 0; i < Ncol - 1; i++) {
        colno[cnt] = i + 1;
        row[cnt++] = pathCosts[i];
	}
    set_obj_fnex(lp, cnt, row, colno);

    // Set x to be binary
    for (int i = 0; i < Ncol; i++) {
        set_binary(lp, i + 1, TRUE);
    }

	// Set to minimize
    set_minim(lp);

    /** Set assignment constraints */
	set_add_rowmode(lp, TRUE);

    // Every nodes should have same number of input and output (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;

        for (int j = 0; j < Ncol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = -1;
            }
        }

        add_constraintex(lp, cnt, row, colno, EQ, 0);
    }

    // Pass every node no more than one time, for core nores, must pass (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;

        for (int j = 0; j < Ncol; j++) {
            if (pathSrcs[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
            }
            if (pathDests[j] == i) {
                colno[cnt] = j + 1;
                row[cnt++] = 1;
            }
        }

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
    set_add_rowmode(lp, FALSE);

    /** Use subtour constraints */
    for (int round = 0; round < MAXROUNDS; round++) {
        // Solve LP of the current constraints
        int ret = solve(lp);
        if ((ret == OPTIMAL) || (ret == SUBOPTIMAL)) {
            // Get results
            get_variables(lp, results);

            // Calculate the number of subtours
            int * subtours[n / 2];
            int * subtoursNodesCnt = new int[n / 2];
            int subtoursCnt = 0;

            int setPathCnt = 0;
            for (int i = 0; i < Ncol; i++) {
                if (results[i] == 1) {
                    setPathCnt++;
                }
            }

            while (setPathCnt > 0) {
                // Create a new subtours
                subtours[subtoursCnt] = new int[n];
                int nodesCnt = 0;
                // Find first point of the subtours
                int now;
                for (int i = 0; i < Ncol; i++) {
                    if (results[i] == 1) {
                        now = pathSrcs[i];
                        subtours[subtoursCnt][nodesCnt] = now;
                        nodesCnt++;
                        break;
                    }
                }
                // Find subtours nodes
                bool flag = true;
                while (flag) {
                    flag = false;
                    for (int i = 0; i < Ncol; i++) {
                        if (results[i] == 1) {
                            if (pathSrcs[i] == now) {
                                // Path recorded
                                results[i] = 0;
                                setPathCnt--;

                                now = pathDests[i];
                                subtours[subtoursCnt][nodesCnt] = now;
                                nodesCnt++;

                                flag = true;
                                break;
                            }
                        }
                    }
                }
                // Move to next subtours
                subtoursNodesCnt[subtoursCnt] = nodesCnt - 1;
                subtoursCnt++;
            }

            if (subtoursCnt == 1) {
                // The result is optimal, so record result
                int * path = new int [subtoursNodesCnt[0]];
                for (int i = 0; i < subtoursNodesCnt[0]; i++) {
                    if (subtours[0][i] == source) {
                        for (int j = i; j < subtoursNodesCnt[0]; j++) {
                            path[j - i] = subtours[0][j];
                        }
                        for (int j = 0; j < i; j++) {
                            path[j + subtoursNodesCnt[0] - i] = subtours[0][j];
                        }
                        break;
                    }
                }

                for (int i = 0; i < subtoursNodesCnt[0] - 1; i++) {
                    for (int j = 0; j < Ncol; j++) {
                        if ((pathSrcs[j] == path[i]) && (pathDests[j] == path[i + 1])) {
                            //record_result(pathIds[j]);
                            record_result(round);
                        }
                    }
                }

                delete [] path;

                // Delete lp
                delete_lp(lp);
                delete [] colno;
                delete [] row;
                delete [] results;

                // Delete memory
                delete [] pathIds;
                delete [] pathSrcs;
                delete [] pathDests;
                delete [] pathCosts;
                delete [] coreNodes;

                return;
            } else {
                // Add subtour constraints (About 50)
                set_add_rowmode(lp, TRUE);

                for (int i = 0; i < subtoursCnt; i++) {
                    if (subtoursNodesCnt[i] <= n / 2) {
                        cnt = 0;

                        for (int j = 0; j < subtoursNodesCnt[i]; j++) {
                            for (int k = 0; k < Ncol; k++) {
                                if (pathSrcs[k] == subtours[i][j]) {
                                    for (int l = 0; l < subtoursNodesCnt[i]; l++) {
                                        if (pathDests[k] == subtours[i][l]) {
                                            colno[cnt] = k + 1;
                                            row[cnt++] = 1;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        add_constraintex(lp, cnt, row, colno, LE, subtoursNodesCnt[i] - 1);
                    }
                }

                set_add_rowmode(lp, FALSE);
            }

            // Delete memory
            for (int i = 0; i < subtoursCnt; i++) {
                delete [] subtours[i];
            }
            delete [] subtoursNodesCnt;
        } else {
            // Delete lp
            delete_lp(lp);
            delete [] colno;
            delete [] row;
            delete [] results;

            // Delete memory
            delete [] pathIds;
            delete [] pathSrcs;
            delete [] pathDests;
            delete [] pathCosts;
            delete [] coreNodes;

            return;
        }
    }

    // Delete lp
    delete_lp(lp);
    delete [] colno;
    delete [] row;
    delete [] results;

	// Delete memory
	delete [] pathIds;
	delete [] pathSrcs;
	delete [] pathDests;
	delete [] pathCosts;
	delete [] coreNodes;
}
