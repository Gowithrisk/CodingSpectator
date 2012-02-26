--This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.

\p Inserting some "pseudo" IDs that correspond with the UDC umbrella IDs for refactorings

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES
('org.eclipse.jdt.ui.rename.all');

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES ('org.eclipse.jdt.ui.move.all');

INSERT INTO "PUBLIC"."ALL_DATA" ("id") VALUES
('org.eclipse.jdt.ui.inline.all');

DROP TABLE "PUBLIC"."UDC_DATA" IF EXISTS;

CREATE TABLE "PUBLIC"."UDC_DATA" (

  "YEARMONTH" VARCHAR(1000),

  "COMMAND" VARCHAR(1000),

  "BUNDLEID" VARCHAR(1000),

  "BUNDLEVERSION" VARCHAR(1000),

  "EXECUTECOUNT" INT,

  "USERCOUNT" INT

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."UDC_DATA"

\p Importing UDC data

\m commands.csv

DROP TABLE "PUBLIC"."REFACTORING_CHANGE_SIZE" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_CHANGE_SIZE" (

  "USERNAME" VARCHAR(100),

  "WORKSPACE_ID" VARCHAR(100000),

  "VERSION" VARCHAR(100),

  "TIMESTAMP" BIGINT,

  "REFACTORING_ID" VARCHAR(100),

  "AFFECTED_FILES_COUNT" INT,

  "AFFECTED_LINES_COUNT" INT

);

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_CHANGE_SIZE"

\p Importing the sizes of refactorings

\m refactoring_change_intensity.csv

* *DSV_TARGET_TABLE ="PUBLIC"."UDC_ECLIPSE_MAPPING"

DROP TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" IF EXISTS;

CREATE TABLE "PUBLIC"."UDC_ECLIPSE_MAPPING" (

"UDCID" VARCHAR(1000),

"ECLIPSEID" VARCHAR(1000)

);

\p Importing the mapping between the IDs of refactorings in UDC data and Eclipse refactoring histories

\m refactoringmapping.csv

\p Importing the mapping between the IDs of refactorings and their human readable names

DROP TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" IF EXISTS;

CREATE TABLE "PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME" (

  REFACTORING_ID VARCHAR(100),

  HUMAN_READABLE_NAME VARCHAR(100)

);

* *DSV_COL_SPLITTER =,

* *DSV_TARGET_TABLE ="PUBLIC"."REFACTORING_ID_TO_HUMAN_NAME"

\m refactoring_id_human_name_mapping.csv

