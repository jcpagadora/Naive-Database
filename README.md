# Naive-Database

This is just a naive in-memory database that uses SQL-like syntax. This was code written by me in 2017.

## .tbl Files
In this project, we use `.tbl` files to store tables in memory. These are basically `.csv` files but with the first row giving the column names and types.
For example, `nums.tbl` with the following contents:

`x int, y int, z String`  
`0, 0, foo`  
`1, 2, bar`   
`9, 8, fizz`  
`5, 5, buzz`  

would be a loaded as a table with the name `nums` with three columns, `x`, `y`, and `z`, respectively, with types `int`, `int`, and `String`, respectively.

## Commands

Here are the commands that this project supports. In particular, we do not have any aggregation (group by, avg, etc.) functionality. 
Also note that all commands and types are case-sensitive.

### Create
First, we can create a brand new table with the following syntax:

`create table <table_name> (<col_name1> <col_type1>, <col_name2> <col_type2>, ...)`

If a table with the same name already exists in the database, then the command will not run.

We can also create a new table by selecting from existing tables:

`create table <table_name> as <select_statement>`

### Load
The load command will search for a file `<table_name>.tbl` in the same directory, and load it into memory to be ready for querying. If a table with the same name 
exists in memory, then the loaded table will replace it.

`load <table_name>`



