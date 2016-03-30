#include "route.h"
#include "lib_record.h"
#include <stdio.h>

#include "lp_lib.h"

#define MAXNUMLENGTH 4
#define INFONUM 4
#define COMMANUM 2
#define TOPOID 0
#define TOPOSRC 1
#define TOPODEST 2
#define TOPOCOST 3
#define MAXCOST 21

//你要完成的功能总入口
void search_route(char *topoData[5000], int edge_num, char *demandData)
{
    // Convert input topology
    int * topoArr = new int[INFONUM * edge_num];
    for (int i = 0; i < edge_num; i++) {
        // Find the positions of ','
        int * commaPos = new int[MAXNUMLENGTH];
        int commaPosCnt = 0;
        for (int j = 0; j < strlen(topoData[i]); j++) {
            if (topoData[i][j] == ',') {
                commaPos[commaPosCnt] = j;
                commaPosCnt++;
            }
        }
        for (int j = 0; j < INFONUM; j++) {
            int beginPos;
            if (j == 0) {
                beginPos = 0;
            } else {
                beginPos = commaPos[j - 1] + 1;
            }
            int endPos;
            if (j == INFONUM - 1) {
                endPos = strlen(topoData[i]);
            } else {
                endPos = commaPos[j];
            }
            char * info = new char[MAXNUMLENGTH];
            int infoCnt = 0;
            for (int k = beginPos; k < endPos; k++) {
                info[infoCnt] = topoData[i][k];
                infoCnt++;
            }
            topoArr[i * INFONUM + j] = atoi((const char *) info);
            delete [] info;
        }
        delete [] commaPos;
    }

    // Find the number of nodes
    int n = 0; // The index of node starts at 0
    for (int i = 0; i < edge_num; i++) {
        int src = topoArr[i * INFONUM + TOPOSRC];
        int dest = topoArr[i * INFONUM + TOPODEST];

        if (src > n) n = src;
        if (dest > n) n = dest;
    }
    n++;

    // Create matrices
    int * topo = new int[n * n];
    int * pathIds = new int[n * n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            topo[j * n + i] = MAXCOST;
            pathIds[j * n + i] = 0;
        }
    }
    for (int i = 0; i < edge_num; i++) {
        int id = topoArr[i * INFONUM + TOPOID];
        int src = topoArr[i * INFONUM + TOPOSRC];
        int dest = topoArr[i * INFONUM + TOPODEST];
        int cost = topoArr[i * INFONUM + TOPOCOST];

        topo[dest * n + src] = cost;
        pathIds[dest * n + src] = id;
    }
    delete [] topoArr;

    // Handle information in demand
    int source;
    int destination;
    int * coreNodes = new int[n]; // Nodes in the includsing set plus source and destination
    int coreNodesCnt = 2;

    // Find the positions of ','
    int * commaPos = new int[COMMANUM];
    int commaPosCnt = 0;
    for (int i = 0; i < strlen(demandData); i++) {
        if (demandData[i] == ',') {
            commaPos[commaPosCnt] = i;
            commaPosCnt++;
            if (commaPosCnt == COMMANUM) {
                break;
            }
        }
    }

    // Find the positions of '|'
    int * tubePos = new int[n - 3]; // Minus source and destination plus 1
    int tubePosCnt = 0; // This value plus one is the number of nodes in the including set
    for (int i = commaPos[COMMANUM - 1] + 1; i < strlen(demandData); i++) {
        if (demandData[i] == '|') {
            tubePos[tubePosCnt] = i;
            tubePosCnt++;
        }
    }

    // Get the source
    char * srcStr = new char[MAXNUMLENGTH];
    int srcStrCnt = 0;
    for (int i = 0; i < commaPos[0]; i++) {
        srcStr[srcStrCnt] = demandData[i];
        srcStrCnt++;
    }
    source = atoi(srcStr);
    delete [] srcStr;

    // Get the destination
    char * destStr = new char[MAXNUMLENGTH];
    int destStrCnt = 0;
    for (int i = commaPos[0] + 1; i < commaPos[1]; i++) {
        destStr[destStrCnt] = demandData[i];
        destStrCnt++;
    }
    destination = atoi(destStr);
    delete [] destStr;

    // Get the including set and core nodes
    coreNodes[0] = source;
    coreNodes[1] = destination;
    if (tubePosCnt == 0) {
        // Only one node in the including set
        char * nodeStr = new char[MAXNUMLENGTH];
        int nodeStrCnt = 0;
        for (int i = commaPos[COMMANUM - 1] + 1; i < strlen(demandData); i++) {
            nodeStr[nodeStrCnt] = demandData[i];
            nodeStrCnt++;
        }
        coreNodes[coreNodesCnt] = atoi(nodeStr);
        coreNodesCnt++;
        delete [] nodeStr;
    } else {
        for (int i = 0; i < tubePosCnt + 1; i++) {
            int beginPos;
            if (i == 0) {
                beginPos = commaPos[COMMANUM - 1] + 1;
            } else {
                beginPos = tubePos[i - 1] + 1;
            }
            int endPos;
            if (i == tubePosCnt) {
                endPos = strlen(demandData);
            } else {
                endPos = tubePos[i];
            }
            char * nodeStr = new char[MAXNUMLENGTH];
            int nodeStrCnt = 0;
            for (int j = beginPos; j < endPos; j++) {
                nodeStr[nodeStrCnt] = demandData[j];
                nodeStrCnt++;
            }
            coreNodes[coreNodesCnt] = atoi(nodeStr);
            coreNodesCnt++;
            delete [] nodeStr;
        }
    }
    delete [] commaPos;
    delete [] tubePos;

    // Fade connection from destination to source
    int fadeN = source * n + destination;
    topo[fadeN] = 0;

    // Create lp
	lprec * lp;

    int xcol = n * n; // x : n * n
	int Ncol = xcol + n; // u : n
	REAL * row;
	int cnt;
	int * colno;

	lp = make_lp(0, Ncol);


	// Set objective
	cnt = 0;
	colno = new int[xcol];
	row = new REAL[xcol];
	for (int i = 0; i < xcol; i++) {
        colno[cnt] = i + 1;
        row[cnt++] = topo[i];
	}
    set_obj_fnex(lp, cnt, row, colno);
    delete [] colno;
    delete [] row;

    // Set x to be binary
    for (int i = 0; i < xcol; i++) {
        set_binary(lp, i + 1, TRUE);
    }

    // Set u to be integer, no need to set u >= 0 since it is inherited in lpsolve
    for (int i = xcol; i < Ncol; i++) {
        set_int(lp, i + 1, TRUE);
    }

	// Estimate the number of constraints
	resize_lp(lp, 2 * n * n + 5 * n + 2 * coreNodesCnt + 1, get_Ncolumns(lp));

	set_add_rowmode(lp, TRUE);

    // Set not connected path (n * n)
    for (int i = 0; i < xcol; i++) {
        if (topo[i] == MAXCOST) {
            colno = new int(i + 1);
            row = new REAL(1);
            add_constraintex(lp, 1, row, colno, EQ, 0);
            delete colno;
            delete row;
        }
    }

    // Every nodes should have same number of input and output (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        colno = new int[xcol];
        row = new REAL[xcol];
        for (int j = 0; j < n; j++) {
            if (i != j) {
                colno[cnt] = j * n + i + 1;
                row[cnt++] = 1;
                colno[cnt] = i * n + j + 1;
                row[cnt++] = -1;
            }
        }
        add_constraintex(lp, cnt, row, colno, EQ, 0);
        delete [] colno;
        delete [] row;
    }

    // Pass every node no more than one time - row (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        colno = new int[xcol];
        row = new REAL[xcol];
        for (int j = 0; j < n; j++) {
            colno[cnt] = j * n + i + 1;
            row[cnt++] = 1;
        }
        add_constraintex(lp, cnt, row, colno, LE, 1);
        delete [] colno;
        delete [] row;
    }

    // Pass every node no more than one time - column (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        colno = new int[xcol];
        row = new REAL[xcol];
        for (int j = 0; j < n; j++) {
            colno[cnt] = i * n + j + 1;
            row[cnt++] = 1;
        }
        add_constraintex(lp, cnt, row, colno, LE, 1);
        delete [] colno;
        delete [] row;
    }

    // Must pass all core nodes - row (coreNodesCnt)
    for (int i = 0; i < coreNodesCnt; i++) {
        cnt = 0;
        colno = new int[xcol];
        row = new REAL[xcol];
        for (int j =0; j < n; j++) {
            colno[cnt] = j * n + coreNodes[i] + 1;
            row[cnt++] = 1;
        }
        add_constraintex(lp, cnt, row, colno, EQ, 1);
        delete [] colno;
        delete [] row;
    }

    // Must pass all core nodes - column (coreNodesCnt)
    for (int i = 0; i < coreNodesCnt; i++) {
        cnt = 0;
        colno = new int[xcol];
        row = new REAL[xcol];
        for (int j =0; j < n; j++) {
            colno[cnt] = coreNodes[i] * n + j + 1;
            row[cnt++] = 1;
        }
        add_constraintex(lp, cnt, row, colno, EQ, 1);
        delete [] colno;
        delete [] row;
    }

    // Get rid of loop - TSP constraints (n * n)
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if ((i != j) && (j != source)) {
                cnt = 0;
                colno = new int[Ncol];
                row = new REAL[Ncol];
                colno[cnt] = xcol + i + 1;
                row[cnt++] = 1;
                colno[cnt] = xcol + j + 1;
                row[cnt++] = -1;
                colno[cnt] = j * n + i + 1;
                row[cnt++] = n;
                add_constraintex(lp, cnt, row, colno, LE, n - 1);
                delete [] colno;
                delete [] row;
            }
        }
    }

    // Get rid of loop - lower bound of u (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        colno = new int[Ncol];
        row = new REAL[Ncol];
        // Set u
        colno[cnt] = xcol + i + 1;
        row[cnt++] = 1;
        // Set x
        for (int j = 0; j < n; j++) {
            if (i != j) {
                colno[cnt] = j * n + i + 1;
                row[cnt++] = -1;
                colno[cnt] = i * n + j + 1;
                row[cnt++] = -1;
            }
        }
        add_constraintex(lp, cnt, row, colno, GE, -1);
        delete [] colno;
        delete [] row;
    }

    // Get rid of loop - Upper bound of u (n)
    for (int i = 0; i < n; i++) {
        cnt = 0;
        colno = new int[Ncol];
        row = new REAL[Ncol];
        // Set u
        colno[cnt] = xcol + i + 1;
        row[cnt++] = -1;
        // Set x
        for (int j = 0; j < n; j++) {
            if (i != j) {
                colno[cnt] = j * n + i + 1;
                row[cnt++] = n;
                colno[cnt] = i * n + j + 1;
                row[cnt++] = n;
            }
        }
        add_constraintex(lp, cnt, row, colno, GE, 0);
        delete [] colno;
        delete [] row;
    }

    set_add_rowmode(lp, FALSE);

    // Set to minimize
    set_minim(lp);

    // Decrease report
    set_verbose(lp, NEUTRAL);

    // Solve
    if (solve(lp) == 0) {
        row = new REAL[Ncol];
        get_variables(lp, row);

        int * path = new int[n];
        int pathCnt = 0;

        int now = 1;
        bool flag = true;
        while (flag) {
            flag = false;
            for (int i = xcol; i < Ncol; i++) {
                if (row[i] == now) {
                    path[pathCnt] = i - xcol;
                    pathCnt++;
                    now++;
                    flag = true;
                }
            }
        }

        for (int i = 0; i < pathCnt - 1; i++) {
            record_result(pathIds[path[i + 1] * n + path[i]]);
        }
        delete [] path;
        delete [] row;
    }

    // Delete lp
    delete_lp(lp);

	// Delete memory
	delete [] topo;
	delete [] pathIds;
	delete [] coreNodes;
}
