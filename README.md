# The Magi DataCenter Platform

[![Maven Central](https://img.shields.io/maven-central/v/org.nervousync/magi-bom?color=green&label=Release)](https://mvnrepository.com/artifact/org.nervousync/magi-bom)
![Maven Snapshot](https://img.shields.io/maven-metadata/v?label=Snapshot&metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Forg%2Fnervousync%2Fmagi-bom%2Fmaven-metadata.xml)
[![License](https://img.shields.io/github/license/wmkm0113/magi)](https://github.com/wmkm0113/magi/blob/mainline/LICENSE)
![Language](https://img.shields.io/badge/language-Java-green)
[![Twitter:wmkm0113](https://img.shields.io/twitter/follow/wmkm0113?label=Follow)](https://twitter.com/wmkm0113)

English
[简体中文](README_zh_CN.md)
[繁體中文](README_zh_TW.md)

The object relationship mapping system created for the data processing platform is completed based on the annotations provided by the Jakarta Persistence API to map the JavaBean and data tables, and the relationship between data tables, etc.
The system provides extended annotations to realize the encryption, storage and query functions of sensitive data, and the secondary cache functions of data. It also provides the query input and output format of the unified data platform, data import and export tools, lazy loading data columns, and other functions.
For data queries, users can query data across databases through a unified data query format. The system will automatically decompose the data query task based on the data table's association relationship, database and other information.
And integrate the query results to return the query results in a unified format.

## Table of contents
* [JDK Version](#JDK-Version)
* [End of Life](#End-of-Life)
* [Usage](#Usage)
  + [Add support to the project](#1-add-support-to-the-project)
  + [Initialize the data source](#2-Initialize-the-data-source)
  + [Register data table entity class](#3-Register-data-table-entity-class)
  + [Execute the operation](#4-Execute-the-operation)
* [Sensitive data support](#Sensitive-data-support)
* [Modify data source configuration with programming](#Modify-data-source-configuration-with-programming)
* [Data import and export](#data-import-and-export)
  + [Modify the entity class](#modify-the-entity-class)
  + [DataUtils initialize and usage](#datautils-initialize-and-usage)
* [Contributions and feedback](#contributions-and-feedback)
* [Donations](#donations)

## JDK Version
**Compile:** OpenJDK 11   
**Runtime:** OpenJDK 11+ or compatible version   
**Jakarta EE Platform:** 9

## End of Life
**Features Freeze:** 31, Dec, 2026   
**Secure Patch:** 31, Dec, 2029

## Usage
### 1. Add support to the project
**Maven:**   
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-nodeps-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
or using the target databases:
Only support MariaDB
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-mariadb-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support Mysql
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-mysql-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support Oracle
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-oracle-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support Postgres
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-pgsql-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support SQLite
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-sqlite-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support SQL Server
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-mssql-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support Apache Derby
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-derby-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support Apache Cassandra
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-cassandra-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
Only support MongoDB
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-mongodb-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```

**Gradle:**   
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-nodeps-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-nodeps-jdk11:${version}'
```
or using the target databases:
Only support MariaDB
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mariadb-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mariadb-jdk11:${version}'
```
Only support Mysql
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mysql-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mysql-jdk11:${version}'
```
Only support Oracle
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-oracle-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-oracle-jdk11:${version}'
```
Only support Postgres
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-pgsql-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-pgsql-jdk11:${version}'
```
Only support SQLite
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-sqlite-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-sqlite-jdk11:${version}'
```
Only support SQL Server
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mssql-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mssql-jdk11:${version}'
```
Only support Apache Derby
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-derby-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-derby-jdk11:${version}'
```
Only support Apache Cassandra
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-cassandra-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-cassandra-jdk11:${version}'
```
Only support MongoDB
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mongodb-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mongodb-jdk11:${version}'
```
**SBT:**   
```
libraryDependencies += "org.nervousync" % "magi-nodeps-jdk11" % "${version}" % "provided"
```
or using the target databases:
Only support MariaDB
```
libraryDependencies += "org.nervousync" % "magi-mariadb-jdk11" % "${version}" % "provided"
```
Only support Mysql
```
libraryDependencies += "org.nervousync" % "magi-mysql-jdk11" % "${version}" % "provided"
```
Only support Oracle
```
libraryDependencies += "org.nervousync" % "magi-oracle-jdk11" % "${version}" % "provided"
```
Only support Postgres
```
libraryDependencies += "org.nervousync" % "magi-pgsql-jdk11" % "${version}" % "provided"
```
Only support SQLite
```
libraryDependencies += "org.nervousync" % "magi-sqlite-jdk11" % "${version}" % "provided"
```
Only support SQL Server
```
libraryDependencies += "org.nervousync" % "magi-mssql-jdk11" % "${version}" % "provided"
```
Only support Apache Derby
```
libraryDependencies += "org.nervousync" % "magi-derby-jdk11" % "${version}" % "provided"
```
Only support Apache Cassandra
```
libraryDependencies += "org.nervousync" % "magi-cassandra-jdk11" % "${version}" % "provided"
```
Only support MongoDB
```
libraryDependencies += "org.nervousync" % "magi-mongodb-jdk11" % "${version}" % "provided"
```
**Ivy:**   
```
<dependency org="org.nervousync" name="magi-nodeps-jdk11" rev="${version}"/>
```
or using the target databases:
Only support MariaDB
```
<dependency org="org.nervousync" name="magi-mariadb-jdk11" rev="${version}"/>
```
Only support Mysql
```
<dependency org="org.nervousync" name="magi-mysql-jdk11" rev="${version}"/>
```
Only support Oracle
```
<dependency org="org.nervousync" name="magi-oracle-jdk11" rev="${version}"/>
```
Only support Postgres
```
<dependency org="org.nervousync" name="magi-pgsql-jdk11" rev="${version}"/>
```
Only support SQLite
```
<dependency org="org.nervousync" name="magi-sqlite-jdk11" rev="${version}"/>
```
Only support SQL Server
```
<dependency org="org.nervousync" name="magi-mssql-jdk11" rev="${version}"/>
```
Only support Apache Derby
```
<dependency org="org.nervousync" name="magi-derby-jdk11" rev="${version}"/>
```
Only support Apache Cassandra
```
<dependency org="org.nervousync" name="magi-cassandra-jdk11" rev="${version}"/>
```
Only support MongoDB
```
<dependency org="org.nervousync" name="magi-mongodb-jdk11" rev="${version}"/>
```

### 2. Initialize the data source
Read the data source configuration information as a parameter and execute the initialize static method of org.nervousync.magi.entity.EntityFactory to initialize the entity class factory.

**Read configuration information from the specified location: **

Use the stringToObject method of org.nervousync.utils.StringUtils to convert the configuration information string to the org.nervousync.brain.configs.BrainConfigure object.

Parameter information:

| Parameter name | Data type | Purpose |
|-------------|-------|--------------------------- ---|
| string | string | string that needs to be converted to JavaBean |
| encoding | string | string encoding set (default is UTF-8), this parameter can be omitted |
| beanClass | Class | JavaBean class definition |
| schemaPaths | String array | An array of XSD file paths used to verify XML strings, this parameter can be omitted |

Use the fileToObject method of org.nervousync.utils.StringUtils to convert the configuration information file to the org.nervousync.brain.configs.BrainConfigure object.

Parameter information:

| Parameter name | Data type | Purpose |
|-------------|-------|--------------------------- ---|
| filePath | String | File storage path that needs to be converted to JavaBean |
| beanClass | Class | JavaBean class definition |
| schemaPaths | String array | An array of XSD file paths used to verify XML strings, this parameter can be omitted |

**Use the Configuration Information Manager to read configuration information: **

First, confirm that org.nervousync.configs.ConfigureManager has been initialized, and call the getInstance method of ConfigureManager to obtain the configuration information manager instance object.
And call the readConfigure method of the configuration information manager instance object to read the configuration information.

### 3. Register data table entity class

Before performing operations on data tables, you need to register the data table entity class in the entity class factory.

**Manually register data table entity class: **
Get the instance object of the entity class factory through the getInstance method of EntityFactory, and call the registerTables method of the entity class factory to register the given data table entity class.

Parameter information:

| Parameter name | Data type   | Purpose                                                   |
|----------------|-------------|-----------------------------------------------------------|
| entityClasses  | Class array | Data table entity class array that needs to be registered |

**Automatically register data table entity class: **
Get the instance object of the entity class factory through the getInstance method of EntityFactory, and call the scanPackages method of the entity class factory to register the given data table entity class.

Parameter information:

| Parameter name | Data type   | Purpose                                                   |
|----------------|-------------|-----------------------------------------------------------|
| scanPackages   | String list | Package name list, which can be a regular expression list |

### 4. Execute the operation

Use the getInstance static method of EntityFactory to get the instance object of the entity class factory.

Parameter information:

| Parameter name | Data type | Purpose |
|-------------|------|---------------------------- --------|
| readOnly | Boolean | TRUE: read-only mode, FALSE: regular mode (default is FALSE) |
| restoreMode | Boolean | TRUE: Data recovery mode, FALSE: Normal mode (default is FALSE) |

**Clear the data table: **

Use the truncateTables method of the entity class factory instance object to delete the data table. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name |  Data type  |                           Notes                           |
|:--------------:|:-----------:|:---------------------------------------------------------:|
| entityClasses  | Class array | Data table entity class array that needs to be registered |

**Delete data table**

Use the dropTables method of the entity class factory instance object to delete the data table. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name |  Data type  |                           Notes                           |
|:--------------:|:-----------:|:---------------------------------------------------------:|
| entityClasses  | Class array | Data table entity class array that needs to be registered |


**Perform data insertion operation: **
Use the saveRecord method of the entity class factory instance object to perform the data insertion operation. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type                             | Purpose                                      |
|----------------|---------------------------------------|----------------------------------------------|
| object         | org.nervousync.magi.entity.BaseObject | Data table entity class object to be written |

**Perform data update operation: **
Use the updateRecord method of the entity class factory instance object to perform data update operations. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type                             | Purpose                                      |
|----------------|---------------------------------------|----------------------------------------------|
| object         | org.nervousync.magi.entity.BaseObject | Data table entity class object to be updated |

**Perform data deletion operation: **
Use the deleteRecord method of the entity class factory instance object to perform the data deletion operation. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type                             | Purpose                                  |
|----------------|---------------------------------------|------------------------------------------|
| object         | org.nervousync.magi.entity.BaseObject | Data table entity class object to delete |

**Perform a unique data retrieval operation: **
Use the retrieveRecord method of the entity class factory instance object to perform a data unique retrieval operation. If an error occurs during execution, all exceptions will be thrown.

Data unique search by the primary key

Parameter information:

| Parameter name | Data type            | Purpose                        |
|----------------|----------------------|--------------------------------|
| primaryKey     | java.io.Serializable | primary key object             |
| entityClass    | java.lang.Class      | Data table entity class        |
| forUpdate      | Boolean              | Search results for data update |

Data unique search by query conditions

Parameter information:

| Parameter name | Data type       | Purpose                        |
|----------------|-----------------|--------------------------------|
| entityClass    | java.lang.Class | Data table entity class        |
| filterMap      | java.util.Map   | Filter Map                     |
| forUpdate      | Boolean         | Search results for data update |

**Perform data query operation: **
Use the query method of the entity class factory instance object to perform data query operations, and if an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type                            | Purpose                           |
|----------------|--------------------------------------|-----------------------------------|
| targetClass    | java.lang.Class                      | Query result entity class         |
| queryInfo      | org.nervousync.brain.query.QueryInfo | Query information instance object |

Use the queryForUpdate method of the entity class factory instance object to perform data query operations. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type       | Purpose                   |
|----------------|-----------------|---------------------------|
| targetClass    | java.lang.Class | Query result entity class |
| filterMap      | java.util.Map   | Filter Map                |

Use the queryTotal method of the entity class factory instance object to get the total number of data query results. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type                            | Purpose                           |
|----------------|--------------------------------------|-----------------------------------|
| queryInfo      | org.nervousync.brain.query.QueryInfo | Query information instance object |

Use the queryTotal method of the entity class factory instance object to get the total number of data query results. If an error occurs during execution, all exceptions will be thrown.

Parameter information:

| Parameter name | Data type       | Purpose                   |
|----------------|-----------------|---------------------------|
| targetClass    | java.lang.Class | Query result entity class |
| filterMap      | java.util.Map   | Filter Map                |

## Sensitive data support
During the data operation process, you will more or less encounter the processing of sensitive data, 
including but not limited to identification codes, phone numbers, email addresses, bank card numbers,
etc. The toolkit provides simple annotations for sensitive data automatic processing.   
Add the org.nervousync.database.annotations.data.Sensitive annotation on the sensitive data field
that needs to be processed.   
The annotated encField parameter is used to specify the encrypted data storage column, and the parameter secureName is used to specify the security configuration name used for encryption.
The configuration information is as follows:

|      type       |             data type              |     enc result      |
|:---------------:|:----------------------------------:|:-------------------:|
|     NORMAL      | Username/Address/Identify code etc |   w(Hidden info)3   |
| CHN_Social_Code |  Social Credit Code(CHN Mainland)  | 91110(Hidden info)X |
|   CHN_ID_Code   |    Identify Code(CHN Mainland)     |  110(Hidden info)X  |
|     E_MAIL      |           E-Mail address           |   w(Hidden info)m   |
|  PHONE_NUMBER   |            Phone number            |  139(Hidden info)1  |
|      Luhn       |          Bank card number          | 62(Hidden info)8888 |

Please execute the method named "desensitization"
before saving the entity instance to the database, toolkit will authenticate and encrypt the sensitive data content.
Notice that if the parameter "type" values are NOT "NORMAL"
toolkit will ignore the encrypting if the sensitive data content not matched the rule of the data type.

## Modify data source configuration with programming

Program developers can configure data sources by using the org.nervousync.brain.configs.builder.BrainConfigureBuilder class,
Includes creating or modifying a given configuration information instance object.

## Data import and export
Toolkit will convert between the Excel file and entity instance automatically
if developers add some annotations to the entity class.
### Modify the entity class
**1.Add Sheet annotation:**   
Add annotation org.nervousync.database.annotations.data.ExcelSheet to the entity class,
the parameter "value" is the sheet name of the entity class.   
**2.Add data column annotation:**   
Add annotation org.nervousync.database.annotations.data.ExcelColumn to the column field,
parameter "value" is the index value of the Excel column to the current field. 

### DataUtils initialize and usage
**1.DataUtils initialize:**   
Call the initialize static method of org.nervousync.database.commons.DataUtils to initialize the data utilities. 
The parameter named "basePath" is the default work folder of the data utilities,
all import/export temporary files will be saved to the default work folder.
+ Parameter "providerName" is the identification code of the task provider implements class, this provider will use to save the task information, if the identification code is empty string or not found, the task information will save it to memory, and the task information will be lost when the utilities were shutdown.
+ Parameter "threadLimit" is the thread limit count of the running threads.
+ Parameter "expireTime" is the expiry time of the finished task clear.

**2.Add task to import data:**   
Call the addTask method of org.nervousync.database.commons.DataUtils to add a data import task.
+ Parameter "inputStream" is the input stream instance of the Excel file which file will import into the database.
+ Parameter "userCode" is the identification code of the operator, operators only can query the task information matched the same identification code.
+ Parameter "transactional" is the flag value of using transactional to import the data, parameter value type: boolean.
+ Parameter "timeout" is the time-out value of transactional.

**3.Add task to export data:**   
Call the addTask method of org.nervousync.database.commons.DataUtils to add a data export task.
+ Parameter "userCode" is the identification code of the operator, operators only can query the task information matched the same identification code.
+ Parameter "queryInfos" is the query information arrays of the export data, one task can export multiple data tables to the same Excel file.

**4.Update configure information:**   
Call the config method of org.nervousync.database.commons.DataUtils to update configure information.
+ Parameter "threadLimit" is the thread limit count of the running threads.
+ Parameter "expireTime" is the expiry time of the finished task clear.

## Contributions and feedback
Friends are welcome to translate the prompt information, error messages, 
etc. in this document and project into more languages to help more users better understand and use this toolkit.   
If you find problems during use or need to improve or add related functions, please submit an issue to this project
or send email to [wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=bugs_and_features)   
For better communication, please include the following information when submitting an issue or sending an email:
1. The purpose is: discover bugs/function improvements/add new features   
2. Please paste the following information (if it exists): incoming data, expected results, error stack information   
3. Where do you think there may be a problem with the code (if provided, it can help us find and solve the problem as soon as possible)

If you are submitting information about adding new features, please ensure that the features to be added are general needs, that is, the new features can help most users.

If you need to add customized special requirements, I will charge a certain custom development fee.
The specific fee amount will be assessed based on the workload of the customized special requirements.   
For customized special features, please send an email directly to [wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=payment_features). At the same time, please try to indicate the budget amount of development cost you can afford in the email.

## Donations
To support this project, you can make a donation to:

- Bitcoin address: bc1q3nfj9gafu3x25ea260g7cyhh5s9gnx347tznsf
- Ethereum address: 0x849D143e943bAA6Dd078d02ebAEc205E2b00a7CA
- Solana address: 4Fvujk8DEkVAtYwzim1vrobNm4s72Ra6Xrsu83v2hqE2
- BNB address: 0x849D143e943bAA6Dd078d02ebAEc205E2b00a7CA