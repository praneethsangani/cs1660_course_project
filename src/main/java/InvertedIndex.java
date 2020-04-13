import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

public class InvertedIndex {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        if (args.length != 2) {
            System.err.println("Usage: Inverted Index <input path> <output path>");
            System.exit(-1);
        }

        Job job = new Job();
        job.setJarByClass(InvertedIndex.class);
        job.setJobName("Inverted Index");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.setMapperClass(InvertedIndexMapper.class);
        job.setReducerClass(InvertedIndexReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.waitForCompletion(true);
    }

    public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
        private Text docID = new Text();
        private Text word = new Text();

        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString().replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase(); // Match anything but letters, numbers, and whitespace
            StringTokenizer tokenizer = new StringTokenizer(line);
            String fileId = ((FileSplit) context.getInputSplit()).getPath().getName();
            docID.set(fileId);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                context.write(word, docID);
            }
        }
    }

    public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            HashMap<Text, Integer> invertedIndex = new HashMap<>();
            for (Text value : values) {
                invertedIndex.put(value, invertedIndex.getOrDefault(value, 0) + 1);
            }
            for (Text value : invertedIndex.keySet()) {
                context.write(value, new Text(invertedIndex.get(value).toString()));
            }
        }
    }
}
