import config from './config.js';

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

function createAndRenderDensityCanvas(x, y, url) {
	const canvas = document.createElement('canvas');
	document.querySelector('body').appendChild(canvas);

	fetch(url, { mode: "cors" })
		.then((response) => response.text())
		.then((data) => {
			canvas.width = x;
			canvas.height = y;
			const ctx = canvas.getContext("2d");
			ctx.fillStyle = "black";
			ctx.fillRect(0, 0, canvas.width, canvas.height);

			const csvLines = data.split("\n");
			const tStartRender = performance.now();
			csvLines.forEach(async (line) => {
				const [x, y, numberOfItems] = line.split(",");
				const ci = (255 * numberOfItems) / 100;
				ctx.fillStyle = `rgb(${ci}, ${ci}, ${ci})`;
				ctx.fillRect(x, y, 1, 1);
			});
			const tEndRender = performance.now();
			const perfP = document.getElementById("performance");
			perfP.textContent = `Render for ${x}x${y} took ${(tEndRender - tStartRender)} milliseconds.`;
		});

	return canvas;
}

const form = document.getElementById('resolutionSelector');

function updateCanvas() {
	const index = form.querySelector('input[name="resolution"]:checked').value;
	console.log(`switching to ${index}`);
	showDensity(config[index].x, config[index].y, config[index].url);
}

updateCanvas();
form.addEventListener('change', updateCanvas);
