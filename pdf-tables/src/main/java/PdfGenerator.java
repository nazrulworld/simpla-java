import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PdfGenerator {
	private final Path outputDir;
	private final Logger LOGGER = Logger.getLogger(PdfGenerator.class.getName());
	private final static Path RESOURCE_DIR =
			FileSystems.getDefault().getPath(
					".", "pdf-tables", "src", "main", "resources");
	private  final BaseFont BASE_FONT;
	private final Font HEADER_FONT;
	private final Font CELL_FONT;
	private final Font CELL_FONT_BOLD;
	private final Pattern B_TAG_PATTERN;
	private final float TABLE_MARGIN_TOP;

	public PdfGenerator(Path outputDir) throws IOException, DocumentException {
		this(outputDir, 50.0F);
	}

	public PdfGenerator(Path outputDir, float table_margin_top) throws IOException, DocumentException {
		BASE_FONT = BaseFont.createFont(
				Paths.get(
						RESOURCE_DIR.toAbsolutePath().toString(),
						"arial-unicode-ms.ttf").toString(),
				BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		HEADER_FONT = new Font(BASE_FONT, 14, Font.BOLD);
		CELL_FONT = new Font(BASE_FONT, 12);
		CELL_FONT_BOLD = new Font(BASE_FONT, 12, Font.BOLD);
		B_TAG_PATTERN = Pattern.compile("<b>(.*?)</b>");
		TABLE_MARGIN_TOP = table_margin_top;
		this.outputDir = outputDir;
	}

	private void generateDoc(String fileName, String pageTitle, Rectangle docSize, int perPage, ArrayList<ArrayList<String>> items) throws FileNotFoundException, DocumentException {

		Document document = new Document(docSize);
		document.addTitle(pageTitle);
		document.addCreator("https://github.com/nazrulworld/simpla-java/tree/main/pdf-tables");
		String filePathName = Paths.get(this.outputDir.toString(), fileName)
				.normalize().toAbsolutePath().toString();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePathName));
		writer.setPageEvent(new PageHeaderFooter(pageTitle, BASE_FONT));
		document.open();
		ArrayList<String> headColumns = items.get(0);
		PdfPTable pdfPTable = new PdfPTable(headColumns.size());
		addTableHeader(pdfPTable, headColumns);
		boolean tableIsOpen = true;
		for (int i=1; items.size() > i; i++){
			addRow(pdfPTable, items.get(i));
			if (i % perPage == 0){
				addTable(pdfPTable, document, writer);
				if (i==items.size()-1){
					// this is last
					tableIsOpen = false;
					break;
				}
				document.newPage();
				pdfPTable = new PdfPTable(headColumns.size());
				addTableHeader(pdfPTable, headColumns);
			}
		}
		if (tableIsOpen){
			addTable(pdfPTable, document, writer);
		}
		document.close();

	}
	private Phrase buildCellPhrase(String rawString){
		// We assume <b> is inside String
		if (!B_TAG_PATTERN.matcher(rawString).find()){
			return new Phrase(rawString, CELL_FONT);
		}
		rawString = rawString.replace("<b>", "~|").replace("</b>", "|~");
		Phrase phrase = new Phrase();
		for (String part: rawString.split("~")){
			if (part.equals("")){
				continue;
			}

			if (part.startsWith("|") && part.endsWith("|")){

				phrase.add(new Chunk(part.substring(1, (part.length() - 1)), CELL_FONT_BOLD));
			}
			else {
				phrase.add(new Chunk(part, CELL_FONT));
			}
		}
		return phrase;
	}
	private void addRow(PdfPTable pdfPTable, ArrayList<String> columns){
		for (String value : columns){
			PdfPCell cell = new PdfPCell();
			cell.setPaddingBottom(5.5F);
			cell.setPaddingLeft(2.5F);
			cell.setBorderWidth(0.15F);
			cell.setBorderColor(BaseColor.GRAY);
			cell.setPhrase(buildCellPhrase(value));
			pdfPTable.addCell(cell);
		}
	}

	private void addTable(PdfPTable table, Document document, PdfWriter writer){
		table.setTotalWidth(document.right() - document.left());
		table.writeSelectedRows(
				0, -1,
				document.right() - table.getTotalWidth(),
				document.top() - (TABLE_MARGIN_TOP + table.getPaddingTop()),
				writer.getDirectContent()
		);
	}

	private void addTableHeader(PdfPTable pdfPTable, ArrayList<String> columns){
		for (String colTitle : columns){
			PdfPCell headCell = new PdfPCell();
			headCell.setPaddingTop(5.5F);
			headCell.setPaddingBottom(10.0F);
			headCell.setPaddingLeft(2.5F);
			headCell.setBorderWidth(0.15F);
			headCell.setBorderColor(BaseColor.GRAY);
			headCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headCell.setPhrase(new Phrase(colTitle, HEADER_FONT));
			pdfPTable.addCell(headCell);
		}
	}
	public static void generate(DataResource dataResource, Path outputDir){
		try {
			PdfGenerator obj = new PdfGenerator(outputDir);
			obj.generateDoc("Adjektiver.pdf", "Det er meste brugte adjektiver",PageSize.A4.rotate() , 20, dataResource.getAdjektiverList());
			obj.generateDoc("Substantiver.pdf", "Det er meste brugte substantiver", PageSize.A4.rotate(), 20, dataResource.getSubstantiverList());
			obj.generateDoc("Verber.pdf", "Det er meste brugte verber" ,PageSize.A4.rotate(), 20, dataResource.getVerberList());
		} catch (DocumentException | IOException exc){
			exc.printStackTrace();
		}
	}
}

class PageHeaderFooter extends PdfPageEventHelper {

	private final BaseFont BASE_FONT;
	private final String pageTitle;

	public PageHeaderFooter(String pageTitle, BaseFont baseFont){
		this.pageTitle = pageTitle;
		BASE_FONT = baseFont;
	}

	private void drawHeader(PdfWriter writer, Document document){
		PdfPTable table = new PdfPTable(1);
		// write the header table
		PdfPCell cell = new PdfPCell(new Phrase(pageTitle, new Font(BASE_FONT, 16, Font.BOLD)));
		cell.setBorder(0);
		table.addCell(cell);
		table.setTotalWidth(document.right() - document.left());
		table.writeSelectedRows(
				0,
				-1,
				document.left(),
				document.top(),
				writer.getDirectContent()
		);
	}

	private void drawFooter(PdfWriter writer, Document document){

		PdfTemplate tplPageCounter = writer.getDirectContent().createTemplate(100, 100);

		PdfContentByte cb = writer.getDirectContent();
		float textBase = document.bottom() - 5;
		float adjust = BASE_FONT.getWidthPoint("0", 12);

		// compose page number inside the footer
		String text = "Page " + writer.getPageNumber();
		float textSize = BASE_FONT.getWidthPoint(text, 12);
		cb.beginText();
		cb.setFontAndSize(BASE_FONT, 12);
		cb.setTextMatrix(document.right() - textSize - adjust, textBase);
		cb.showText(text);
		cb.endText();
		cb.addTemplate(tplPageCounter, document.right() - adjust, textBase);

		// compose gitlab link
		String link = "https://github.com/nazrulworld/simpla-java/tree/main/pdf-tables";
		textSize = BASE_FONT.getWidthPoint(link, 12);
		PdfTemplate tplSourceLink = writer.getDirectContent().createTemplate(textSize, 100);
		cb.beginText();
		cb.setFontAndSize(BASE_FONT, 12);
		cb.setColorFill(BaseColor.GRAY);
		cb.setTextMatrix(document.left(), textBase);
		cb.showText(link);
		cb.endText();
		cb.addTemplate(tplSourceLink, document.left(), textBase);
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		// https://www.codota.com/web/assistant/code/rs/5c695cbd49efcb000177ba7e#L55
		drawHeader(writer, document);
		drawFooter(writer, document);
	}

}
