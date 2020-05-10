self.importScripts(
	'streams.js'
);

self.startFetch = function( resolutionKey, url, riverUrl ) {
	fetch(url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, resolutionKey, 'dot' )})
		.catch(err => console.error(err));
	if(riverUrl) {
		fetch(riverUrl, { mode: "cors" })
			.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
			.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
			.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
			.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, resolutionKey, 'line' )})
			.catch(err => console.error(err));
	}
}

onmessage = function(e) {
	const [resolutionKey, url, riverUrl] = e.data;
	console.log('Worker: received message to render ' + resolutionKey);
	console.log('Worker: URL: ' + url);
	console.log('Worker: URL (river): ' + riverUrl);
	self.startFetch( resolutionKey, url, riverUrl )
}
