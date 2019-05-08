import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.impl.io.SocketOutputBuffer;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;

class FileWriter{
	String fileName;
	private PrintWriter pwriter;
	
	
	FileWriter(String name)
	{
		synchronized(this)
		{
			File f = new File(name);
			if(f.exists())
				return;
			
			fileName = name;
			try {
				pwriter = new PrintWriter(new FileOutputStream(fileName));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
    public  void writeData(String data)
    {   
    	synchronized(this){
	    	pwriter.println(data);    	
	    	pwriter.flush();
    	}
    }
	
    public void closeWriter()
    {
    	pwriter.close();
    }
	
}
class Locker
{
	static Object lock =  new Object();
}
public class MyCrawler extends WebCrawler {
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|cfm|ico"+ "mp3|mp3|zip|gz))$");
	private final static Pattern FILTERS2 = Pattern.compile(".*(\\.(html|htm|pdf|doc))$");
	static FileWriter fetchWriter = new FileWriter("/Users/apple/Desktop/fetch.csv");
	static FileWriter visitWriter = new FileWriter("/Users/apple/Desktop/visit.csv");
	static FileWriter urlsWriter = new FileWriter("/Users/apple/Desktop/urls.csv");
	static int attemptedFetch = 0;
	static int successfulFetch = 0;
	static int failedFetches = 0;
	static int abortedFetches = 0;
	static ArrayList<String>totalUrlList = new ArrayList<String>();
	static LinkedHashSet<String>uniqueUrlList = new LinkedHashSet<String>();
	static LinkedHashSet<String>uniqueUrlListInReuters = new LinkedHashSet<String>();
	static LinkedHashSet<String>uniqueUrlListInUSC = new LinkedHashSet<String>();
	static LinkedHashSet<String>one = new LinkedHashSet<String>();
	static LinkedHashSet<String>two = new LinkedHashSet<String>();
	
	static String crawlStorageFolder = "C:\\";
	static LinkedHashMap <Integer,Integer> statusCodeList = new LinkedHashMap<Integer,Integer>();
	static LinkedHashMap <String,Integer> contentTypeList = new LinkedHashMap<String,Integer>();
	static LinkedHashMap <String,Integer> sizeTypeMap = new LinkedHashMap<String,Integer>();
	
	
	
	public void addtoList(Set t,String data)
	{
		synchronized(Locker.lock)
		{
			t.add(data);
		}
	}
	
	public void addSize(int size)
	{
		synchronized(Locker.lock)
		{
			Map<String, Integer> m = sizeTypeMap;
			if(size < 1)
			{
				Integer val = m.get("1KB");
				if(val == null)
					m.put("1KB",1);
				else
					m.put("1KB",val+1);			
			}
			else if(size < 10)
			{
				Integer val = m.get("10KB");
				if(val == null)
					m.put("10KB",1);
				else
					m.put("10KB",val+1);
			}
			else if(size < 100)
			{
				Integer val = m.get("100KB");
				if(val == null)
					m.put("100KB",1);
				else
					m.put("100KB",val+1);
			}
			else if(size < 1024)
			{
				Integer val = m.get("1024KB");
				if(val == null)
					m.put("1024KB",1);
				else
					m.put("1024KB",val+1);
			}
			else
			{
				Integer val = m.get("1MB");
				if(val == null)
					m.put("1M B",1);
				else
					m.put("1MB",val+1);
			}
		}
	}
	
	/**
	 * This method receives two parameters. The first parameter is the page
	 * in which we have discovered this new url and the second parameter is
	 * the new url. You should implement this function to specify whether
	 * the given url should be crawled or not (based on your crawling logic).
	 * In this example, we are instructing the crawler to ignore urls that
	 * have css, js, git, ... extensions and to only accept urls that start
	 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	 * referringPage parameter to make the decision.
	 */
	@Override
	 public boolean shouldVisit(Page referencedPage,WebURL url) {
			 String href = url.getURL().toLowerCase();
			 String hrefC = url.getURL();
			 
			
			 synchronized(Locker.lock)
			{
				 totalUrlList.add(hrefC);
				 
				 addtoList(uniqueUrlList, hrefC);
			
			 
	
			 String urlStatus = null;
             if(href.contains("www.reuters.com"))
			 {
				 urlStatus = "OK";
				 addtoList(uniqueUrlListInReuters, hrefC);
			 }
			 else
			 {
				 urlStatus = "N_OK";
			 }
			 urlsWriter.writeData(href+","+urlStatus);
			} 
			 boolean retStatus =!FILTERS.matcher(href).matches()
					 && href.startsWith("https://www.reuters.com/");			 			
			 
			 return retStatus; 			 			 
		 }
		 
		 /**
		  * This function is called when a page is fetched and ready
		  * to be processed by your program.
		  */
		  @Override
		  public void visit(Page page) {			  
			  StringBuilder dataS = new StringBuilder();
		
			  String url = page.getWebURL().getURL();
			  String surl =url.toLowerCase();
			  surl.replace('-', ' ');
			  String contentType = page.getContentType().toLowerCase();
			  //System.out.println("EURL:"+contentType+":"+url);			  				 
				
				 if(contentTypeList.get(contentType) == null)
				 {
					 contentTypeList.put(contentType,1);
				 }
				 else
				 {
					 contentTypeList.put(contentType,contentTypeList.get(contentType)+1);
				 }
			
				 int sizeKB = page.getContentData().length/1024;
				 addSize(sizeKB);
				  System.out.println("Visiting : "+url);
				  dataS.append(url+",");		  		  
				  ParseData  pd = page.getParseData();	
				 
				  Set<WebURL>olinks = pd.getOutgoingUrls();
				  dataS.append(page.getContentData().length+",");
				  dataS.append(olinks.size()+",");
				  dataS.append(page.getContentType());
				  visitWriter.writeData(dataS.toString());
				  two.add(surl);
				/*
				  if (page.getParseData() instanceof HtmlParseData) {
					  HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
					  String text = htmlParseData.getText();
					  String html = htmlParseData.getHtml();
					  
					  
					  Set<WebURL> links =  htmlParseData.getOutgoingUrls();			  
					  dataS.append(page.getContentData().length+",");
					  dataS.append(links.size()+",");
					  dataS.append(page.getContentType());
					  
					  visitWriter.writeData(dataS.toString());
					  System.out.println("Downloaded :"+dataS.toString());
					 try {
						  String fileName = url.substring(url.lastIndexOf("/"));
						  System.out.println("URL:"+url);
						  System.out.println("Fname:"+fileName);
						  FileOutputStream fout = new FileOutputStream(crawlStorageFolder+fileName);
						  fout.write(html.getBytes());
						  fout.close();
					}   catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  catch(IOException io)
					  {
						io.printStackTrace();
					  }
					
					  //System.out.println("Text length: " + text.length());
					 // System.out.println("Html length: " + html.length());
					  //System.out.println("Number of outgoing links: " + links.size());		
					  
			  }	*/
	  }
	 
	  
	  @Override
	  protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		  webUrl.getURL().replace('-', ' ');
			  System.out.println("Fetch "+webUrl.getURL()+","+statusCode);
			  fetchWriter.writeData(webUrl.getURL()+","+statusCode);
			  synchronized(Locker.lock)
			  {
				  attemptedFetch++;
				  if(statusCode>=300 && statusCode < 400)
					  abortedFetches++;
				  if(statusCode >= 400)
					  failedFetches++;
				  if(statusCode >=200 && statusCode < 300) {
					  successfulFetch++;
					  one.add(webUrl.getURL().toLowerCase());
				  }
				  
				  if(statusCodeList.get(statusCode)  == null)
					  statusCodeList.put(statusCode, 1);
				  else
					  statusCodeList.put(statusCode, statusCodeList.get(statusCode)+1);
			  }
			  
			  if(webUrl.getURL().contains("pdf"))
			  {
				  System.out.println("PContent Typ Encountered in status: "+webUrl.getURL());
				 // System.exit(1);
			  }
		  
	
}
}
