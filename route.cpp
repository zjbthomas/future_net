#include "route.h"
#include "lib_record.h"
#include <stdio.h>

#include "include/CbcModel.hpp"
#include "include/OsiClpSolverInterface.hpp"

#define INFONUM 4
#define COMMANUM 2
#define TOPOID 0
#define TOPOSRC 1
#define TOPODEST 2
#define TOPOCOST 3
#define MAXROUNDS 300

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
    int Ncol = edge_num;
    double * elements = new double[Ncol];
    int * columns = new int[Ncol];
    int cnt;

    OsiClpSolverInterface model;
    model.loadProblem(Ncol, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

    // Set objective (default is minimization)
    for (int i = 0; i < Ncol - 1; i++) {
        model.setObjCoeff(i, (double) pathCosts[i]);
    }

    // Set x to be binary
    for (int i = 0; i < Ncol; i++) {
        model.setInteger(i);
        model.setColBounds(i, 0, 1);
    }

    /** Set assignment constraints */

    // Every nodes should have same number of input and output
    for (int i = 0; i < n; i++) {
        cnt = 0;

        for (int j = 0; j < Ncol; j++) {
            if (pathSrcs[j] == i) {
                columns[cnt] = j;
                elements[cnt++] = 1;
            }
            if (pathDests[j] == i) {
                columns[cnt] = j;
                elements[cnt++] = -1;
            }
        }

        model.addRow(cnt, columns, elements, 0, 0);
    }

    // Pass every node no more than one time, for core nodes, must pass
    for (int i = 0; i < n; i++) {
        cnt = 0;

        for (int j = 0; j < Ncol; j++) {
            if (pathSrcs[j] == i) {
                columns[cnt] = j;
                elements[cnt++] = 1;
            }
            if (pathDests[j] == i) {
                columns[cnt] = j;
                elements[cnt++] = 1;
            }
        }

        for (int j = 0; j < coreNodesCnt; j++) {
            if (coreNodes[j] == i) {
                model.addRow(cnt, columns, elements, 2, 2);
                break;
            }
            if (j == coreNodesCnt - 1) {
                model.addRow(cnt, columns, elements, 0, 2);
            }
        }
    }

    /** Use subtour constraints */
    for (int round = 0; round < MAXROUNDS; round++) {
        // Solve LP of the current constraints
        CbcModel solver(model);
        solver.setLogLevel(0);
        solver.branchAndBound();
        bool optimal = solver.isProvenOptimal();
        if (optimal) {
            // Get results
            const double * getResults = solver.getColSolution();
            int * results = new int[Ncol];

            // Calculate the number of subtours
            int * subtours[n / 2];
            int * subtoursNodesCnt = new int[n / 2];
            int subtoursCnt = 0;

            int setPathCnt = 0;
            for (int i = 0; i < Ncol; i++) {
                results[i] = getResults[i];
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

            delete [] results;

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
                            record_result(pathIds[j]);
                        }
                    }
                }

                delete [] path;

                // Delete memory
                delete [] columns;
                delete [] elements;

                delete [] pathIds;
                delete [] pathSrcs;
                delete [] pathDests;
                delete [] pathCosts;
                delete [] coreNodes;

                return;
            } else {
                // Add subtour constraints
                for (int i = 0; i < subtoursCnt; i++) {
                    if (subtoursNodesCnt[i] <= n / 2) {
                        cnt = 0;

                        for (int j = 0; j < subtoursNodesCnt[i]; j++) {
                            for (int k = 0; k < Ncol; k++) {
                                if (pathSrcs[k] == subtours[i][j]) {
                                    for (int l = 0; l < subtoursNodesCnt[i]; l++) {
                                        if (pathDests[k] == subtours[i][l]) {
                                            break;
                                        }
                                        if (l == subtoursNodesCnt[i] - 1) {
                                            columns[cnt] = k;
                                            elements[cnt++] = 1;
                                        }
                                    }
                                }
                            }
                        }
                        model.addRow(cnt, columns, elements, 1, Ncol);
                    }
                }
            }

            // Delete memory
            for (int i = 0; i < subtoursCnt; i++) {
                delete [] subtours[i];
            }
            delete [] subtoursNodesCnt;
        } else {
            // Delete memory
            delete [] columns;
            delete [] elements;

            delete [] pathIds;
            delete [] pathSrcs;
            delete [] pathDests;
            delete [] pathCosts;
            delete [] coreNodes;

            return;
        }
    }

    // Delete momory
    delete [] columns;
    delete [] elements;

	delete [] pathIds;
	delete [] pathSrcs;
	delete [] pathDests;
	delete [] pathCosts;
	delete [] coreNodes;
}
