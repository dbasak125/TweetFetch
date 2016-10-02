# TweetFetch
Twitter REST API data pull

This is using the twitter4j library (wrapper for Twitter REST APIs) to query Twitter with REST API parameters and returns Tweet JSON documents. The execution starts in main(). Queries are stored in a String array sarr.

After validating application identity using OAuth, queries are issued in a loop. A counter keeps count (variable int j) of the number of queries issued to Twitter API and sleeps the thread if the standard limit of 180 queries per 15 minutes has been crossed. The program thus keeps a tarck of 15 minute windows and manages query issuance automatically in a single execution instance.

The json is written in an external file as per required formatting.

This .json file containing collected Tweets is then further used by the Append class to format the json suitably for indexing in Apache Lucene/Solr.
