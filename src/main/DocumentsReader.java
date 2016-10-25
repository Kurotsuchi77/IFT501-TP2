package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class DocumentsReader {

	private static ArrayList<Document> documents = new ArrayList<>();

	public static void addDocuments(String words) {

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
				currentDocument.setFrequency(Integer.parseInt(split[1]) - 1, Byte.parseByte(split[2]));
			}
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + words + "'");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void doWordReduction() {
		byte[] documentsContainingWords = new byte[Document.nbWords];
		for (int i = 0; i < Document.nbWords; ++i) {
			for (Document d : documents) {
				if (d.getFrequency(i) > 0)
					documentsContainingWords[i]++;
			}
		}
		for (Document d : documents) {
			
		}
	}

	public static void resetDocuments() {
		documents.clear();
	}

	private DocumentsReader() {
		// TODO Auto-generated constructor stub
	}

}
