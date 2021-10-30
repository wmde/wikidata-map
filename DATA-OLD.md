# Data Old (wikidata-analysis & wikidata toolkit)

Hadoop only currently has the last few months of data in it.
So this dataset can not easily (over the WikidataCon weekend) be used to generate maps of previous years.
However all of the "old" map data still exists in JSON form and we can use this to extra what we need from past dumps quickly...

## The old format

The old format files are stored at https://wikidata-analysis.toolforge.org/ sorted by date.

### wdlabel.json

The `wdlabel.json` files contain all Items that are detected as having coordinates.
Along with the labels, which we can ignore...

In 2019 this file was 547M.

```json
{
    "Q202445": {
        "label": "Cabourg",
        "y": -0.13333361111111,
        "x": 49.283333611111
    },
    "Q202446": {
        "label": "-",
        "y": 13.54763611,
        "x": 49.90032222
    }
}
```

To get this into a table we would need to transform it to something like this:

```csv
Q202445, Q2, -0.13333361111111, 49.283333611111
Q202446, Q2, 13.54763611, 49.90032222
```

### graph.json

The graph.json files contain Properties, and the various relationships between items as part of that property.

In 2019 this file was 163M.

```json
{
    "P403": {
        "Q4161321": [
            "Q4537685"
        ],
        "Q4218828": [
            "Q4430628",
            "Q1009412"
        ]
    }
}
```

To get this into a table we would need to transform it to something like this:

```csv
Q4161321, Q4537685, P403
Q4218828, Q4430628, P403
Q4218828, Q1009412, P403
```

## Downloading the data

Download the files

```sh
curl --proxy http://webproxy.eqiad.wmnet:8080 https://wikidata-analysis.toolforge.org/2014/20141103/wdlabel.json > 2014-11-03-wdlabel.json
curl --proxy http://webproxy.eqiad.wmnet:8080 https://wikidata-analysis.toolforge.org/2014/20141103/graph.json > 2014-11-03-graph.json
```

## Munging the data

Munge it into csv format that looks like our tables? :)

```sh
# Outputs id, globe, lon, lat
cat 2014-11-03-wdlabel.json | jq --join-output --raw-output 'keys[] as $k | $k,"," ,"http://www.wikidata.org/entity/Q2",",", .[$k].y,",", .[$k].x, "\n"' > 2014-11-03-wdlabel.csv
# Output item1, item2, property
cat 2014-11-03-graph.json | jq --join-output --raw-output 'keys[] as $prop | .[$prop] | keys[] as $i1 | .[$i1][] as $i2 | $i1,",", $i2,",", $prop, "\n"' > 2014-11-03-graph.csv
```

Note: This output Lon the Lat for the coordinate (as was defined in the table field order)

## Create tables for storage

Use seperate tables from the main hadoop flow for now.

This uses `TEXTFILE`, so we can load the CSVs right in...

```sql
CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_coordinates_old_backfill_text (
    `id` string                            COMMENT 'The id of the entity, Q32753077 for instance',
    `globe` string,
    `longitude` string,
    `latitude` string
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
row format delimited 
fields terminated by ',' 
STORED AS TEXTFILE;

CREATE TABLE IF NOT EXISTS addshore.wikidata_map_item_relations_old_backfill_text (
    `fromId` string,
    `toId` string,
    `forId` string
)
PARTITIONED BY (
  `snapshot` string COMMENT 'Versioning information to keep multiple datasets (YYYY-MM-DD for regular weekly imports)')
row format delimited 
fields terminated by ',' 
STORED AS TEXTFILE;
```

## Loading into hdfs

Get a hive shell

```sh
hive
```

And set the date:

```sql
SET hivevar:WIKIDATA_MAP_SNAPSHOT='2014-11-03';
```


Load it into the text table first...

```sql
LOAD DATA LOCAL INPATH '2014-11-03-wdlabel.csv' INTO TABLE addshore.wikidata_map_item_coordinates_old_backfill_text PARTITION(snapshot="2014-11-03");
LOAD DATA LOCAL INPATH '2014-11-03-graph.csv' INTO TABLE addshore.wikidata_map_item_relations_old_backfill_text PARTITION(snapshot="2014-11-03");
```

Then the data can be processed into the "next" tables via the regular process..

