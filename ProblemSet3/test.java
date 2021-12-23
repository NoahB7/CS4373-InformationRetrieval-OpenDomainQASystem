import java.io.RandomAccessFile;

public class test {
	
	public static final long POST_REC_LENGTH = 43;
	
	public static void main(String[] args) throws Exception{
		
		
		RandomAccessFile post = new RandomAccessFile("output/post.raf","rw");
		//                                MAKE SURE THIS IS FALSE            V
		double filecount = 5;
		for(long i =0; i < post.length()/POST_REC_LENGTH; i++) {
			post.seek(i*POST_REC_LENGTH);
			String line = post.readUTF();
			String[] split = line.split(" ");
			split[split.length-1] = split[split.length-1].substring(0,split[split.length-1].length()-1);
			double val = Double.parseDouble(split[split.length-1]);
			val = val * filecount;
			double rtfidf = Math.log((filecount/val) +0.00000000000000000001);
			System.out.println();
			StringBuilder build = new StringBuilder();
			build.append(String.format("%-22s", split[0]))
			.append(String.format(" %.15f", rtfidf)).append("\n");
			post.writeUTF(build.toString());
		}
		
	}
}
