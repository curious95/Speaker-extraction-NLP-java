package speakerExtraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Soundbank;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class ExtractSpeackers {

	static FileWriter fwr = null;
	static String ROW2 = "ID|Sentence|Speaker";

	public static final String delimiter = "\\|";

	static int i = 0, j = 0, k = 0, trueCOunter = 0, fasleCounter = 0;

	public static void read(String csvFile) {

		try {

			File file = new File(csvFile);
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line = "";

			String[] tempArr;
			while ((line = br.readLine()) != null) {
				tempArr = line.split(delimiter);

				// System.out.println(tempArr[0].replace("\"", ""));
				if (i != 0 /*&& tempArr[0].replace("\"", "").equals("22575")*/) {
					// System.out.println(tempArr[0]);
					k++;
					//System.out.println(k + "th record Processing");
					// System.out.println(tempArr[2].substring(1, tempArr[2].length()-1));
					processRecord(tempArr[0].replace("\"", ""), tempArr[1].replace("\"", "").replace("_copy", ""),
							tempArr[2].substring(1, tempArr[2].length() - 1));
				} else{
					i = 1;
				}

			}
			br.close();

			//System.out.println("True : " + trueCOunter);
			//System.out.println("False : " + fasleCounter);
			//System.out.println(k);

		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

	public static void processRecord(String ID, String URL, String sentence) {

		//System.out.println("Processing Record : " + ID);

		File tempFile = new File("doc_final/" + ID + ".pdf");
		Boolean exists = false;
		//boolean exists = tempFile.exists();

		if (!exists) {
			try {
				HttpResponse<InputStream> response = Unirest.get(URL).header("cache-control", "no-cache")
						.header("postman-token", "57d24d93-4c45-6320-73e1-f03bc072c116")
						.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
						.header("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3").header("Accept-Encoding", "none")
						.header("Accept-Language", "en-US,en;q=0.8").header("Connection", "keep-alive").asBinary();

				FileUtils.copyInputStreamToFile(response.getBody(), new File("doc_final/" + ID + ".pdf"));
				//System.out.println("File Downloaded : " + ID);
			} catch (Exception e) {
					
				e.printStackTrace();
				//System.out.println("Error in downloading file with ID : " + ID);
				
				
			}
		} else {
			//System.out.println("File Present Download Skipping : " + ID);
		}

		// System.out.println(ID);
		try (PDDocument document = PDDocument.load(new File("doc_final/" + ID + ".pdf"))) {

			document.getClass();

			if (!document.isEncrypted() && j == 0) {

				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);

				PDFTextStripper tStripper = new PDFTextStripper();

				String pdfFileInText = tStripper.getText(document);
				// System.out.println("Text:" + st);

				// split by whitespace
				String corpus = "";

				
				 /* String lines[] = pdfFileInText.split("\\r?\\n"); for (String line : lines) {
				  System.out.println(line); corpus += " " + line; }*/
				 

				corpus = pdfFileInText.replaceAll("\\r?\\n", " ");
				corpus = corpus.toLowerCase();

				// System.out.println(corpus);
				corpus = corpus.replaceAll(" +", " ").replaceAll("(- )", "").replace("-", "").replace("—", "");

				// Writing corpus for testing
				FileUtils.writeStringToFile(new File("corpus_final/" + ID + ".txt"), corpus);

				// System.out.println(sentence);
				// System.out.println(sentence.substring(1, sentence.length() - 1));

				String query = sentence.substring(0, sentence.length() - 1).replace("'", "’").replaceAll(" +", " ")
						.replace("-", "").replace("—", "").toLowerCase();

				// System.out.println(query);

				if (query.length() > 100) {
					query = query.substring(0, 50);
				}

				// query = query.substring(1,50);
				System.out.println(query);
				boolean status = corpus.contains(query);
				// System.out.println("dasdasd "+corpus.indexOf(query));
				// System.out.println("Queryyyyyyyyyy === "+query);
				System.out.println(status);

				if (status) {
					findSpeaker(corpus, query, ID, sentence);
				} else if (!status) {

					status = corpus.replaceAll("[^a-zA-Z0-9\\. ]", "").contains(query.replaceAll("[^a-zA-Z0-9 ]", ""));
					System.out.println("asds "
							+ corpus.replaceAll("[^a-zA-Z0-9\\. ]", "").indexOf(query.replaceAll("[^a-zA-Z0-9 ]", "")));
					System.out.println(query.replaceAll("[^a-zA-Z0-9 ]", ""));
					System.out.println(status);
					if (status) {
						findSpeaker(corpus.replaceAll("[^a-zA-Z0-9\\. ]", ""), query.replaceAll("[^a-zA-Z0-9 ]", ""),
								ID, sentence);
					}

				}

				// System.out.println(corpus.indexOf(query));

				if (status) {
					trueCOunter++;
				} else {
					fasleCounter++;
				}

				j = 0;
			}

		} catch (Exception e) {
			System.out.println(ID+"     "+URL);
			//e.printStackTrace();
			// TODO: handle exception
		}
	}

	public static void findSpeaker(String corpus, String statement, String ID, String Sentence) {

		// System.out.println(ID);

		corpus = corpus.substring(1, corpus.indexOf(statement));
		// System.out.println(corpus.indexOf(statement));
		String lastMatch = "";
		// Matcher m = Pattern.compile("(mrs. |mr. |ms.)[a-z of]*\\.|(mrs. |mr. |ms.
		// )[a-z]*\\.( mr. president,| madam speaker,| madam chair,| mr. speaker,| madam
		// president,)").matcher(corpus);
		Matcher m = Pattern.compile(
				"(mr.|ms.|mrs.) (([a-z])*|(([a-z])* (of) ([a-z])*)|(([a-z])* (of) ([a-z])* ([a-z])*))\\.|(mr.|ms.|mrs.) (([a-z])*|(([a-z])* (of) ([a-z])*)|(([a-z])* (of) ([a-z])* ([a-z])*))\\.( mr. president,| madam speaker,| madam chair,| mr. speaker,| madam president,)")
				.matcher(corpus);
		while (m.find()) {
			m.group();
			if (!m.group().equals("ms. sec.") && !m.group().contains(",")) {
				lastMatch = m.group();
				//System.out.println(lastMatch);
			}
			
		}

		int k = lastMatch.indexOf(".", lastMatch.indexOf(".") + 1);

		try {
			lastMatch = lastMatch.substring(0, k);
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println(lastMatch);
		try {
			fwr.write(ID + "|" + Sentence + "|"
					+ lastMatch.toUpperCase().replace("MRS. ", "Mrs. ").replace("MR. ", "Mr. ").replace("MS. ", "Ms. ")
					+ "\n");
			fwr.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(lastMatch);

	}

	public static void main(String[] args) {

		// logger settings
		String[] loggers = { "org.apache.pdfbox.util.PDFStreamEngine", "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
				"org.apache.pdfbox.pdmodel.font.PDFont", "org.apache.pdfbox.pdmodel.font.FontManager",
				"org.apache.pdfbox.pdfparser.PDFObjectStreamParser" };
		for (String logger : loggers) {
			org.apache.log4j.Logger logpdfengine = org.apache.log4j.Logger.getLogger(logger);
			logpdfengine.setLevel(org.apache.log4j.Level.OFF);
		}

		// csv writer
		try {
			fwr = new FileWriter("copy_speakrs_2.csv", true);
			fwr.write(ROW2 + "\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// csv file to read
		try {
			// System.out.println("kamal");
			String csvFile = "/home/groot/DevX/eclipse-projects/speakerExtraction/Copy_speakers.csv";
			// System.out.println(csvFile);
			// String csvFile =
			// "/home/groot/DevX/eclipse-projects/speakerExtraction/Multiple Speakers.csv";
			ExtractSpeackers.read(csvFile);
			fwr.close();
		} catch (Exception e) {
			e.printStackTrace();

			// TODO: handle exception
		}
	}

}
