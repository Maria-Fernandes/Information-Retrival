import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class InvertedIndexProgram{
        public static class InvertedIndexMapperProgram extends Mapper<LongWritable, Text, Text, Text>
        {
                private Text keyword = new Text();
                @Override
                public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
                {
                   String line = value.toString();
                   String splitData[] = line.split("\t", 2);
                   Text documentId = new Text(splitData[0]);
		   String filteredString=splitData[1].replaceAll("[^a-zA-Z]", " ").toLowerCase();
                   StringTokenizer tokenizer = new StringTokenizer(filteredString);
                   while(tokenizer.hasMoreTokens())
                   {
                    keyword.set(tokenizer.nextToken());
                    context.write(keyword, documentId);
                   }
                }
        }


        public static class InvertedIndexReducerProgram extends Reducer<Text, Text, Text, Text>
        {
                @Override
                public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
                {
                         HashMap<String,Integer> counter = new HashMap<String,Integer>();
                         for(Text val: values)
                         {
                                String value = val.toString();
                                if(counter.containsKey(value))
                                 {
                                        counter.put(value,new Integer(counter.get(value)+1));
                                 }else{
								 
                                         counter.put(value,  new Integer(1));
                                         }
                         }
                         StringBuilder stringBuilder = new StringBuilder("");
                         for(String val: counter.keySet())
                         {
                                 stringBuilder.append(val+":"+counter.get(val)+" ");
                         }
                         context.write(key, new Text(stringBuilder.toString()));
                }
        }
        public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException
        {
                if(args.length < 2)
                {
                        System.out.println("Usage Word Count <input path> <output path>");
                        System.exit(-1);
                }else{
                        Configuration conf = new Configuration();
                        Job job = Job.getInstance(conf, "word count");
                        job.setJarByClass(InvertedIndexProgram.class);
                        job.setMapperClass(InvertedIndexMapperProgram.class);
                        job.setReducerClass(InvertedIndexReducerProgram.class);
                        job.setMapOutputKeyClass(Text.class);
                        job.setMapOutputValueClass(Text.class);
                        job.setOutputKeyClass(Text.class);
                        job.setOutputValueClass(Text.class);
                        FileInputFormat.addInputPath(job, new Path(args[0]));
                        FileOutputFormat.setOutputPath(job, new Path(args[1]));
                        System.exit(job.waitForCompletion(true)? 0 : 1);
                }
        }
}
