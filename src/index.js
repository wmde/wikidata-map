import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let worker = new Worker('./../src/worker.js');

const wdMapCanvases = {};

function getResolutionKey(mapConfig) {
	return `${mapConfig.x}x${mapConfig.y}`;
}

function showDensity(mapConfig, layerConfig, layerStates) {
	let resolutionKey = getResolutionKey(mapConfig)

	console.log(layerStates)

	// Render any missing resolutions
	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey] = createAndRenderCanvases(resolutionKey, mapConfig, layerConfig);
		// Add them to the DOM
		Object.keys(wdMapCanvases[resolutionKey]).forEach(function(key) {
			document.querySelector('#canvas-container').appendChild(wdMapCanvases[resolutionKey][key]);
		});
	}

	// Hide all layers
	for (const resolution in wdMapCanvases) {
		Object.keys(wdMapCanvases[resolutionKey]).forEach(function(key) {
			wdMapCanvases[resolutionKey][key].style.display = 'none';
		});
	}

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

function createAndRenderCanvases(resolutionKeyToRender, mapConfig, layerConfig) {
	let resolutionKey = getResolutionKey(mapConfig)

	const allCanvases = {
		items: newCanvas(mapConfig.x, mapConfig.y, 'black'),
	};
	layerConfig.forEach( function(layer){
		allCanvases[layer.id] = newCanvas(mapConfig.x, mapConfig.y, 'clear');
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
						allCanvases.items.getContext("2d"),
						drawValue
					)
				} else {
					drawLine(
						allCanvases[batchData.drawData.drawType].getContext("2d"),
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
