package edu.tce.cse.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.tce.cse.document.Document;

public class PreProcessTester {
	public static void main(String args[]) throws IOException{
		System.out.println("Pre-Processing Output");
		System.out.println();
		sampleData s = new sampleData("TestDocuments");
		List<Document> words = s.getSampleDoc();

	}
}
