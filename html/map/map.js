var map = [];
var maxx = 0;
var maxy = 0;
var geodata = {};
var lang = 'en';

var zoom = 0;
var lat = 0;
var lon = 0;

function hexToRgb(hex) {
    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
};

var globalrefreshcount = 0;

function refresh() {
	globalrefreshcount += 1;
	setTimeout("refresh__(" + globalrefreshcount + ")", 80);
};

function refresh__(refreshcount) {
	if (globalrefreshcount > refreshcount) return;
	var canvas = document.getElementById('canvas');
	var context = canvas.getContext('2d');
	canvas.width = window.innerWidth-20;
	canvas.height = window.innerHeight-80;
	context.clearRect(0, 0, canvas.width, canvas.height);
	context.fillStyle="#000000";
	context.fillRect(0, 0, canvas.width, canvas.height);
		
	var imageData = context.getImageData(0, 0, canvas.width, canvas.height);
	var pixels = imageData.data;
	maxx = canvas.width;
	maxy = canvas.height;
	map = [];
	map2 = [];
	
	var color = $('input[name=loccolor]').val();
	var rgb = hexToRgb(color);
	var diffr = rgb.r;
	var diffg = rgb.g;
	var diffb = rgb.b;
	
	//lang = document.getElementById('lang').value;
	
	var addpixel = function(x, y) {
		pixels[x*4   + y*maxx*4] = Math.min(255, pixels[x*4   + y*maxx*4]+diffr);
		pixels[x*4+1 + y*maxx*4] = Math.min(255, pixels[x*4+1 + y*maxx*4]+diffg);
		pixels[x*4+2 + y*maxx*4] = Math.min(255, pixels[x*4+2 + y*maxx*4]+diffb);
	};
	
	if (zoom == 0) {
		var trafox = function(x) {
			return Math.floor((180.0+x)/360.0*maxx);
		};
		var trafoy = function(y) {
			return Math.floor((90.0-y)/180.0*maxy);
		};
		var inBoundary = function(item) {
			return true;
		};
	} else {
		var trafox = function(x) {
			var lonWindow = 180.0 * Math.pow(0.5, zoom);
			return Math.floor((x-(lon-lonWindow))/(lonWindow*2)*maxx);
		};
		var trafoy = function(y) {
			var latWindow = 90.0 * Math.pow(0.5, zoom);
			return maxy-Math.floor((y-(lat-latWindow))/(latWindow*2)*maxy);
		};
		var inBoundary = function(item) {
			var latWindow = 90.0 * Math.pow(0.5, zoom);
			var lonWindow = 180.0 * Math.pow(0.5, zoom);
			if (geodata[item].x > lat+latWindow) return false; // north
			if (geodata[item].x < lat-latWindow) return false; // south
			if (geodata[item].y > lon+lonWindow) return false; // east
			if (geodata[item].y < lon-lonWindow) return false; // west
			return true;
		};
	}
	
	for (var item in geodata) {
		if (inBoundary(item)) {
			addpixel(trafox(geodata[item].y), trafoy(geodata[item].x));
			map[trafox(geodata[item].y) + trafoy(geodata[item].x) * maxx] = item;
			map2[trafox(geodata[item].y)+1 + trafoy(geodata[item].x) * maxx] = item;
			map2[trafox(geodata[item].y)-1 + trafoy(geodata[item].x) * maxx] = item;
			map2[trafox(geodata[item].y) + (trafoy(geodata[item].x)+1) * maxx] = item;
			map2[trafox(geodata[item].y) + (trafoy(geodata[item].x)-1) * maxx] = item;
			map2[trafox(geodata[item].y)+1 + (trafoy(geodata[item].x)+1) * maxx] = item;
			map2[trafox(geodata[item].y)-1 + (trafoy(geodata[item].x)-1) * maxx] = item;
			map2[trafox(geodata[item].y)-1 + (trafoy(geodata[item].x)+1) * maxx] = item;
			map2[trafox(geodata[item].y)+1 + (trafoy(geodata[item].x)-1) * maxx] = item;
		}
	}
	
	context.putImageData(imageData, 0, 0);
	
	for (var property in graph) {
		var transparency = $('input[name=' + property + 'trans]').val();
		if (transparency == undefined) {
			transparency = $('input[name=otherstrans]').val();
		};
		transparency = transparency / 100;
		if (transparency  == 0) continue;
		var color = $('input[name=' + property + 'color]').val();
		if (color == undefined) {
			color = $('input[name=otherscolor]').val();
		}
		var rgb = hexToRgb(color);
		context.strokeStyle =  'rgba(' + rgb.r + ',' + rgb.g + ',' + rgb.b + ',' + transparency + ')';
		for (var from in graph[property]) {
			for (var number in graph[property][from]) {
				var to = graph[property][from][number];
				if (!((from in geodata) && (to in geodata))) continue;
				left = from;
				right = to;
				if (geodata[left].y > geodata[right].y) {
					left = to;
					right = from;
				};
				if ((geodata[right].y-geodata[left].y) < 190) {
					context.beginPath();
					context.moveTo(trafox(geodata[left].y), trafoy(geodata[left].x));
					context.lineTo(trafox(geodata[right].y), trafoy(geodata[right].x));
					context.stroke();
				} else {
					context.beginPath();
					context.moveTo(trafox(geodata[left].y), trafoy(geodata[left].x));
					context.lineTo(trafox(geodata[right].y-360), trafoy(geodata[right].x));
					context.stroke();
					context.beginPath();
					context.moveTo(trafox(geodata[left].y+360), trafoy(geodata[left].x));
					context.lineTo(trafox(geodata[right].y), trafoy(geodata[right].x));
					context.stroke();
				}
			}
		}
	}
	
	document.getElementById('zoomout').disabled = (zoom<1);
};

function show(e) {
	var canvas = document.getElementById('canvas');
	var x = e.pageX - canvas.offsetLeft - 1;
	var y = e.pageY - canvas.offsetTop - 2;
	if (map[x + y * maxx] !== undefined) {
		document.getElementById('location').innerHTML = geodata[map[x + y * maxx]].label;
		document.getElementById('location').href = "https://www.wikidata.org/wiki/" + map[x + y * maxx];
		document.getElementById('location').focus();
	} else if (map2[x + y * maxx] !== undefined) {
		document.getElementById('location').innerHTML = geodata[map2[x + y * maxx]].label;
		document.getElementById('location').href = "https://www.wikidata.org/wiki/" + map2[x + y * maxx];
		document.getElementById('location').focus();
	}
};

function zoomout() {
	zoom -= 1;
	refresh();
};

function setlat(y) {
	if (zoom == 0) {
		lat = 90.0-y/maxy*180.0;
	} else {
		var latWindow = 90.0 * Math.pow(0.5, zoom);
		lat = lat + ((0.5-(y/maxy)) * latWindow * 2);
	};
};

function setlon(x) {
	if (zoom == 0) {
		lon = x/maxx*360.0-180.0;
	} else {
		var lonWindow = 180.0 * Math.pow(0.5, zoom);
		lon = lon + (((x/maxx)-0.5) * lonWindow * 2);
	};
};

function zoomin(e) {
	var canvas = document.getElementById('canvas');
	var x = e.pageX - canvas.offsetLeft - 1;
	var y = e.pageY - canvas.offsetTop - 2;
	setlat(y);
	setlon(x);
	zoom += 1;
	refresh();
};

$( document ).ready(function() {
	document.getElementById('loading').style.display = 'none';
	refresh();
	document.getElementById('canvas').onmousemove = show;
	document.getElementById('canvas').ondblclick = zoomin;
	document.getElementById('location').focus();
	$('input').change(refresh);
});