import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let worker = new Worker('./../src/worker.js');

const wdMapCanvases = {};

function getResolutionKey(mapConfig) {
	return `${mapConfig.x}x${mapConfig.y}`;
}

function showDensity(mapConfig, layerConfig) {
	let resolutionKey = getResolutionKey(mapConfig)

	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey]=[]
		createAndRenderCanvases(resolutionKey, mapConfig, layerConfig).forEach(function(canvas){
			document.querySelector('#canvas-container').appendChild(canvas);
			wdMapCanvases[resolutionKey].push(canvas)
		})
	}

	for (const resolution in wdMapCanvases) {
		wdMapCanvases[resolution].forEach(function(canvas){
			canvas.style.display = 'none';
		})
	}

	wdMapCanvases[resolutionKey].forEach(function(canvas){
		canvas.style.display = 'block';
	})

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

function createAndRenderCanvases(resolutionKeyToRender, mapConfig, layerConfig) {
	let resolutionKey = getResolutionKey(mapConfig)

	const allCanvases = [];
	const mainCanvas = newCanvas(mapConfig.x, mapConfig.y, 'black');
	allCanvases.push(mainCanvas);
	const layerCanvases = {};
	layerConfig.forEach( function(layer){
		layerCanvases[layer.id] = newCanvas(mapConfig.x, mapConfig.y, 'clear');
		allCanvases.push(layerCanvases[layer.id]);
	})

	worker.onmessage = function(e) {
		let batchData = e.data;
		if(resolutionKey !== batchData.drawData.resolutionKey) {
			return;
		}
		window.requestAnimationFrame( function() {
			batchData.batchedValues.forEach( function(drawValue){
				if(batchData.drawData.drawType === 'dot') {
					drawDot(
						mainCanvas.getContext("2d"),
						drawValue
					)
				} else {
					drawLine(
						layerCanvases[batchData.drawData.drawType].getContext("2d"),
						drawValue,
						batchData.drawData.lineMaxPercent,
						batchData.drawData.strokeStyle,
					)
				}
			} )
		} )
	}
	worker.postMessage([resolutionKey, mapConfig, layerConfig])

	return allCanvases;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config.maps[index], config.layers);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
