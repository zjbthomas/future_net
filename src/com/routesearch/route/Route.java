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
        //initial the adjacent matrix
    	short nodeCount = 20;
    	short[][] adjMat;
    	adjMat = new short[nodeCount][nodeCount];
    	adjMatIni(adjMat);
    	
    	return "hello world!";
    }
    
    //adjacent matrix initialization
    private static void adjMatIni(short[][] adjMat)
    {
    	short unrDis = 13000;
    	for(int i=0;i<adjMat.length;i++)
    		for(int j=0;j<adjMat.length;j++)
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

}