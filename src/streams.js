export function chunksToLinesReadableStream( reader ) {
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

export function csvLinesToPartsReadableStream( reader ) {
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

export function valuesToBatchedValuesReadableStream( reader, batchSize ) {
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

export function batchedToDrawingReadableStream( reader, drawMethod, ctx ) {
	return new ReadableStream({
		start(controller) {
			return pump();
			function pump() {
				return reader.read().then(({ done, value }) => {
					if (done) {
						controller.close();
						return;
					}
					console.log('req')
					window.requestAnimationFrame( function() {
						console.log('do')
						value.forEach( function(value){
							drawMethod(ctx, value)
						} )
					} )
					return pump();
				});
			}
		}
	})
}
