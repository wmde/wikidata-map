const config = {
	maps: {
		'2020-08-24' : {
			x: 7680,
			y: 4320,
			url: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2020-08-24-7680-4320-pixels.csv',
			layerUrl: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2020-08-24-7680-4320-relation-pixels-{property}.csv',
		},
		'2021-10-18' : {
			x: 7680,
			y: 4320,
			url: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2021-10-18-7680-4320-pixels.csv',
			layerUrl: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2021-10-18-7680-4320-relation-pixels-{property}.csv',
		}
	},
	layers: [
		{
			id: 'P17',
			name: 'country',
			lineMaxPercent: 75,
			strokeStyle: "rgba(255, 255, 100, 0.015)"// YELLOW
		},
		{
			id: 'P36',
			name: 'capital',
			lineMaxPercent: 75,
			strokeStyle: "rgba(135, 75, 255, 0.2)"// PURPLE
		},
		{
			id: 'P47',
			name: 'shares border with',
			lineMaxPercent: 75,
			strokeStyle: "rgba(255, 150, 100, 0.1)"// ORANGE
		},
		{
			id: 'P138',
			name: 'nammed after',
			lineMaxPercent: 75,
			strokeStyle: "rgba(100, 255, 255, 0.025)"// CYAN
		},
		{
			id: 'P150',
			name: 'administritive entity',
			lineMaxPercent: 75,
			strokeStyle: "rgba(255, 75, 255, 0.025)"// PINK
		},
		{
			id: 'P190',
			name: 'twinned administrative body',
			lineMaxPercent: 75,
			strokeStyle: "rgba(255, 0, 0, 0.025)"// RED
		},
		{
			id: 'P197',
			name: 'adjacent station',
			lineMaxPercent: 25,
			strokeStyle: "rgba(0, 255, 0, 0.20)"// GREEN
		},
		{
			id: 'P403',
			name: 'mouth of the watercourse',
			lineMaxPercent: 33,
			strokeStyle: "rgba(0, 0, 255, 0.12)"// BLUE
		},
	]
};

export default config;