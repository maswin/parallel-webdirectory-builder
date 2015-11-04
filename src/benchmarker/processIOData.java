package benchmarker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.clustering.Node;

public class processIOData {

	public MinCluster processInputData(String folder){
		return processInputDataUtil(folder,0);
	}
	
	public MinCluster processInputDataUtil(String folder, int level){
		MinCluster root = new MinCluster();
		root.level = level;
		File input = new File(folder);
		File[] tmpFiles = input.listFiles();
		for(File f : tmpFiles){
			if(f.isDirectory()){
				root.addChild(processInputDataUtil(f.getAbsolutePath(), level+1));
			}else{
				root.addFile(f.getName().trim());
			}
		}	
		for(MinCluster child : root.children){
			root.addFile(child.files);
		}
		return root;
	}

	public MinCluster processOutputData(String file){
		MinCluster root = new MinCluster();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			List<String> lines = new ArrayList<String>();
			String line;
			while((line = in.readLine())!=null){
				lines.add(line);
			}
			if(processOutputDataUtil(lines, 0, root, 0, 0)!=-1){
				System.out.println("End Error");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return root;
	}

	private int indentCount(String line){
		int thisIndentCount = 0;
		for(int i=0;i<line.length();i++){
			if(line.charAt(i)== ' '){
				thisIndentCount++;
			}else{
				break;
			}
		}
		//System.out.println("Tested :"+thisIndentCount+" "+line);
		return thisIndentCount;
	}
	private void addFiles(MinCluster c,String files){
		StringTokenizer st = new StringTokenizer(files,";");
		while(st.hasMoreTokens()){
			c.addFile(st.nextToken());
		}
	}
	private MinCluster formChild(String files){
		MinCluster child = new MinCluster();
		StringTokenizer st = new StringTokenizer(files,";");
		while(st.hasMoreTokens()){
			child.addFile(st.nextToken());
		}
		return child;
	}
	private int  processOutputDataUtil(List<String> lines, int index, MinCluster root, int indent, int level) throws IOException{
		int indentCount;
		String line;
		while(index<lines.size() && index != -1){
			line = lines.get(index);
			indentCount = indentCount(line);
			if(index == 0){
				if(indentCount != 0){
					System.out.println("Error");
					return -1;
				}
				addFiles(root,line);
				root.level = level;
				return processOutputDataUtil(lines, index+1, root, indent, level+1);
			}else{
				if(indentCount == indent+1){
					line = line.substring(indentCount);
					MinCluster child = formChild(line);
					child.level = level;
					root.addChild(child);
					index = processOutputDataUtil(lines, index+1, child, indent+1, level+1);
				}else{
					return index;
				}
			}
		}
		return -1;
	}

	//Generate hierarchical clusters
	public List<List<MinCluster>>  getLevelsOfClusters(MinCluster root){
		List<List<MinCluster>> hCluster = new ArrayList<List<MinCluster>>();
		getLevelsOfClustersUtil(hCluster, root);
		return hCluster;	
	}
	//Generate All Clusters
	public List<MinCluster>  getAllClusters(MinCluster root){
		List<MinCluster> clusters = new ArrayList<MinCluster>();
		getAllClustersUtil(clusters, root);
		return clusters;	
	}
	
	public void getLevelsOfClustersUtil(List<List<MinCluster>> hCluster, MinCluster root){
		List<MinCluster> clusters;
		//Assuming level i will get added before level i+1
		if(hCluster.size()<=root.level){
			clusters = new ArrayList<MinCluster>(); 
			hCluster.add(root.level, clusters);
		}
		clusters = hCluster.get(root.level);
		clusters.add(root);
		for(MinCluster child : root.children){
			getLevelsOfClustersUtil(hCluster, child);
		}
	}
	
	public void getAllClustersUtil(List<MinCluster> clusters, MinCluster root){
		if(root == null){
			return;
		}
		clusters.add(root);
		for(MinCluster child : root.children){
			getAllClustersUtil(clusters, child);
		}
	}
	
	/*
	public static void main(String args[]) throws FileNotFoundException{
		processIOData po = new processIOData();
		MinCluster root = po.processOutputData("output.txt");
		root = po.processInputData("TestDocuments");
		//Print Result to File
		PrintWriter out = new PrintWriter(new File("output1.txt"));
		generateOutputFile(out, root, 0);
		out.close();
		
		//Levels of cluster Test
		List<List<MinCluster>> hCluster = po.getLevelsOfClusters(root);
		
		//Print Levels of cluster
		int count = 0;
		for(int i=0;i<hCluster.size();i++){
			List<MinCluster> clusters = hCluster.get(i);
			System.out.println("Cluster Level "+i);
			for(MinCluster cluster : clusters){
				System.out.println(cluster.files.toString());
			}
		}
	}*/

	//Testing Purpose
	public static void generateOutputFile(PrintWriter out, MinCluster root, int indent){
		for(int i=0;i<indent;i++){
			out.print(" ");
		}
		out.print(root.level+" ");
		for(String file : root.files){
			out.print(file+";");
		}
		out.println();
		if(root.children != null && root.children.size()>0){
			for(MinCluster child : root.children){
				generateOutputFile(out, child, indent+1);
			}
		}
	}
}
