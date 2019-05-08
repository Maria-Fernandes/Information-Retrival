<?php 

include 'SpellCorrector.php';
include 'simple_html_dom.php';

// make sure browsers see this page as utf-8 encoded HTML
header('Content-Type: text/html; charset=utf-8');
ini_set('memory_limit',-1);
ini_set('max_execution_time', 300);
error_reporting(E_ALL & ~E_WARNING & ~E_NOTICE);
$correct_query="";
$limit = 10;
$flag = 0;
$query = isset($_REQUEST['q']) ? $_REQUEST['q'] : false;
$results = false;

if ($query)
{
  require_once('/Applications/XAMPP/xamppfiles/htdocs/solr-php-client-master/Apache/Solr/Service.php');

  $solr = new Apache_Solr_Service('localhost', 8983, '/solr/myexample/');
  $query_terms = explode(" ", $query);
  // if magic quotes is enabled then stripslashes will be needed
  if (get_magic_quotes_gpc() == 1)
  {
    $query = stripslashes($query);
  }

  for($i = 0 ; $i < sizeof($query_terms); $i++)
  {
  	$chk = SpellCorrector::correct($query_terms[$i]);
  	if($i == 0)
  		$correct_query = $correct_query . $chk;
  	else
  		$correct_query = $correct_query .' '. $chk;
  }
  if(strtolower($query) != strtolower($correct_query))
  {
  	$flag = 1;
  }
  try
  {
  	if($_GET['algo'] == "lucene")
  	{
    		$results = $solr->search($query, 0, $limit);
  	}
  	else
  	{
  		$additionalParameters = array('sort' => 'pageRankFile desc');
  		  	$results = $solr->search($query, 0, $limit, $additionalParameters);
  	}
  }
  catch (Exception $e)
  {
    die("<html><head><title>SEARCH EXCEPTION</title><body><pre>{$e->__toString()}</pre></body></html>");
  }
}

?>

<html>
  <head>
    <title>PHP Solr Client Example</title>
    <link href="http://code.jquery.com/ui/1.10.4/themes/ui-lightness/jquery-ui.css" rel="stylesheet"></link>
	<script src="http://code.jquery.com/jquery-1.10.2.js"></script>
	<script src="http://code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
  </head>
  <body style='background-color: darkgray;'>
    <form  accept-charset="utf-8" method="get" style='text-align: center;'>
      <label for="q" style='display: block;font-weight: bold;font-size: 30px;text-align: center;'>Search : </label>
      <input id="q" name="q" type="text" value="<?php echo htmlspecialchars($query, ENT_QUOTES, 'utf-8'); ?>"/>
      <br/><br/>
      <input type="radio" name="algo" value="lucene"<?php if(isset($_REQUEST['algo']) && $_REQUEST['algo'] == 'lucene') {echo 'checked="checked"';} ?>> Lucene
      <input type="radio" name="algo" value="pagerank"<?php if(isset($_REQUEST['algo']) && $_REQUEST['algo'] == 'pagerank') {echo 'checked="checked"';} ?>> Page Rank
      <br/>
      <input type="submit"/>
    </form>
    <script>
	$(function() {
		var URL_PREFIX = "http://localhost:8983/solr/myexample/suggest?q=";
		var URL_SUFFIX = "&wt=json";
		var final_suggest = [];
		var previous= "";
		$("#q").autocomplete({
			source : function(request, response) {
				var q = $("#q").val().toLowerCase();
         		var sp =  q.lastIndexOf(' ');
         		if(q.length - 1 > sp && sp != -1)
         		{
          			final_query = q.substr(sp+1);
          			previous = q.substr(0,sp);
        		}
        		else
        		{
          			final_query = q.substr(0); 
        		}
				var URL = URL_PREFIX + final_query + URL_SUFFIX;
				$.ajax({
					url : URL,
					success : function(data) {
							  var docs = JSON.stringify(data.suggest.suggest);
							  var jsonData = JSON.parse(docs);
							  var result =jsonData[final_query].suggestions;
							  var j=0;
							  var suggest = [];
							  for(var i=0 ; i<5 && j<result.length ; i++,j++){
									if(final_query == result[j].term)
									{
								  		--i;
								  		continue;
									}
									for(var l=0;l<i && i>0;l++)
									{
									  	if(final_suggest[l].indexOf(result[j].term) >=0)
									  	{
											--i;
									  	}
									}
									if(suggest.length == 5)
									  break;
									if(suggest.indexOf(result[j].term) < 0)
									{
									  suggest.push(result[j].term);
									  if(previous == ""){
										final_suggest[i]=result[j].term;
									  }
									  else
									  {
										final_suggest[i] = previous + " ";
										final_suggest[i]+=result[j].term;
									  }
									}
							  }
							  response(final_suggest);
					},
					close: function () {
         				this.value='';
    					},
					dataType : 'jsonp',
 					jsonp : 'json.wrf'
 				});
 				},
 			minLength : 1
 			})
 		});
</script>
<?php

// display results
if ($results)
{
  $total = (int) $results->response->numFound;
  $start = min(1, $total);
  $end = min($limit, $total);

  if($flag == 1){
	echo "Showing results for ", ucwords($query);
	$link = "http://localhost/pageRank3.php?q=$correct_query";
	echo "<br>Search instead for <a href='$link'>$correct_query</a>";
}
?>
    <div>Results <?php echo $start; ?> - <?php echo $end;?> of <?php echo $total; ?>:</div>
    <ol>
<?php
  // iterate result documents
  $csv = array_map('str_getcsv', file('/Users/apple/Desktop/IR/URLtoHTML_reuters_news.csv'));
	
  foreach ($results->response->docs as $doc)
  {  
	$id = $doc->id;
  	$title = $doc->og_title;
  	$url = $doc->og_url;
  	$desc = $doc->og_description;
  	if($desc == "" || $desc == null)
  	{
  		$desc = "N/A";
	}
	if($title == "" || $title == null)
  	{
  		$title = "N/A";
	}
	if($url == "" || $url == null)
	{
	foreach($csv as $row)
		{
			$cmp = "/Users/apple/Desktop/IR/reutersnews" + $row[0];
			if($id == $cmp)
			{
				$url = $row[1];
				unset($row);
				break;
			}
		}
	}
	$snip = "";
	$query_terms = explode(" ", $query);
	$count = 0;
	$max = sizeof($query_terms);
	$prev_max = 0;
	$file_content = file_get_contents($id);
	$html = str_get_html($file_content);
	$content =  strtolower($html->plaintext);
	foreach(preg_split("/((\r?\n)|(\r\n?))/", $content) as $line)
	{
  		$sent = strtolower($line);
  		for($i = 0 ; $i < sizeof($query_terms); $i++)
  		{
  			$query_term_lower = strtolower($query_terms[i]);
  			if(strpos($sent, $query_term_lower) == 0)
  			{
  				$count = $count+1;
  			}
  		}
  		if($max==$count)
	    	{
	    		$snip = $sent;
	      		break;
	    	}
	    	else if($count > 0)
	    	{
	    	    $snip = $sent;
				break;
	    	}
	    	$count = 0;
    	
  	}
  	if($snip == "")
		$snip = $desc;
  	$pos_term = 0;
  	$start_pos = 0;
  	$end_pos = 0;
	for($i = 0 ; $i < sizeof($query_terms); $i++)
  	{
  	if (strpos(strtolower($snip), strtolower($query_terms[$i])) !== false) 
		{
		  $pos_term = strpos(strtolower($snip), strtolower($query_terms[$i]));
		  break;
		}
	}
	if($pos_term > 80)
	{
		$start_pos = $pos_term - 80; 
	}
	$end_pos = $start_pos + 160;
	if(strlen($snip) < $end_pos)
	{
		$end_pos = strlen($snip) - 1;
		$trim_end = "";
	}
	else
	{
		$trim_end = "....";
	}
	if(strlen($snip) > 160)
	{
		if($start > 0)
			$trim_beg = "....";
		else
			$trim_beg = "";
		$snip = $trim_beg.substr($snip , $start_pos , $end_pos - $start_pos + 1).$trim_end;
	}
  	echo "<div style='border-radius: 13px;padding: 5px;border: 2px solid black;margin-bottom: 5px;'>Title : <a href = '$url'>$title</a></br>
		URL : <a href = '$url'>$url</a></br>
		ID : $id</br>
		Snippet : ";
		$ary = explode(" ",$snip);
		$fullflag = 0;
		$snipper = "";
		
		foreach ($ary as $word)
		{
			$flag = 0;
			for($i = 0 ; $i < sizeof($query_terms); $i++)
			{
				if(stripos($word,$query_terms[$i])!=false)
				{

					$flag = 1;
					$fullflag = 1;
					break;
				}
			}
			if($flag == 1)
				$snipper =  $snipper.'<b>'.$word.'</b>';
			else
				$snipper =  $snipper.$word;	
			$snipper =  $snipper." ";	
		}
		$words1 = preg_split('/\s+/', $query);
		foreach($words1 as $item)
			$snipper = str_ireplace($item, "<strong>".$item."</strong>",$snipper);
		echo $snipper."</br></br></div>";

	}
}
?>
	</body>
</html>
