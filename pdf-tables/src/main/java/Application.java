import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class Application {
	private final static Path OUTPUT_DIR = FileSystems.getDefault().getPath(".", "output");
	private final static Path INPUT_DIR =
			FileSystems.getDefault().getPath(
					".", "pdf-tables", "src", "main", "resources");
	public static void main(String[] args) throws IOException, DocumentException {
		Path jsonFilePath = Paths.get(INPUT_DIR.toString(), "resource.json");
		byte[] jsonData = Files.readAllBytes(jsonFilePath);
		ObjectMapper objectMapper = new ObjectMapper();
		DataResource dataResource = objectMapper.readValue(jsonData, DataResource.class);
		//System.out.println(dataResource.getAdjektiverList());
		PdfGenerator.generate(dataResource, OUTPUT_DIR);
		//Document pdfDoc = new Document(PageSize.A4);
		//PdfWriter.getInstance(pdfDoc, new FileOutputStream("out.pdf"));
		//generate(pdfDoc);

	}

	public static void  generate(Document document) throws DocumentException {
		//PdfWriter.getInstance(document)
		document.open();
		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
		Chunk chunk = new Chunk("Hello world", font);
		document.add(chunk);
		PdfPTable pdfPTable = new PdfPTable(3);
		Stream.of("Col1", "Col2", "Col3").forEach(colTitle ->{
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(colTitle));
			pdfPTable.addCell(header);
		});
		pdfPTable.addCell("row 1 col1");
		pdfPTable.addCell("row 1 col2");
		pdfPTable.addCell("row 1 col3");
		document.add(pdfPTable);
		document.close();

	}

}
// https://www.baeldung.com/java-pdf-creation
// https://www.journaldev.com/2324/jackson-json-java-parser-api-example-tutorial
// table.setFixedPosition(ps.getWidth() - doc.getRightMargin() - totalWidth, ps.getHeight() - doc.getTopMargin() - totalHeight, totalWidth);
// PageSize ps = pdfDoc.getDefaultPageSize();
//	IRenderer tableRenderer = table.createRendererSubTree().setParent(doc.getRenderer());
//	LayoutResult tableLayoutResult =
//			tableRenderer.layout(new LayoutContext(new LayoutArea(0, new Rectangle(ps.getWidth(), 1000))));
//	float totalHeight = tableLayoutResult.getOccupiedArea().getBBox().getHeight();
//
//PdfContentByte canvas = writer.getDirectContent();
//table.writeSelectedRows(0, -1, document.right() - tablewidth, document.top(), canvas);
// https://kb.itextpdf.com/home/it7kb/faq/how-do-i-insert-a-hyperlink-to-another-page-in-an-existing-pdf
