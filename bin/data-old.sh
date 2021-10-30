#!/bin/bash
# You can run this like ./bin/data-old-1.sh 2015-10-05 2015/20151005

WIKIDATA_MAP_SNAPSHOT=$1
WIKIDATA_ANALYSIS_DATE=$2
WIKIDATA_MAP_ITEM_COORD_TABLE=addshore.wikidata_map_item_coordinates_old_backfill_text
WIKIDATA_MAP_ITEM_RELATION_TABLE=addshore.wikidata_map_item_relations_old_backfill_text
PropertyArray=("P17"  "P36"  "P47"  "P138"  "P150"  "P190"  "P197"  "P403")

echo WIKIDATA_MAP_SNAPSHOT=$1
echo WIKIDATA_ANALYSIS_DATE=$2

# Download
curl --proxy http://webproxy.eqiad.wmnet:8080 https://wikidata-analysis.toolforge.org/${WIKIDATA_ANALYSIS_DATE}/wdlabel.json > ${WIKIDATA_MAP_SNAPSHOT}-wdlabel.json
curl --proxy http://webproxy.eqiad.wmnet:8080 https://wikidata-analysis.toolforge.org/${WIKIDATA_ANALYSIS_DATE}/graph.json > ${WIKIDATA_MAP_SNAPSHOT}-graph.json

# Munge
cat ${WIKIDATA_MAP_SNAPSHOT}-wdlabel.json | jq --join-output --raw-output 'keys[] as $k | $k,"," ,"http://www.wikidata.org/entity/Q2",",", .[$k].y,",", .[$k].x, "\n"' > ${WIKIDATA_MAP_SNAPSHOT}-wdlabel.csv
cat ${WIKIDATA_MAP_SNAPSHOT}-graph.json | jq --join-output --raw-output 'keys[] as $prop | .[$prop] | keys[] as $i1 | .[$i1][] as $i2 | $i1,",", $i2,",", $prop, "\n"' > ${WIKIDATA_MAP_SNAPSHOT}-graph.csv

# Load
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "LOAD DATA LOCAL INPATH '${WIKIDATA_MAP_SNAPSHOT}-wdlabel.csv' INTO TABLE addshore.wikidata_map_item_coordinates_old_backfill_text PARTITION(snapshot='${WIKIDATA_MAP_SNAPSHOT}');"
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "LOAD DATA LOCAL INPATH '${WIKIDATA_MAP_SNAPSHOT}-graph.csv' INTO TABLE addshore.wikidata_map_item_relations_old_backfill_text PARTITION(snapshot='${WIKIDATA_MAP_SNAPSHOT}');"

# Setup Process
echo "SET hive.exec.dynamic.partition.mode=nonstrict;
INSERT INTO addshore.wikidata_map_item_pixels
PARTITION(snapshot)
SELECT
    id, 
    cast((cast(longitude as decimal(15, 10)) + 180) / 361 * 7680 as int) as posx,
    cast(abs((cast(latitude as decimal(15, 10)) - 90) / 181 * 4320)as int) as posy,
    snapshot
FROM ${WIKIDATA_MAP_ITEM_COORD_TABLE}
WHERE snapshot='${WIKIDATA_MAP_SNAPSHOT}'
    AND globe = \"http://www.wikidata.org/entity/Q2\";" > map-process-1-tmp-${WIKIDATA_MAP_SNAPSHOT}.hql
echo "SET hive.exec.dynamic.partition.mode=nonstrict;
INSERT INTO addshore.wikidata_map_item_relation_pixels
PARTITION(snapshot)
SELECT
    x.forId as forId,
    a.posx as posx1,
    a.posy as posy1,
    b.posx as posx2,
    b.posy as posy2,
    x.snapshot as snapshot
FROM (
    SELECT fromId, toId, forId, snapshot
    FROM ${WIKIDATA_MAP_ITEM_RELATION_TABLE}
    WHERE snapshot='${WIKIDATA_MAP_SNAPSHOT}'
) x
JOIN addshore.wikidata_map_item_pixels a ON (a.id = x.fromId) AND a.snapshot=x.snapshot
JOIN addshore.wikidata_map_item_pixels b ON (b.id = x.toId) AND b.snapshot=x.snapshot
WHERE x.snapshot='${WIKIDATA_MAP_SNAPSHOT}'
GROUP BY
    x.forId,
    a.posx,
    a.posy,
    b.posx,
    b.posy,
    x.snapshot
LIMIT 100000000;" > map-process-2-tmp-${WIKIDATA_MAP_SNAPSHOT}.hql

# Actually process
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -f map-process-1-tmp-${WIKIDATA_MAP_SNAPSHOT}.hql
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -f map-process-2-tmp-${WIKIDATA_MAP_SNAPSHOT}.hql

# Generate our files
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "SELECT posx, posy, COUNT(*) as count FROM addshore.wikidata_map_item_pixels WHERE snapshot = '${WIKIDATA_MAP_SNAPSHOT}' GROUP BY posx, posy ORDER BY count DESC LIMIT 100000000" | tail -n +2 | sed 's/[\t]/,/g'  > map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv
for PROPERTY in ${PropertyArray[*]}; do
    echo $PROPERTY
    spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "SELECT posx1, posy1, posx2, posy2 FROM addshore.wikidata_map_item_relation_pixels WHERE snapshot = '${WIKIDATA_MAP_SNAPSHOT}' AND forId = '${PROPERTY}' LIMIT 100000000" | tail -n +2 | sed 's/[\t]/,/g'  > map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv
done

# Copy them to published/datasets
cp map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv /srv/published/datasets/one-off/wikidata/addshore/map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv
for PROPERTY in ${PropertyArray[*]}; do
    echo $PROPERTY
    cp map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv /srv/published/datasets/one-off/wikidata/addshore/map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv 
done

# You would then want to sync them!
published-sync