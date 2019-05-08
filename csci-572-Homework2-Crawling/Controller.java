import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {
	
 public static void main(String[] args) throws Exception {
 String crawlStorageFolder = "/Users/apple/Desktop/data/crawl";
 int numberOfCrawlers=7;
 int maxDepthofCrawling = 16;
 int maxPagesToFetch =20000;
 /**
  * Politeness delay in milliseconds (delay between sending two requests to
  * the same host).
  */
 int politenessDelay = 200;
 CrawlConfig config = new CrawlConfig();
 config.setCrawlStorageFolder(crawlStorageFolder);
 config.setMaxDepthOfCrawling(maxDepthofCrawling);
 config.setMaxPagesToFetch(maxPagesToFetch);
 config.setPolitenessDelay(politenessDelay);
 /**
  * Should we fetch binary content such as images, audio, ...?
  */
 config.setIncludeBinaryContentInCrawling(true);
 /**
  * Should we also crawl https pages?
  */
 config.setIncludeHttpsPages(true);
 
 /*
 * Instantiate the controller for this crawl.
 */
 
 PageFetcher pageFetcher = new PageFetcher(config);
 RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
 RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
 CrawlController controller = null;
 try {
		controller = new CrawlController(config, pageFetcher, robotstxtServer);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 
 /*
 * For each crawl, you need to add some seed urls. These are the first
 * URLs that are fetched and then the crawler starts following links
 * which are found in these pages
 */
 controller.addSeed("https://www.reuters.com/");

 /*
 * Start the crawl. This is a blocking operation, meaning that your code
 * will reach the line after this only when crawling is finished.
 */
		 
 controller.start(MyCrawler.class, numberOfCrawlers);
 System.out.println("fetches attempted:"+MyCrawler.attemptedFetch);
 System.out.println("fetches succeeded:"+MyCrawler.successfulFetch);
 System.out.println("fetches failed or aborted:"+(MyCrawler.failedFetches+MyCrawler.abortedFetches));
 System.out.println("Total URLs extracted:"+MyCrawler.totalUrlList.size());
 System.out.println("unique URLs extracted:"+MyCrawler.uniqueUrlList.size());
 System.out.println("unique URLs within news:"+MyCrawler.uniqueUrlListInReuters.size());
 System.out.println("unique URLs outside news:"+(MyCrawler.uniqueUrlList.size()-MyCrawler.uniqueUrlListInReuters.size()));
 System.out.println("Status Codes" + MyCrawler.statusCodeList);
 System.out.println("File Size" + MyCrawler.sizeTypeMap);
 System.out.println("Content Type" + MyCrawler.contentTypeList);
 for(String prop:MyCrawler.two) {
	 if(MyCrawler.one.contains(prop)) {
		 MyCrawler.one.remove(prop);
	 }
 }
 System.out.println(MyCrawler.one);
 }
 
}