/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

public final class Route
{
	/**
     * 你需要完成功能的入口
     * 
     * @author XXX
     * @since 2016-3-4
     * @version V1
     */
    public static String searchRoute(String graphContent, String condition)
    {
        //testing code
    	/*for(short i=0;i<nodeCount;i++)
		System.out.printf("%d ",oldDisVec[i]);*/
    	//initial the adjacent matrix
    	short nodeCount = 20;
    	short[][] adjMat= new short[nodeCount][nodeCount];
        short[][] adjMatLID=new short[nodeCount][nodeCount];
    	adjMatIni(adjMat,adjMatLID);
    	
    	//initial the source, destination, including set
    	short P_souInd=2;
    	short P_desInd=19;
    	short P_incSetCou=6;
    	short[] P_incSet = new short[P_incSetCou];
        P_incSet[0]=3;P_incSet[1]=5;P_incSet[2]=7;P_incSet[3]=11;P_incSet[4]=13;P_incSet[5]=17;
    	short[] P_node = new short[P_incSetCou+2];
    	P_node[0]=P_souInd;P_node[P_incSetCou+1]=P_desInd;
    	for(short i=0;i<P_incSetCou;i++)
    	{
    		P_node[i+1]=P_incSet[i];
    	}
    	
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
    
    //adjacent matrix initialization
    private static void adjMatIni(short[][] adjMat,short[][] adjMatLID)
    {
    	//initial adjMat
    	for(short i=0;i<adjMat.length;i++)
    		for(short j=0;j<adjMat.length;j++)
    			adjMat[i][j]=13000;
    	//initial adjMatLID
    	for(short i=0;i<adjMatLID.length;i++)
    		for(short j=0;j<adjMatLID.length;j++)
    			adjMatLID[i][j]=-1;

    	adjMat[0][13]=15;adjMat[0][8]=17;adjMat[0][19]=1;adjMat[0][4]=8;
    	adjMatLID[0][13]=0;adjMatLID[0][8]=1;adjMatLID[0][19]=2;adjMatLID[0][4]=3;
    	
    	adjMat[1][0]=4;
    	adjMatLID[1][0]=4;
    	
    	adjMat[2][3]=20;adjMat[2][9]=19;adjMat[2][15]=8;
    	adjMatLID[2][3]=36;adjMatLID[2][9]=5;adjMatLID[2][15]=6;
    	
    	adjMat[3][0]=14;adjMat[3][5]=20;adjMat[3][11]=12;
    	adjMatLID[3][0]=7;adjMatLID[3][5]=37;adjMatLID[3][11]=8;
    	
    	adjMat[4][1]=15;adjMat[4][5]=17;
    	adjMatLID[4][1]=9;adjMatLID[4][5]=10;
    	
    	adjMat[5][7]=20;adjMat[5][8]=18;adjMat[5][9]=14;adjMat[5][6]=2;adjMat[5][19]=20;
    	adjMatLID[5][7]=38;adjMatLID[5][8]=11;adjMatLID[5][9]=12;adjMatLID[5][6]=13;adjMatLID[5][19]=44;
    	
    	adjMat[6][17]=4;
    	adjMatLID[6][17]=14;
    	
    	adjMat[7][11]=20;adjMat[7][13]=1;adjMat[7][16]=19;
    	adjMatLID[7][11]=39;adjMatLID[7][13]=15;adjMatLID[7][16]=16;
    	
    	adjMat[8][6]=1;adjMat[8][12]=17;
    	adjMatLID[8][6]=17;adjMatLID[8][12]=18;
    	
    	adjMat[9][14]=11;
    	adjMatLID[9][14]=19;
    	
    	adjMat[10][12]=1;
    	adjMatLID[10][12]=20;
    	
    	adjMat[11][7]=12;adjMat[11][4]=7;adjMat[11][13]=20;adjMat[11][19]=20;
    	adjMatLID[11][7]=21;adjMatLID[11][4]=22;adjMatLID[11][13]=40;adjMatLID[11][19]=20;
    	
    	adjMat[12][14]=5;
    	adjMatLID[12][14]=23;
    	
    	adjMat[13][17]=12;adjMat[13][4]=2;
    	adjMatLID[13][17]=24;adjMatLID[13][4]=25;
    	
    	adjMat[14][19]=9;
    	adjMatLID[14][19]=26;
    	
    	adjMat[15][10]=14;adjMat[15][18]=2;
    	adjMatLID[15][10]=27;adjMatLID[15][18]=28;
    	
    	adjMat[16][8]=14;
    	adjMatLID[16][8]=29;
    	
    	adjMat[17][9]=14;adjMat[17][19]=3;adjMat[17][18]=10;adjMat[17][11]=20;adjMat[17][5]=20;
    	adjMatLID[17][9]=30;adjMatLID[17][19]=31;adjMatLID[17][18]=32;adjMatLID[17][11]=41;adjMatLID[17][5]=43;
    	
    	adjMat[18][15]=8;adjMat[18][3]=8;
    	adjMatLID[18][15]=33;adjMatLID[18][3]=34;
    	
    	adjMat[19][18]=12;
    	adjMatLID[19][18]=35;
    	    	
    }
    private static void shortestPathTree(short[] pathSou,short[] pathDes,short[] oldDisVec, short[] finDisVec, short pathCount,short[][] adjMat,short nodeCount,short souInd)
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

}