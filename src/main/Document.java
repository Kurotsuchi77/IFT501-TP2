package main;

import java.util.HashMap;
import java.util.Set;

public class Document {

	static final int nbWords = 30799;

	private HashMap<Integer, Byte> tf;

	public Document() {
		tf = new HashMap<>();
	}

	public void setFrequency(int word, byte frequency) {
		if (word >= 0 && word < nbWords && frequency >= 0)
			tf.put(word, frequency);
	}

	public byte getFrequency(int word) {
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

}
