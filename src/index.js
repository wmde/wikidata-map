import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let worker = new Worker('./../src/worker.js');

const wdMapCanvases = {};

worker.onmessage = function(e) {
	let batchData = e.data;
	let canvasesForBatch = wdMapCanvases[batchData.drawData.resolutionKey]
	window.requestAnimationFrame( function() {
		batchData.batchedValues.forEach( function(drawValue){
			if(batchData.drawData.drawType === 'dot') {
				drawDot(
					canvasesForBatch.items.getContext("2d"),
					drawValue
				)
			} else {
				drawLine(
					canvasesForBatch[batchData.drawData.drawType].getContext("2d"),
					drawValue,
					batchData.drawData.lineMaxPercent,
					batchData.drawData.strokeStyle,
				)
			}
		} )
	} )
}

function getResolutionKey(mapConfig) {
	return `${mapConfig.x}x${mapConfig.y}`;
}

function showDensity(mapConfig, layerConfig, layerStates) {
	let resolutionKey = getResolutionKey(mapConfig)

	// Add any missing canvases & trigger render
	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey] = createCanvases(resolutionKey, mapConfig, layerConfig);
		Object.keys(wdMapCanvases[resolutionKey]).forEach(function(key) {
			document.querySelector('#canvas-container').appendChild(wdMapCanvases[resolutionKey][key]);
		});
		// Start the render
		worker.postMessage([resolutionKey, mapConfig, layerConfig])
	}

	// Hide all layers
	Object.keys(wdMapCanvases).forEach(function(resolutionKey) {
		Object.keys(wdMapCanvases[resolutionKey]).forEach(function(layerKey) {
			wdMapCanvases[resolutionKey][layerKey].style.display = 'none';
		});
	});

	// Show the requested layers
	Object.keys(layerStates).forEach(function(key) {
		if(layerStates[key] === true) {
			wdMapCanvases[resolutionKey][key].style.display = 'block';
		}
	});

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

function createCanvases(resolutionKeyToRender, mapConfig, layerConfig) {
	let resolutionKey = getResolutionKey(mapConfig)

	const allCanvases = {
		items: newCanvas(mapConfig.x, mapConfig.y, 'black'),
	};
	layerConfig.forEach( function(layer){
		allCanvases[layer.id] = newCanvas(mapConfig.x, mapConfig.y, 'clear');
	})
	return allCanvases;
}

function updateCanvas() {
	const resolutionIndex = resolutionForm.querySelector('input[name="resolution"]:checked').value;

	// TODO generate this dynamically?
	const layerStates = {
		items: layerForm.querySelector('input[name="layer-items"]').checked,
		P190: layerForm.querySelector('input[name="layer-P190"]').checked,
		P197: layerForm.querySelector('input[name="layer-P197"]').checked,
		P403: layerForm.querySelector('input[name="layer-P403"]').checked,
	};

	showDensity(config.maps[resolutionIndex], config.layers, layerStates);
}

const resolutionForm = document.getElementById('resolutionSelector');
resolutionForm.addEventListener('change', updateCanvas);

const layerForm = document.getElementById('layerSelector');
layerForm.addEventListener('change', updateCanvas);

updateCanvas();
