package main;

import java.util.HashMap;
import java.util.Set;

public class Document {

    static final int		     nbWords = 30799;

    private HashMap<Integer, Double> tf;

    public Document() {
	tf = new HashMap<>();
    }

    public void setFrequency(int word, double frequency) {
	if (word >= 0 && word < nbWords && frequency >= 0)
	    tf.put(word, frequency);
    }

    public double getFrequency(int word) {
	if (word >= 0 && word < nbWords && tf.containsKey(word))
	    return tf.get(word);
	else
	    return 0;
    }

    public Set<Integer> getWords() {
	return tf.keySet();
    }

    public void removeWord(int word) {
	tf.remove(word);
    }

    public double euclidianDistance(Document d2, HashMap<Integer, Float> weight) {
	double res = 0;
	for (Integer word : getWords()) {
	    res += (weight != null ? weight.get(word) : 1)
		    * ((getFrequency(word) - d2.getFrequency(word)) * (getFrequency(word) - d2.getFrequency(word)));
	}
	for (Integer word : d2.getWords()) {
	    if (getFrequency(word) == 0)
		res += (weight != null ? weight.get(word) : 1) * (d2.getFrequency(word) * d2.getFrequency(word));
	}
	return Math.sqrt(res);
    }

    public double euclidianDistance(Document d2) {
	return euclidianDistance(d2, null);
    }

    public void add(Document d2) {
	for (Integer word : d2.getWords()) {
	    setFrequency(word, getFrequency(word) + d2.getFrequency(word));
	}
    }

    public void divide(int number) {
	for (Integer word : getWords()) {
	    setFrequency(word, getFrequency(word) / (double) number);
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((tf == null) ? 0 : tf.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Document other = (Document) obj;
	if (tf == null) {
	    if (other.tf != null)
		return false;
	} else if (!tf.equals(other.tf))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return tf.toString();
    }
}
