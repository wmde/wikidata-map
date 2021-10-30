self.importScripts(
	'streams.js'
);

self.startFetch = async function( dateIndex, mapConfig, layerConfig ) {
	await fetch(mapConfig.url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
			dateIndex: dateIndex,
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
				dateIndex: dateIndex,
				drawType: propertyId,
				lineMaxPercent: layer.lineMaxPercent,
				strokeStyle: layer.strokeStyle,
			} )})
			.catch(err => console.error(err));
	})
}

onmessage = function(e) {
	const [dateIndex, mapConfig, layerConfig] = e.data;
	console.log('Worker: received message to render ' + dateIndex);
	self.startFetch( dateIndex, mapConfig, layerConfig )
}
