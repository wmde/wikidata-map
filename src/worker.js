self.importScripts(
	'streams.js'
);

/**
 * - Receive an instruction to render a layer
 * - Fetch the data from the web
 * - Use stream processing to decode it and batch it up (see stream.js)
 * - Send it back to the main thread to render
 */
self.startFetch = async function( layerKey, dateIndex, intensityScale, propertyLayerConfig ) {
	console.log("Worker: Rendering canvas " + dateIndex + " " + layerKey)
	if(layerKey.lastIndexOf("items", 0) === 0) {
		let mapUrl = 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/wmde_wikidata_map/map-' + dateIndex + '-7680-4320-pixels.csv'
		await fetch(mapUrl, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
		.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
			layerKey: layerKey,
			dateIndex: dateIndex,
			drawType: 'dot',
			intensityScale: intensityScale,
		} )})
		.catch(err => console.error(err));
	} else {
		propertyLayerConfig.forEach( function(layer){
			let propertyId = layer.id;
			if(propertyId == layerKey) {
				let layerUrl = 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/wmde_wikidata_map/map-' + dateIndex + '-7680-4320-relation-pixels-{property}.csv'
				layerUrl = layerUrl.replace('{property}', propertyId);
				fetch(layerUrl, { mode: "cors" })
					.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
					.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 5000 )})
					.then(batchedStream => {return batchedToWorkerMessageReadableStream( batchedStream.getReader(), postMessage, {
						layerKey: layerKey,
						dateIndex: dateIndex,
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
	const [layerKey, dateIndex, intensityScale, propertyLayerConfig] = e.data;
	console.log('Worker: Message to render canvas ' + dateIndex + " " + layerKey)
	self.startFetch( layerKey, dateIndex, intensityScale, propertyLayerConfig )
}
