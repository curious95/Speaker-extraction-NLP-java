package speakerExtraction;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.fontbox.pfb.PfbParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class DetectText {
	public static void main(String[] args) {
		
		try (PDDocument document = PDDocument.load(new File("/home/groot/DevX/eclipse-projects/speakerExtraction/doc_final/1.pdf"))) {
			document.getClass();
			
			PDFTextStripperByArea stripper = new PDFTextStripperByArea();
			stripper.setSortByPosition(true);

			PDFTextStripper tStripper = new PDFTextStripper();

			String pdfFileInText = tStripper.getText(document);
			
			
			//FileUtils.writeStringToFile(new File("corpus_final/"+"2"+".txt"),pdfFileInText);

			
			
			// split by whitespace
			String corpus = "";
			/*String lines[] = pdfFileInText.split("\\r?\\n");
			for (String line : lines) {
				// System.out.println(line);
				corpus += " " + line;
			}	*/
			
			
			corpus=pdfFileInText.replaceAll("\\r?\\n", " ");

			corpus = corpus.toLowerCase();
			corpus = corpus.replaceAll(" +", " ").replaceAll("(- )", "").replace("-", "");


			FileUtils.writeStringToFile(new File("corpus_final/"+"2_etx"+".txt"),corpus);
			
			
			//System.out.println(corpus);
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			
		}
		
	}
}
