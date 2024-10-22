# Data New (Hadoop)

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
```

## Generating data

### Setup

- Log into `stat1011.eqiad.wmnet`
- Do `kinit` to auth yourself
- Open up a spark shell

```sh
spark3-sql --master yarn --executor-memory 8G --executor-cores 4 --driver-memory 2G --conf spark.dynamicAllocation.maxExecutors=64
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

## Check the ammount of data;

You should have rows for the correct snapshot in all of the tables...

```sql
SELECT COUNT(*) FROM addshore.wikidata_map_item_coordinates WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
SELECT COUNT(*) FROM addshore.wikidata_map_item_relations WHERE snapshot=${WIKIDATA_MAP_SNAPSHOT};
```
