package edu.tce.cse.document;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.model.Centroid;

public class DocMemManager {

	public static long accessTime = 0;
	public static int maxSize = 1500;
	public static int totalSize = 1000;

	/*private static LinkedHashMap<Long, Document> documentMap = new <Long, Document>LinkedHashMap(maxSize + 1, .75F, false) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > maxSize;
		}
	};
	private static LinkedHashMap<Long, DocNode> docNodeMap = new <Long, DocNode>LinkedHashMap(maxSize + 1, .75F, false) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > maxSize;
		}
	};
	private static LinkedHashMap<Long, Cluster> clusterMap = new <Long, Cluster>LinkedHashMap(maxSize + 1, .75F, false) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > maxSize;
		}
	};
	private static LinkedHashMap<Long, Centroid> centroidMap = new <Long, Centroid>LinkedHashMap(maxSize + 1, .75F, false) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > maxSize;
		}
	};*/

	private static Map<Long, Document> documentMap = new <Long, Document>HashMap();
	private static Map<Long, DocNode> docNodeMap = new <Long, DocNode>HashMap();
	private static Map<Long, Cluster> clusterMap = new <Long, Cluster>HashMap();
	private static Map<Long, Centroid> centroidMap = new <Long, Centroid>HashMap();
	
	public static DocNode sampleNode;
	public static Document sampleDocument;
	//Read & Write Document
	public static void writeDocument(Document doc) {
		long startTime = System.currentTimeMillis();
		try {
			FileOutputStream fout = new FileOutputStream("var/"+doc.docID+"Doc.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			sampleDocument = doc;
			oos.writeObject(doc);
			checkSafety();
			documentMap.put(doc.docID, doc);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write Document Exception");
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
	}

	public static Document getDocument(long id){
		long startTime = System.currentTimeMillis();
		Document doc = null;
		if(documentMap.containsKey(id)){
			doc = documentMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Doc.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				doc = (Document) ois.readObject();
				checkSafety();
				documentMap.put(id, doc);
				ois.close();
				fin.close();
			} catch (Exception e){
				System.out.println("Read Document Exception");
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
		return doc;
	}

	//Read & Write DocNode
	public static void writeDocNode(DocNode doc) {
		long startTime = System.currentTimeMillis();
		try {
			FileOutputStream fout = new FileOutputStream("var/"+doc.nodeID+"Node.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			sampleNode = doc;
			oos.writeObject(doc);
			checkSafety();
			docNodeMap.put(doc.nodeID, doc);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write DocNode Exception");
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
	}

	public static DocNode getDocNode(long id){
		long startTime = System.currentTimeMillis();
		DocNode doc = null;
		if(docNodeMap.containsKey(id)){
			doc = docNodeMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Node.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				doc = (DocNode) ois.readObject();
				checkSafety();
				docNodeMap.put(id, doc);
				ois.close();
				fin.close();
			} catch (Exception e){
				System.out.println("Read DocNode Exception");
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
		return doc;
	}

	//Read & Write Cluster
	public static void writeCluster(Cluster c) {
		long startTime = System.currentTimeMillis();
		try {
			FileOutputStream fout = new FileOutputStream("var/"+c.nodeID+"Cluster.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(c);
			checkSafety();
			clusterMap.put(c.nodeID, c);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write Cluster Exception");
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
	}

	public static Cluster getCluster(long id){
		long startTime = System.currentTimeMillis();
		Cluster c = null;
		if(clusterMap.containsKey(id)){
			c = clusterMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Cluster.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				c = (Cluster) ois.readObject();
				checkSafety();
				clusterMap.put(id, c);
				ois.close();
				fin.close();
			} catch (Exception e){
				System.out.println("Read Cluster Exception");
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
		return c;
	}

	//Read & Write Centroid
	public static void writeCentroid(Centroid c) {
		long startTime = System.currentTimeMillis();
		try {
			FileOutputStream fout = new FileOutputStream("var/"+c.clusterId+"Centroid.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(c);
			checkSafety();
			centroidMap.put(c.clusterId, c);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write Centroid Exception");
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
	}

	public static Centroid getCentroid(long id){
		long startTime = System.currentTimeMillis();
		Centroid c = null;
		if(centroidMap.containsKey(id)){
			c = centroidMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Centroid.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				c = (Centroid) ois.readObject();
				checkSafety();
				centroidMap.put(id, c);
				ois.close();
				fin.close();
			} catch (Exception e){
				System.out.println("Read Centroid Exception");
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		accessTime += endTime-startTime;
		return c;
	}
	private static void checkSafety(){
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		//Less thn 2 GB
		if((freeMemory + (maxMemory - allocatedMemory))<=1000000000l){
			System.out.println("Memory : "+(freeMemory + (maxMemory - allocatedMemory)) );
			int size = documentMap.size()+docNodeMap.size()+clusterMap.size()+centroidMap.size();
			System.out.println("Size in Use : "+size);
			System.out.println("Docuent Use : "+documentMap.size());
			System.out.println("Doc Node Use : "+docNodeMap.size());
			System.out.println("Cluster Use : "+clusterMap.size());
			flushMemory();
		}
		/*if(size>totalSize){
			flushMemory();
		}*/
	}

	private static void flushMemory(){
		flushDocument();
		flushDocNode();
		flushCluster();
		flushCentroid();
		System.gc();
		System.out.println("Memory Flushed");
	}

	private static void flushDocument(){
		DocMemManager.documentMap.clear();	
	}

	private static void flushDocNode(){
		DocMemManager.docNodeMap.clear();
	}
	private static void flushCluster(){
		DocMemManager.clusterMap.clear();
	}
	private static void flushCentroid(){
		DocMemManager.centroidMap.clear();
	}

}
