package webscrapers;

import java.net.URL;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class WebScraper {
	
	private HashMap<String, String> data = new HashMap<>();

	
	protected void setData(HashMap<String, String> data) {
		this.data = data;
	}
	
	protected HashMap<String, String> getScrapedData() {
		return data;
	}
	
	/* "main" method for a webscraper  */
	public abstract WebScraper getData();
	
	/* implement this if you want to put data into a file */
	public abstract void outputToFile(String dir);
	
	/* Universal method to convert webpage to Jsoup document
	 * But, of course check that the doc is the correct one and not an error page */
	public static Document grabDocFromURL(URL url) {
		Document doc = new Document(url.toString());
		try {
			doc = Jsoup.connect(doc.baseUri()).maxBodySize(0).timeout(0).get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return doc;
	}
}
