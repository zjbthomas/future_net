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
     * @author JindDeBuXing
     * @since 2016-3-4
     * @version V1
     */
	
	/** Maximum cost estimation */
	private static final int MAXCOST = 12000;
	
	/** Store the position of information of paths */
	private static final int PATHID = 0;
	private static final int PATHSRC = 1;
	private static final int PATHDEST = 2;
	private static final int COST = 3;
	private static final int HASHMAPPATHID = 0;
	private static final int HASHMAPCOST = 1;
	private static final int INPUTCNT = 0;
	private static final int INPUTCOST = 1;
	
	/** Store the information of the topology */
	private static HashMap<Integer, Integer[]> srcDests = new HashMap<Integer, Integer[]>();
	private static HashMap<PathKey, Integer[]> edgesInfo = new HashMap<PathKey, Integer[]>();
	/** Store the information of the demand */
	private static int source;
	private static int destination;
	private static int[] includingSet;
	private static ArrayList<Integer> coreNodes = new ArrayList<Integer>();
	/** Global variables */
	private static ArrayList<Integer> basePath;
	private static int baseCost;
	
	/**
	 * The entry point of the route search function
	 * @param graphContent
	 * @param condition
	 * @return
	 */
    public static String searchRoute(String graphContent, String condition)
    {
    	// Pre-handle of graphContent
    	HashMap<Integer, ArrayList<Integer>> srcDestsTemp = new HashMap<Integer, ArrayList<Integer>>();
    	HashMap<Integer, ArrayList<Integer>> srcDestsCost = new HashMap<Integer, ArrayList<Integer>>();
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		int pathId = Integer.parseInt(edgeSplit[PATHID]);
    		int pathSrc = Integer.parseInt(edgeSplit[PATHSRC]);
    		int pathDest = Integer.parseInt(edgeSplit[PATHDEST]);
    		int cost = Integer.parseInt(edgeSplit[COST]);
    		
    		// One node to all its destinations
    		ArrayList<Integer> dests;
    		ArrayList<Integer> costs;
    		if (srcDestsTemp.containsKey(pathSrc)) {
    			dests = srcDestsTemp.get(pathSrc);
    			costs = srcDestsCost.get(pathSrc);
    		} else {
    			dests = new ArrayList<Integer>();
    			costs = new ArrayList<Integer>();
    		}
    		dests.add(pathDest);
    		srcDestsTemp.put(pathSrc, dests);
    		costs.add(cost);
    		srcDestsCost.put(pathSrc, costs);
    		
    		// Convert the information into a HashMap
    		PathKey key=new PathKey(pathSrc, pathDest);
    		if (edgesInfo.containsKey(key)) {
    			if (edgesInfo.get(key)[HASHMAPPATHID] <= cost) {
    				continue; // Skip if the old path has smaller cost
    			}
    		}
    		edgesInfo.put(key, new Integer[]{pathId,cost});
    	}
    	
    	// Sort the source-to-destinations HashMap
    	for (Entry<Integer, ArrayList<Integer>> kvp : srcDestsTemp.entrySet()) {
    		int src = kvp.getKey();
    		Integer[] dests = (Integer[]) kvp.getValue().toArray(new Integer[0]);
    		Integer[] costs = (Integer[]) srcDestsCost.get(src).toArray(new Integer[0]);
    		
    		// Quicksort
    		quickSort(costs, dests, 0, costs.length - 1);
    		
    		srcDests.put(src, dests);
    	}

    	// Get the information of source, destination and including set
    	String[] conditionSplit = condition.split(",|\n");
    	
    	source = Integer.parseInt(conditionSplit[0]);
    	destination = Integer.parseInt(conditionSplit[1]);
    	
    	String[] splitIncludingSet = conditionSplit[2].split("\\|");
    	int includingSetCnt = splitIncludingSet.length;
    	includingSet = new int[includingSetCnt];

    	coreNodes.add(source); // Store the source of demand
    	
    	for (int i = 0; i < includingSetCnt; i++) {
    		int currentNode = Integer.parseInt(splitIncludingSet[i]);
    		includingSet[i] = currentNode;
    		coreNodes.add(currentNode);
    	}
    	
		basePath = new ArrayList<Integer>();
		basePath.add(source);
		baseCost = MAXCOST;
		int[] input = new int[]{1, 0};
		ArrayList<Integer> uselessNodes = new ArrayList<Integer>();
		
		if (findNextCoreNode(source, basePath, input, uselessNodes)) {
			baseCost = input[INPUTCOST];
			boolean flag = true;
			do {
				ArrayList<Integer> path = new ArrayList<Integer>();
				path.add(source);
				input = new int[]{1, 0};
				uselessNodes = new ArrayList<Integer>();
				if (findNextCoreNode(source, path, input, uselessNodes)) {
					basePath = path;
					baseCost = input[INPUTCOST];
				} else {
					flag = false;
				}
			} while (flag);
			return pathOutput(basePath);
		} else {
			return "NA";
		}
    }
    
    public static void quickSort(Integer[] arr, Integer[] ret, int low, int high) {
    	int l = low;
    	int h = high;
    	int povit = arr[low];
    	
    	while (l < h) {
    		while (l < h && arr[h] >= povit) h--;
    		if (l < h) {
    			int temp = arr[h];
    			arr[h] = arr[l];
    			arr[l] = temp;
    			
    			temp = ret[h];
    			ret[h] = ret[l];
    			ret[l] = temp;
    			
    			l++;
    		}
    		
    		while (l < h && arr[l] <= povit) l++;
    		
    		if (l < h) {
    			int temp = arr[h];
    			arr[h] = arr[l];
    			arr[l] = temp;
    			
    			temp = ret[h];
    			ret[h] = ret[l];
    			ret[l] = temp;
    			
    			h--;
    		}
    	}
    	
    	if (l > low) quickSort(arr, ret, low, l - 1);
    	if (h < high) quickSort(arr, ret, l + 1, high);
    }
    
    public static boolean findNextCoreNode(int src, ArrayList<Integer> lastPath, int[] input, ArrayList<Integer> lastUseless) {
    	Integer[] dests = srcDests.get(src);
    	int[] tempInput = new int[]{input[INPUTCNT], input[INPUTCOST]};
    	ArrayList<Integer> tempUseless = new ArrayList<Integer>(lastUseless);
    	
    	if (dests == null) {
    		return false;
    	}
    	
    	if (input[INPUTCNT] == coreNodes.size()) {
    		for (int node : dests) {
    			if (node == destination) {
    				int pathCost = searchPathCost(src, destination);
    				if (input[INPUTCOST] + pathCost < baseCost) {
    					lastPath.add(destination);
    					input[INPUTCOST] += pathCost;
        				return true;
    				}
    			} else {
    				if (!lastPath.contains(node) && !lastUseless.contains(node)) {
    					int pathCost = searchPathCost(src, node);
    					int newCost = input[INPUTCOST] + pathCost;
    					if (newCost < baseCost) {
    						lastPath.add(node);
    						tempInput[INPUTCOST] = newCost;
        					if (findNextCoreNode(node, lastPath, tempInput, tempUseless)) {
        						input[INPUTCOST] = tempInput[INPUTCOST];
        						return true;
        					} else {
        						lastPath.remove(lastPath.size() - 1);
        						tempInput[INPUTCOST] -= pathCost;
        					}
    					}
    				}
    			}
    		}
    		if (!coreNodes.contains(src)) {
    			lastUseless.add(src);
    		}
    		return false;
    	} else {
    		// First, find a core node
    		for (int node : dests) {
    			if ((!lastPath.contains(node)) && (node != destination) && !lastUseless.contains(node)) {
    				if (coreNodes.contains(node)) {
    					int pathCost = searchPathCost(src, node);
    					int newCost = input[INPUTCOST] + pathCost;
    					if (newCost < baseCost) {
    						lastPath.add(node);
    						tempInput[INPUTCNT]++;
        					tempInput[INPUTCOST] = newCost;
        					if (findNextCoreNode(node, lastPath, tempInput, tempUseless)) {
        						input[INPUTCNT] = tempInput[INPUTCNT];
        						input[INPUTCOST] = tempInput[INPUTCOST];
        						return true;
        					} else {
        						lastPath.remove(lastPath.size() - 1);
        						tempInput[INPUTCNT]--;
        						tempInput[INPUTCOST] -= pathCost;
        					}
    					}
    				}
    			}
    		}
    		// If no core node, expand from the first node (with less weight)
    		for (int node : dests) {
    			if (!lastPath.contains(node) && (node != destination) && !lastUseless.contains(node)) {
    				if (!coreNodes.contains(node)) {
    					int pathCost = searchPathCost(src, node);
    					int newCost = input[INPUTCOST] + pathCost;
    					if (newCost < baseCost) {
    						lastPath.add(node);
    						tempInput[INPUTCOST] = newCost;
            				if (findNextCoreNode(node, lastPath, tempInput, tempUseless)) {
            					input[INPUTCNT] = tempInput[INPUTCNT];
            					input[INPUTCOST] = tempInput[INPUTCOST];
            					return true;
            				} else {
            					lastPath.remove(lastPath.size() - 1);
            					tempInput[INPUTCOST] -= pathCost;
            				}
    					}
    				}
    			}
    		}
    		if (!coreNodes.contains(src)) {
    			lastUseless.add(src);
    		}
    		return false;
    	}
    }
    
    public static String pathOutput(ArrayList<Integer> path) {
    	String ret = "";
		for (int i = 0; i < path.size() - 1; i++) {
			ret += searchPathID(path.get(i), path.get(i + 1));
			if (i != path.size() - 2) {
				ret += '|';
			}
		}
		return ret;
    }
    
    public static int searchPathCost(int src, int dest) {
    	PathKey key = new PathKey(src, dest);
    	return edgesInfo.get(key)[HASHMAPCOST];
    }
    
    public static int searchPathID(int src, int dest) {
    	PathKey key = new PathKey(src, dest);
    	return edgesInfo.get(key)[HASHMAPPATHID];
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
