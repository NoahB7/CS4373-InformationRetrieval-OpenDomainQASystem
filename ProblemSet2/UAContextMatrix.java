import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

public class UAContextMatrix {

	public static HashMap<String, Integer> Freq;

	public static void main(String[] args) throws IOException {

		Freq = new HashMap<>();// 69500 roughly for actual thing
		// filling words to keep and words to drop
		HashMap<String, Integer> StopWords = new HashMap<>();
		HashMap<String, Integer> KeepWords = new HashMap<>();
		KeepWords.put("data", 0);
		KeepWords.put("computer", 0);
		KeepWords.put("cloud", 0);
		KeepWords.put("cake", 0);
		KeepWords.put("hot", 0);
		KeepWords.put("dog", 0);
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"));
		String line = "";
		while ((line = br.readLine()) != null) {
			StopWords.put(line, 0);
		}
		br.close();
		// filling freq with only the words we want based on keep and drop words
		// provided
		br = new BufferedReader(new FileReader("../IRPS1/frequency.txt"));
		while ((line = br.readLine()) != null) {
			String[] split = line.split(" ");
			if (KeepWords.get(split[split.length - 1].toLowerCase()) != null
					|| (count <= 70000 && StopWords.get(split[split.length - 1].toLowerCase()) == null
							&& Integer.parseInt(split[split.length - 2]) > 3)) {
				Freq.put(split[split.length - 1].toLowerCase(), Integer.parseInt(split[split.length - 2]));
			}
			count++;
		}
		br.close();
		// getting alphabetically sorted Array of Lexicon
		count = 0;
		String[] Lexicon = new String[Freq.keySet().size()];
		for (String key : Freq.keySet()) {
			Lexicon[count] = key;
			count++;
		}
		Arrays.sort(Lexicon);
		// filtering word.txt data into cleaned.txt and filling hashmap with bigrams and
		// respective frequencies
		HashMap<String, Integer> Bigrams = new HashMap<>();
		br = new BufferedReader(new FileReader(args[0]));
		BufferedWriter bw = new BufferedWriter(new FileWriter("cleaned.txt"));
		while ((line = br.readLine()) != null) {
			line = line.toLowerCase();
			if (Freq.get(line) != null) {
				StringBuilder build = new StringBuilder();
				build.append(line);
				build.append("\n");
				bw.write(build.toString());
			}
		}
		br.close();
		bw.close();
		br = new BufferedReader(new FileReader("cleaned.txt"));
		String prev = br.readLine();
		while ((line = br.readLine()) != null) {
			StringBuilder build = new StringBuilder();
			build.append(prev);
			build.append(" ");
			build.append(line);
			if (Bigrams.get(build.toString()) == null) {
				Bigrams.put(build.toString(), 1);
			} else {
				Bigrams.put(build.toString(), Bigrams.get(build.toString()) + 1);
			}
			prev = line;
		}
		br.close();

		// writing cmatrix to file and lexicon to file
		float[][] Cmatrix = BuildTermContextMatrix(Lexicon, Bigrams);
		bw = new BufferedWriter(new FileWriter(args[1] + "/cmatrix.txt"));
		for (int i = 0; i < Cmatrix.length; i++) {
			for (int j = 0; j < Cmatrix.length; j++) {
				StringBuilder string = new StringBuilder();
				string.append(Cmatrix[i][j]);
				string.append("\t");
				bw.write(string.toString());
			}
			bw.newLine();
		}
		bw.close();
		bw = new BufferedWriter(new FileWriter(args[1] + "/lexicon.txt"));
		for (int i = 0; i < Lexicon.length; i++) {
			bw.write(Lexicon[i]);
			bw.newLine();
		}
		bw.close();
	}

	/**
	 * Builds a Term Context Matrix of PPMI values using Pr(w1) Pr_a(w2) and
	 * Pr(w1,w2) for each word combination
	 * 
	 * @param Lexicon List of words being used post removal of stop words
	 * @param Bigrams list of unique bigrams and their respecting frequencies
	 * @return returns a context matrix of PPMI values
	 */
	public static float[][] BuildTermContextMatrix(String[] Lexicon, HashMap<String, Integer> Bigrams) {

		float total = 0;
		float weightedtotal = 0;
		float[][] Cmatrix = new float[Lexicon.length][Lexicon.length];
		// filling Cmatrix with frequency of each bigram and calculating the unweighted
		// total for probabilities later
		for (int i = 0; i < Lexicon.length; i++) {
			float subtotal = 0;
			for (int j = 0; j < Lexicon.length; j++) {
				StringBuilder string = new StringBuilder();
				string.append(Lexicon[i]);
				string.append(" ");
				string.append(Lexicon[j]);
				if (Bigrams.get(string.toString()) != null) {
					Cmatrix[i][j] = Bigrams.get(string.toString());
				} else {
					Cmatrix[i][j] = 0;
				}
				subtotal += Cmatrix[i][j];
			}
			total += subtotal;
		}

		// calculating the weighted probabilities of the columns
		for (int i = 0; i < Lexicon.length; i++) {
			float subtotal = 0;
			for (int j = 0; j < Lexicon.length; j++) {
				subtotal += Cmatrix[j][i];
			}
			weightedtotal += Math.pow(subtotal, .75);
		}

		// finishing the math using our precalculated unweighted and weightedtotal for
		// pr_a(w2) and pr(w1) and pr(w1,w2)
		for (int i = 0; i < Lexicon.length; i++) {
			for (int j = 0; j < Lexicon.length; j++) {
				// Pr(w1,w2) //Pr(w1) //Pr_a(w2)
				Cmatrix[i][j] = (Cmatrix[i][j] / total) / ((float) (Freq.get(Lexicon[i]) / total)
						* (float) ((float) Math.pow(Freq.get(Lexicon[j]), 0.75) / weightedtotal));
				// log_2
				Cmatrix[i][j] = (float) (Math.log10(Cmatrix[i][j]) + 0.00000000000000001) / (float) (Math.log10(2));
				// max function
				if (Cmatrix[i][j] < 0) {
					Cmatrix[i][j] = 0;
				}
			}
		}

		return Cmatrix;
	}

}