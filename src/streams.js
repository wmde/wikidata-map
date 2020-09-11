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
						controller.enqueue(line);
					}
					return pump();
				});
			}
		}
	})
}

let csvLinesToPartsReadableStream = function( reader ) {
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (done) {
						controller.close();
						return;
					}
					controller.enqueue(value.split( ',' ));
					return pump();
				});
			}
		}
	})
}

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
