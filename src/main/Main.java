package main;

public class Main {

	public Main() {

	}

	public static void main(String[] args) {

		DocumentsProcessing reader = new DocumentsProcessing();
		reader.addDocuments("data/nsfabs_part1_out/docwords.txt");
		reader.addDocuments("data/nsfabs_part2_out/docwords.txt");
		reader.addDocuments("data/nsfabs_part3_out/docwords.txt");

		reader.doWordReduction();

		System.out.println("PREPROCESSING DONE !");

		reader.doClustering();

	}

}
