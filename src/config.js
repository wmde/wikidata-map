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
			id: 'P190',
			name: 'twinned administrative body',
			lineMaxPercent: 75,
			strokeStyle: "rgba(255, 0, 0, 0.025)"
		},
		{
			id: 'P197',
			name: 'adjacent station',
			lineMaxPercent: 25,
			strokeStyle: "rgba(0, 255, 0, 0.20)"
		},
		{
			id: 'P403',
			name: 'mouth of the watercourse',
			lineMaxPercent: 33,
			strokeStyle: "rgba(0, 0, 255, 0.15)"
		},
	]
};

export default config;