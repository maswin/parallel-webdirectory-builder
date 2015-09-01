package edu.tce.cse.example;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.PorterStemmer;

public class StopWordTester {

	public List<String> extractWords(String fileContent) throws IOException {
		List<String> words = new LinkedList<String>();
		
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
	public static void main(String args[]) throws IOException{
		
		StopWordTester st = new StopWordTester();
		String text = "and the 20th century (19012000) which includes the modern (18901930) that overlaps from the late 19th-century, the high modern (mid 20th-century), and contemporary or postmodern (1975present) eras.";
		List<String> words = st.extractWords(text);
		for(String word : words){
			System.out.print(word+" ");
		}
	}
}
