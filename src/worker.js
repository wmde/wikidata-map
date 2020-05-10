self.importScripts(
	'streams.js'
);

self.startFetch = function( url, riverUrl ) {
	// Indexed by the full fetch url for now..
	fetch(url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), 'dot', postMessage )})
		.then(() => {
			// Rivers must run after the main render as they currently use the same canvas and must be on top
			if(riverUrl) {
				fetch(riverUrl, { mode: "cors" })
					.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
					.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
					.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
					.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), 'line', postMessage )})
					.catch(err => console.error(err));
			}
		})
		.catch(err => console.error(err));
}

onmessage = function(e) {
	const [url, riverUrl] = e.data;
	console.log('Worker: received message to render');
	console.log('Worker: URL: ' + url);
	console.log('Worker: URL (river): ' + riverUrl);
	self.startFetch( url, riverUrl )
}
