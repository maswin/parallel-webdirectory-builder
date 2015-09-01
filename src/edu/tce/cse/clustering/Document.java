package edu.tce.cse.clustering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import org.tartarus.snowball.ext.PorterStemmer;

import edu.tce.cse.util.SuperBit;


public class Document {
	long docID;
	String filePath;

	Map<String, Integer> termFrequency;
	int totalTokens; 

	//Computed during Initialization
	Map<String, Double> tfIdfVector;
	boolean signatureVector[];

	//Across all documents
	static int totalDocuments = 0;
	static Map<String, Integer> documentFrequency;
	static SuperBit sb;
	public Document(String location) throws IOException{
		//Set ID
		this.docID = totalDocuments;
		totalDocuments++;

		//Initialize termfrequency
		termFrequency = new HashMap<String, Integer>();
		totalTokens = 0;
		parseDocument(location);

		//Generate tfIdfVector - - To be done after initializing all Documents
		tfIdfVector = null;

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
		}

		if(documentFrequency == null){
			documentFrequency = new HashMap<String, Integer>();
		}
		
		for(String word : termFrequency.keySet()){
			if(documentFrequency.containsKey(word)){
				count = documentFrequency.get(word)+1;
				documentFrequency.put(word, count);
			}else{
				documentFrequency.put(word, 0);
			}
			totalTokens++;
		}

	}
	private List<String> extractWords(String fileName) throws IOException {
		List<String> words = new LinkedList<String>();
		String fileContent = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		Tokenizer tokenizer = new StandardTokenizer();
		tokenizer.setReader(new StringReader(fileContent));

		StandardFilter standardFilter = new StandardFilter(tokenizer);
		StopFilter stopFilter = new StopFilter(standardFilter,
				StopAnalyzer.ENGLISH_STOP_WORDS_SET);

		CharTermAttribute charTermAttribute
		= tokenizer.addAttribute(CharTermAttribute.class);
		stopFilter.reset();

		PorterStemmer stemmer = new PorterStemmer();

		while (stopFilter.incrementToken()) {
			String token = charTermAttribute.toString().toLowerCase();

			stemmer.setCurrent(token);
			stemmer.stem();
			words.add(stemmer.getCurrent());

		}

		return words;
	}
	private void calculateTfIdf(){		
		int tf;
		int df;
		double idf;
		double tfidf;
		tfIdfVector = new HashMap<String, Double>();
		for(String word : termFrequency.keySet()){
			tf = termFrequency.get(word);
			df = documentFrequency.get(word);

			idf = 1 + Math.log((totalDocuments*1.0)/((df+1)*1.0));

			tfidf = tf*idf;
			tfIdfVector.put(word, tfidf);
		}
	}

	private void generateSignature(){
		
		if(sb==null){
			sb = new SuperBit(documentFrequency.size(),10,10);
		}
		double[] vector = new double[documentFrequency.size()];
		int index = 0;
		for(String term : documentFrequency.keySet()){
			if(this.getTfIdfVector().containsKey(term)){
				vector[index] = tfIdfVector.get(term);
			}else{
				vector[index] = 0.0;
			}
			index++;
		}
		signatureVector = sb.signature(vector);
	}


}
