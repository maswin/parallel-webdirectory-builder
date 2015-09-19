package edu.tce.cse.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.tartarus.snowball.ext.PorterStemmer;

import edu.tce.cse.util.SuperBit;


public class Document {
	private long docID;
	private String filePath;

	private Map<String, Integer> termFrequency;
	private int totalTokens; 
	private int totalNumOfWords;


	//Computed during Initialization
	private Map<String, Double> tfIdfVector;
	private double[] tfIdf;
	private boolean[] signatureVector;

	//Across all documents
	static int totalDocuments = 0;
	static Map<String, Integer> documentFrequency;
	static SuperBit sb;
	static List<String> stopWordSet = null;
	public Document(String location) throws IOException{
		//Initialize Stop Word Set
		if(stopWordSet == null){
			initializeStopWordSet();
		}

		//Set ID
		this.docID = totalDocuments;
		totalDocuments++;
		filePath = location;
		//Initialize termfrequency
		termFrequency = new HashMap<String, Integer>();
		totalTokens = 0;
		totalNumOfWords = 0;
		parseDocument(location);

		//Generate tfIdfVector - - To be done after initializing all Documents
		tfIdfVector = null;
		tfIdf = null;

		//Generate Signature - To be done after initializing all Documents
		signatureVector = null;
	}

	//Getters & Setters
	public long getDocID() {
		return docID;
	}

	public String getFilePath() {
		return filePath;
	}

	public Map<String, Integer> getTermFrequency() {
		return termFrequency;
	}

	public int getTotalTokens() {
		return totalTokens;
	}

	public Map<String, Double> getTfIdfVector() {
		if(tfIdfVector == null){
			calculateTfIdf();
		}
		return tfIdfVector;
	}

	public boolean[] getSignatureVector() {
		if(signatureVector == null){
			generateSignature();
		}
		return signatureVector;
	}

	public static int getTotalDocuments() {
		return totalDocuments;
	}

	public static Map<String, Integer> getDocumentFrequency() {
		return documentFrequency;
	}
	public double[] getTfIdf() {
		if(tfIdf == null){
			generateTfIdf();
		}
		return tfIdf;
	}

	public void setTfIdf(double[] tfIdf) {
		this.tfIdf = tfIdf;
	}
	static void initializeStopWordSet(){
		/* Default Stop Word Set
		 "a", "an", "and", "are", "as", "at", "be", "but", "by",
		"for", "if", "in", "into", "is", "it",
		"no", "not", "of", "on", "or", "such",
		"that", "the", "their", "then", "there", "these",
		"they", "this", "to", "was", "will", "with" 
		 */
		stopWordSet = new ArrayList<String>();
		stopWordSet.add("from");
	}
	private void parseDocument(String location) throws IOException{
		List<String> words = extractWords(location);
		int count = 0;
		for(String term : words){
			if(termFrequency.containsKey(term)){
				count = termFrequency.get(term)+1;
				termFrequency.put(term, count);
			}else{
				termFrequency.put(term, 1);
			}
			totalNumOfWords++;
		}

		if(documentFrequency == null){
			documentFrequency = new HashMap<String, Integer>();
		}

		for(String word : termFrequency.keySet()){
			if(documentFrequency.containsKey(word)){
				count = documentFrequency.get(word)+1;
				documentFrequency.put(word, count);
			}else{
				documentFrequency.put(word, 1);
			}
			totalTokens++;
		}

	}
	private List<String> extractWords(String fileName) throws IOException {
		List<String> words = new LinkedList<String>();
		
		//Read the File and store the content in a single String
		String fileContent = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		
		//Tokenize The content
		Tokenizer tokenizer = new StandardTokenizer();
		tokenizer.setReader(new StringReader(fileContent.toLowerCase()));

		StandardFilter standardFilter = new StandardFilter(tokenizer);
		
		CharArraySet stopSet = CharArraySet.copy(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		stopSet.addAll(stopWordSet);
		StopFilter stopFilter = new StopFilter(standardFilter,
				stopSet);

		CharTermAttribute charTermAttribute
		= tokenizer.addAttribute(CharTermAttribute.class);
		stopFilter.reset();

		PorterStemmer stemmer = new PorterStemmer();

		while (stopFilter.incrementToken()) {
			String token = charTermAttribute.toString();
			stemmer.setCurrent(token);
			stemmer.stem();
			words.add(stemmer.getCurrent());
		}
		
		return words;
	}
	private void calculateTfIdf(){		
		double tf;
		int df;
		double idf;
		double tfidf;
		tfIdfVector = new HashMap<String, Double>();
		for(String word : termFrequency.keySet()){
			tf = (termFrequency.get(word)/(totalNumOfWords*1.0));
			df = documentFrequency.get(word);

			idf = 1 + Math.log(totalDocuments/(df*1.0));

			tfidf = tf*idf;
			tfIdfVector.put(word, tfidf);
		}
	}

	private void generateSignature(){
		//Initialized only once
		if(sb==null){
			initializeSuperBit();
		}

		double[] vector = this.getTfIdf();
		signatureVector = sb.signature(vector);
	}
	private void initializeSuperBit(){
		File f = new File("SuperBit/superbit.ser");
		if(f.exists()){
			FileInputStream fin;
			try {
				fin = new FileInputStream("SuperBit/superbit.ser");
				ObjectInputStream ois = new ObjectInputStream(fin);
				sb = (SuperBit) ois.readObject();
				System.out.println("Read existing Super Bit");
				ois.close();
				fin.close();
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			sb = new SuperBit(documentFrequency.size(),20,20);
			System.out.println("New Super Bit Generated");
			FileOutputStream fout;
			try {
				fout = new FileOutputStream("SuperBit/superbit.ser");
				ObjectOutputStream oos = new ObjectOutputStream(fout);   
				oos.writeObject(sb);
				oos.close();
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	private void generateTfIdf(){		
		tfIdf = new double[documentFrequency.size()];
		int index = 0;
		for(String term : documentFrequency.keySet()){
			if(this.getTfIdfVector().containsKey(term)){
				tfIdf[index] = tfIdfVector.get(term);
			}else{
				tfIdf[index] = 0.0;
			}
			index++;
		}
	}
	public float findCosSimilarity(Document d){
		double E = 0.0;
		double E1 = 0.0;
		double E2 = 0.0;
		for(int i=0;i<tfIdf.length;i++){
			E1 += Math.pow(this.tfIdf[i],2);
			E2 += Math.pow(d.tfIdf[i],2);
			E += this.tfIdf[i]*d.tfIdf[i];
		}
		E1 = Math.sqrt(E1);
		E2 = Math.sqrt(E2);
		E = (E / (E1*E2));
		return (float)(Math.abs(E));
	}

}
