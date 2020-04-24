const dataUrl =
	"https://gist.githubusercontent.com/addshore/e51a9f03decc66d9267f19323d1ac0cf/raw/f0c0aba2e1cc39a2ff14ee14ace81d1e51bac43b/map-20200424-1920-1080-pixels.csv";
fetch(dataUrl, { mode: "cors" })
	.then((response) => response.text())
	.then((data) => {
		const canvas = document.getElementById("wdmap");
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
		console.log(
			"Render took " + (tEndRender - tStartRender) + " milliseconds."
		);
	});
