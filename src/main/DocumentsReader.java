package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class DocumentsReader {

	private ArrayList<Document> documents;

	private int[] documentsContainingWords;

	public DocumentsReader() {
		documents = new ArrayList<>();
		documentsContainingWords = new int[Document.nbWords];
	}

	public void addDocuments(String words) {

		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(words))) {
			String line = null;
			int currentDocumentNumber = 1;
			Document currentDocument = new Document();
			while ((line = bufferedReader.readLine()) != null) {
				String[] split = line.split(" ");
				if (Integer.parseInt(split[0]) != currentDocumentNumber) {
					documents.add(currentDocument);
					currentDocument = new Document();
					currentDocumentNumber = Integer.parseInt(split[0]);
				}
				documentsContainingWords[Integer.parseInt(split[1]) - 1]++;
				currentDocument.setFrequency(Integer.parseInt(split[1]) - 1, Byte.parseByte(split[2]));
			}
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + words + "'");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void doWordReduction() {

		for (Document d : documents) {
			ArrayList<Integer> words_copy = new ArrayList<>(d.getWords());
			for (Integer word : words_copy) {
				double tfidf = d.getFrequency(word) * Math.log(documents.size() / (1 + documentsContainingWords[word]));
				if (tfidf < 50) {
					d.removeWord(word);
				}
			}
		}

		HashSet<Integer> words = new HashSet<>();
		for (Document d : documents) {
			words.addAll(d.getWords());
		}

		System.out.println("Percentage removed = " + (1.0 - words.size() / (float)Document.nbWords) * 100.0);
	}

	public void resetDocuments() {
		documents.clear();
	}

}
