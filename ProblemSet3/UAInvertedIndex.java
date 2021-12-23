import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;

/********************************
 * Name: Noah Buchanan Username: info03 Problem Set: PS3 Due Date: July 29, 2021
 ********************************/

public class UAInvertedIndex {
	public static final long DICT_REC_LENGTH = 30;
	public static final long POST_REC_LENGTH = 43;
	public static final int branch = 1000;
	public static HashMap<String, long[]> gh = new HashMap<>();
	
	/**
	 * 
	 * @param args input and output directory given from command line
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		BuildInvertedIndex(args[0], args[1]);
	}
	/**
	 * builds an inverted index based on tokenized input files, dict.raf and post.raf are stored in the given
	 * output_dir location given by the parameter. Builds .temp files from input and consolidates them into a smaller
	 * amount of subfiles then reads the first line from each and are sorted alphabetically this is how we fill post.raf
	 * immediately after dict.raf is filled from the global hash table.
	 * @param input_dir input directory of sub-directories containing tokenized files
	 * @param output_dir otuput directory to where dict.raf and post.raf will be written in
	 * @throws Exception
	 */
	public static void BuildInvertedIndex(String input_dir, String output_dir) throws Exception {

		// Initial pass through
		File input = new File(input_dir);
		File[] input_files = input.listFiles();
		int docid = 0;
		ArrayList<String> files = new ArrayList<>();
		long size = 0;

		for (File k : input_files) {

			File[] input_files_inner = k.listFiles();

			for (File f : input_files_inner) {

				files.add(f.getName());
				long total = 0;
				BufferedReader br = new BufferedReader(new FileReader(f.getPath()));
				HashMap<String, Integer> ht = new HashMap<>();
				String line = "";
				while ((line = br.readLine()) != null) {

					String[] split = line.split(" ");
					String word = split[split.length - 1];
					if (word.matches("[a-zA-Z]{1,15}")) {
						word = word.toLowerCase();
						if (word.length() > 10) {
							word = word.substring(0, 10);
						}

						if (ht.get(word) == null) {
							ht.put(word, 1);
						} else {
							ht.put(word, ht.get(word) + 1);
						}
						total++;
					}
				}
				br.close();
				for (String key : ht.keySet()) {
					if (gh.get(key) == null) {
						long[] postdoc = new long[2];
						postdoc[0] = -1;// postings location placeholder
						postdoc[1] = 1;// doc count
						// swapping the order simply for readability from doc count , posting to posting
						// , doc count
						gh.put(key, postdoc);
						// this is simply because hashmap.size() returns an integer instead of long
						// giving the possibility of overflowing
						size++;
					} else {
						long[] postdoc = gh.get(key);
						postdoc[1]++;
						gh.put(key, postdoc);
					}
				}

				TreeMap<String, Integer> sort = new TreeMap<>();
				sort.putAll(ht);
				BufferedWriter bw = new BufferedWriter(new FileWriter(new StringBuilder().append("temp/")
						.append(String.format("%06d", docid)).append(".temp").toString()));
				for (String term : sort.keySet()) {
					bw.write(new StringBuilder().append(String.format("%-10s", term))
							.append(String.format(" %06d", docid))
							.append(String.format(" %.15f", (double) ht.get(term) / total)).toString());
					bw.newLine();
				}
				bw.close();

				docid++;
			}
		}

		int tempcount = mergeSortFiles(docid);
		BufferedReader[] brs = new BufferedReader[tempcount];
	
		
		PriorityQueue<String> heap = new PriorityQueue<String>(new Comparator<String>() {
			public int compare(String i, String j) {
				return i.compareTo(j);
			}
		});
		
		int stop = 0;

		for(int i = 0; i < tempcount; i++) {
			StringBuilder build = new StringBuilder();
			build.append("temp/");
			build.append(String.format("%03d", i));
			build.append(".temp");
			brs[i] = new BufferedReader(new FileReader(build.toString()));
			if(brs[i] == null) {
				stop++;
				brs[i].close();
			} else {
				String line = brs[i].readLine();
				if(line == null) {
					stop++;
					brs[i].close();
				} else {
					heap.add(line);
				}
			}
		}
		RandomAccessFile post = new RandomAccessFile(output_dir + "/post.raf", "rw");
		long posting = 0;
		while (stop < brs.length) {
			String line = heap.remove();
			String[] split = line.split(" ");
			if (gh.get(split[0])[0] == -1) {
				long[] postdoc = gh.get(split[0]);
				postdoc[0] = posting;
				gh.put(split[0], postdoc);
			}
			post.seek(posting * POST_REC_LENGTH);
			StringBuilder build = new StringBuilder();
			double rtf = Double.parseDouble(split[split.length - 1]);
			double idf = Math.log(((double) docid / (double) gh.get(split[0])[1]) + 0.00000000000000000001);
			build.append(String.format("%-22s", files.get(Integer.parseInt(split[split.length - 2]))))
					.append(String.format(" %.15f", rtf * idf)).append("\n");
			post.writeUTF(build.toString());
			posting++;
			
			//loop criterion
			int doc_num = Integer.parseInt(split[split.length-2]);
			if(brs[doc_num/branch] == null) {
				brs[doc_num/branch].close();
				stop++;
			} else {
				line = brs[doc_num/branch].readLine();
				if(line == null) {
					brs[doc_num/branch].close();
					stop++;
				} else {
					heap.add(line);
				}
			}
			
		}
		post.close();

		DiskHashTable dict = new DiskHashTable(size * 2, DICT_REC_LENGTH, output_dir, true);

		for (Entry<String, long[]> entry : gh.entrySet()) {
			StringBuilder build = new StringBuilder();
			build.append(String.format("%-10s", entry.getKey())).append(String.format(" %09d", entry.getValue()[0]))
					.append(String.format(" %06d", entry.getValue()[1]));
			dict.put(build.toString());
		}
	}
	/**
	 * Consolidates temp files down to a smaller number so that all files first line can be fit in memory at once
	 * @param size the number of files processed
	 * @throws IOException
	 */
	public static int mergeSortFiles(int size) throws IOException {

		// 1024 is max amount of files to be open on server
		// we will use 1000 and consolodate into approx 840 consolidated files for real
		// thing
		PriorityQueue<String> heap = new PriorityQueue<String>(new Comparator<String>() {
			public int compare(String i, String j) {
				return i.compareTo(j);
			}
		});
		int iterations = (int) Math.ceil((float) size / branch);
		int count = 0;
		int bwcount = 0;
		for (int i = 0; i < iterations; i++) {

			BufferedReader[] brs = new BufferedReader[branch];
			int stop = 0;

			for (int j = 0; j < brs.length; j++) {

				StringBuilder build = new StringBuilder();
				// add .append("temp/") for server
				build.append("temp/").append(String.format("%06d", count)).append(".temp");
				if (count < size) {
					brs[j] = new BufferedReader(new FileReader(build.toString()));
					String line = brs[j].readLine();
					if (line == null) {
						stop++;
					} else {
						heap.add(line);
					}

				} else {
					stop++;
				}
				count++;

			}

			// same here add .append("temp/") for server
			BufferedWriter bw = new BufferedWriter(new FileWriter(new StringBuilder().append("temp/")
					.append(String.format("%03d", bwcount)).append(".temp").toString()));

			while (stop < brs.length) {

				String temp = heap.remove();
				String[] split = temp.split(" ");
				int doc_num = Integer.parseInt(split[split.length - 2]);

				bw.write(temp);
				bw.newLine();
				if ((temp = brs[doc_num - (i * branch)].readLine()) != null) {
					heap.add(temp);
				} else {
					brs[doc_num - (i * branch)].close();
					stop++;
				}

			}
			bw.close();
			bwcount++;
		}
		return bwcount;

	}

}
