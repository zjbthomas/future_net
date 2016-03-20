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
	
	// Daniel
    public static String searchRoute(String graphContent, String condition)
    {
    	//testing code (Daniel)
    	/*for(short i=0;i<nodeCount;i++)
		System.out.printf("%d ",oldDisVec[i]);*/
    	
    	// find the total node number
    	int nodeCount=0;
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		if (Integer.parseInt(edgeSplit[PATHSRC])>nodeCount)
    			nodeCount=Integer.parseInt(edgeSplit[PATHSRC]);
    		
    		if (Integer.parseInt(edgeSplit[PATHDEST])>nodeCount)
    			nodeCount=Integer.parseInt(edgeSplit[PATHDEST]);
    	}
    	nodeCount++;
    	System.out.printf("No.of node is %d\n", nodeCount);
    	
    	//initial the adjacent matrix
    	short[][] adjMat= new short[nodeCount][nodeCount];
        short[][] adjMatLID=new short[nodeCount][nodeCount];
    	//adjMatIni(adjMat,adjMatLID);
    	
    	// cxc 13:41 (substitute of adjMatIni())
        //initiate adjMat
    	for(short i=0;i<adjMat.length;i++)
    		{ for(short j=0;j<adjMat.length;j++)
    			adjMat[i][j]=13000;}
    	
    	//initial adjMatLID
    	for(short i=0;i<adjMatLID.length;i++)
    		{ for(short j=0;j<adjMatLID.length;j++)
    			adjMatLID[i][j]=-1;}
    	
    	for (String edge : graphContent.split("\n")) {
    		String[] edgeSplit = edge.split(",");
    		if (Short.parseShort(edgeSplit[COST])<
    				adjMat[Integer.parseInt(edgeSplit[PATHSRC])][Integer.parseInt(edgeSplit[PATHDEST])])
    	  { adjMat[Integer.parseInt(edgeSplit[PATHSRC])][Integer.parseInt(edgeSplit[PATHDEST])]
    				=Short.parseShort(edgeSplit[COST]);
    		adjMatLID[Integer.parseInt(edgeSplit[PATHSRC])][Integer.parseInt(edgeSplit[PATHDEST])]
    				=Short.parseShort(edgeSplit[PATHID]); }
    	}
    	// end of this part
    	
    	//initial the source, destination, including set 
    	
    	String[] conditionSplit = condition.split(",|\n");
    	short P_souInd=Short.parseShort(conditionSplit[0]);
    	short P_desInd=Short.parseShort(conditionSplit[1]);
    	int P_incSetCou=0;
    	for (String node : conditionSplit[2].split("\\|")) {
    		P_incSetCou++; // calculate number of includingSet Points
    	}
    	short[] P_node = new short[P_incSetCou+2];
    	short[] P_incSet = new short[P_incSetCou];
    	P_node[0]=P_souInd; P_node[P_incSetCou+1]=P_desInd;
    	int indexNode=0;
    	for (String node : conditionSplit[2].split("\\|")) {
    		P_node[indexNode+1]=Short.parseShort(node);
    		P_incSet[indexNode]=Short.parseShort(node);
    		indexNode++;
    	}
    	
    	//initial the source, destination, including set
    	/*short P_souInd=2;
    	short P_desInd=19;
    	short P_incSetCou=6;
    	short[] P_incSet = new short[P_incSetCou];
        P_incSet[0]=3;P_incSet[1]=5;P_incSet[2]=7;P_incSet[3]=11;P_incSet[4]=13;P_incSet[5]=17;
    	short[] P_node = new short[P_incSetCou+2];
    	P_node[0]=P_souInd;P_node[P_incSetCou+1]=P_desInd;
    	for(short i=0;i<P_incSetCou;i++)
    	{
    		P_node[i+1]=P_incSet[i];
    	}*/
    	
        //declare the incidence matrix
    	short[] pathSou = new short[nodeCount];
    	short[] pathDes = new short[nodeCount];
    	short pathCount; 
    	
        //declare the second stage adjacent matrix
    	short[][] S_adjMat = new short[P_incSetCou+2][P_incSetCou+2];
    	for(short i=0;i<S_adjMat.length;i++)
    		for(short j=0;j<S_adjMat.length;j++)
    			S_adjMat[i][j]=13000;
    	
    	//find STP of all including set
    	for(short count=0;count<P_incSetCou+2;count++)
    	{
    		//initial the path table
    		for(short i=0;i<nodeCount;i++)
        		pathSou[i]=-1;
    		for(short i=0;i<nodeCount;i++)
        		pathDes[i]=-1;
    		pathCount = 0;
    		
    		//STP
        		//declare the distance vector
    		short SPT_souInd = P_node[count];
    		short oldDisVec[] = new short[nodeCount];
        	short finDisVec[] = new short[nodeCount];
        		//distance vector output
    		   		
    		shortestPathTree(pathSou,pathDes,oldDisVec,finDisVec,pathCount,adjMat,nodeCount,SPT_souInd);
    		for(short i=0;i<P_incSetCou+2;i++)
    		{
    			if(finDisVec[P_node[i]]!=0)
    			{
    				S_adjMat[count][i]=finDisVec[P_node[i]];
    			}
    			else
    			{
    				S_adjMat[count][i]=13000;
    			}
    		}
    		System.out.printf("No.%d node\n", count+1);
    		nodeOfPathDec(pathSou,pathDes,finDisVec,P_incSet,P_souInd,P_desInd);
    	}
    	System.out.print("Shortest Path Tree for the nodes\n");
    	
        //visualize the Second stage adjacent matrix
    	nodeCount=(short) (P_incSetCou+2);
    	for(short i=0;i<P_incSetCou+2;i++)
    	{
    		for(short j=0;j<P_incSetCou+2;j++)
    		{
    			System.out.printf("%6d ", S_adjMat[i][j]);
    		}
    		System.out.print('\n');
    	}
    	

    	return "hello world!";
    }
    
    private static void shortestPathTree(short[] pathSou,short[] pathDes,short[] oldDisVec, short[] finDisVec, short pathCount,short[][] adjMat,int nodeCount,short souInd)
    {
    	//initial the distance vector
    	for(short i=0;i<nodeCount;i++)
    		oldDisVec[i]=adjMat[souInd][i];
    	for(short i=0;i<nodeCount;i++)
    		finDisVec[i]=oldDisVec[i];
    	finDisVec[souInd]=0;
    	
    	//temporary array to store existing node
    	short[] exiInd = new short[nodeCount];
    	for(short i=0;i<nodeCount;i++)
    		exiInd[i]=-1;
    	short exiIndCou=0;
    	exiInd[exiIndCou] = souInd;
    	
    	//generate the shortest path tree
    	while(ifNoNodeRemain(oldDisVec))
    	{
    		//find the minimum value
    		short minIndex=0;
    		short minValue=13000;
    		for(short i=0;i<nodeCount;i++)
    		{
    			if(oldDisVec[i]<minValue)
    			{
    				minValue=oldDisVec[i];
    				minIndex=i;
    			}
    		}
    		//add the destination node to existing index
    		exiIndCou++;
    		exiInd[exiIndCou]=minIndex;
    		
    		//path decision
    			//path destination
    		short desInd=minIndex;
    			//path source
    		for(short i=0;i<exiInd.length;i++)
    		{
    			for(short j=0;j<nodeCount;j++)
    			{
    				if(adjMat[j][minIndex]!=13000)
    				{
    					if(exiInd[i]==j)
    					{
    						if(finDisVec[minIndex]==finDisVec[exiInd[i]]+adjMat[exiInd[i]][desInd])
    						{
    							souInd=exiInd[i];
    						}
    					}
    				}
    			}
    		}
    		pathSou[pathCount]=souInd;
    		pathDes[pathCount]=desInd;
    		pathCount++;
    		
    		//update distance vector
    		for(short i=0;i<nodeCount;i++)
    			oldDisVec[i]=(short) Math.min(oldDisVec[i], oldDisVec[minIndex]+adjMat[minIndex][i]);
    			//existing index unreachable
    		for(short i=0;i<exiInd.length;i++)
    		{
    			if(exiInd[i]!=-1)
    				oldDisVec[exiInd[i]]=13000;
    		}

    		//update final distance vector
    		for(short i=0;i<nodeCount;i++)
    		{
    			finDisVec[i]=(short) Math.min(finDisVec[i], oldDisVec[i]);
    		}
    		
    	}
    }
    
    //considering the possibility that not all node can be connected
    private static boolean ifNoNodeRemain(short[] oldDisVec)
    {
    	boolean result = false;
    	for(short i=0;i<oldDisVec.length;i++)
    	{
    		if(oldDisVec[i]!=13000)
    		{
    			result= true;
    			break;
    		}
    	}
    	return result;
    }
    
    //considering how the including set should be arranged
    private static void nodeOfPathDec(short[] pathSou,short[] pathDes,short[] finDisVec,short[] P_incSet,short P_souInd, short P_desInd)
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
		
		//all including set receivable
		short[] allNodeSet = new short[P_incSet.length+2];
		allNodeSet[0]=P_souInd;allNodeSet[1]=P_desInd;
		for(short i=0;i<P_incSet.length;i++)
		{
			allNodeSet[i+2]=P_incSet[i];
		}
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
    
    // cxc
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

