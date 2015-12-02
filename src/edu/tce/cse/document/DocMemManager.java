package edu.tce.cse.document;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import edu.tce.cse.clustering.Cluster;

public class DocMemManager {

	private static final int maxSize = 10000;

	private static Map<Long, Document> documentMap = new <Long, Document>HashMap();
	private static Map<Long, DocNode> docNodeMap = new <Long, DocNode>HashMap();
	private static Map<Long, Cluster> clusterMap = new <Long, Cluster>HashMap();

	//Read & Write Document
	public static void writeDocument(Document doc) {
		try {
			FileOutputStream fout = new FileOutputStream("var/"+doc.docID+"Doc.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(doc);
			if(isNotSafe()){
				System.out.println("Flushing Document Memory");
				flushMemory();
			} 
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
			if(isNotSafe()){
				System.out.println("Flushing DocNode Memory");
				flushMemory();
			} 
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
				return doc;
			} catch (Exception e){
				System.out.println("Read DocNode Exception");
				e.printStackTrace();
			}
		}
		return null;
	}

	//Read & Write Cluster
	public static void writeDocNode(Cluster c) {
		try {
			FileOutputStream fout = new FileOutputStream("var/"+c.nodeID+"Cluster.ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(c);
			if(isNotSafe()){
				System.out.println("Flushing Cluster Memory");
				flushMemory();
			} 
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
				return c;
			} catch (Exception e){
				System.out.println("Read Cluster Exception");
				e.printStackTrace();
			}
		}
		return null;
	}

	private static boolean isNotSafe(){
		if(documentMap.size()>=maxSize){
			return true;
		}
		if(docNodeMap.size()>=maxSize){
			return true;
		}
		if(clusterMap.size()>=maxSize){
			return true;
		}
		return false;
	}

	private static void flushMemory(){
		flushDocument();
		flushDocNode();
		flushCluster();
	}

	private static void flushDocument(){
		DocMemManager.documentMap = new <Long, Document>HashMap();
		System.gc();
	}

	private static void flushDocNode(){
		DocMemManager.docNodeMap = new <Long, DocNode>HashMap();
		System.gc();
	}
	private static void flushCluster(){
		DocMemManager.clusterMap= new <Long, Cluster>HashMap();
		System.gc();
	}

}
