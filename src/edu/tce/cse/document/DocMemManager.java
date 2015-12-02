package edu.tce.cse.document;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.tce.cse.clustering.Cluster;
import edu.tce.cse.model.Centroid;

public class DocMemManager {

	public static int maxSize = 5000;
	public static int totalSize = 10000;

	private static LinkedHashMap<Long, Document> documentMap = new <Long, Document>LinkedHashMap(maxSize + 1, .75F, false) {
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
	};

	//Read & Write Document
	public static void writeDocument(Document doc) {
		try {
			FileOutputStream fout = new FileOutputStream("var/"+doc.docID+"Doc.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(doc);
			checkSafety();
			documentMap.put(doc.docID, doc);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write Document Exception");
			e.printStackTrace();
		}
	}

	public static Document getDocument(long id){
		Document doc;
		if(documentMap.containsKey(id)){
			return documentMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Doc.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				doc = (Document) ois.readObject();
				checkSafety();
				documentMap.put(id, doc);
				return doc;
			} catch (Exception e){
				System.out.println("Read Document Exception");
				e.printStackTrace();
			}
		}
		return null;
	}

	//Read & Write DocNode
	public static void writeDocNode(DocNode doc) {
		try {
			FileOutputStream fout = new FileOutputStream("var/"+doc.nodeID+"Node.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(doc);
			checkSafety();
			docNodeMap.put(doc.nodeID, doc);
			oos.close();
			fout.close();
		} catch (Exception e){
			System.out.println("Write DocNode Exception");
			e.printStackTrace();
		}
	}

	public static DocNode getDocNode(long id){
		DocNode doc;
		if(docNodeMap.containsKey(id)){
			return docNodeMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Node.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				doc = (DocNode) ois.readObject();
				checkSafety();
				docNodeMap.put(id, doc);
				return doc;
			} catch (Exception e){
				System.out.println("Read DocNode Exception");
				e.printStackTrace();
			}
		}
		return null;
	}

	//Read & Write Cluster
	public static void writeCluster(Cluster c) {
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
	}

	public static Cluster getCluster(long id){
		Cluster c;
		if(clusterMap.containsKey(id)){
			return clusterMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Cluster.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				c = (Cluster) ois.readObject();
				checkSafety();
				clusterMap.put(id, c);
				return c;
			} catch (Exception e){
				System.out.println("Read Cluster Exception");
				e.printStackTrace();
			}
		}
		return null;
	}

	//Read & Write Centroid
	public static void writeCentroid(Centroid c) {
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
	}

	public static Centroid getCentroid(long id){
		Centroid c;
		if(centroidMap.containsKey(id)){
			return centroidMap.get(id);
		}else{
			try {
				FileInputStream fin = new FileInputStream("var/"+id+"Centroid.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				c = (Centroid) ois.readObject();
				checkSafety();
				centroidMap.put(id, c);
				return c;
			} catch (Exception e){
				System.out.println("Read Centroid Exception");
				e.printStackTrace();
			}
		}
		return null;
	}
	private static void checkSafety(){
		int size = documentMap.size()+docNodeMap.size()+clusterMap.size()+centroidMap.size();
		if(size>totalSize){
			flushMemory();
		}
	}

	private static void flushMemory(){
		flushDocument();
		flushDocNode();
		flushCluster();
		flushCentroid();
		System.gc();
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
