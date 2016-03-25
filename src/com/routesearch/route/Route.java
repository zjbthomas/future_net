/**
 * Implementation
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public final class Route
{
    /**
     * Class for routing
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
	
	/** Maximum cost of a path */
	private static final int MAXCOST = 13000;
	
	/** Store the position of information of paths */
	private static final int PATHID = 0;
	private static final int PATHSRC = 1;
	private static final int PATHDEST = 2;
	private static final int COST = 3;
	private static final int STOREPATHID = 0;
	private static final int STORECOST = 1;
	
	/** Store the information of the topology */
	private static HashMap<PathKey, Integer[]> edgesInfo = new HashMap<PathKey, Integer[]>();
	private static int nodeCount;
	
	/**
	 * The entry point of the route search function
	 * @param graphContent
	 * @param condition
	 * @return
	 */
    public static String searchRoute(String graphContent, String condition)
    {
    	// Store the information in graphContent into a desired one
    	storeGraphContent(graphContent);
    	
    	// Find the total number of nodes
    	nodeCount = edgesInfo.size();
    	
    	// Create two adjacent matrices
    	int[][] adjMat= new int[nodeCount][nodeCount];
    	int[][] adjMatLID=new int[nodeCount][nodeCount];
    	
    	// Initialize the value of two adjacent matrices to MAXCOST and -1
    	for (int i = 0; i < nodeCount; i++) {
    		for (int j = 0; j < nodeCount; j++) {
    			adjMat[i][j] = MAXCOST;
    			adjMatLID[i][j] = -1;
    		}
    	}

    	// Initialize the value of two adjacent matrices according to the topology
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		int pathId = Integer.parseInt(edgeSplit[PATHID]);
    		int pathSrc = Integer.parseInt(edgeSplit[PATHSRC]);
    		int pathDest = Integer.parseInt(edgeSplit[PATHDEST]);
    		int cost = Integer.parseInt(edgeSplit[COST]);
    		
    		if (cost < adjMat[pathSrc][pathDest]) {
    			adjMat[pathSrc][pathDest] = cost;
    			adjMatLID[pathSrc][pathDest] = pathId;
    		}
    	}

    	// Get the information of source, destination and including set
    	String[] conditionSplit = condition.split(",|\n");
    	
    	int source = Integer.parseInt(conditionSplit[0]);
    	int destination = Integer.parseInt(conditionSplit[1]);
    	
    	String[] splitIncludingSet = conditionSplit[2].split("\\|");
    	int includingSetCnt = splitIncludingSet.length;
    	int[] includingSet = new int[includingSetCnt];
    	
    	int[] nodes = new int[includingSetCnt + 2];
    	nodes[0] = source;
    	nodes[includingSetCnt + 1] = destination;
    	
    	for (int i = 0; i < includingSetCnt; i++) {
    		int currentNode = Integer.parseInt(splitIncludingSet[i]);
    		includingSet[i] = currentNode;
    		nodes[i + 1] = currentNode;
    	}
    	
        // Create the path matrices
    	int[] pathSrc = new int[nodeCount];
    	int[] pathDest = new int[nodeCount];
    	int pathCount; 
    	int[][] path = new int[2 * (includingSetCnt + 2)][nodeCount];
    	
    	
        // Crate the second-stage adjacent matrix and initialization
    	int[][] adjMatSecond = new int[includingSetCnt + 2][includingSetCnt + 2];
    	for (int i = 0; i < (includingSetCnt + 2); i++) {
    		for (int j = 0; j < (includingSetCnt + 2); j++) {
    			adjMatSecond[i][j] = MAXCOST;
    		}
    	} 	
    	
    	// Find SPTs for all nodes in including set
    	for (int count = 0; count < (includingSetCnt + 2); count++) {
    		// Reset the path tables and pathCount
    		for(int i = 0; i < nodeCount; i++) {
    			pathSrc[i] = -1;
    			pathDest[i] = -1;
    		}
    		pathCount = 0;
    		
    		// Find the SPT
        	// Declare the distance vector
    		int SPTSrc = nodes[count];
    		int oldDisVec[] = new int[nodeCount];
        	int finDisVec[] = new int[nodeCount];
        	// The output of distance vector   		
    		shortestPathTree(pathSrc, pathDest, oldDisVec, finDisVec, pathCount, adjMat, nodes, SPTSrc);
    		for(int i = 0; i < (includingSetCnt + 2); i++) {
    			if(finDisVec[nodes[i]] != 0) {
    				adjMatSecond[count][i] = finDisVec[nodes[i]];
    			} else {
    				adjMatSecond[count][i] = MAXCOST;
    			}
    		}
    	}
    	
    	//find out the hamilton path of S_adjMat[i][j]
    	int[] HamiltonPath = new int[includingSetCnt + 2];
    	for(int i = 0; i < (includingSetCnt + 2); i++)
    		HamiltonPath[i] = i;
    	
    	if(adjMatSecond[HamiltonPath[includingSetCnt]][HamiltonPath[includingSetCnt + 1]] != MAXCOST)
    	{
    		System.out.print(pathDet(path,HamiltonPath,nodes));
    		System.out.print('\n');
    	}
    	else
    	{
    		System.out.print("No such path");
    		System.out.print('\n');
    	}
    	StringBuilder a = new StringBuilder();
    	
    	
    	return a.toString();
    }
    
    public static void storeGraphContent(String graphContent) {
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		PathKey key=new PathKey(Integer.parseInt(edgeSplit[PATHSRC]), Integer.parseInt(edgeSplit[PATHDEST]));
    		if (edgesInfo.containsKey(key)) {
    			if (edgesInfo.get(key)[STORECOST] <= Integer.parseInt(edgeSplit[COST])) {
    				continue; // Skip if the old path has smaller cost
    			}
    		}
    		edgesInfo.put(key, new Integer[]{Integer.parseInt(edgeSplit[PATHID]),Integer.parseInt(edgeSplit[COST])});
    	}
    }    
    
    /**
     * Function to find SPT
     * @param pathSrc
     * @param pathDest
     * @param oldDisVec
     * @param finDisVec
     * @param pathCount
     * @param adjMat
     * @param nodes
     * @param src
     */
    private static void shortestPathTree(int[] pathSrc, int[] pathDest, int[] oldDisVec, int[] finDisVec, int pathCount, int[][] adjMat, int[] nodes, int src)
    {
    	// Temporary array to store existing node
    	int[] exist = new int[nodeCount];
    	
    	// Initial the distance vector and existing node
    	for(int i = 0; i < nodeCount; i++) {
    		oldDisVec[i]=adjMat[src][i];
    		finDisVec[i]=oldDisVec[i];
    		
    		exist[i]=-1;
    	}
    	finDisVec[src]=0;
    	
    	// Initialization on existing node
    	int existCnt=0;
    	exist[existCnt] = src;
    	
    	// Generate the shortest path tree
    	while(ifNoNodeRemain(oldDisVec))
    	{
    		// Find the minimum value
    		int minIndex = 0;
    		int minValue = MAXCOST;
    		for(int i = 0; i < nodeCount;i++)
    		{
    			if(oldDisVec[i]<minValue)
    			{
    				minValue=oldDisVec[i];
    				minIndex=i;
    			}
    		}
    		
    		// Add the destination node to existing index
    		existCnt++;
    		exist[existCnt] = minIndex;
    		
    		// Path decision
    		// Path destination
    		int dest=minIndex;
    		// Path source
    		for (int node : exist) {
    			for (int i = 0; i < nodeCount; i++) {
    				if (adjMat[i][minIndex] != MAXCOST) {
    					if (node == i) {
    						if (finDisVec[minIndex] == finDisVec[node] + adjMat[node][dest]) {
    							src=node;
    						}
    					}
    				}
    			}
    		}
    		pathSrc[pathCount]=src;
    		pathDest[pathCount]=dest;
    		pathCount++;
    		
    		// Update distance vector
    		if (ifMinIsIncSet(nodes,minIndex)) {
    			// Set the including set node as unreachable
    			oldDisVec[minIndex] = MAXCOST;
    		} else {
    			for (int i = 0; i < nodeCount; i++)
    				oldDisVec[i] = (int) Math.min(oldDisVec[i], oldDisVec[minIndex] + adjMat[minIndex][i]);
    		}

    		// Existing index unreachable
    		for(int i = 0; i < exist.length; i++) {
    			if (exist[i] != -1) oldDisVec[exist[i]] = MAXCOST;
    		}

    		// Update final distance vector
    		for (int i = 0; i < nodeCount; i++) {
    			finDisVec[i] = (int) Math.min(finDisVec[i], oldDisVec[i]);
    		}
    	}
    }
    

    private static boolean ifMinIsIncSet(int[] nodes, int minIndex) {
    	for(int node : nodes) {
    		if (node == minIndex) return true;
    	}
    	return false;
    }
    
    /**
     * Check if there is node not covered in the generation of SPT
     * @param oldDisVec
     * @return
     */
    private static boolean ifNoNodeRemain(int[] oldDisVec) {
    	for (int node : oldDisVec) {
    		if (node != MAXCOST) return true;
    	}
    	return false;
    }
    
    //considering how the including set should be arranged
    private static void showPath(short[] pathSou,short[] pathDes,short[] finDisVec,short[] allNodeSet)
    {
		System.out.print("Sou:");
    	for(short i=0;i<pathSou.length;i++)
			System.out.printf("%5d ",pathSou[i]);
		System.out.print('\n');
		System.out.print("Des:");
		for(short i=0;i<pathSou.length;i++)
			System.out.printf("%5d ",pathDes[i]);
		System.out.print('\n');
		System.out.print("Cos:");
		for(short i=0;i<finDisVec.length;i++)
			System.out.printf("%5d ", finDisVec[i]);
		System.out.print("\n\n");
		
		short trueCount=0;
		for(short i=0;i<allNodeSet.length;i++)
		{
			for(short j=0;j<pathDes.length;j++)
			{
				if(allNodeSet[i]==pathDes[j] && allNodeSet[i]!=pathSou[0])
				{
					trueCount++;
				}				
			}
		}
		if(trueCount==allNodeSet.length-1)
		{
			System.out.print("can reach all\n");
		}
		else
		{
			System.out.print("can't reach all\n");
		}
    }

    public static int searchPathID(Integer src, Integer dest) {
    	PathKey key1=new PathKey(src,dest);
    	if (edgesInfo.containsKey(key1)) {
    		return edgesInfo.get(key1)[0]; // return PathID
    	}
    	return 10000; // exception
    }
    
    //find out the hamilton path of the question
    private static int modPath(int[] HamiltonPath, int[][] S_adjMat )
    {
    	boolean flag=true;
    	int cost=0;
    	while(flag)
    	{
    		flag=false;
    		for(short n=0;n<HamiltonPath.length-3;n++)
    		{
    			for(short m=(short) (n+2);m<HamiltonPath.length-2;m++)
    			{
    				if(S_adjMat[HamiltonPath[n]][HamiltonPath[m]]+S_adjMat[HamiltonPath[n+1]][HamiltonPath[m+1]]
    						<S_adjMat[HamiltonPath[n]][HamiltonPath[n+1]]+S_adjMat[HamiltonPath[m]][HamiltonPath[m+1]])
    				{
    					flag=true;
    					int[] sample = new int[HamiltonPath.length];
    					for(short i=0;i<HamiltonPath.length;i++)
    						sample[i]=HamiltonPath[i];
    					for(short i=(short) (n+1);i<m+1;i++)
    					{
    						HamiltonPath[i]=sample[m-i+n+1];
    					}   					
    				}
    			}
    		}	
    	}
    	for(short i=0;i<HamiltonPath.length-1;i++)
    		cost+=S_adjMat[HamiltonPath[i]][HamiltonPath[i+1]];
    	//visualize the final hamilton path
    	System.out.print("Hamilt:");
    	for(short i=0;i<HamiltonPath.length;i++)
    		System.out.printf("%3d", HamiltonPath[i]);
    	System.out.print('\n');
    	return cost;
    	
    }
    
    private static String pathDet(int[][] path,int[] HamiltonPath, int[] P_node)
    {
    	String output="";
    	for (short hamt=0;hamt<HamiltonPath.length-1;hamt++) {
    		int sorIndex=HamiltonPath[hamt];
    		int desIndex=HamiltonPath[hamt+1];
    		boolean flag=true;
    		int desValue = P_node[desIndex];
    		String suboutput="";
    		while(flag)
    		{	
    			for(short i=0;i<path[0].length;i++)
    			{
    				if(path[2*sorIndex+1][i]==desValue)
    				{
    					System.out.printf("Sou: %3d\n",path[2*sorIndex][i]);
    					System.out.printf("Des: %3d\n",desValue);
    					suboutput = path[2*sorIndex][i] + suboutput;
    					suboutput = " s:" + suboutput;
    					desValue=path[2*sorIndex][i];
    					int LID = searchPathID((int)path[2*sorIndex][i],(int)path[2*sorIndex+1][i]);
    					suboutput = LID+suboutput; 
    					if(desValue==P_node[sorIndex])
    						flag=false;
    					else
    						suboutput = '|'+suboutput;
    				}
    			}
    		}
    		output+=suboutput;
    		if(hamt!=HamiltonPath.length-2)
    			output+='|';
    	}
    	return output;
    }
    
    
}

class PathKey {
    public int pathSrc;
    public int pathDes;

    public PathKey(int src, int des) {
        this.pathSrc = src;
        this.pathDes = des;
    }

    public boolean equals(Object object) {
        if (!(object instanceof PathKey)) return false;

        PathKey otherKey = (PathKey) object;
        return (this.pathSrc == otherKey.pathSrc) && (this.pathDes == otherKey.pathDes);
    }

    public int hashCode() {
    	int result = 0; // This can be any prime number
    	result = result + pathSrc;
    	result = 600 * result + pathDes;
    	return result;
    }
}
