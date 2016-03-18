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
    	short[][] adjMat;
    	adjMat = new short[nodeCount][nodeCount];
    	adjMatIni(adjMat);
    	
    	//initial the incidence matrix
    	short[] pathSou = new short[nodeCount];
    	short[] pathDes = new short[nodeCount];
    	short pathCount = 0;
    	
    	shortestPathTree(pathSou,pathDes,pathCount,adjMat,nodeCount);
    	return "hello world!";
    }
    
    //adjacent matrix initialization
    private static void adjMatIni(short[][] adjMat)
    {
    	
    	short unrDis = 13000;
    	for(short i=0;i<adjMat.length;i++)
    		for(short j=0;j<adjMat.length;j++)
    			adjMat[i][j]=unrDis;

    	adjMat[0][13]=15;adjMat[0][8]=17;adjMat[0][19]=1;adjMat[0][4]=8;
    	adjMat[1][0]=4;
    	adjMat[2][3]=20;adjMat[2][9]=19;adjMat[2][15]=8;
    	adjMat[3][0]=14;adjMat[3][5]=20;adjMat[3][11]=12;
    	adjMat[4][1]=15;adjMat[4][5]=17;
    	adjMat[5][7]=20;adjMat[5][8]=18;adjMat[5][9]=14;adjMat[5][6]=2;
    	adjMat[6][17]=4;
    	adjMat[7][11]=20;adjMat[7][13]=1;adjMat[7][16]=19;
    	adjMat[8][6]=1;adjMat[8][12]=17;
    	adjMat[9][14]=11;
    	adjMat[10][12]=1;
    	adjMat[11][7]=12;adjMat[11][4]=7;adjMat[11][13]=20;
    	adjMat[12][14]=5;
    	adjMat[13][17]=12;adjMat[13][4]=2;
    	adjMat[14][19]=9;
    	adjMat[15][10]=14;adjMat[15][18]=2;
    	adjMat[16][8]=14;
    	adjMat[17][9]=14;adjMat[17][19]=3;adjMat[17][18]=10;
    	adjMat[18][15]=8;adjMat[18][3]=8;
    	adjMat[19][18]=12;
    	    	
    }
    private static void shortestPathTree(short[] pathSou,short[] pathDes,short pathCount,short[][] adjMat,short nodeCount)
    {
    	
    	//initial the distance vector
    	short souInd = 2;
    	short[] exiInd = new short[nodeCount];
    	for(short i=0;i<nodeCount;i++)
    		exiInd[i]=-1;
    	short exiIndCou=0;
    	exiInd[exiIndCou] = souInd;
    	short oldDisVec[] = new short[nodeCount];
    	for(short i=0;i<nodeCount;i++)
    		oldDisVec[i]=adjMat[souInd][i];
    	short finDisVec[] = new short[nodeCount];
    	for(short i=0;i<nodeCount;i++)
    		finDisVec[i]=oldDisVec[i];
    	finDisVec[souInd]=0;
    	
    	//generate the shortest path tree
    	for(short count=0;count<(nodeCount-1);count++)
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
    	for(short i=0;i<nodeCount-1;i++)
    		System.out.printf("%d %d ",pathSou[i],pathDes[i]);
    }

}