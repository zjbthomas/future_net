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
    	
    	// Create the adjacent matrices
    	int[][] adjMat= new int[nodeCount][nodeCount];
    	
    	// Initialize the value of the adjacent matrices to MAXCOST and -1
    	for (int i = 0; i < nodeCount; i++) {
    		for (int j = 0; j < nodeCount; j++) {
    			adjMat[i][j] = MAXCOST;
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
    	
    	/*
    	 * for testing
    	 * remember to delete
    	 */
    	System.out.print("adjMatSecond is \n");
    	for(int i=0;i<adjMatSecond.length;i++){
    		for(int j=0;j<adjMatSecond.length;j++){
    			System.out.printf("%6d", adjMatSecond[i][j]);
    		}
    		System.out.print('\n');
    	}
    	
    	int[] HamiltonPath = new int[includingSetCnt + 2];
    	boolean findAPath = false;
    	findAPath=modPath(HamiltonPath,adjMatSecond);
    	/*
    	 * for testing
    	 * remember to delete
    	 */
    	System.out.print("HamiltonPath is \n");
    	for(int i=0;i<HamiltonPath.length;i++){
    		System.out.printf("%3d",HamiltonPath[i]);
    	}
    	System.out.print('\n');
    	System.out.print("All nodes are \n");
    	for(int i=0;i<coreNodes.length;i++){
    		System.out.printf("%3d",coreNodes[i]);
    	}
    	System.out.print('\n');
    	/*
    	 * 
    	 */
    	
    	if(findAPath) {
    		return findPath(path,HamiltonPath,coreNodes,adjMat);
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
    					if (node == i && ifNodeIsNotIncExcSou(node,coreNodes,src)) {
    						if (finDisVec[minIndex] == finDisVec[node] + adjMat[node][dest]) {
    							pathSrc[pathCount]=node;
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
    

    private static boolean ifNodeIsNotIncExcSou(int node,int[] coreNodes,int src)
    {
    	boolean result = true;
    	if(node != src){
    		for(int i=1;i<coreNodes.length-1;i++){
    			if(node == coreNodes[i]){
    				result = false;
    			}
    		}    			
    	}
    	return result;    		
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
     * find out the hamilton path
     * @param HamiltonPath
     * @param adjMatSecond
     */
    private static boolean modPath(int[] HamiltonPath, int[][] adjMatSecond ){
    	boolean findAPath = false;
    	
    	int nodeCountSecond = HamiltonPath.length;   	

    	//cost session
    	int oldCost = MAXCOST;
    	
    	//matrix to store the interim result
    	int[][] adjMatSecondTemp = new int[nodeCountSecond][nodeCountSecond];
    	for(int i=0;i<nodeCountSecond;i++){
    		for(int j=0;j<nodeCountSecond;j++){
    			adjMatSecondTemp[i][j] = MAXCOST;
    		}
    	}
    	   	
    	//loop variable session
    	int lastNode = nodeCountSecond-2;
    	int sou = 0;
    	boolean loopBack = false;
    	boolean loopBackLast = false;
    	int[] endNode = new int[nodeCountSecond-2];
    	for(int i =0;i<nodeCountSecond-2;i++){
    		endNode[i]=-1;
    	}
    	
    	//exiPath session
    	int[] exiPath = new int[nodeCountSecond];
    	for(int i=0;i<nodeCountSecond;i++)
    		exiPath[i]=-1;
    	exiPath[0] = sou;
    	exiPath[nodeCountSecond-1]=nodeCountSecond-1;
    	int exiPathCou = 0;
    	
    	//while loop to search through all path
    	while(exiPathCou >= 0){
    		//while loop to search through a path
        	while(ifNoOutOrReaLastNode(adjMatSecondTemp,adjMatSecond,exiPath,sou,exiPathCou,lastNode,endNode,loopBack )){         		    			
   					
    			//find out one of the possible node
    			int posIndex = 0;
    			innerloop:
    			for(int i=0;i<nodeCountSecond;i++){
    				if(adjMatSecondTemp[exiPathCou][i] != MAXCOST){   					
    					posIndex = i;
    					break innerloop; 
    				}
    			}

    			//add the chosen node the the path
    			exiPathCou++;
    			exiPath[exiPathCou] = posIndex;
    			
    			//change the source for the next loop
    			sou = posIndex;
    			//initial the loop back
    			loopBack = false;
    			loopBackLast = false;
    			for(int i=0;i<nodeCountSecond-2;i++){
    				endNode[i] = -1;
    			}
    			
    		}         	
        	
    		if(adjMatSecond[sou][nodeCountSecond-1]!=MAXCOST && exiPathCou == lastNode){
    			//calculate the new cost
    			int newCost = 0;
    			for(int i=0;i<nodeCountSecond-1;i++){
    				newCost+=adjMatSecond[exiPath[i]][exiPath[i+1]];
    			}
    			//find and record the better Hamilton Path
    			if(newCost < oldCost){
    				for(int j=0;j<nodeCountSecond;j++){
    					HamiltonPath[j]=exiPath[j];
    				}
    				oldCost = newCost;
    			}
    			//indicate find a path
    			findAPath = true;
    			
    			//loop back procedure
    			loop1:
    			for(int i=0;i<nodeCountSecond-2;i++){
    				if(endNode[i] == -1){
    					endNode[i] = exiPath[exiPathCou];
    					break loop1;
    				}
    			}    			
    			loopBack = true;
    			loopBackLast = true;
    			exiPath[exiPathCou] = -1;
    			exiPathCou--;
    			sou=exiPath[exiPathCou];   	    	
    		}else{
    			//loop back procedure
    			if(loopBackLast)
    			{
    				for(int i=0;i<nodeCountSecond-2;i++){
    					endNode[i]=-1;
        			} 
    			}
    			loop2:
    			for(int i=0;i<nodeCountSecond-2;i++){
    				if(endNode[i] == -1){
    					endNode[i] = exiPath[exiPathCou];
    					break loop2;
    				}
    			}    			
    			loopBack = true;
    			loopBackLast = true;
    			exiPath[exiPathCou]=-1;
    			exiPathCou--;
    			if(exiPathCou >=0){
    				sou=exiPath[exiPathCou];
    			}
    			
    		}
    	} 
    	return findAPath;
    }
    
    private static boolean ifNoOutOrReaLastNode(int[][] adjMatSecondTemp, int[][] adjMatSecond, int[] exiPath, int sou, int exiPathCou, int lastNode, int[] endNode,boolean loopBack ){
    	
    	
    	boolean result = false;
    	int nodeCountSecond = exiPath.length;
    	//store the current distance vector
		if(!loopBack){
			for(int i=0;i<nodeCountSecond;i++){
				adjMatSecondTemp[exiPathCou][i]=adjMatSecond[sou][i];
			}	    	
			//exist nodes are unreachable
			for(int i=0;i<nodeCountSecond;i++){
				if(exiPath[i] != -1){
					adjMatSecondTemp[exiPathCou][exiPath[i]] = MAXCOST;
				}
			}
		}else{
			//end node is unreachable
			for(int i=0;i<endNode.length;i++){
				if(endNode[i]!=-1){
					adjMatSecondTemp[exiPathCou][endNode[i]] = MAXCOST;
				}
				
			}
		}
    	outerloop:
		for(int i=0;i<adjMatSecondTemp.length;i++){
    		if(adjMatSecondTemp[exiPathCou][i] != MAXCOST && exiPathCou!= lastNode){
    			result = true;
    			break outerloop;
    		}
    	}
		//erase the endNode as a source record
		if(!result && exiPathCou!= lastNode){
			for(int i=0;i<nodeCountSecond;i++){
				adjMatSecondTemp[exiPathCou][i]=MAXCOST;
			}
		}
    	return result;
    }
          
    /**
     * find out the path according to the Hamilton Path
     * @param path
     * @param HamiltonPath
     * @param coreNodes
     * @param adjMat
     */
    private static String findPath(int[][] path,int[] HamiltonPath, int[] coreNodes, int[][] adjMat) {
    	int cost = 0;
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
    					cost+=adjMat[path[2 * src][j]][path[2 * src + 1][j]];
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
    	System.out.printf("Cost: %3d\n", cost);
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
