CREATE TABLE SRC_DB(
ID INT PRIMARY KEY auto_increment,
VERSION_DATE varchar(20), 
TABLE_NAME VARCHAR(255),
COLUMN_NAME VARCHAR(255),
COLUMN_TYPE VARCHAR(100),
COLUMN_SIZE int
);
CREATE TABLE TAR_DB(
ID INT PRIMARY KEY auto_increment,
VERSION_DATE varchar(20), 
TABLE_NAME VARCHAR(255),
COLUMN_NAME VARCHAR(255),
COLUMN_TYPE VARCHAR(100),
COLUMN_SIZE int
);