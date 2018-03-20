// References to the HTML page / content
var elSearch = document.getElementById("searchData");
var elCount = document.getElementById("searchCount");
var elMaxResult = document.getElementById("maxResults");
var elResult = document.getElementById("searchResult");

// indexSize == number of lines in "files.txt"
var indexSize = 0;
// Default to show only 50 results (as defined above)
var showMaxResults = parseInt(elMaxResult.value);
// Search 300ms after last user input to avoid searches while user is writing
var searchDelay = 300;

searchData.value="";

var hrefProtocol = "";
var fileProtocol1 = "";
var fileProtocol2 = "";
if (window.location.protocol == "file:") {
	// Set the file handler when running locally
	fileProtocol1 = "file:///";
	fileProtocol2 = window.location.href.replace(/[^\/]*$/, "");
}

// Define the ElasticLunr index with id, metaData and filename
var index = elasticlunr(function () {
    this.addField('metaData');
    this.addField('fileName');
    this.setRef('id');
});

// Remove all English default stop words
elasticlunr.stopWordFilter.stopWords = {};





// A search will be executed as soon as the query string is stable (no modification within the last "searchDelay" ms).
var doSearchTimeout;
function triggerSearch() {
	try {
		clearTimeout(doSearchTimeout);
	} catch(e) {};
	doSearchTimeout = setTimeout(doSearch, searchDelay);
}
// Search in the index
function doSearch() {
	var tSearchStart = new Date();
	var queryTerm = elSearch.value;
	
	var expandQuery = false;
	if (queryTerm.includes("*")) {
		expandQuery = true;
	}
	
	// All terms must match and if "*" is used the query term is expanded (wildcard-search like) 
	var searchResult = index.search(queryTerm, { fields: { metaData: { bool: "AND", expand: expandQuery }}});
	var tSearchEnd = new Date();

	var resultCount = searchResult.length;
	elCount.innerHTML = "&nbsp;" + resultCount + " results:";
	var resultString = "";
	for (var i = 0; i < resultCount; i++) {
		var qScore = searchResult[i]['score'];
		var qDoc = searchResult[i]['doc'];
		var qmetaData = qDoc['metaData'];
		var qFileName = qDoc['fileName'];
		if ( (fileProtocol1 == "file:///") && (qFileName.charAt(0) == '.') ) {
			hrefProtocol = fileProtocol2;
		} else {
		hrefProtocol = fileProtocol1;
		}
		resultString = resultString + '<a target="sTarget" href="' + hrefProtocol + qFileName + '">' + qFileName + "</a> (" + parseFloat(qScore).toFixed(2) + ")<br/>";
		if (i == showMaxResults) {
			// show only 'showMaxResults' results
			resultString = resultString + '...<br/>' + showMaxResults + ' results displayed; ' + (resultCount - showMaxResults) + ' results hidden<br/><br/>';
			break;
		}
	}
	elResult.innerHTML = resultString;
	tPrintEnd = new Date();
	console.log( (tPrintEnd - tSearchStart) + " ms (" +
		(tSearchEnd - tSearchStart) + " ms search; " + (tPrintEnd - tSearchEnd) + " ms output). " + 
		Math.min(resultCount, showMaxResults) + " results for query string: " + queryTerm);
}

// Search as you type, every keyup triggers a search
elSearch.addEventListener('keyup', triggerSearch);
elSearch.addEventListener('onchange', triggerSearch);



// Build Custom stop words
function buildStopWords() {
	var data;
	try {
		data = importStopWords;
	} catch (e) {
		console.error("'importStopWords' is not defined. Does 'stopwords.js' exist?");
		return;
	}
	try {
		var customStopWords = data.split(/[\r\n|\n|\t|;|,]/);
		console.log("Stopwords: " + customStopWords);
		// Add the stopwords to ElasticLunr
		elasticlunr.addStopWords(customStopWords);
	} catch (e) {
		console.error("Error parsing 'importStopWords'");
	}
}
buildStopWords();


// Live update of the result set when using the max results slider
function updateMaxResult() {
	showMaxResults = parseInt(elMaxResult.value);
	if (showMaxResults > 50) {
		showMaxResults = showMaxResults * ((showMaxResults-50)/10);
	}
	elMaxResult.title = "Show max. " + showMaxResults + " results.";
	triggerSearch();
}
elMaxResult.oninput = function() {
	updateMaxResult();
}
