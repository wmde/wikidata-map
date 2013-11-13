# This is the main entry point

#initial analysis....
cd wda
python wda-analyze-edits-and-write-kb.py
echo Initial analysis done.
gzip -f kb.txt

echo Move Knowledgebase to public
cp kb.txt.gz ../~/public_html/kb.txt.gz
cp kb.txt.gz ../wikidata-analytics/kb.txt.gz
cd ../wikidata-analytics

echo Coordinates
python geo.py
cp geo.txt.gz ../~/public_html/geo.txt.gz
python geo2png.py
cp map_icon.png ../~/public_html/map_icon.png
cp map_tiny.png ../~/public_html/map_tiny.png
cp map_small.png ../~/public_html/map_small.png
cp map_normal.png ../~/public_html/map.png
cp map_normal.png ../~/public_html/map_normal.png
cp map_big.png ../~/public_html/map_big.png
cp map_huge.png ../~/public_html/map_huge.png

echo Globe
python geo2json.py
cp wd.js ../wikiglobe/data/wd.js
cd ../wikiglobe/data
node twodtothreed.js
cd ../..
cp wikiglobe/data/wd.js ~/public_html/wikiglobe/data/wd.js
cp wikiglobe/data/data-wd1.json ~/public_html/wikiglobe/data/data-wd1.json
cp wikiglobe/data/data-wd2.json ~/public_html/wikiglobe/data/data-wd2.json
cp wikiglobe/data/data-wd3.json ~/public_html/wikiglobe/data/data-wd3.json
cp wikiglobe/data/data-wd4.json ~/public_html/wikiglobe/data/data-wd4.json
cp wikiglobe/data/data-wd5.json ~/public_html/wikiglobe/data/data-wd5.json
cp wikiglobe/data/data-wd10.json ~/public_html/wikiglobe/data/data-wd10.json

cd wikidata-analytics
echo Time
python timeline.py
sort timepoints.txt > timeline.txt
gzip -f timeline.txt
cp timeline.txt.gz ~/public_html/timeline.txt.gz

echo Create Knowledge graph
python graph.py
echo Copy Knowledge graph to public_html
cp graph.txt.gz ~/public_html/graph.txt.gz

echo Create Wikidata map data
python geolabel.py
python geolabel2wdlabel.py
cp wdlabel.js ~/public_html/map/wdlabel.js
python geograph.py
python geograph2geojs.py
cp graph.js ~/public_html/map/graph.js

echo Create trees
python propertyset.py 279
python propertyset.py 171
python tree.py 279
python tree.py 171
cp data/P279-tree.txt ~/public_html/P279-tree.txt
cp data/P171-tree.txt ~/public_html/P171-tree.txt

echo Types
python types.py
cp types.txt ~/public_html/types.txt

echo List of Wikipedia language codes
python wikipedias.py
cp data/wikipedias.txt ~/public_html/wikipedias.txt

echo Stats
python countstatements.py
cp stats.txt ~/public_html/stats.txt
