import config from './config';

const wdMapCanvases = [];

function showDensity(x, y, url) {
	if (!wdMapCanvases[`${x}x${y}`]) {
		wdMapCanvases[`${x}x${y}`] = createAndRenderDensityCanvas(x, y, url)
	}
	wdMapCanvases.forEach(canvas => canvas.style.display = 'none');
	wdMapCanvases[`${x}x${y}`].style.display = 'block;'
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

showDensity(config[0].x, config[0].y, config[0].url);
