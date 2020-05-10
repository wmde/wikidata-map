const config = {
	maps: [
		{
			x: 1920,
			y: 1080,
			url: "https://gist.githubusercontent.com/addshore/e51a9f03decc66d9267f19323d1ac0cf/raw/f0c0aba2e1cc39a2ff14ee14ace81d1e51bac43b/map-20200424-1920-1080-pixels.csv",
			layerUrl: "https://gist.githubusercontent.com/addshore/01c9aa9c449b8208c1da06010017a469/raw/54fefc91332dcb779abe5b862c262558236dacb5/map-20200424-3840-2160-relations-{property}.csv"
		},
		{
			x: 3840,
			y: 2160,
			url: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-20200424-3840-2160-pixels.csv',
			layerUrl: '',
		},
		{
			x: 7680,
			y: 4320,
			url: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-pixels.csv',
			layerUrl: 'https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-relation-pixels-{property}.csv',
		}
	],
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
