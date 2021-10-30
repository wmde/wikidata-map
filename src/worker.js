self.importScripts(
	'streams.js'
);

self.startFetch = async function( canvasIndex, mapConfig, layerConfig, intensityScale ) {
	await fetch(mapConfig.url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
			canvasIndex: canvasIndex,
			drawType: 'dot',
			intensityScale: intensityScale,
		} )})
		.catch(err => console.error(err));

	layerConfig.forEach( function(layer){
		let propertyId = layer.id;
		let layerUrl = mapConfig.layerUrl.replace('{property}', propertyId);
		fetch(layerUrl, { mode: "cors" })
			.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
			.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
			.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
				canvasIndex: canvasIndex,
				drawType: propertyId,
				intensityScale: intensityScale,
				lineMaxPercent: layer.lineMaxPercent,
				strokeStyle: layer.strokeStyle,
			} )})
			.catch(err => console.error(err));
	})
}

onmessage = function(e) {
	const [canvasIndex, mapConfig, layerConfig, intensityScale] = e.data;
	console.log('Worker: received message to render ' + canvasIndex);
	self.startFetch( canvasIndex, mapConfig, layerConfig, intensityScale )
}
