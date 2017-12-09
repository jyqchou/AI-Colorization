/*
================================================================================================================
Project - Colorization

Class for Colorization Assignment.
================================================================================================================
*/

package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

public class Colorization {
	
	static int MAX_INTENSITY = 255;
	static int CLUSTERS = 10;
	static String IN_COLOR_PATH = "src//data//color.csv";
	static String IN_BW_PATH = "src//data//input.csv";
	static String OUT_BW_PATH = "src//data//data.csv";
	static String OUT_COLOR_PATH = "src//data//output.csv";
	static HashMap<Integer, ArrayList<Integer>> inBwMap = new HashMap<Integer, ArrayList<Integer>>(); // Map for BlackWhite data - (Key,Value) as (i, Cell<0 to 8>)
	static HashMap<Integer, ArrayList<Integer>> inColorMap = new HashMap<Integer, ArrayList<Integer>>(); // Map for Color data - (Key,Value) as (i, <R,G,B>)
	//static HashMap<Integer, ArrayList<Integer>> outBwMap = new HashMap<Integer, ArrayList<Integer>>();
	
	
	public static void main(String[] args) {
		
		Colorization colorization = new Colorization();
		colorization.readData();
		colorization.createBWClusters();
		colorization.createColorClusters();
		//colorization.extractData();
	}
	
	public void readData() {
	
		String colorCurrentLine = "", bwCurrentLine = "";
		FileReader freader = null, freader1 = null; 
		BufferedReader breader = null, breader1 = null;
		int bwCount = 0, colorCount = 0;
		
		try {
			freader = new FileReader(IN_COLOR_PATH);
			breader = new BufferedReader(freader);
			
			freader1 = new FileReader(IN_BW_PATH);
			breader1 = new BufferedReader(freader1);
			
			while ((colorCurrentLine = breader.readLine()) != null && (bwCurrentLine = breader1.readLine()) != null) {
				
				String[] colorVal = colorCurrentLine.split(",");
				ArrayList<Integer> colorValues = new ArrayList<Integer>();
				for(int i = 0; i < colorVal.length; i++) {
					colorValues.add(Integer.parseInt(colorVal[i].trim()));
				}
				
				String[] bwVal = bwCurrentLine.split(",");
				ArrayList<Integer> bwValues = new ArrayList<Integer>();
				for(int i = 0; i < bwVal.length; i++) {
					bwValues.add(Integer.parseInt(bwVal[i].trim()));
				}
				
				inColorMap.put(colorCount++, colorValues);
				inBwMap.put(bwCount++, bwValues);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (breader != null)
					breader.close();
				if (freader != null)
					freader.close();
				if (breader1 != null)
					breader1.close();
				if (freader1 != null)
					freader1.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void createBWClusters() {
		
		int[] bwClusters = new int[CLUSTERS];
		int totalValues = 48894;
		
		for(int i=0; i<CLUSTERS; i++) { //Picking Random cluster points
			boolean flag = false;
			while(! flag) {
				int getPos = (int) (Math.random() * totalValues);
				bwClusters[i] = inBwMap.get(getPos).get(4);
				flag = true;
				for(int j=0; j<i; j++) {
					if(bwClusters[i] == bwClusters[j]) {
						flag = false;
						break;
					}
				}
			}
		}
		
		System.out.println("BW CLUSTER VALUES");
		for(int i=0; i<CLUSTERS; i++) {
			System.out.print(bwClusters[i]+" ");
		}
		System.out.println();
		
		boolean flag = false;
		int loopCount = 0;
		while(! flag) { //looping till the cluster values get updated
			++loopCount;
			LinkedHashMap<Integer,ArrayList<Integer>> clusterMap = new LinkedHashMap<Integer,ArrayList<Integer>>();
			
			//puts each value in inBwMap to a nearest cluster
			for(int i=0; i<totalValues; i++) {
				int dist = Integer.MAX_VALUE, bucket = 0;
				for(int j=0; j<CLUSTERS; j++) {
					if(dist > Math.abs(inBwMap.get(i).get(4) - bwClusters[j])){
						dist = Math.abs(inBwMap.get(i).get(4) - bwClusters[j]); 
						bucket = j;
					}
				}
				
				if(clusterMap.containsKey(bwClusters[bucket])) {
					clusterMap.get(bwClusters[bucket]).add(inBwMap.get(i).get(0)); //Map as (cluster's center, points in cluster)
				} else {
					ArrayList<Integer> arrayList = new ArrayList<Integer>();
					arrayList.add(inBwMap.get(i).get(0));
					clusterMap.put(bwClusters[bucket], arrayList);
				}
			}
			
			flag = true;
			
			System.out.println(clusterMap.keySet());
			
			
			//Calculating the average distance inside clusters, between centre and the points
			for(int i=0; i<CLUSTERS; i++) {
				int totalValue = 0;
				for(int j=0; j<clusterMap.get(bwClusters[i]).size(); j++) {
					totalValue += clusterMap.get(bwClusters[i]).get(j);
				}
				int newCluster = (int) (totalValue/clusterMap.get(bwClusters[i]).size());
				if(bwClusters[i] != newCluster) {
					bwClusters[i] = (int) (totalValue/clusterMap.get(bwClusters[i]).size()); // updating the average as new cluster center
					flag = false;
				}
			}
		}
		
		System.out.println("BW LOOP COUNT :: "+loopCount);
		System.out.println("CLUSTER VALUES");
		for(int i=0; i<CLUSTERS; i++) {
			System.out.print(bwClusters[i]+" ");
		}
		System.out.println("\n");
	}
	
public void createColorClusters() {
		
		ArrayList<int[]> colorClusters = new ArrayList<int[]>();
		int totalValues = 48894;
		
		for(int i=0; i<CLUSTERS; i++) {
			int[] rgb = new int[3];
			boolean flag = false;
			while(! flag) {
				int getPos = (int) (Math.random() * totalValues);
				
				for(int j=0; j<3; j++) {
					rgb[j] = inColorMap.get(getPos).get(j);
				}
				
				flag = true;
				for(int j=0; j<i; j++) {
					if(rgb[0] == colorClusters.get(j)[0] && rgb[1] == colorClusters.get(j)[1] 
							&& rgb[2] == colorClusters.get(j)[2]) {
						flag = false;
						break;
					}
				}
			}
			colorClusters.add(rgb);
		}
		
		System.out.println("COLOR CLUSTER VALUES");
		for(int i=0; i<CLUSTERS; i++) {
			System.out.print(colorClusters.get(i)[0]+" "+colorClusters.get(i)[1]+" "+colorClusters.get(i)[2]+", \t");
		}
		System.out.println();
		
		boolean flag = false;
		int loopCount = 0;
		while(! flag) {
			++loopCount;
			LinkedHashMap<int[],ArrayList<ArrayList<Integer>>> clusterMap = new LinkedHashMap<int[],ArrayList<ArrayList<Integer>>>();
			
			//puts each value in a cluster
			for(int i=0; i<totalValues; i++) {
				int dist = Integer.MAX_VALUE, bucket = 0;
				for(int j=0; j<CLUSTERS; j++) {
					int distance = calcDist(inColorMap.get(i), inColorMap.get(j));
					if(dist > distance){
						dist = distance; 
						bucket = j;
					}
				}
				
				if(clusterMap.containsKey(colorClusters.get(bucket))) {
					clusterMap.get(colorClusters.get(bucket)).add(inColorMap.get(i));
				} else {
					ArrayList<ArrayList<Integer>> arrayList = new ArrayList<ArrayList<Integer>>();
					arrayList.add(inColorMap.get(i));
					clusterMap.put(colorClusters.get(bucket), arrayList);
				}
			}
			
			flag = true;
			
			Set<int[]> print = clusterMap.keySet();
			Iterator<int[]> itr = print.iterator();
			while(itr.hasNext()){
				int[] printVal = itr.next();
				System.out.print(printVal[0]+" "+printVal[1]+" "+printVal[2]+", \t");
		    }
			
			System.out.println();
			
			//Calculating the average distance inside clusters
			for(int i=0; i<CLUSTERS; i++) {
				int totalRValue = 0, totalGValue = 0, totalBValue = 0;
				for(int j=0; j<clusterMap.get(colorClusters.get(i)).size(); j++) {
					totalRValue += clusterMap.get(colorClusters.get(i)).get(j).get(0);
					totalGValue += clusterMap.get(colorClusters.get(i)).get(j).get(1);
					totalBValue += clusterMap.get(colorClusters.get(i)).get(j).get(2);
				}
				
				int[] newCluster = new int[3];
				newCluster[0] = (int) (totalRValue/clusterMap.get(colorClusters.get(i)).size());
				newCluster[1] = (int) (totalGValue/clusterMap.get(colorClusters.get(i)).size());
				newCluster[2] = (int) (totalBValue/clusterMap.get(colorClusters.get(i)).size());
				
				if(!(colorClusters.get(i)[0] == newCluster[0] && colorClusters.get(i)[1] == newCluster[1] && colorClusters.get(i)[2] == newCluster[2])) {
					colorClusters.get(i)[0] = (int) (totalRValue/clusterMap.get(colorClusters.get(i)).size());
					colorClusters.get(i)[1] = (int) (totalGValue/clusterMap.get(colorClusters.get(i)).size());
					colorClusters.get(i)[2] = (int) (totalBValue/clusterMap.get(colorClusters.get(i)).size());
					flag = false;
				}
			}
		}
		
		System.out.println("COLOR LOOP COUNT :: "+loopCount);
		System.out.println("COLOR CLUSTER VALUES");
		for(int i=0; i<CLUSTERS; i++) {
			System.out.print(colorClusters.get(i)[0]+" "+colorClusters.get(i)[1]+" "+colorClusters.get(i)[2]+", \t");
		}
		
	}

	public void extractData() {
		
		LinkedHashMap<Integer, ArrayList<Integer>> outBwMap = new LinkedHashMap<Integer, ArrayList<Integer>>();
		String bwCurrentLine = "";
		FileReader freader = null; 
		BufferedReader breader = null;
		try {
			RandomAccessFile randomFile = new RandomAccessFile(OUT_COLOR_PATH, "rw");
			randomFile.setLength(0);
			
			freader = new FileReader(OUT_BW_PATH);
			breader = new BufferedReader(freader);
			
			while ((bwCurrentLine = breader.readLine()) != null) {
				String[] bwVal = bwCurrentLine.split(",");
				ArrayList<Integer> bwValues = new ArrayList<Integer>();
				for(int i = 0; i < bwVal.length; i++) {
					bwValues.add(Integer.parseInt(bwVal[i].trim()));
				}
				
				
				
				StringBuffer sb = new StringBuffer();
				/*for(int i = 0; i < colorValues.size(); i++) {
					if(i < colorValues.size()-1)
						sb.append(colorValues.get(i)+",");
					else
						sb.append(colorValues.get(i));
				}*/
				    
				 long fileLength = randomFile.length();
				 randomFile.seek(fileLength);
				 randomFile.writeBytes(sb+"\n");
			}
		
			randomFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (breader != null)
					breader.close();
				if (freader != null)
					freader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static int calcDist(ArrayList<Integer> color1, ArrayList<Integer> color2) {
		Double dist = Math.sqrt(( 2 * Math.pow(color1.get(0) - color2.get(0), 2) + 4 * Math.pow(color1.get(1) - color2.get(1), 2) + 3 * Math.pow(color1.get(2) - color2.get(2), 2) ));
		return dist.intValue();
	}
	
	/*public static ArrayList<Integer> colorGroups(ArrayList<Integer> input) {
		ArrayList<Integer> groups = new ArrayList<Integer>();
		int colorRange = (MAX_INTENSITY + 1)/COLOR_GROUPS;
		
		for(int j = 0; j < input.size(); j++) {
			for(int i = 0; i < COLOR_GROUPS; i++) {
				if((i * colorRange) <= input.get(j) && (((i+1) * colorRange) - 1) >= input.get(j)) {
					groups.add((i * colorRange) + ((colorRange - 1)/2));
					break;
				}
			}
		}
		
		return groups;
	}
	
	public static ArrayList<Integer> bwGroups(ArrayList<Integer> input) {
		ArrayList<Integer> groups = new ArrayList<Integer>();
		int bwRange = (MAX_INTENSITY + 1)/BW_GROUPS;
		
		for(int j = 0; j < input.size(); j++) {
			for(int i = 0; i < COLOR_GROUPS; i++) {
				if((i * bwRange) <= input.get(j) && (((i+1) * bwRange) - 1) >= input.get(j)) {
					groups.add((i * bwRange) + ((bwRange - 1)/2));
					break;
				}
			}
		}
		
		return groups;
	}*/
}