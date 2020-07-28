package es.rcs.tfm.srv;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatsProcessor {

	public static void main(String[] args) {
		
		try {

			String dirUpload = "J:\\Workspace-TFM\\TFM\\es.rcs.tfm\\es.rcs.tfm.corpus\\stats";
			String datafile = "J:\\Workspace-TFM\\TFM\\es.rcs.tfm\\es.rcs.tfm.corpus\\stats.txt";
			StringBuffer results = new StringBuffer();
			Files.walk(
				Paths
					.get(dirUpload ))
		            .filter(Files::isRegularFile)
		            .forEach(path -> {
						try {
							String content = StatsProcessor.getData(path);
							String data = StatsProcessor.convert(path.getFileName(), content);
							results.append(data);
							results.append("\r\n");
						} catch (IllegalArgumentException ex) {
							System.out.println("StatsProcessor ... file error ex: " + ex.toString());
						}
		            });			

			FileWriter fileWriter = new FileWriter(datafile);
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    printWriter.print(results.toString());
		    printWriter.close();
		    
		} catch (IOException ex) {
			System.out.println("StatsProcessor ... file error ex: " + ex.toString());
		} catch (Exception ex) {
			System.out.println("StatsProcessor ... file error ex: " + ex.toString());
		} finally {
		}

	}

	/*
		Name of the selected graph: D:\Workspace-TFM\TFM\es.rcs.tfm\es.rcs.tfm.corpus\models\tensorflow\blstm_10_1024_128_120.pb
		Training started - total epochs: 150 - lr: 0.01 - batch size: 8 - labels: 8 - chars: 58 - training examples: 13833
	 */
	private static final Pattern GET_METADATA = Pattern.compile("^.*blstm(.+)\\r?\\n.*epochs\\:\\s*(\\d+).*lr\\:\\s*(\\d+)\\.(\\d+).*batch size\\:\\s*(\\d+).*labels\\:\\s*(\\d+).*chars\\:\\s*(\\d+).*training examples\\:\\s*(\\d+).*(?:\\r?\\n)+");
	private static final Pattern GET_INFO = Pattern.compile("TRAIN:\\s+(.*)(?:.*[\\r\\n]*)+?TEST:\\s+.*(?:.*[\\r\\n]*)+?BERT:\\s+(.*)(?:.*[\\r\\n]*)+?NER:\\s+(DL\\d+_+LR_(\\d+_\\d+)_+PO_(\\d+)(?:,(\\d+))+_+?DROP_(\\d+_\\d*))");
	private static final Pattern GET_DATA = Pattern.compile("Epoch\\s+(\\d+)/(?:\\d+)\\s+started,\\s+lr:\\s+([E\\d\\.\\-]*)(?:.*[\\r\\n])+?Epoch\\s+(?:\\d+)/(?:\\d+)\\s+-\\s+([\\d\\.,]*)s\\s+-\\s+loss:\\s+([E\\d\\.\\-]*)\\s+-\\s+batches:\\s+([E\\d\\.\\-]*)(?:.*[\\r\\n])+?Quality on validation(?:.*[\\r\\n])+?tp:\\s+(\\d+)\\s+fp:\\s+(\\d+)\\s+fn:\\s+(\\d+)(?:.*[\\r\\n])+?Macro-average\\s+prec:\\s+([E\\d\\.\\-]*),\\s+rec:\\s+([E\\d\\.\\-]*),\\s+f1:\\s+([E\\d\\.\\-]*)(?:.*[\\r\\n])+?Micro-average\\s+prec:\\s+([E\\d\\.\\-]*),\\s+rec:\\s+([E\\d\\.\\-]*),\\s+f1:\\s+([E\\d\\.\\-]*)(?:.*[\\r\\n])+?Quality on test(?:.*[\\r\\n])+?tp:\\s+(\\d+)\\s+fp:\\s+(\\d+)\\s+fn:\\s+(\\d+)(?:.*[\\r\\n])+?Macro-average\\s+prec:\\s+([E\\d\\.\\-]*),\\s+rec:\\s+([E\\d\\.\\-]*),\\s+f1:\\s+([E\\d\\.\\-]*)(?:.*[\\r\\n])+?Micro-average\\s+prec:\\s+([E\\d\\.\\-]*),\\s+rec:\\s+([E\\d\\.\\-]*),\\s+f1:\\s+([E\\d\\.\\-]*)");
	private static String convert(Path path, String content) {

		String filename = path.getFileName().toString();
		Matcher matcher = null;
		
		String graph = "";
		String numEpochs = "";
		String lr = "";
		String batchSize = "";
		String labels = "";
		String chars = "";
		String samples = "";
		matcher = GET_METADATA.matcher(content);
		if (matcher.find()) {
			graph = "blstm" + matcher.group(1);
			numEpochs = matcher.group(2);
			lr = matcher.group(3) + "," + matcher.group(4);
			batchSize = matcher.group(5);
			labels = matcher.group(6);
			chars = matcher.group(7);
			samples = matcher.group(8);
			content = matcher.replaceAll("");
		}

		String corpus = "";
		String bertmodel = "";
		String lrIni = "";
		String po = "";
		String dropout = "";
		matcher = GET_INFO.matcher(content);
		if (matcher.find()) {
			corpus = matcher.group(1);
			bertmodel = matcher.group(2);
			lrIni = matcher.group(3) + "," + matcher.group(4);
			po = matcher.group(5) + "," + matcher.group(6);
			dropout = matcher.group(7) + "," + matcher.group(8);
			content = matcher.replaceAll("");
		}

		matcher = GET_DATA.matcher(content);
		if (matcher.find()) {
			content = matcher.replaceAll(filename + "\t$1\t$2\t$3\t$4\t$5\t$6\t$7\t$8\t$9\t$10\t$11\t$12\t$13\t$14\t$15\t$16\t$17\t$18\t$19\t$20\t$21\t$22\t$23");
		}

		String result = null;
		result = content.replaceAll("(\r?\n){2,}+", "\r\n");
		result = result.replaceAll("(\\d)\\.(\\d)", "$1,$2");

		return result;

	}

	private static String getData(Path path) {

		String content = null;
		try (Stream<String> lines = Files.lines(path)) {

            // Formatting like \r\n will be lost
            // String content = lines.collect(Collectors.joining());

            // UNIX \n, WIndows \r\n
            content = lines.collect(Collectors.joining(System.lineSeparator()));

			// File to List
            //List<String> list = lines.collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }		
		return content;
		
	}

}
