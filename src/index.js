import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let worker = new Worker('./../src/worker.js');

const wdMapCanvases = {};

function showDensity(x, y, url, riverUrl) {
	const resolutionKey = `${x}x${y}`;
	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey] = createAndRenderCanvases(resolutionKey, x, y, url, riverUrl)
		document.querySelector('#canvas-container').appendChild(wdMapCanvases[resolutionKey][0]);
		document.querySelector('#canvas-container').appendChild(wdMapCanvases[resolutionKey][1]);
	}

	for (const resolution in wdMapCanvases) {
		const canvases = wdMapCanvases[resolution];
		canvases[0].style.display = 'none';
		canvases[1].style.display = 'none';
	}

	wdMapCanvases[resolutionKey][0].style.display = 'block';
	wdMapCanvases[resolutionKey][1].style.display = 'block';

}

function newCanvas(x, y, fillStyle){
	const canvas = document.createElement('canvas');
	canvas.width = x;
	canvas.height = y;
	canvas.style= "position: absolute; left: 0; top: 0; z-index: 0;";
	const ctx = canvas.getContext("2d");
	if(fillStyle === 'clear') {
		ctx.clearRect(0, 0, canvas.width, canvas.height)
	} else {
		ctx.fillStyle = fillStyle;
		ctx.fillRect(0, 0, canvas.width, canvas.height);
	}
	return canvas
}

function createAndRenderCanvases(resolutionKeyToRender, x, y, url, riverUrl) {
	const mainCanvas = newCanvas(x, y, 'black');
	const riverCanvas = newCanvas(x, y, 'clear');
	worker.onmessage = function(e) {
		const [resolutionKey, drawType, batchedValues] = e.data;
		if(resolutionKeyToRender !== resolutionKey) {
			return;
		}
		window.requestAnimationFrame( function() {
			batchedValues.forEach( function(drawData){
				if(drawType === 'dot') {
					drawDot(mainCanvas.getContext("2d"), drawData)
				}
				if(drawType === 'line') {
					drawLine(riverCanvas.getContext("2d"), drawData)
				}
			} )
		} )
	}
	worker.postMessage([resolutionKeyToRender, url, riverUrl])
	return [mainCanvas, riverCanvas];
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config[index].x, config[index].y, config[index].url, config[index].riverUrl);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
