import config from './config.js';
import { drawDot, drawLine } from './draw.js';

let nextWorker = 0;
let workerCount = window.navigator.hardwareConcurrency
// hardwareConcurrency will be undefined on some old browsers, so just assume we can make 2 workers?
if(workerCount == undefined) {
	workerCount = 2;
}
console.log("Detected cores to run " + workerCount + " workers");

// Create workers, to allow multiple canvas data processings at once in the background
let workers = {};
for (let i = 0; i < workerCount; i++) {
	console.log("Creating worker: " + i)
	workers[i] = new Worker('./../src/worker.js')
}

/**
 * Receive batched draw data to render from a worker
 */
Object.keys(workers).forEach(function(workerKey) {
	workers[workerKey].onmessage = function(e) {
		let batchData = e.data;
		let canvasForBatch = canvases[batchData.drawData.dateStr][batchData.drawData.layerKey]
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
	if(nextWorker === (workerCount-1)) {
		nextWorker = 0;
	} else {
		nextWorker = nextWorker + 1;
	}
}

const canvases = {};

function updateCanvas() {
	console.log("updateCanvas called");
	const dateIndex = parseInt(dateSlider.value);
	const dateStr = dates[dateIndex];
	const intensityScale = parseInt(intensitySlider.value);
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
	if (!canvases[dateStr]) {
		canvases[dateStr] = {};
		propertyLayerConfig.forEach( function(layerData){
			let propertyId = layerData.id
			canvases[dateStr][propertyId] = newCanvas('clear');
		})
	}
	if (!canvases[dateStr][itemCanvasKey]) {
		canvases[dateStr][itemCanvasKey] = newCanvas('black')
	}

	// Hide them ALL, and attach them to the DOM if not already there
	Object.keys(canvases).forEach(function(dateStr) {
		Object.keys(canvases[dateStr]).forEach(function(canvasKey) {
			canvases[dateStr][canvasKey].style.display = 'none';
			if(canvases[dateStr][canvasKey].id != "canvas_" + dateStr + "_" + canvasKey ) {
				canvases[dateStr][canvasKey].id = "canvas_" + dateStr + "_" + canvasKey
				document.querySelector('#canvas-container').appendChild(canvases[dateStr][canvasKey]);
			}
		});
	});

	// Show (and render if needed) the requested canvases
	Object.keys(layerStates).forEach(function(layerKey) {
		if(layerStates[layerKey] === true) {
			let canvas = canvases[dateStr][layerKey]
			canvas.style.display = 'block';
			if(canvas.getAttribute('data-render-scheduled') !== 'true') {
				canvas.setAttribute('data-render-scheduled', 'true')
				console.log("Requesting render: " + dateStr + " layer " + layerKey);
				postToWorker([layerKey, dateStr, intensityScale, propertyLayerConfig])
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

// Date mapping for slider
const dates = [
	'2014-11-03',
	'2015-10-05',
	'2016-10-03',
	'2017-10-02',
	'2018-10-08',
	'2019-10-07',
	'2020-11-02',
	'2021-10-18',
	'2023-06-26',
	'2024-10-07',
	'2025-10-13'
];

const dateSlider = document.getElementById('dateSlider');
const dateValue = document.getElementById('dateValue');
dateSlider.addEventListener('input', function() {
	const index = parseInt(this.value);
	dateValue.textContent = dates[index];
	updateCanvas();
});

const layerForm = document.getElementById('layerSelector');
layerForm.addEventListener('change', updateCanvas);

const intensitySlider = document.getElementById('intensitySlider');
const intensityValue = document.getElementById('intensityValue');
intensitySlider.addEventListener('input', function() {
	intensityValue.textContent = this.value;
	updateCanvas();
});

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
