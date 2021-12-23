import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class UAWordSimilarity {

	public static ArrayList<String> Lexicon = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(args[0]));
		String line = "";
		while ((line = br.readLine()) != null) {
			Lexicon.add(line);
		}
		br.close();
		br = new BufferedReader(new FileReader(args[1]));
		float[][] Cmatrix = new float[Lexicon.size()][Lexicon.size()];
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] split = line.split("\t");
			for (int i = 0; i < split.length; i++) {
				Cmatrix[count][i] = Float.parseFloat(split[i]);
			}
			count++;
		}
//................................................................................
		String[] context;
		/*
		 * System.out.println("Word 1:   " + args[2] + "\t\t Word 1 Lexicon Index:\t" +
		 * GetWordIndex(Lexicon, args[2])); System.out.println("Word 2:   " + args[3] +
		 * "\t\t Word 2 Lexicon Index:\t" + GetWordIndex(Lexicon, args[3]));
		 * System.out.println(); System.out.println("Cosine Similarity Score: " +
		 * CalculateSimilarity(Cmatrix, GetWordIndex(Lexicon,
		 * args[2]),GetWordIndex(Lexicon, args[3]))); System.out.println();
		 * System.out.print("Word 1 Context Words:    "); context =
		 * GetContext(Cmatrix,GetWordIndex(Lexicon, args[2]),Integer.parseInt(args[4]));
		 * for(int i = 0; i < context.length; i++) { if(context[i] != null) {
		 * System.out.print(context[i] + ", "); } } System.out.println();
		 * System.out.print("Word 2 Context Words:    "); context =
		 * GetContext(Cmatrix,GetWordIndex(Lexicon, args[3]),Integer.parseInt(args[4]));
		 * for(int i = 0; i < context.length; i++) { if(context[i] != null) {
		 * System.out.print(context[i] + ", "); } } System.out.println();
		 * System.out.println();
		 */
		/// *
		System.out.println("similarity of data and computer: "
				+ CalculateSimilarity(Cmatrix, GetWordIndex(Lexicon, "data"), GetWordIndex(Lexicon, "computer")));
		System.out.println("similarity of data and cake:     "
				+ CalculateSimilarity(Cmatrix, GetWordIndex(Lexicon, "data"), GetWordIndex(Lexicon, "cake")));
		System.out.println("similarity of data and cloud:    "
				+ CalculateSimilarity(Cmatrix, GetWordIndex(Lexicon, "data"), GetWordIndex(Lexicon, "cloud")));
		System.out.println("similarity of hot and dog:       "
				+ CalculateSimilarity(Cmatrix, GetWordIndex(Lexicon, "hot"), GetWordIndex(Lexicon, "dog")));
		System.out.println();
		System.out.print("computer Context Words:    ");
		context = GetContext(Cmatrix, GetWordIndex(Lexicon, "computer"), 10);
		for (int i = 0; i < context.length; i++) {
			if (context[i] != null) {
				System.out.print(context[i] + ", ");
			}
		}
		System.out.println();
		System.out.print("data Context Words:        ");
		context = GetContext(Cmatrix, GetWordIndex(Lexicon, "data"), 10);
		for (int i = 0; i < context.length; i++) {
			if (context[i] != null) {
				System.out.print(context[i] + ", ");
			}
		}
		System.out.println();
		System.out.print("cake Context Words:        ");
		context = GetContext(Cmatrix, GetWordIndex(Lexicon, "cake"), 10);
		for (int i = 0; i < context.length; i++) {
			if (context[i] != null) {
				System.out.print(context[i] + ", ");
			}
		}
		System.out.println();
		System.out.print("cloud Context Words:        ");
		context = GetContext(Cmatrix, GetWordIndex(Lexicon, "cloud"), 10);
		for (int i = 0; i < context.length; i++) {
			if (context[i] != null) {
				System.out.print(context[i] + ", ");
			}
		}
		System.out.println();
		// */

	}

	/**
	 * Method to return the index of a word based on alphabetically sorted Index
	 * 
	 * @param Lexicon Lexicon of words alphabetically sorted
	 * @param word    word of interest that we want the index of
	 * @return Index of word passed into method
	 */
	public static int GetWordIndex(ArrayList<String> Lexicon, String word) {
		int found = -1;
		for (int i = 0; i < Lexicon.size(); i++) {
			if (word.equals(Lexicon.get(i))) {
				found = i;
			}
		}
		return found;
	}

	/**
	 * Calculates Cosine Similarity of two words given their index in the lexicon
	 * and the Context Matrix
	 * 
	 * @param Cmatrix Context Matrix of PPMI values
	 * @param index1  index of first word
	 * @param index2  index of second word
	 * @return returns Cosine Similarity of the two words of the given indexesa
	 */
	public static float CalculateSimilarity(float[][] Cmatrix, int index1, int index2) {

		float numer = 0;
		for (int i = 0; i < Cmatrix.length; i++) {
			numer += Cmatrix[index1][i] * Cmatrix[index2][i];
		}
		float denom1 = 0;
		float denom2 = 0;
		for (int i = 0; i < Cmatrix.length; i++) {
			denom1 += Cmatrix[index1][i];
			denom2 += Cmatrix[index2][i];
		}
		return numer / ((float) Math.sqrt(denom1) * (float) Math.sqrt(denom2));
	}

	/**
	 * This method extracts the context words by performing Cosine Similarity on
	 * each combination of word with the word of the given index
	 * 
	 * @param Cmatrix Context Matrix of PPMI values
	 * @param index   index of word we want the context words for
	 * @param k       number of context words we want to return(returns null spots
	 *                if there are not enough context words)
	 * @return String vector of context words related to the word of the given index
	 */
	public static String[] GetContext(float[][] Cmatrix, int index, int k) {

		String[] context_vector = new String[k];
		HashMap<Float, Integer> valueindex = new HashMap<>();
		for (int i = 0; i < Lexicon.size(); i++) {
			valueindex.put(CalculateSimilarity(Cmatrix, index, i), i);
		}
		float[] sims = new float[valueindex.keySet().size()];
		int count = 0;
		for (float sim : valueindex.keySet()) {
			sims[count] = sim;
			count++;
		}
		Arrays.sort(sims);
		for (int i = 0; i < k; i++) {
			context_vector[i] = Lexicon.get(valueindex.get(sims[i]));
		}
		return context_vector;
	}

}
