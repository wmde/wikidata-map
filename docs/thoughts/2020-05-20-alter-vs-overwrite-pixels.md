 - By: Addshore
 - Date: 20 May 2020
 
 In order to be able to use a single data set of a single resolution instead of providing multiple resolution files
 I had the idea of using the higher resolution files and scaling the xy coordinates when drawing.
 
 This requires one of the below:
  - the code to know about the pixels around itself that might be combined into 1
  - the ability to update pixels with a new intensity (adding onto what has already been rendered).
 
 I experimented with the idea, but ultimately my initial experiment decreased the performance too much to be nice.
 
 Other alternatives would be:
  - Do this in the worker? But then either:
    - The worker needs to know about all pixel locations and values (keeping its own fake canvas? or some other index.)
  - Do the scaling down logic on the server side?
    - but then we would need a server with logic

 **Rewrite pixels experiment**
 
 My experiment:
  - maintained the same map apperance
  - set RGB instead of HSL color
  - faked the hsl value using some simple maths correctly scaling the rgb values which you can access via image data
  - DECREASED PERFORMANCE of the render (due to the extra logic)

The below code can be used as a drop in replacement of the drawDot method at the current code revision. 
 ```
export function drawDot( ctx, values ) {
	const [ x, y, numberOfItems ] = values;
	// We might try to manipulate "bad" pixels, so dont do that....
	try{
		let pixel = ctx.getImageData(x, y, 1, 1)

		let lastRed = pixel.data[0]
		let lastBlue = pixel.data[2]
		let lastIntensityPercent = ((lastRed/2) + (lastBlue/2)) / 255 * 100
		let nextIntensityPercent = lastIntensityPercent + (numberOfItems)

		pixel.data[0] = 5 * nextIntensityPercent;
		pixel.data[1] = 2 * nextIntensityPercent;
		pixel.data[2] = 5 * (nextIntensityPercent-50)

		ctx.putImageData(pixel, x, y)

		//ctx.fillStyle = `hsl(23, 100%, ${numberOfItems}%)`;
		//ctx.fillRect( x, y, 1, 1 );

	} catch(err){
	}
}
```
