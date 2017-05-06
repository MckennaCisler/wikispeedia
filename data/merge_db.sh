#!/bin/sh
# based on http://stackoverflow.com/questions/80801/how-can-i-merge-many-sqlite-databases
sqlite3 wikipedia.sqlite3 -cmd "ATTACH 'wikipedia-newer.sqlite3' as toMerge;
BEGIN;
INSERT OR IGNORE INTO links SELECT * FROM toMerge.links;
COMMIT;
DETACH toMerge;"
