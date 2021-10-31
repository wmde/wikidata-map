
/**
 * Takes a stream of data from a CSV file, normally from a http request such as `response.body.getReader()`
 * splits it into lines, and split those lines into their CSV values, and sends it onto the next processor.
 **/
let chunksToLinesReadableStream = function( reader ) {
	const decoder = new TextDecoder();
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (done) {
						controller.close();
						return;
					}
					const lines = decoder.decode(value, {stream: true}).split(/\r?\n/);
					for (let line of lines) {
						controller.enqueue(line.split( ',' ));
					}
					return pump();
				});
			}
		}
	})
}

/**
 * Takes a stream of values, and batches of `batchSize` before sending them onto the next processor.
 **/
let valuesToBatchedValuesReadableStream = function( reader, batchSize ) {
	let soFar = [];
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (value) {
						soFar.push( value )
					}
					if((done && soFar.length) || soFar.length >= batchSize) {
						controller.enqueue(soFar.slice());
						soFar = []
					}
					if (done) {
						controller.close();
						return;
					}
					return pump();
				});
			}
		}
	})
}

/**
 * Takes a stream of batched values, attaches some drawData to them and posts that message.
 **/
let batchedToWorkerMessageReadableStream = function( reader, postMessage, drawData ) {
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (value) {
						postMessage( {
							drawData: drawData,
							batchedValues: value,
						} );
					}
					if (done) {
						controller.close();
						return;
					}
					return pump();
				});
			}
		}
	})
}
