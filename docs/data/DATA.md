# Data

Data is currently generated from a Wikidata JSON dump loaded into Hadoop on the Wikimedia Foundation Analaytics cluster, see [DATA-NEW.md](DATA-NEW.md).

Old data can also be gathered from the old wikidata-analysis project, see [DATA-OLD.md](DATA-OLD.md).

Once data is collected, it then needs to be turned into pixel data, for that see [DATA-PIXELS.md](DATA-PIXELS.md).

The below diagram should provide some picture of both the OLD and NEW flows.

```
                                      +----------------+
                                      |                |
                                      |    Wikidata    |
                                      |                |
                                      +-------+--------+
                                              |
                                              | generate
                                              v
                                      +----------------+
                                      |                |
                                      |    Wikidata    |
                                      |   JSON  dump   |
                                      |                |
                                  +---+----------------+---+
                        old data  |                        | new data
                        copied to |                        | loaded into
                                  v                        v
                          +---------------+      +--------------------+
                          |               |      |                    |
                          |   Wikimedia   |      |   Wikimedia        |
                          |   ToolForge   |      |   Hadoop Cluster   |
                          |               |      |                    |
                          +---------------+      +--------------------+
                                  ^                        ^
                           access |                        | access
                                  |                        |
                      +-----------+----------+       +-----+---------------+
                      |                      |       |                     |
                      |   Wikidata Toolkit   |       | Spark SQL query     |
                      |   Dump Scan &        |       | Data Extraction     |
                      |   Data Extraction    |       |                     |
                      |                      |       |                     |
                      +------+---------------+       +-------------------+-+
                             |                                           |
                    generate |                                           | generate
                             v                                           v
+----------------------------------+        +-------------+          +------------------------------------+
|                                  |        |             |          |                                    |
| JSON files of:                   |        |  Munging &  |          | Tables of:                         |
|  - Item Coordinates & Labels     |<-------+  Loading    +--------->|  - Item, Globe, Cordinate          |
|  - Item relations for Properties | access |  (old)      | generate |  - Item1, Item2, Property relation |
|                                  |        |             |          |                                    |
+----------------------------------+        +-------------+          +-----------------+------------------+
                                                                                       |
                                              +----------------------------------------+
                                              |              process
                                              v
                         +-------------------------------------------+
                         |                                           |
                         |  Tables of pixel locations:               |
                         |   - Item, posx, posy                      |
                         |   - Property, posx1, posy1, posx2, posy2  |
                         |                                           |
                         +--------------------+----------------------+
                                              |
                                              | output
                                              v
                                     +----------------+
                                     |                |
                                     |   CSV Files    |
                                     |                |
                                     +----------------+
```

-- Generated on https://asciiflow.com/
