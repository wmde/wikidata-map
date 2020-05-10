let chunksToLinesReadableStream = function( reader ) {
	const decoder = new TextDecoder();
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				let soFar = null;
				return reader.read().then(({ done, value }) => {
					if (done) {
						controller.close();
						return;
					}
					const lines = ((soFar != null ? soFar: "") + decoder.decode(value, {stream: true})).split(/\r?\n/);
					soFar = lines.pop();
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
					if (done) {
						controller.close();
						return;
					}
					soFar.push(value)
					if(soFar.length >= batchSize) {
						controller.enqueue(soFar.slice());
						soFar = []
					}
					return pump();
				});
			}
		}
	})
}

let batchedToWorkerMessageReadableStream = function( reader, postMessage, resolutionKey, drawType ) {
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (done) {
						controller.close();
						return;
					}
					postMessage([resolutionKey, drawType, value]);
					return pump();
				});
			}
		}
	})
}
