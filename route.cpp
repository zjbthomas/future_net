
#include "route.h"
#include "lib_record.h"
#include <stdio.h>

#include "lp_lib.h"

//你要完成的功能总入口
void search_route(char *topoData[5000], int edge_num, char *demandData)
{
    // Constants
    int const MAXCOST = 12000;

	// Get the number of nodes
	int nodesNum = 0;
	for (int i = 0; i < edge_num; i++) {
		int testNode = atoi((const char *) topoData[i][2]);
		if (testNode > nodesNum) {
			nodesNum = testNode;
		}
		testNode = atoi((const char *) topoData[i][3]);
		if (testNode > nodesNum) {
			nodesNum = testNode;
		}
	}
	nodesNum++;

    // Create matrix
    int[][] topo = new int[nodesNum][nodesNum];
    for (int i = 0; i < nodesNum; i++) {
        for (int j = 0; j < nodesNum; j++) {
            topo[i][j] = MAXCOST;
        }
    }
    for (int i = 0; i < edge_num; i++) {
        int src = atoi((const char *) topoData[i][2]);
        int dest = atoi((const char *) topoData[i][3]);
        int cost = atoi((const char *) topoData[i][4]);

        topo[dest][src] = cost;
    }

    // Handle information in demand
    int source;
    int destination;
    int[] includingSet;

    char*
    for (int i = 0; i < strlen(demandData); i++) {

    }

	unsigned short result[] = {2, 6, 3};//示例中的一个解

	for (int i = 0; i < 3; i++)
		record_result(result[i]);

	lprec * lp;
	lp = make_lp(0, 4);

	delete_lp(lp);
}
