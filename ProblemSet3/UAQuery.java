import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/********************************
 * Name: Noah Buchanan Username: info03 Problem Set: PS3 Due Date: July 29, 2021
 ********************************/

public class UAQuery extends UAInvertedIndex {
	
	/**
	 * 
	 * @param args input query from command line
	 * @throws Exception
	 */

	public static void main(String[] args) throws Exception {

		// bad way of tokenizing it into just words but the pattern matcher class would
		// not work
		ArrayList<String> tokenized = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			String line = "";
			for (int j = 0; j < args[i].length(); j++) {
				if ((args[i].charAt(j) >= 65 && args[i].charAt(j) <= 90)
						|| args[i].charAt(j) >= 97 && args[i].charAt(j) <= 122) {
					line += args[i].charAt(j);
				} else {
					if (!line.equals(""))
						tokenized.add(line);
					line = "";
				}
			}
			if (!line.equals(""))
				tokenized.add(line);
		}
		String[] query = new String[tokenized.size()];
		for (int i = 0; i < tokenized.size(); i++) {
			query[i] = tokenized.get(i);
		}

		String[] results = RunQuery(query);
		for (String s : results) {
			System.out.println(s);
		}

	}
	
	/**
	 * Truncates and converts input query words to lowercase, then fetches the postings list for each individual word
	 * and builds a list of doc -> {term,rtf-idf} for an easy way of calculating the cosine similarity of the query in
	 * relation to the document, then returns the top k documents ranked, k is 10 hard coded at this point because I 
	 * see no reason as of now to make it dynamic
	 * @param query Input array of words that represents the query to the inverted index
	 * @return returns the top k ranked documents
	 * @throws Exception
	 */
	public static String[] RunQuery(String[] query) throws Exception {

		for (int i = 0; i < query.length; i++) {
			if (query[i].length() > 10)
				query[i] = query[i].substring(0, 10);

			query[i] = query[i].toLowerCase();
		}

		RandomAccessFile dict = new RandomAccessFile("output/dict.raf", "rw");
		RandomAccessFile post = new RandomAccessFile("output/post.raf", "rw");
		// MAKE SURE THIS IS FALSE V
		DiskHashTable hash = new DiskHashTable(dict.length() / DICT_REC_LENGTH, DICT_REC_LENGTH, "output", false);
		HashMap<String, HashMap<String, Double>> HMDT = new HashMap<>();

		Double[] RTFIDFQuery = new Double[query.length];
		HashMap<String, Integer> count = new HashMap<>();
		for (String term : query) {
			if (count.get(term) == null) {
				count.put(term, 1);
			} else {
				count.put(term, count.get(term) + 1);
			}
		}

		for (int i = 0; i < query.length; i++) {
			RTFIDFQuery[i] = (double) count.get(query[i]) / (double) count.size(); // just RTF so far
			String line = hash.get(query[i]);
			String[] split = line.split(" ");
			String term = split[0];

			RTFIDFQuery[i] *= Math.log(839917 / Long.parseLong(split[split.length - 1]) + 0.000000000001); // IDF
			long posting = Long.parseLong(split[split.length - 2]);
			long doccount = Long.parseLong(split[split.length - 1].substring(0, split[split.length - 1].length()));
			for (long j = 0; j < doccount; j++) {
				post.seek(posting * POST_REC_LENGTH + j * POST_REC_LENGTH);
				line = post.readUTF();
				split = line.split(" ");
				String doc = split[0];
				double RI = Double.parseDouble(split[split.length - 1]);

				if (HMDT.get(doc) == null)
					HMDT.put(doc, new HashMap<String, Double>());

				if (HMDT.get(doc).get(term) == null)
					HMDT.get(doc).put(term, RI);

			}
		}

		HashMap<String, Double> cosvals = new HashMap<>();
		

		for (String doc : HMDT.keySet()) {
			// summation of A_i * B_i
			double numerator = 0;
			// sqrt(summation of A_i^2) * sqrt(summation of B_i^2)
			double denominator = 0;
			// summation of A_i^2
			double abssum1 = 0;
			// summation of B_i^2
			double abssum2 = 0;
			//int j = 0;
			for(int j = 0; j < query.length; j++) {
				if(HMDT.get(doc).get(query[j]) != null) 
					numerator += HMDT.get(doc).get(query[j]) * RTFIDFQuery[j];
				abssum1 += Math.pow(RTFIDFQuery[j], 2);
				if(HMDT.get(doc).get(query[j]) != null) 
					abssum2 += Math.pow(HMDT.get(doc).get(query[j]), 2);
				
			}
			//for (String term : HMDT.get(doc).keySet()) {
			//	numerator += HMDT.get(doc).get(term) * RTFIDFQuery[j];
			//	abssum1 += Math.pow(RTFIDFQuery[j], 2);
			//	abssum2 += Math.pow(HMDT.get(doc).get(term), 2);
			//	j++;
			//}
			// sqrt(summation of A_i^2)
			abssum1 = Math.sqrt(abssum1);
			// sqrt(summation of B_i^2)
			abssum2 = Math.sqrt(abssum2);
			// sqrt(summation of A_i^2) * sqrt(summation of B_i^2)
			denominator = abssum1 * abssum2;
			// cosine similarity of query and document
			cosvals.put(doc, numerator / denominator);
		}

		dict.close();
		post.close();
		return topK(cosvals, 10);
	}
	/**
	 * This method uses simple logic to return the top k documents from a hash map of document -> cosine similarity score
	 * @param map A hashmap of documents and their matching cosine similarity score on the query, this is how they will be
	 * ranked
	 * @param k this represents how many documents we want to return "the top k" documents
	 * @return returns top k document names, an array of Strings
	 */
	public static String[] topK(HashMap<String, Double> map, int k) {

		String[] topk = new String[k];
		for (int i = 0; i < map.size(); i++) {
			double max = 0;
			String insert = "";
			for (String key2 : map.keySet()) {
				if (map.get(key2) > max) {
					max = map.get(key2);
					insert = key2;
				}
			}
			map.remove(insert);
			topk[i] = insert;
			if (i >= topk.length - 1)
				return topk;
		}
		return topk;
	}

}
