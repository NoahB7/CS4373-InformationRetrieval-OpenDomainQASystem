import java.io.RandomAccessFile;

/********************************
 * Name: Noah Buchanan Username: info03 Problem Set: PS3 Due Date: July 29, 2021
 ********************************/

public class DiskHashTable {

	public long size;
	public RandomAccessFile dict;
	public long rec_length;
	/**
	 * Constructor for initialization and reuse of initialized dict file
	 * @param size size of dict.raf file in terms of lines
	 * @param rec_length byte size of rec_length included 2 bytes for unicode character and 1 for new line
	 * @param output_dir directory where dict.raf will be stored in
	 * @param init declares whether to initialize or reuse initialized dict.raf. Important: make sure to set false if
	 * you are to use this for searches instead of creating dict.raf or it will reinitialize the entire dict file back to "-1" lines
	 * @throws Exception
	 */
	public DiskHashTable(long size, long rec_length, String output_dir, boolean init) throws Exception {

		if (init) {
			dict = new RandomAccessFile(output_dir + "/dict.raf", "rw");
			this.rec_length = rec_length;
			this.size = size;

			StringBuilder pad = new StringBuilder();
			pad.append("-1");
			for (int i = 0; i < rec_length - 5; i++) {
				pad.append(" ");
			}
			pad.append("\n");

			for (long i = 0; i < size; i++) {
				dict.seek(i * rec_length);
				dict.writeUTF(pad.toString());
			}
		} else {
			dict = new RandomAccessFile(output_dir + "/dict.raf", "rw");
			this.rec_length = rec_length;
			this.size = size;
		}

	}

	public void put(String input) throws Exception {

		String[] split = input.split(" ");
		seek(split[0]);
		dict.writeUTF(input);
	}

	public String get(String input) throws Exception {

		String[] split = input.split(" ");
		seek(split[0]);
		return dict.readUTF();
	}

	public void seek(String term) throws Exception {

		boolean found = false;
		boolean breaker = false;
		long hash = Math.abs(term.hashCode());
		while (!found && !breaker) {

			long location = ((hash % (size)) * rec_length);
			dict.seek(location);
			String line = dict.readUTF();

			if (line.split(" ")[0].equals(term)) {
				found = true;
			}

			if (line.split(" ")[0].equals("-1")) {
				breaker = true;
			}

			if (!breaker && !found) {
				hash++;
			} else {
				dict.seek(location);
			}
		}
	}

}
