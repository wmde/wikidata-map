import config from './config.js';
import { drawDot, drawLine } from './draw.js';
import {
	chunksToLinesReadableStream,
	csvLinesToPartsReadableStream,
	valuesToBatchedValuesReadableStream,
	batchedToDrawingReadableStream
} from './streams.js';

const wdMapCanvases = {};

function showDensity(x, y, url, riverUrl) {
	const resolutionKey = `${x}x${y}`;
	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey] = createAndRenderDensityCanvas(x, y, url, riverUrl)
	}

	for (const resolution in wdMapCanvases) {
		const canvas = wdMapCanvases[resolution];
		canvas.style.display = 'none';
	}

	wdMapCanvases[resolutionKey].style.display = 'block';
}

const progressElement = document.getElementById('progress');
async function updateProgress(current, total) {
	window.requestAnimationFrame(() => {
		progressElement.value = Math.ceil( current / total * 100 );
	});
}

function createAndRenderDensityCanvas(x, y, url, riverUrl) {
	const canvas = document.createElement('canvas');
	canvas.width = x;
	canvas.height = y;
	const ctx = canvas.getContext("2d");
	ctx.fillStyle = "black";
	ctx.fillRect(0, 0, canvas.width, canvas.height);
	document.querySelector('body').appendChild(canvas);

	fetch(url, { mode: "cors" })
		.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
		.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
		.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 1000 )})
		.then(batchedStream => {return batchedToDrawingReadableStream( batchedStream.getReader(), drawDot, ctx )})
		.then(() => {
			// Rivers must run after the main render as they currently use the same canvas and must be on top
			if(riverUrl) {
				return fetch(riverUrl, { mode: "cors" })
					.then(response => {return chunksToLinesReadableStream( response.body.getReader() )})
					.then(lineStream => {return csvLinesToPartsReadableStream( lineStream.getReader() )})
					.then(dataStream => {return valuesToBatchedValuesReadableStream( dataStream.getReader(), 100 )})
					.then(batchedStream => {return batchedToDrawingReadableStream( batchedStream.getReader(), drawLine, ctx )})
					.catch(err => console.error(err));
			}
		})
		.catch(err => console.error(err));

	return canvas;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config[index].x, config[index].y, config[index].url, config[index].riverUrl);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
