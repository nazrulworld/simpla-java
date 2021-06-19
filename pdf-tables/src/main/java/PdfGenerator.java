import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class PdfGenerator {
	private final Path outputDir;
	private final Logger LOGGER = Logger.getLogger(PdfGenerator.class.getName());
	private final Font headerFont;
	private final Font cellFont;
	private final Font cellFontBold;
	private final Pattern bTagPattern;

	public PdfGenerator(Path outputDir) {
		this.outputDir = outputDir;
		headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
		cellFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
		cellFontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
		bTagPattern = Pattern.compile("<b>(.*?)</b>");
	}
	private void generateDoc(String fileName, Rectangle docSize, int perPage, ArrayList<ArrayList<String>> items) throws FileNotFoundException, DocumentException {
		LOGGER.info("Start logging");
		Document document = new Document(docSize);
		String filePathName = Paths.get(this.outputDir.toString(), fileName)
				.normalize().toAbsolutePath().toString();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePathName));
		writer.setPageEvent(new PageHeaderFooter("My Page Title"));
		document.open();
		ArrayList<String> headColumns = items.get(0);
		PdfPTable pdfPTable = new PdfPTable(headColumns.size());
		addTableHeader(pdfPTable, headColumns);
		for (int i=1; items.size() > i; i++){
			addRow(pdfPTable, items.get(i));
			if (i % perPage == 0){
				document.add(pdfPTable);
				document.newPage();
				pdfPTable = new PdfPTable(headColumns.size());
				addTableHeader(pdfPTable, headColumns);
			}
		}
		document.close();

	}
	private Phrase buildCellPhrase(String rawString){
		// We assume <b> is inside String
		if (!bTagPattern.matcher(rawString).find()){
			return new Phrase(rawString, cellFont);
		}
		rawString = rawString.replace("<b>", "~|").replace("</b>", "|~");
		Phrase phrase = new Phrase();
		for (String part: rawString.split("~")){
			if (part.equals("")){
				continue;
			}

			if (part.startsWith("|") && part.endsWith("|")){

				phrase.add(new Chunk(part.substring(1, (part.length() - 1)), cellFontBold));
			}
			else {
				phrase.add(new Chunk(part, cellFont));
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

	private void addTableHeader(PdfPTable pdfPTable, ArrayList<String> columns){
		for (String colTitle : columns){
			PdfPCell headCell = new PdfPCell();
			headCell.setPaddingTop(5.5F);
			headCell.setPaddingBottom(10.0F);
			headCell.setPaddingLeft(2.5F);
			headCell.setBorderWidth(0.15F);
			headCell.setBorderColor(BaseColor.GRAY);
			headCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
			headCell.setPhrase(new Phrase(colTitle, headerFont));
			pdfPTable.addCell(headCell);
		}
	}
	public static void generate(DataResource dataResource, Path outputDir){
		PdfGenerator obj = new PdfGenerator(outputDir);
		try {
			obj.generateDoc("Adjektiver.pdf", PageSize.A4.rotate() , 20, dataResource.getAdjektiverList());
			obj.generateDoc("Substantiver.pdf", PageSize.A4.rotate(), 20, dataResource.getSubstantiverList());
			obj.generateDoc("Verber.pdf", PageSize.A4.rotate(), 20, dataResource.getVerberList());
		} catch (FileNotFoundException | DocumentException exc){
			exc.printStackTrace();
		}
	}
}

class PageHeaderFooter extends PdfPageEventHelper {
	private  BaseFont headerFont;
	private  BaseFont footerFont;
	private final String pageTitle;

	public PageHeaderFooter(String pageTitle){
		this(pageTitle, FontFactory.HELVETICA, FontFactory.HELVETICA);
	}
	public PageHeaderFooter(String pageTitle, String headerFontName){
		this(pageTitle, headerFontName, headerFontName);
	}

	public PageHeaderFooter(String pageTitle, String headerFontName, String footerFontName){
		this.pageTitle = pageTitle;
		try
		{
			this.headerFont = BaseFont.createFont(headerFontName, BaseFont.WINANSI, false);
			this.footerFont = BaseFont.createFont(footerFontName, BaseFont.WINANSI, false);
		}
		catch (DocumentException | IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onEndPage(PdfWriter writer, Document document) {
		// https://www.codota.com/web/assistant/code/rs/5c695cbd49efcb000177ba7e#L55
		/** The headertable. */
		PdfPTable table = new PdfPTable(1);
		/** A template that will hold the total number of pages. */
		PdfTemplate tpl = writer.getDirectContent().createTemplate(100, 100);
		/** The font that will be used. */

		PdfContentByte cb = writer.getDirectContent();
		// write the header table
		PdfPCell cell = new PdfPCell(new Phrase(pageTitle));
		cell.setBorder(0);
		table.addCell(cell);
		table.setTotalWidth(document.right() - document.left());
		table.writeSelectedRows(0, -1, document.left() + 76, document.getPageSize().getHeight() - 10, cb);
		// compose the footer
		String text = "Page " + writer.getPageNumber();
		assert footerFont != null;
		float textSize = footerFont.getWidthPoint(text, 12);

		float textBase = document.bottom() - 20;
		cb.beginText();
		cb.setFontAndSize(footerFont, 12);
		float adjust = footerFont.getWidthPoint("0", 12);
		cb.setTextMatrix(document.right() - textSize - adjust, textBase);
		cb.showText(text);
		cb.endText();
		cb.addTemplate(tpl, document.right() - adjust, textBase);
	}

	/**
	 * Fills out the total number of pages before the document is closed.
	 * @see com.itextpdf.text.pdf.PdfPageEventHelper#onCloseDocument(
	 *      com.itextpdf.text.pdf.PdfWriter, com.itextpdf.text.Document)
	 */
	public void onCloseDocument(PdfWriter writer, Document document) {
		/*
		ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
				new Phrase(String.valueOf(writer.getPageNumber() - 1), normal),
				2, 2, 0);

		 */
	}

}
