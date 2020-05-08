export function drawDots( data, ctx, progressCallback ) {
	return new Promise( ( resolve ) => {
		const csvLines = data.split( '\n' );
		const totalLength = csvLines.length;
		const dotsPerFrame = 5000;

		function drawDot() {
			for ( let currentDots = 0; currentDots < dotsPerFrame; currentDots++ ) {
				if ( csvLines.length === 0 ) {
					progressCallback( totalLength - csvLines.length, totalLength );
					resolve();
					return;
				}
				const [ x, y, numberOfItems ] = csvLines.pop().split( ',' );
				ctx.fillStyle = `hsl(23, 100%, ${numberOfItems}%)`;
				ctx.fillRect( x, y, 1, 1 );
			}
			progressCallback( totalLength - csvLines.length, totalLength );
			window.requestAnimationFrame( drawDot );
		}

		window.requestAnimationFrame( drawDot );
	} );
}

export function drawRivers( data, ctx, width ) {
	return new Promise( ( resolve ) => {
		const csvLines = data.split( '\n' );

		csvLines.forEach( async ( line ) => {
			const [ x1, y1, x2, y2 ] = line.split( ',' );
			const distance = Math.sqrt( Math.pow( x2 - x1, 2 ) + Math.pow( y2 - y1, 2 ) );
			if ( distance > width/3 ) {
				return;
			}
			ctx.strokeStyle = `blue`;
			ctx.beginPath();
			ctx.moveTo( x1, y1 );
			ctx.lineTo( x2, y2 );
			ctx.stroke();
		} );
		resolve();
	});
}
