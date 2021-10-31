import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let nextWorker = 0;
// Create 4 workers, to allow multiple canvas renderings at once
let workers = {
	0: new Worker('./../src/worker.js'),
	1: new Worker('./../src/worker.js'),
	2: new Worker('./../src/worker.js'),
	3: new Worker('./../src/worker.js'),
};

/**
 * Receive batched draw data to render from a worker
 */
Object.keys(workers).forEach(function(workerKey) {
	workers[workerKey].onmessage = function(e) {
		let batchData = e.data;
		let canvasForBatch = canvases[batchData.drawData.dateIndex][batchData.drawData.layerKey]
		window.requestAnimationFrame( function() {
			batchData.batchedValues.forEach( function(drawValue){
				if(batchData.drawData.drawType === 'dot') {
					drawDot(
						canvasForBatch.getContext("2d"),
						drawValue,
						batchData.drawData.intensityScale
					)
				} else {
					drawLine(
						canvasForBatch.getContext("2d"),
						drawValue,
						batchData.drawData.intensityScale,
						batchData.drawData.lineMaxPercent,
						batchData.drawData.strokeStyle,
					)
				}
			} )
		} )
	}
});

/**
 * Post work to one of our pool of workers
 */
function postToWorker(data) {
	console.log("Using worker: " + nextWorker)
	workers[nextWorker].postMessage(data);
	if(nextWorker === 3) {
		nextWorker = 0;
	} else {
		nextWorker = nextWorker + 1;
	}
}

const canvases = {};

function updateCanvas() {
	console.log("updateCanvas called");
	const dateIndex = dateForm.querySelector('input[name="date"]:checked').value;
	const intensityScale = parseInt(intensityForm.querySelector('input[name="scale"]:checked').value);
	let itemCanvasKey = 'items.' + intensityScale
	let propertyLayerConfig = config.layers

	// TODO generate this dynamically?
	const layerStates = {
		[itemCanvasKey]: layerForm.querySelector('input[name="layer-items"]').checked,
		P17: layerForm.querySelector('input[name="layer-P17"]').checked,
		P36: layerForm.querySelector('input[name="layer-P36"]').checked,
		P47: layerForm.querySelector('input[name="layer-P47"]').checked,
		P138: layerForm.querySelector('input[name="layer-P138"]').checked,
		P150: layerForm.querySelector('input[name="layer-P150"]').checked,
		P190: layerForm.querySelector('input[name="layer-P190"]').checked,
		P197: layerForm.querySelector('input[name="layer-P197"]').checked,
		P403: layerForm.querySelector('input[name="layer-P403"]').checked,
	};

	// Create any missing canvases that we need
	if (!canvases[dateIndex]) {
		canvases[dateIndex] = {};
		propertyLayerConfig.forEach( function(layerData){
			let propertyId = layerData.id
			canvases[dateIndex][propertyId] = newCanvas('clear');
		})
	}
	if (!canvases[dateIndex][itemCanvasKey]) {
		canvases[dateIndex][itemCanvasKey] = newCanvas('black')
	}

	// Hide them ALL, and attach them to the DOM if not already there
	Object.keys(canvases).forEach(function(dateIndex) {
		Object.keys(canvases[dateIndex]).forEach(function(canvasKey) {
			canvases[dateIndex][canvasKey].style.display = 'none';
			if(canvases[dateIndex][canvasKey].id != "canvas_" + dateIndex + "_" + canvasKey ) {
				canvases[dateIndex][canvasKey].id = "canvas_" + dateIndex + "_" + canvasKey
				document.querySelector('#canvas-container').appendChild(canvases[dateIndex][canvasKey]);
			}
		});
	});

	// Show (and render if needed) the requested canvases
	Object.keys(layerStates).forEach(function(layerKey) {
		if(layerStates[layerKey] === true) {
			let canvas = canvases[dateIndex][layerKey]
			canvas.style.display = 'block';
			if(canvas.getAttribute('data-render-scheduled') !== 'true') {
				canvas.setAttribute('data-render-scheduled', 'true')
				console.log("Requesting render: " + dateIndex + " layer " + layerKey);
				postToWorker([layerKey, dateIndex, intensityScale, propertyLayerConfig])
			}
		}
	});
}

function newCanvas(fillStyle){
	const x = 7680;
	const y = 4320;
	const canvas = document.createElement('canvas');
	canvas.width = x;
	canvas.height = y;
	canvas.style= "position: absolute; left: 0; top: 0; z-index: 0;";
	const ctx = canvas.getContext("2d");
	if(fillStyle === 'clear') {
		ctx.clearRect(0, 0, canvas.width, canvas.height)
		canvas.style.zIndex = 1; // Should always be in front, as it is clear
	} else {
		ctx.fillStyle = fillStyle;
		ctx.fillRect(0, 0, canvas.width, canvas.height);
		canvas.style.zIndex = 0;
	}
	return canvas
}

const dateForm = document.getElementById('dateSelector');
dateForm.addEventListener('change', updateCanvas);

const layerForm = document.getElementById('layerSelector');
layerForm.addEventListener('change', updateCanvas);

const intensityForm = document.getElementById('intensitySelector');
intensityForm.addEventListener('change', updateCanvas);

const megaCanvas = document.querySelector('#canvas-container')
megaCanvas.addEventListener('click', zoomCanvas);
function zoomCanvas() {
	if(megaCanvas.classList.contains('zoomed')) {
		megaCanvas.classList.remove('zoomed');
	} else {
		megaCanvas.classList.add('zoomed');
	}
}

updateCanvas();
