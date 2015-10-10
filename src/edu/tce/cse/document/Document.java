package edu.tce.cse.document;

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

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.algo.DenseDoubleAlgebra;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import edu.tce.cse.util.SuperBit;


public class Document {
	private long docID;
	private String filePath;
	private String fileName;

	private Map<String, Integer> termFrequency;
	private int totalTokens; 
	private int totalNumOfWords;


	//Computed during Initialization
	private double[] tfIdf;


	/*
	 * Call ParseDocument & generateTfIdfVector to complete Initialization
	 * Send document frequency as parameter to both methods
	 * ParseDocuments populate document frequency
	 * After parsing all documents
	 * generateTfIdfVector uses the populated document frequency
	 * (Combine the documentfrequency from all processor before generating 
	 *  tfIdf vector)
	 */
	public Document(int docId, String location, String fileName) throws IOException{

		//Set ID & Location
		this.docID = docId;
		this.filePath = location;
		this.fileName = fileName;
		
		//Initialize termfrequency
		termFrequency = new HashMap<String, Integer>();
		totalTokens = 0;
		totalNumOfWords = 0;
		
		//Generate tfIdfVector - - To be done after initializing all Documents
		tfIdf = null;
	}

	
	//Getters & Setters
	public long getDocID() {
		return docID;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public String getFileName() {
		return fileName;
	}

	public Map<String, Integer> getTermFrequency() {
		return termFrequency;
	}

	public int getTotalTokens() {
		return totalTokens;
	}

	public double[] getTfIdf() {
		if(tfIdf == null){
			System.out.println("Error : tfIdf Not Generated");
		}
		return tfIdf;
	}

	public void setTfIdf(double[] tfIdf) {
		this.tfIdf = tfIdf;
	}
	
	public void parseDocument(Map<String, Integer> documentFrequency) throws IOException{
		List<String> words = extractWords(this.filePath);
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
	public void calculateTfIdf(int totalDocuments, Map<String, Integer> documentFrequency){		
		int tf;
		int df;
		double idf;
		double tfidf;
		int index = 0;
		
		this.tfIdf = new double[documentFrequency.size()];
		for(String word : documentFrequency.keySet()){
			tf = 0;
			if(termFrequency.containsKey(word)){
				tf = termFrequency.get(word);
			}
			
			df = documentFrequency.get(word);
			idf = Math.log10((double)totalDocuments/(1.0 * df));

			tfidf = tf*idf;
			tfIdf[index++] = tfidf;
		}

	}
	
	public float findCosSimilarity(Document d){
		DoubleMatrix1D vector1 = new DenseDoubleMatrix1D(this.getTfIdf());
        DoubleMatrix1D vector2 = new DenseDoubleMatrix1D(d.getTfIdf());

        DenseDoubleAlgebra algebra = new DenseDoubleAlgebra();
        
        return (float) (vector1.zDotProduct(vector2) / 
                (algebra.norm2(vector1)*algebra.norm2(vector2)));
	}
	public float findCosDistance(Document d){
		return (1-findCosSimilarity(d));
	}
	public float findEuclideanSimilarity(Document d){
		float E = 0.0f;
		for(int i=0; i<tfIdf.length; i++){
			E += Math.pow((tfIdf[i]-d.tfIdf[i]), 2);
		}
		return (float)(Math.abs(Math.sqrt(E)));
	}
}
