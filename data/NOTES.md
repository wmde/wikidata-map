Data is generated from a Wikidata JSON dump loaded into Hadoop.

Queries in this file were written and run on 9 May 2020 by Addshore...

**Extract the item coordinates from the dump into our own table:**
```
DROP TABLE IF EXISTS addshore.wikidata_map_item_coordinates;

CREATE EXTERNAL TABLE addshore.wikidata_map_item_coordinates (
    `id` string                            COMMENT 'The id of the entity, Q32753077 for instance',
    `globe` string,
    `longitude` string,
    `latitude` string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES ( 'escapeChar'='\\',  'quoteChar'='\"',  'separatorChar'=',' )
STORED AS TEXTFILE;

INSERT INTO addshore.wikidata_map_item_coordinates
SELECT
    id,
    get_json_object(claim.mainsnak.datavalue.value, '$.globe') as globe,
    get_json_object(claim.mainsnak.datavalue.value, '$.longitude') as longitude,
    get_json_object(claim.mainsnak.datavalue.value, '$.latitude') as latitude
FROM wmf.wikidata_entity
LATERAL VIEW explode(claims) t AS claim
WHERE snapshot='2020-03-02'
    AND typ = 'item'
    AND claim.mainsnak.property = 'P625'
    AND claim.mainsnak.typ = 'value'
```

**Figure out item pixel locations**

From here was want to calculate pixel locations for a canvas, to avoid doing any computation on the client.

The primary target canvas size is 1920 x 1080.

The old "huge" map rendered at 8000 x 4000.

In order to get to a similar quality we will x4 the target size, to 7680 x 4320.

 - TODO add ids to the pixel entries, so they can be displayed on the map..
 - TODO the below query is for earth only...

```
DROP TABLE IF EXISTS addshore.wikidata_map_item_pixels;

CREATE EXTERNAL TABLE addshore.wikidata_map_item_pixels (
    `id` string,
    `posx` int,
    `posy` int
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES ( 'escapeChar'='\\',  'quoteChar'='\"',  'separatorChar'=',' )
STORED AS TEXTFILE;

INSERT INTO addshore.wikidata_map_item_pixels
SELECT
    id, 
    cast((cast(longitude as decimal(15, 10)) + 180) / 361 * 7680 as int) as posx,
    cast(abs((cast(latitude as decimal(15, 10)) - 90) / 181 * 4320)as int) as posy
FROM addshore.wikidata_map_item_coordinates
WHERE globe = "http://www.wikidata.org/entity/Q2";
```

**Figure out item relations**

Currently this is done for:
 - P403 (mouth of watercourse)

Figure out the items with relations:
```
DROP TABLE IF EXISTS addshore.wikidata_map_item_relations;

CREATE EXTERNAL TABLE addshore.wikidata_map_item_relations (
    `fromId` string,
    `toId` string,
    `forId` string
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES ( 'escapeChar'='\\',  'quoteChar'='\"',  'separatorChar'=',' )
STORED AS TEXTFILE;

INSERT INTO addshore.wikidata_map_item_relations
SELECT
    id AS fromId,
    get_json_object(claim.mainsnak.datavalue.value, '$.id') as toId,
    claim.mainsnak.property as forId
FROM wmf.wikidata_entity
LATERAL VIEW explode(claims) t AS claim
WHERE snapshot='2020-03-02'
    AND typ = 'item'
    AND claim.mainsnak.property IN ( 'P190', 'P197', 'P403' )
    AND claim.mainsnak.typ = 'value';
```

**Figure out item relation pixel locations**

TODO make the below query generic and not hard coded to the 1 property P403

Then figure out how the relations relate to our pixel map:
```
DROP TABLE IF EXISTS addshore.wikidata_map_item_relation_pixels;

CREATE EXTERNAL TABLE addshore.wikidata_map_item_relation_pixels (
    `forId` string,
    `posx1` int,
    `posy1` int,
    `posx2` int,
    `posy2` int
)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES ( 'escapeChar'='\\',  'quoteChar'='\"',  'separatorChar'=',' )
STORED AS TEXTFILE;

INSERT INTO addshore.wikidata_map_item_relation_pixels
SELECT
    x.forId as forId,
    a.posx as posx1,
    a.posy as posy1,
    b.posx as posx2,
    b.posy as poxy2
FROM (
    SELECT fromId, toId, forId
    FROM addshore.wikidata_map_item_relations
) x
JOIN addshore.wikidata_map_item_pixels a ON (a.id = x.fromId)
JOIN addshore.wikidata_map_item_pixels b ON (b.id = x.toId)
GROUP BY
    x.forId,
    a.posx,
    a.posy,
    b.posx,
    b.posy
LIMIT 100000000;
```

**Generate the CSVs**

```
hive -e "SELECT posx, posy, COUNT(*) as count FROM addshore.wikidata_map_item_pixels GROUP BY posx, posy ORDER BY count ASC LIMIT 100000000" | sed 's/[\t]/,/g'  > map-2020-03-02-7680-4320-pixels.csv 
hive -e "SELECT posx1, posy1, posx2, posy2 FROM addshore.wikidata_map_item_relation_pixels WHERE forId = 'P190' LIMIT 100000000" | sed 's/[\t]/,/g'  > map-2020-03-02-7680-4320-relation-pixels-P190.csv 
hive -e "SELECT posx1, posy1, posx2, posy2 FROM addshore.wikidata_map_item_relation_pixels WHERE forId = 'P197' LIMIT 100000000" | sed 's/[\t]/,/g'  > map-2020-03-02-7680-4320-relation-pixels-P197.csv 
hive -e "SELECT posx1, posy1, posx2, posy2 FROM addshore.wikidata_map_item_relation_pixels WHERE forId = 'P403' LIMIT 100000000" | sed 's/[\t]/,/g'  > map-2020-03-02-7680-4320-relation-pixels-P403.csv 
```

**Publish the CSVs**

This can take a little while to show up...

```
cp map-2020-03-02-7680-4320-pixels.csv /srv/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-pixels.csv
cp map-2020-03-02-7680-4320-relation-pixels-P190.csv  /srv/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-relation-pixels-P190.csv 
cp map-2020-03-02-7680-4320-relation-pixels-P197.csv  /srv/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-relation-pixels-P197.csv 
cp map-2020-03-02-7680-4320-relation-pixels-P403.csv  /srv/published/datasets/one-off/wikidata/addshore/map-2020-03-02-7680-4320-relation-pixels-P403.csv 
published-sync
```

Make sure the file appears: https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/
