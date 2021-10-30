# Data

Data is generated from a Wikidata JSON dump loaded into Hadoop on the Wikimedia Foundation Analaytics cluster.

Queries in this file were origionally written and run on 9 May 2020 by Addshore...

## Setting up data infrastruture

These tables only need to be created once...

```sql
CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_coordinates (
    `id` string                            COMMENT 'The id of the entity, Q32753077 for instance',
    `globe` string,
    `longitude` string,
    `latitude` string
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';

CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_pixels (
    `id` string,
    `posx` int,
    `posy` int
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';

CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_relations (
    `fromId` string,
    `toId` string,
    `forId` string
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';

CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_relation_pixels (
    `forId` string,
    `posx1` int,
    `posy1` int,
    `posx2` int,
    `posy2` int
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
ROW FORMAT SERDE 'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
STORED AS INPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat';
```

## Generating data

### Setup

- Log into `stat1007.eqiad.wmnet`
- Do `kinit` to auth yourself
- Open up a spark shell

```sh
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64
```

You can read more about the WMF spark setup [here](https://wikitech.wikimedia.org/wiki/Analytics/Systems/Cluster/Spark).

### Selecting snapshot & poking settings

```sql
SHOW PARTITIONS wmf.wikidata_entity;
```

And set the `WIKIDATA_MAP_SNAPSHOT` variable to the snapshot you wish to generate data for.

```sql
SET hivevar:WIKIDATA_MAP_SNAPSHOT='2021-10-18';
```

You also need to set this:

```sql
SET hive.exec.dynamic.partition.mode=nonstrict;
```

### Extracting initial data

```sql
INSERT INTO addshore.wikidata_map_item_coordinates
PARTITION(snapshot)
SELECT
    id,
    get_json_object(claim.mainsnak.datavalue.value, '$.globe') as globe,
    get_json_object(claim.mainsnak.datavalue.value, '$.longitude') as longitude,
    get_json_object(claim.mainsnak.datavalue.value, '$.latitude') as latitude,
    snapshot
FROM wmf.wikidata_entity
LATERAL VIEW explode(claims) t AS claim
WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT}
    AND typ = 'item'
    AND claim.mainsnak.property = 'P625'
    AND claim.mainsnak.typ = 'value'; LIMIT 10;
```

### Calculate pixel locations

From here was want to calculate pixel locations for a canvas, to avoid doing any computation on the client.

The primary target canvas size is 1920 x 1080.

The old "huge" map rendered at 8000 x 4000.

In order to get to a similar quality we will x4 the target size, to 7680 x 4320.

 - TODO add ids to the pixel entries, so they can be displayed on the map..
 - TODO the below query is for earth only...

```sql
INSERT INTO addshore.wikidata_map_item_pixels
PARTITION(snapshot)
SELECT
    id, 
    cast((cast(longitude as decimal(15, 10)) + 180) / 361 * 7680 as int) as posx,
    cast(abs((cast(latitude as decimal(15, 10)) - 90) / 181 * 4320)as int) as posy,
    snapshot
FROM addshore.wikidata_map_item_coordinates
WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT}
    AND globe = "http://www.wikidata.org/entity/Q2";
```

### Calculate item relations

Currently this is done for:

- [P17](https://www.wikidata.org/wiki/Property:P17)
- [P36](https://www.wikidata.org/wiki/Property:P36)
- [P47](https://www.wikidata.org/wiki/Property:P47)
- [P138](https://www.wikidata.org/wiki/Property:P138)
- [P150](https://www.wikidata.org/wiki/Property:P150)
- [P190 (twinned administrative body)](https://www.wikidata.org/wiki/Property:P190)
- [P197 (adjacent station)](https://www.wikidata.org/wiki/Property:P197)
- [P403 (mouth of watercourse)](https://www.wikidata.org/wiki/Property:P403)

```sql
INSERT INTO addshore.wikidata_map_item_relations
PARTITION(snapshot)
SELECT
    id AS fromId,
    get_json_object(claim.mainSnak.dataValue.value, '$.id') as toId,
    claim.mainSnak.property as forId,
    snapshot
FROM wmf.wikidata_entity
LATERAL VIEW explode(claims) t AS claim
WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT}
    AND typ = 'item'
    AND claim.mainSnak.property IN ( 'P17', 'P36', 'P47', 'P138', 'P150', 'P190', 'P197', 'P403' )
    AND claim.mainSnak.typ = 'value';
```

### Calculate item relation pixel locations

Then figure out how the relations relate to our pixel map:

```sql
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
    FROM addshore.wikidata_map_item_relations
    WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT}
) x
JOIN addshore.wikidata_map_item_pixels a ON (a.id = x.fromId) AND a.snapshot=x.snapshot
JOIN addshore.wikidata_map_item_pixels b ON (b.id = x.toId) AND b.snapshot=x.snapshot
WHERE x.snapshot=${WIKIDATA_MAP_SNAPSHOT}
GROUP BY
    x.forId,
    a.posx,
    a.posy,
    b.posx,
    b.posy,
    x.snapshot
LIMIT 100000000;
```

## Check the ammount of data;

You should have rows for the correct snapshot in all of the tables...

```sql
SELECT COUNT(*) FROM addshore.wikidata_map_item_coordinates WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
SELECT COUNT(*) FROM addshore.wikidata_map_item_pixels WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
SELECT COUNT(*) FROM addshore.wikidata_map_item_relations WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
SELECT COUNT(*) FROM addshore.wikidata_map_item_relation_pixels WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
```

## Generate the CSVs

Exit spark and do the rest!

Set an environment variable with the snapshot date:

```sh
WIKIDATA_MAP_SNAPSHOT='2021-10-18'
PropertyArray=("P17"  "P36"  "P47"  "P138"  "P150"  "P190"  "P197"  "P403")
```

And write the files...

- `tail -n +2` removes the firt line of output, which will be `PYSPARK_PYTHON=python3.7`
- `sed 's/[\t]/,/g'` turns the TSV into a CSV

```sh
spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "SELECT posx, posy, COUNT(*) as count FROM addshore.wikidata_map_item_pixels WHERE snapshot = '${WIKIDATA_MAP_SNAPSHOT}' GROUP BY posx, posy ORDER BY count DESC LIMIT 100000000" | tail -n +2 | sed 's/[\t]/,/g'  > map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv
for PROPERTY in ${PropertyArray[*]}; do
    echo $PROPERTY
    spark2-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64 -e "SELECT posx1, posy1, posx2, posy2 FROM addshore.wikidata_map_item_relation_pixels WHERE snapshot = '${WIKIDATA_MAP_SNAPSHOT}' AND forId = '${PROPERTY}' LIMIT 100000000" | tail -n +2 | sed 's/[\t]/,/g'  > map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv
done
```

You should find the new files on disk in your current working directory.
You can check how many lines they have.

```sh
cat map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv | wc -l
for PROPERTY in ${PropertyArray[*]}; do
    echo $PROPERTY
    cat map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv | wc -l
done
```

## Publishing data

If everything went well, you are ready to publish the data.

### Publishing the CSVs

Set an environment variable with the snapshot date:

```sh
WIKIDATA_MAP_SNAPSHOT='2021-10-18'
PropertyArray=("P17"  "P36"  "P47"  "P138"  "P150"  "P190"  "P197"  "P403")
```

And move them into the published directory

```sh
cp map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv /srv/published/datasets/one-off/wikidata/addshore/map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-pixels.csv
for PROPERTY in ${PropertyArray[*]}; do
    echo $PROPERTY
    cp map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv /srv/published/datasets/one-off/wikidata/addshore/map-${WIKIDATA_MAP_SNAPSHOT}-7680-4320-relation-pixels-${PROPERTY}.csv 
done
published-sync
```

This can take a little while to show up...

Make sure the file appears: https://analytics.wikimedia.org/published/datasets/one-off/wikidata/addshore/
