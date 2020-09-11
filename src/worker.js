self.importScripts(
	'streams.js'
);

self.startFetch = function( resolutionKey, mapConfig, layerConfig ) {
	fetch(mapConfig.url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
			resolutionKey: resolutionKey,
			drawType: 'dot',
		} )})
		.catch(err => console.error(err));

	layerConfig.forEach( function(layer){
		let propertyId = layer.id;
		let layerUrl = mapConfig.layerUrl.replace('{property}', propertyId);
		fetch(layerUrl, { mode: "cors" })
			.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
			.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
			.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
				resolutionKey: resolutionKey,
				drawType: propertyId,
				lineMaxPercent: layer.lineMaxPercent,
				strokeStyle: layer.strokeStyle,
			} )})
			.catch(err => console.error(err));
	})
}

onmessage = function(e) {
	const [resolutionKey, mapConfig, layerConfig] = e.data;
	console.log('Worker: received message to render ' + resolutionKey);
	self.startFetch( resolutionKey, mapConfig, layerConfig )
}
