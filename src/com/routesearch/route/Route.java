/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.HashMap;

public final class Route
{
    /**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
	
	// Define constant
	private static final int PATHID = 0;
	private static final int PATHSRC = 1;
	private static final int PATHDEST = 2;
	private static final int COST = 3;
	
	private static HashMap<PathKey, Integer[]> edges1 = new HashMap<PathKey, Integer[]>();
	
    public static String searchRoute(String graphContent, String condition)
    {
    	
    	/*// Handle graphContent
    	HashMap<Integer, Integer[]> edges = new HashMap<Integer, Integer[]>();
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		edges.put(Integer.parseInt(edgeSplit[PATHID]), new Integer[]{Integer.parseInt(edgeSplit[PATHSRC]),
    																	 Integer.parseInt(edgeSplit[PATHDEST]),
    																	 Integer.parseInt(edgeSplit[COST])});
    	}*/

    	// Handle condition
    	String[] conditionSplit = condition.split(",|\n");
    	Integer sourceID = Integer.parseInt(conditionSplit[0]);
    	Integer destinationID = Integer.parseInt(conditionSplit[1]);
    	ArrayList<Integer> includingSet = new ArrayList<Integer>();
    	for (String node : conditionSplit[2].split("\\|")) {
    		includingSet.add(Integer.parseInt(node));
    	}
    	
        return "hello world!";
    }
    
    public static String setrout(String graphContent) {
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		PathKey key1=new PathKey(Integer.parseInt(edgeSplit[PATHSRC]), Integer.parseInt(edgeSplit[PATHDEST]));
    		// Key of PathKey type
    		if (edges1.containsKey(key1)) {
    			if (edges1.get(key1)[1] <= Integer.parseInt(edgeSplit[COST])) {
    				continue; // skip if the old path has smaller cost
    			}
    		}
    		edges1.put(key1, 
    				   new Integer[]{Integer.parseInt(edgeSplit[PATHID]),Integer.parseInt(edgeSplit[COST])});
    		// put new path or update old path (with same src & des)
    	}
    	return "hello";
    }
    
    public static int searchPathID(Integer src, Integer dest) {
    	PathKey key1=new PathKey(src,dest);
    	if (edges1.containsKey(key1)) {
    		return edges1.get(key1)[0]; // return PathID
    	}
    	return 10000; // exception
    }
    
    /*
    public ArrayList<Integer> searchBasePath(ArrayList<Integer[]> edges, ArrayList<Integer> includingSet, Integer src, Integer dest) {
    	ArrayList<Integer> ret = new ArrayList<Integer>(); // Record and return paths
    	ret.add(0); // The first one store the cost
    	
    	for (Integer[] edge : edges) {
    		if (edge[PATHSRC].equals(src)) {
    			if (!nodeSet.contains(edge[PATHDEST])) {
    				if (includingSet.contains(edge[PATHDEST])) {
    					ret.set(0, ret.get(0) + edge[COST]);
    					ret.add(edge[PATHID]);
    				}
    			}
    		}
    	}
    	
    	return ret;
    }
    */
    
    
}

class PathKey { // overwrite the Key element (cxc)
    public final int pathsrc;
    public final int pathdes;

    public PathKey(int src, int des) {
        pathsrc = src;
        pathdes = des;
    }

    @Override
    public boolean equals(Object object) {
        if (! (object instanceof PathKey)) {
            return false;
        }

        PathKey otherKey = (PathKey) object;
        return (this.pathsrc == otherKey.pathsrc) && (this.pathdes == otherKey.pathdes);
    }

    @Override
    public int hashCode() {
    	int result = 0; // any prime number
    	result = result + pathsrc;
    	result =  600 * result + pathdes;
    	return result;
    }
}

