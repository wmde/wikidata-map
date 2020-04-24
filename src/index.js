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
			perfP.textContent += ` Render for ${x}x${y} took ${(tEndRender - tStartRender)} milliseconds.`;

			if (x < 3000) {
				const riverUrl = 'https://gist.githubusercontent.com/addshore/01c9aa9c449b8208c1da06010017a469/raw/54fefc91332dcb779abe5b862c262558236dacb5/map-20200424-3840-2160-relations-P403.csv';
				fetch(riverUrl, { mode: "cors" })
					.then((response) => response.text())
					.then((data) => {
						const ctx = canvas.getContext("2d");
						const csvLines = data.split("\n");

						const tStartRender = performance.now();

						csvLines.forEach(async (line) => {
							const [x1, y1, x2, y2] = line.split(",");
							const distance = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
							if (distance > 300) {
								return;
							}
							ctx.strokeStyle = `blue`;
							ctx.beginPath();
							ctx.moveTo(x1, y1);
							ctx.lineTo(x2, y2);
							ctx.stroke();
						});

						const tEndRender = performance.now();
						const perfP = document.getElementById("performance");
						perfP.textContent += ` Render for rivers took ${(tEndRender - tStartRender)} milliseconds.`;
					});

			}
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
