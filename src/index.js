import config from './config.js';
import { drawDot, drawLine } from './draw.js';

const wdMapCanvases = {};
let worker = new Worker('./../src/worker.js');

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

	worker.onmessage = function(e) {
		const [drawType, batchedValues] = e.data;
		window.requestAnimationFrame( function() {
			batchedValues.forEach( function(drawData){
				if(drawType === 'dot') {
					drawDot(ctx, drawData)
				}
				if(drawType === 'line') {
					drawLine(ctx, drawData)

				}
			} )
		} )
	}
	worker.postMessage([url, riverUrl])
	return canvas;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config[index].x, config[index].y, config[index].url, config[index].riverUrl);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
