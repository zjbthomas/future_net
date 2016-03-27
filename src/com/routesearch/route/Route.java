/**
 * Implementation
 * 
 * @author JindDeBuXing
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.HashMap;

public final class Route
{
    /**
     * Class for routing
     * 
     * @author JindDeBuXing
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
	private static final int LISTPATHSRC = 0;
	private static final int LISTPATHDEST = 1;
	private static final int LISTCOST = 2;
	private static final int HASHMAPPATHID = 0;
	private static final int HASHMAPCOST = 1;
	
	/** Store the information of the topology */
	private static HashMap<PathKey, Integer[]> edgesInfo = new HashMap<PathKey, Integer[]>();
	private static int nodeCount = 0;
	
	/**
	 * The entry point of the route search function
	 * @param graphContent
	 * @param condition
	 * @return
	 */
    public static String searchRoute(String graphContent, String condition)
    {
    	// Pre-handle of graphContent
    	ArrayList<Integer[]> edges = new ArrayList<Integer[]>();
    	ArrayList<Integer> nodes = new ArrayList<Integer>();
    	for (String edge : graphContent.split("\n")) {
    		// Store the information of graphContent into an ArrayList
    		String[] edgeSplit = edge.split(",");
    		int pathId = Integer.parseInt(edgeSplit[PATHID]);
    		int pathSrc = Integer.parseInt(edgeSplit[PATHSRC]);
    		int pathDest = Integer.parseInt(edgeSplit[PATHDEST]);
    		int cost = Integer.parseInt(edgeSplit[COST]);
    		
    		edges.add(new Integer[]{pathSrc, pathDest, cost});
    		
    		// Store the node
    		if (!nodes.contains(pathSrc)) {
    			nodes.add(pathSrc);
    		}
    		
    		if (!nodes.contains(pathDest)) {
    			nodes.add(pathDest);
    		}
    		
    		// Convert the information into a HashMap
    		PathKey key=new PathKey(pathSrc, pathDest);
    		if (edgesInfo.containsKey(key)) {
    			if (edgesInfo.get(key)[HASHMAPPATHID] <= cost) {
    				continue; // Skip if the old path has smaller cost
    			}
    		}
    		edgesInfo.put(key, new Integer[]{pathId,cost});
    	}
    	
    	// Find the total number of nodes
    	nodeCount = nodes.size();
    	
    	// Create the adjacent matrix
    	int[][] adjMat= new int[nodeCount][nodeCount];
    	
    	// Initialize the value of the adjacent matrix to MAXCOST
    	for (int i = 0; i < nodeCount; i++) {
    		for (int j = 0; j < nodeCount; j++) {
    			adjMat[i][j] = MAXCOST;
    		}
    	}

    	// Initialize the value of two adjacent matrices according to the topology
    	for (Integer[] edge : edges) {
    		if (edge[LISTCOST] < adjMat[edge[LISTPATHSRC]][edge[LISTPATHDEST]]) {
    			adjMat[edge[LISTPATHSRC]][edge[LISTPATHDEST]] = edge[LISTCOST];
    		}
    	}

    	// Get the information of source, destination and including set
    	String[] conditionSplit = condition.split(",|\n");
    	
    	int source = Integer.parseInt(conditionSplit[0]);
    	int destination = Integer.parseInt(conditionSplit[1]);
    	
    	String[] splitIncludingSet = conditionSplit[2].split("\\|");
    	int includingSetCnt = splitIncludingSet.length;
    	int[] includingSet = new int[includingSetCnt];
    	
    	int[] coreNodes = new int[includingSetCnt + 2];
    	coreNodes[0] = source;
    	coreNodes[includingSetCnt + 1] = destination;
    	
    	for (int i = 0; i < includingSetCnt; i++) {
    		int currentNode = Integer.parseInt(splitIncludingSet[i]);
    		includingSet[i] = currentNode;
    		coreNodes[i + 1] = currentNode;
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
    		int SPTSrc = coreNodes[count];
    		int oldDisVec[] = new int[nodeCount];
        	int finDisVec[] = new int[nodeCount];
        	// The output of distance vector   		
    		shortestPathTree(pathSrc, pathDest, oldDisVec, finDisVec, pathCount, adjMat, coreNodes, SPTSrc);
    		for(int i = 0; i < (includingSetCnt + 2); i++) {
    			if(finDisVec[coreNodes[i]] != 0) {
    				adjMatSecond[count][i] = finDisVec[coreNodes[i]];
    			} else {
    				adjMatSecond[count][i] = MAXCOST;
    			}
    		}
    		
    		for(int i = 0; i < nodeCount; i++) {
    			path[2 * count][i]=pathSrc[i];
    			path[2 * count + 1][i]=pathDest[i];
    		}
    	}
    	
    	// Find out the hamilton path and output the results
    	int[] HamiltonPath = new int[includingSetCnt + 2];
    	HamiltonPath[0] = 0;
    	HamiltonPath[includingSetCnt + 1] = includingSetCnt + 1;
    	for(int i = 1; i < (includingSetCnt + 1); i++)
    		HamiltonPath[i] = includingSetCnt + 1 - i;
    	
    	findHamiltonPath(HamiltonPath, adjMatSecond);
    	
    	if(validateHamiltonPath(HamiltonPath, adjMatSecond)) {
    		return findPath(path, HamiltonPath, coreNodes);
    	} else {
    		return "NA";
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
     * @param coreNodes
     * @param src
     */
    private static void shortestPathTree(int[] pathSrc, int[] pathDest, int[] oldDisVec, int[] finDisVec, int pathCount, int[][] adjMat, int[] coreNodes, int src)
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
    					if (node == i && ifNodeIsNotCoreOrSrc(node, coreNodes, src)) {
    						if (finDisVec[minIndex] == finDisVec[node] + adjMat[node][dest]) {
    							pathSrc[pathCount] = node;
    						}
    					}
    				}
    			}
    		}
    		pathDest[pathCount]=dest;
    		pathCount++;
    		
    		// Update distance vector
    		if (!ifMinIsIncSet(coreNodes,minIndex)) {
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
    
    private static boolean ifNodeIsNotCoreOrSrc(int node, int[] coreNodes, int src) {
    	if (node != src) {
    		for (int i = 1; i < coreNodes.length - 1; i++) {
    			if (node == coreNodes[i]) {
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    private static boolean ifMinIsIncSet(int[] coreNodes, int minIndex) {
    	for(int node : coreNodes) {
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
    
    /**
     * Find out the hamilton path
     * @param HamiltonPath
     * @param adjMatSecond
     */
    private static void findHamiltonPath(int[] HamiltonPath, int[][] adjMatSecond) {
    	boolean flag = false;
    	while(flag) {
    		for (int n = 0; n < HamiltonPath.length - 3; n++) {
    			for (int m = n + 2; m < HamiltonPath.length - 2; m++) {
    				if (adjMatSecond[HamiltonPath[n]][HamiltonPath[m]] + adjMatSecond[HamiltonPath[n + 1]][HamiltonPath[m + 1]] < 
    						adjMatSecond[HamiltonPath[n]][HamiltonPath[n + 1]] + adjMatSecond[HamiltonPath[m]][HamiltonPath[m + 1]]) {
    					flag = true;
    					int[] sample = new int[HamiltonPath.length];
    					for (int i = 0; i < HamiltonPath.length; i++) {
    						sample[i] = HamiltonPath[i];
    					}
    					for (int i = (n + 1); i < m + 1; i++) {
    						HamiltonPath[i] = sample[m - i + n + 1];
    					}
    				}
    			}
    		}
    	}
    }
    
    /**
     * Determine whether the hamilton path is a valid path
     * @param HamiltonPath
     * @param adjMatSecond
     * @return
     */
    private static boolean validateHamiltonPath(int[] HamiltonPath, int[][] adjMatSecond) {
    	for (int i = 0; i < HamiltonPath.length - 1; i++) {
    		if (adjMatSecond[HamiltonPath[i]][HamiltonPath[i + 1]] == MAXCOST) {
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Find out the path according to the Hamilton Path
     * @param path
     * @param HamiltonPath
     * @param coreNodes
     * @return
     */
    private static String findPath(int[][] path,int[] HamiltonPath, int[] coreNodes) {
    	String ret = "";
    	for (int i = 0; i < (HamiltonPath.length - 1); i++) {
    		int src = HamiltonPath[i];
    		int dest = HamiltonPath[i + 1];
    		int destValue = coreNodes[dest];
    		
    		boolean flag = true;
    		String subString = "";
    		while(flag) {	
    			for (int j = 0; j < nodeCount; j++) {
    				if (path[2 * src + 1][j] == destValue) {
    					destValue = path[2 * src][j];
    					int LID = searchPathID(path[2 * src][j],path[2 * src + 1][j]);
    					subString = LID + subString; 
    					if(destValue == coreNodes[src])
    						flag = false;
    					else
    						subString = '|' + subString;
    				}
    			}
    		}
    		ret += subString;
    		if(i != HamiltonPath.length - 2)
    			ret += '|';
    	}
    	return ret;
    }
    
    public static int searchPathID(int src, int dest) {
    	PathKey key = new PathKey(src, dest);
    	if (edgesInfo.containsKey(key)) {
    		return edgesInfo.get(key)[HASHMAPPATHID];
    	}
    	return -1; // Exception
    }
}

class PathKey {
    public int pathSrc;
    public int pathDest;

    public PathKey(int src, int dest) {
        this.pathSrc = src;
        this.pathDest = dest;
    }
    
    @Override
    public boolean equals(Object object) {
        if (! (object instanceof PathKey)) {
            return false;
        }
        PathKey otherKey = (PathKey) object;
        return (this.pathSrc == otherKey.pathSrc) && (this.pathDest == otherKey.pathDest);
    }

    @Override
    public int hashCode() {
    	int result = 0; // any prime number
    	result = result + pathSrc;
    	result = 600 * result + pathDest;
    	return result;
    }
}
