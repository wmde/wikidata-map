self.importScripts(
	'streams.js'
);

self.startFetch = async function( canvasIndex, dateIndex, intensityScale, layerKey, layerConfig ) {
	if(layerKey == "items") {
		console.log("Worker: Rendering items on canvas " + canvasIndex)
		let mapUrl = 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-' + dateIndex + '-7680-4320-pixels.csv'
		await fetch(mapUrl, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
			canvasIndex: canvasIndex,
			drawType: 'dot',
			intensityScale: intensityScale,
		} )})
		.catch(err => console.error(err));
	} else {
		layerConfig.forEach( function(layer){
			let propertyId = layer.id;
			if(propertyId == layerKey) {
				console.log("Worker: Rendering " + propertyId + " on canvas " + canvasIndex)
				let layerUrl = 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-' + dateIndex + '-7680-4320-relation-pixels-{property}.csv'
				layerUrl = layerUrl.replace('{property}', propertyId);
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
			}
		})
	}
}

onmessage = function(e) {
	const [canvasIndex, dateIndex, intensityScale, layerKey, layerConfig] = e.data;
	console.log('Worker: Message to render ' + layerKey + ' on canvas ' + canvasIndex);
	self.startFetch( canvasIndex, dateIndex, intensityScale, layerKey, layerConfig )
}
