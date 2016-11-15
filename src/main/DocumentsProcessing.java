package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

public class DocumentsProcessing {

    private ArrayList<Document> documents;

    /* Used for IDF */
    private int[]               documentsContainingWords;
    public static int           C = 1;

    byte[][]                    membershipMatrix;
    HashMap<Integer, Float>[]   weightClusterWordMaps;
    HashSet<Integer>            remainingWords;

    ArrayList<Document>         centers;

    public DocumentsProcessing() {
        documents = new ArrayList<>();
        documentsContainingWords = new int[Document.nbWords];
        remainingWords = new HashSet<>();
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

        for (Document d : documents) {
            remainingWords.addAll(d.getWords());
        }
    }

    public void doWordReduction() {
        for (Document d : documents) {
            HashMap<Integer, Float> tfidfsForDocument = new HashMap<>();
            for (Integer word : d.getWords()) {
                tfidfsForDocument.put(word, (float) (d.getFrequency(word)
                        * Math.log(documents.size() / (1 + documentsContainingWords[word]))));
            }
            tfidfsForDocument.entrySet().stream().sorted(new Comparator<Entry<Integer, Float>>() {

                @Override
                public int compare(Entry<Integer, Float> o1, Entry<Integer, Float> o2) {
                    if (o1.getValue() > o2.getValue())
                        return 1;
                    else if (o1.getValue() < o2.getValue())
                        return -1;
                    return 0;
                }

            }).skip(2).forEach(e -> {
                if (e.getValue() < 40)
                    d.removeWord(e.getKey());
            });
        }

        remainingWords.clear();
        for (Document d : documents) {
            remainingWords.addAll(d.getWords());
        }

        System.out.println("Percentage removed = " + (1.0 - remainingWords.size() / (float) Document.nbWords) * 100.0);
    }

    @SuppressWarnings("unchecked")
    public void doClustering() {
        int K = 50;
        float epsilon = 1f;

        // Initialization centers
        centers = new ArrayList<Document>();
        Random r = new Random();
        for (int i = 0; i < K; ++i) {
            while (true) {
                Document d = documents.get(r.nextInt(documents.size()));
                if (!centers.contains(d)) {
                    centers.add(d);
                    break;
                }
            }
        }

        // Main loop
        double diffCenters = Double.MAX_VALUE;
        int loopNumber = 0;
        while (diffCenters > epsilon) {

            // Calculate matrix
            membershipMatrix = new byte[documents.size()][K];
            weightClusterWordMaps = new HashMap[K];
            Arrays.fill(weightClusterWordMaps, new HashMap<Integer, Float>());
            for (int k = 0; k < K; ++k) {
                for (int word : remainingWords) {
                    weightClusterWordMaps[k].put(word, C / (float)remainingWords.size());
                }
            }

            for (int i = 0; i < documents.size(); ++i) {
                double mindist = Double.MAX_VALUE;
                int nearestCluster = 0;
                for (int j = 0; j < K; ++j) {
                    double dist = documents.get(i).euclidianDistance(centers.get(j), weightClusterWordMaps[j]);
                    if (dist < mindist) {
                        mindist = dist;
                        nearestCluster = j;
                    }
                }
                membershipMatrix[i][nearestCluster] = 1;
            }

            // New centers
            ArrayList<Document> newcenters = new ArrayList<Document>();
            for (int i = 0; i < K; ++i) {
                Document d = new Document();
                int clusterNumber = 0;
                for (int j = 0; j < documents.size(); ++j) {
                    if (membershipMatrix[j][i] == 1) {
                        d.add(documents.get(j));
                        clusterNumber++;
                    }
                }
                d.divide(clusterNumber);
                newcenters.add(d);
            }

            // New weights
            for (int k = 0; k < K; ++k) {
                double weight = 0;
                // Get all the documents contained in cluster k
                ArrayList<Document> documentsOfCluster = new ArrayList<Document>();
                for (int i = 0; i < documents.size(); ++i) {
                    if (membershipMatrix[i][k] == 1) {
                        documentsOfCluster.add(documents.get(i));
                    }
                }
                // Variances calcul
                HashMap<Integer, Float> variances = new HashMap<>();
                for (Document d : documentsOfCluster) {
                    for (int word : d.getWords())
                        variances.put(word, (float) ((variances.containsKey(word) ? variances.get(word) : 0)
                                + Math.pow(d.getFrequency(word) - newcenters.get(k).getFrequency(word), 2)));
                }
                for (int key : variances.keySet()) {
                    variances.put(key, (float) Math.sqrt(variances.get(key)));
                }
                float varianceOfMissing = variances.values().stream().max(Float::compare).orElse(0.0f) + 100;
                // Update of weights
                for (int j : remainingWords) {
                    weightClusterWordMaps[k].put(j, weightClusterWordMaps[k].get(j)
                            / (1 + (variances.containsKey(j) ? variances.get(j) : varianceOfMissing)));
                    weight += Math.pow(weightClusterWordMaps[k].get(j), 2);
                }
                for (int j : remainingWords) {
                    weightClusterWordMaps[k].put(j,
                            (float) (C * weightClusterWordMaps[k].get(j) / (Math.sqrt(weight))));
                }
            }

            // Calculate diff center
            diffCenters = 0;
            for (int i = 0; i < K; ++i) {
                diffCenters += centers.get(i).euclidianDistance(newcenters.get(i));
            }

            centers = new ArrayList<>(newcenters);
            loopNumber++;
            System.out.println("Loop " + loopNumber + ", diff = " + diffCenters);

        }

        for (int clusterNumber = 0; clusterNumber < K; ++clusterNumber) {
            int documentNumber = 0;
            for (int i = 0; i < documents.size(); ++i) {
                if (membershipMatrix[i][clusterNumber] == 1)
                    documentNumber++;
            }
            System.out.println("Center of cluster " + clusterNumber + " : " + centers.get(clusterNumber) + ", "
                    + documentNumber + " documents inside cluster");
        }

    }

    public void resetDocuments() {
        documents.clear();
        remainingWords.clear();
        Arrays.fill(documentsContainingWords, 0);
    }

}
