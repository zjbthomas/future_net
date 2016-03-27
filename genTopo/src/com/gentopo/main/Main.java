package com.gentopo.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Main {
	
	private static int MAXPATH = 8;
	private static int MAXCOST = 20;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 3)
        {
            System.err.println("please input args: number of nodes, output path of topology, output path of demand");
            return;
        }

        int maxNodes = Integer.parseInt(args[0]);
        String topoPath = args[1];
        String demandPath = args[2];
		
		// Create a random system
		Random rand = new Random();
		
		// Create topology
		File topoFile = new File(topoPath);
		if (topoFile.exists() && topoFile.isFile() && (!topoFile.canRead()))
        {
			System.err.println(topoPath + " can not be created.");
            return;
        }
		
		topoFile.createNewFile();
		
		FileWriter fw = new FileWriter(topoFile, false);
		BufferedWriter bw = new BufferedWriter(fw);
		
		int pathCnt = 0;
		
		HashMap<Integer, Integer> pathStatus = new HashMap<Integer, Integer>();
		
		int threshold = (MAXPATH > (maxNodes - 1))? (maxNodes - 1): MAXPATH;
		
		for (int i = 0; i < maxNodes; i++) {
			int currentCnt = 0;
			if (pathStatus.containsKey(i)) {
				currentCnt = pathStatus.get(i);
			}
			
			if (currentCnt >= threshold) {
				continue;
			}
			
			// How many nodes will be the current node's destinations
			int destsNum = rand.nextInt(threshold - currentCnt) + 1;
			
			ArrayList<Integer> destsList = new ArrayList<Integer>();
			
			destsList.add(i);
			
			for (int j = 0; j < destsNum; j++) {
				int testNode;
				int testCnt = 0;
				do {
					testNode = rand.nextInt(maxNodes);
					if (pathStatus.containsKey(testNode)) {
						testCnt = pathStatus.get(testNode);
					}
				} while ((testCnt >= threshold) || (destsList.contains(testNode))); 
				
				String out = pathCnt + "," + i + "," + testNode + "," + (rand.nextInt(MAXCOST) + 1) + "\n";
				bw.write(out);
				
				pathStatus.put(testNode, testCnt + 1);
				destsList.add(testNode);
				pathCnt++;
			}
			
			pathStatus.put(i, currentCnt + destsNum);
		}
		
		if (fw != null) bw.close();
		if (bw != null) fw.close();
		
		// Create demand
		File demandFile = new File(demandPath);
		if (demandFile.exists() && demandFile.isFile() && (!demandFile.canRead()))
        {
			System.err.println(demandPath + " can not be created.");
            return;
        }
		
		demandFile.createNewFile();
		
		fw = new FileWriter(demandFile, false);
		bw = new BufferedWriter(fw);
		
		int source = rand.nextInt(maxNodes);
		int destination;
		do {
			destination = rand.nextInt(maxNodes);
		} while (source == destination);
		
		// How many nodes should be in the including set
		int includingSetNum = rand.nextInt(maxNodes - 2) + 1;
		
		ArrayList<Integer> includingSet = new ArrayList<Integer>();
		
		String out = source + "," + destination + ",";
		
		for (int i = 0; i < includingSetNum; i++) {
			int testNode;
			do {
				testNode = rand.nextInt(maxNodes);
			} while ((testNode == source) || (testNode == destination) || (includingSet.contains(testNode)));
			
			includingSet.add(testNode);
			
			out += testNode;
			
			if (i != includingSetNum - 1) {
				out += '|';
			}
		}
		
		bw.write(out);
		
		if (bw != null) bw.close();
		if (fw != null) fw.close();
	}

}
