import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let worker = new Worker('./../src/worker.js');

const wdMapCanvases = {};

worker.onmessage = function(e) {
	let batchData = e.data;
	let canvasesForBatch = wdMapCanvases[batchData.drawData.canvasIndex]
	window.requestAnimationFrame( function() {
		batchData.batchedValues.forEach( function(drawValue){
			if(batchData.drawData.drawType === 'dot') {
				drawDot(
					canvasesForBatch.items.getContext("2d"),
					drawValue,
					batchData.drawData.intensityScale
				)
			} else {
				drawLine(
					canvasesForBatch[batchData.drawData.drawType].getContext("2d"),
					drawValue,
					batchData.drawData.intensityScale,
					batchData.drawData.lineMaxPercent,
					batchData.drawData.strokeStyle,
				)
			}
		} )
	} )
}

function showDensity(dateIndex, intensityScale) {
	let mapConfig = config.maps[dateIndex]
	let layerConfig = config.layers
	let canvasIndex = dateIndex + '.' + intensityScale;

	// TODO generate this dynamically?
	const layerStates = {
		items: layerForm.querySelector('input[name="layer-items"]').checked,
		P190: layerForm.querySelector('input[name="layer-P190"]').checked,
		P197: layerForm.querySelector('input[name="layer-P197"]').checked,
		P403: layerForm.querySelector('input[name="layer-P403"]').checked,
	};

	// Add any missing canvases & trigger render
	if (!wdMapCanvases[canvasIndex]) {
		wdMapCanvases[canvasIndex] = createCanvases(mapConfig, layerConfig);
		Object.keys(wdMapCanvases[canvasIndex]).forEach(function(layerKey) {
			wdMapCanvases[canvasIndex][layerKey].id = "canvas_" + canvasIndex
			document.querySelector('#canvas-container').appendChild(wdMapCanvases[canvasIndex][layerKey]);
		});
	}

	// Hide all layers
	Object.keys(wdMapCanvases).forEach(function(canvasIndex) {
		Object.keys(wdMapCanvases[canvasIndex]).forEach(function(layerKey) {
			wdMapCanvases[canvasIndex][layerKey].style.display = 'none';
		});
	});

	// Show the requested layers
	Object.keys(layerStates).forEach(function(layerKey) {
		if(layerStates[layerKey] === true) {
			wdMapCanvases[canvasIndex][layerKey].style.display = 'block';
			let canvas = wdMapCanvases[canvasIndex][layerKey]
			canvas.style.display = 'block';
			if(canvas.getAttribute('data-render-scheduled') !== 'true') {
				canvas.setAttribute('data-render-scheduled', 'true')
				console.log("Requesting render: " + canvasIndex + " layer " + layerKey + " scale " + intensityScale);
				worker.postMessage([canvasIndex, dateIndex, intensityScale, layerKey, mapConfig, layerConfig])
			}
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

function createCanvases(mapConfig, layerConfig) {
	const allCanvases = {
		items: newCanvas(mapConfig.x, mapConfig.y, 'black'),
	};
	layerConfig.forEach( function(layer){
		allCanvases[layer.id] = newCanvas(mapConfig.x, mapConfig.y, 'clear');
	})
	return allCanvases;
}

function updateCanvas() {
	const dateIndex = dateForm.querySelector('input[name="date"]:checked').value;
	const intensityScale = parseInt(intensityForm.querySelector('input[name="scale"]:checked').value);

	console.log("updateCanvas called");
	showDensity(dateIndex, intensityScale);
}

const dateForm = document.getElementById('dateSelector');
dateForm.addEventListener('change', updateCanvas);

const layerForm = document.getElementById('layerSelector');
layerForm.addEventListener('change', updateCanvas);

const intensityForm = document.getElementById('intensitySelector');
intensityForm.addEventListener('change', updateCanvas);

updateCanvas();
