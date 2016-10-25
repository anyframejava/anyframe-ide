<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE hibernate-reverse-engineering
SYSTEM "http://hibernate.sourceforge.net/hibernate-reverse-engineering-3.0.dtd" >
<hibernate-reverse-engineering>
<!--schema-selection here-->
<!--schema-selection-START-->
    <schema-selection match-schema="${schema}" />
<!--schema-selection-END-->
    <type-mapping>
<!--jdbc-type is name from java.sql.Types-->
<!--length, scale and precision can be used to specify the mapping precisly-->
        <sql-type jdbc-type="NUMERIC" precision='1' hibernate-type="boolean" />
<!--the type-mappings are ordered. This mapping will be consulted last,
        thus overriden by the previous one if precision=1 for the column-->
        <sql-type jdbc-type="SMALLINT" hibernate-type="java.lang.Short" />
        <sql-type jdbc-type="BIGINT" hibernate-type="java.lang.Long" />
        <sql-type jdbc-type="INTEGER" hibernate-type="java.lang.Integer" />
        <sql-type jdbc-type="NUMERIC" scale="0" hibernate-type="java.lang.Long" />
        <sql-type jdbc-type="NUMERIC" hibernate-type="java.lang.Float" />
        <sql-type jdbc-type="DECIMAL" scale="0" hibernate-type="java.lang.Long" />
        <sql-type jdbc-type="DECIMAL" hibernate-type="java.lang.Float" />
        <sql-type jdbc-type="VARCHAR" hibernate-type="string" />
        <sql-type jdbc-type="CHAR" hibernate-type="string" />
        <sql-type jdbc-type="BLOB" hibernate-type="byte[]" />
        <sql-type jdbc-type="CLOB" hibernate-type="string" />
        <sql-type jdbc-type="TIME" hibernate-type="time" />
        <sql-type jdbc-type="OTHER" hibernate-type="timestamp" />
    </type-mapping>
<!--BIN$ is recycle bin tables in Oracle 
    <table-filter match-name="BIN$.*" exclude="true"/>-->
<!--Add table names to generate domain classes-->
</hibernate-reverse-engineering>
