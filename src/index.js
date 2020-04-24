import config from './config';

const dataUrl =	config[0].url;
fetch(dataUrl, { mode: "cors" })
	.then((response) => response.text())
	.then((data) => {
		const canvas = document.getElementById("wdmap");
		canvas.width = config[0].x;
		canvas.height = config[0].y;
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
		perfP.textContent = "Render took " + (tEndRender - tStartRender) + " milliseconds.";
	});
