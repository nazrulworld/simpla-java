import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class Application {
	private final static Logger LOGGER = Logger.getLogger("Application:main");
	private final static Path OUTPUT_DIR = FileSystems.getDefault().getPath(".", "output");
	private final static Path INPUT_DIR =
			FileSystems.getDefault().getPath(
					".", "pdf-tables", "src", "main", "resources");
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();
		Path jsonFilePath = Paths.get(INPUT_DIR.toString(), "resource.json");
		LOGGER.info("Loading JSON file from " + jsonFilePath.toAbsolutePath().toString());
		byte[] jsonData = Files.readAllBytes(jsonFilePath);
		ObjectMapper objectMapper = new ObjectMapper();
		// Let's read json data and put into DataResource
		DataResource dataResource = objectMapper.readValue(jsonData, DataResource.class);
		PdfGenerator.generate(dataResource, OUTPUT_DIR);
		LOGGER.info("Generated files are saved to " + OUTPUT_DIR.toAbsolutePath().toString());
		LOGGER.info(String.format("Operation has been completed with in %.4f seconds.", (double)(System.currentTimeMillis() - startTime) / 1000));
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
