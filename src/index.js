import config from './config.js';
import { drawDots, drawRivers } from './draw.js';

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
		.then((response) => response.text())
		.then(async (data) => {
			const tStartRender = performance.now();

			await drawDots(data, ctx, updateProgress);

			const tEndRender = performance.now();
			const perfP = document.getElementById("performance");
			const key = `${x}x${y}`;
			perfP.textContent += ` Rendering ${key} took ${Math.ceil(tEndRender - tStartRender)} milliseconds.`;
		})
		.then(() => {
			if (!riverUrl) {
				return;
			}
			return fetch(riverUrl, { mode: "cors" });
		})
		.then((response) => response.text())
		.then(async (data) => {
			const tStartRender = performance.now();

			await drawRivers(data, ctx, canvas.width);

			const tEndRender = performance.now();
			const perfP = document.getElementById( 'performance' );
			perfP.textContent += ` Rendering rivers took ${( tEndRender - tStartRender )} milliseconds.`;
		});

	return canvas;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config[index].x, config[index].y, config[index].url, config[index].riverUrl);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
