package edu.tce.cse.clustering;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
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
	private boolean signatureVector[];

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
			sb = new SuperBit(documentFrequency.size(),100,100);
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
