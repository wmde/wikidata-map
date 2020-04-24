import config from './config.js';
import { drawDots, drawRivers } from './draw.js';

const wdMapCanvases = {};

function showDensity(x, y, url) {
	const resolutionKey = `${x}x${y}`;
	if (!wdMapCanvases[resolutionKey]) {
		wdMapCanvases[resolutionKey] = createAndRenderDensityCanvas(x, y, url)
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

function createAndRenderDensityCanvas(x, y, url) {
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
			if (x > 3000) {
				return;
			}
			const riverUrl = 'https://gist.githubusercontent.com/addshore/01c9aa9c449b8208c1da06010017a469/raw/54fefc91332dcb779abe5b862c262558236dacb5/map-20200424-3840-2160-relations-P403.csv';
			return fetch(riverUrl, { mode: "cors" });
		})
		.then((response) => response.text())
		.then(async (data) => {
			const tStartRender = performance.now();

			await drawRivers(data, ctx);

			const tEndRender = performance.now();
			const perfP = document.getElementById( 'performance' );
			perfP.textContent += ` Rendering rivers took ${( tEndRender - tStartRender )} milliseconds.`;
		});

	return canvas;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	showDensity(config[index].x, config[index].y, config[index].url);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
