# Naive-Database

This is just a naive in-memory database that uses SQL-like syntax. This was code written by me in 2017.

To start a database, run

`java Main` 

in the terminal. To exit, simply type

`exit`.



## .tbl Files
In this project, we use `.tbl` files to store tables in memory. These are basically `.csv` files but with the first row giving the column names and types.
For example, `nums.tbl` with the following contents:

`x int, y int, z String`  
`0, 0, foo`  
`1, 2, bar`   
`9, 8, fizz`  
`5, 5, buzz`  

would be loaded as a table with the name `nums` with three columns, `x`, `y`, and `z`, respectively, with types `int`, `int`, and `String`, respectively.

## Commands

Here are the commands that this project supports. In particular, we do not have any aggregation (group by, avg, etc.) functionality. 
Also note that all commands and types are case-sensitive.

### Create
First, we can create a brand new table with the following syntax:

`create table <table_name> (<col_name_1> <col_type_1>, ..., <col_name_p>, <col_type_p>)`

If a table with the same name already exists in the database, then the command will not run.

We can also create a new table by selecting from existing tables:

`create table <table_name> as <select_statement>`

### Load
The load command will search for a file `<table_name>.tbl` in the same directory, and load it into memory to be ready for querying. If a table with the same name 
exists in memory, then the loaded table will replace it.

`load <table_name>`

### Store
The store command stores the table as a `.tbl` file in the same directory, to be used in a later session. If the table name exists already, it will be overwritten.

`store <table_name>`

### Drop
Classic drop table command. It will delete the given table from the database.

`drop table <table_name>`

### Print
Prints a string representation of the given table.

`print <table_name>`

### Insert
Inserts row data into the given table. The list of literals must match the number of attributes of the table and must match the order of the attribute types of the table.

`insert into <table_name> values <data_1>, ..., <data_p>`

### Select
Typical SQL select statement. Projects onto the desired columns, and selects only the rows that satisfy the given conditions. Note that we allow column expressions that are at most binary arithmetic operations on columns. Where conditions only handle equality and comparisons, i.e. not BETWEEN. Furthermore, note that joins are implemented by only selecting from multiple tables, i.e. there is no JOIN command. By default, this join will simply perform an inner join, where the join condition must be handled in the following where clauses.

`select <col_expr_1>,..., <col_expr_m> from <table_1>,..., <table_t> where <cond_1> and ... and <cond_c>`
<<<<<<< HEAD


Note that there are no semicolons in this syntax!































=======
>>>>>>> f9577d2b3b53a35456b8c5d1b91421971f987bce
