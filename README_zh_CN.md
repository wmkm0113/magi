# 三圣贤数据中心平台

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.nervousync/magi-jdk11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.nervousync/magi-jdk11/)
[![License](https://img.shields.io/github/license/wmkm0113/magi-jdk11.svg)](https://github.com/wmkm0113/magi-jdk11/blob/master/LICENSE)
![Language](https://img.shields.io/badge/language-Java-green)
[![Twitter:wmkm0113](https://img.shields.io/twitter/follow/wmkm0113?label=Follow)](https://twitter.com/wmkm0113)

[English](README.md)
简体中文
[繁體中文](README_zh_TW.md)

为数据处理平台打造的对象关系映射系统，依据Jakarta Persistence API提供的注解，完成JavaBean和数据表的映射关系、数据表间的关联关系等，
系统提供了扩展注解来实现敏感数据的加密保存和查询功能、数据的二级缓存功能，同时提供统一数据平台的查询输入输出格式、数据导入导出工具、懒加载数据列等功能，
对于数据查询，使用者可以通过统一的数据查询格式进行跨数据库的数据查询，系统会自动根据数据表的关联关系、所在数据库等信息，进行数据查询任务的分解，
并对查询结果进行整合，返回统一格式的查询结果。

## 目录
* [JDK版本](#JDK版本)
* [生命周期](#生命周期)
* [使用方法](#使用方法)
  + [在项目中添加支持](#1在项目中添加支持)
  + [初始化数据源](#2初始化数据源)
  + [注册数据表实体类](#3注册数据表实体类)
  + [执行增删改查操作](#4执行增删改查操作)
* [敏感数据的处理](#敏感数据的处理)
* [自定义查询优化器](#自定义查询优化器)
* [使用代码进行数据源的配置](#使用代码进行数据源的配置)
* [数据导入导出](#数据导入导出)
  + [数据表实体类的修改](#数据表实体类的修改)
  + [导入导出工具的初始化和使用](#导入导出工具的初始化和使用)
* [贡献与反馈](#贡献与反馈)
* [赞助与鸣谢](#赞助与鸣谢)

## JDK版本：
编译：OpenJDK 11   
运行：OpenJDK 11+ 或兼容版本

## 生命周期：
**功能冻结：** 2026年12月31日   
**安全更新：** 2029年12月31日

## 使用方法：
### 1、在项目中添加支持
**Maven:**   
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-nodeps-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
仅使用目标数据库：
仅支持JDBC数据库（不包含Apache Derby）
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-jdbc-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
仅支持Apache Derby
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-derby-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
仅支持Apache Cassandra
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-cassandra-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
仅支持MongoDB
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
仅使用目标数据库：
仅支持JDBC数据库（不包含Apache Derby）
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-jdbc-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-jdbc-jdk11:${version}'
```
仅支持Apache Derby
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-derby-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-derby-jdk11:${version}'
```
仅支持Apache Cassandra
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-cassandra-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-cassandra-jdk11:${version}'
```
仅支持MongoDB
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mongodb-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mongodb-jdk11:${version}'
```
**SBT:**   
```
libraryDependencies += "org.nervousync" % "magi-nodeps-jdk11" % "${version}" % "provided"
```
仅使用目标数据库：
仅支持JDBC数据库（不包含Apache Derby）
```
libraryDependencies += "org.nervousync" % "magi-jdbc-jdk11" % "${version}" % "provided"
```
仅支持Apache Derby
```
libraryDependencies += "org.nervousync" % "magi-derby-jdk11" % "${version}" % "provided"
```
仅支持Apache Cassandra
```
libraryDependencies += "org.nervousync" % "magi-cassandra-jdk11" % "${version}" % "provided"
```
仅支持MongoDB
```
libraryDependencies += "org.nervousync" % "magi-mongodb-jdk11" % "${version}" % "provided"
```
**Ivy:**   
```
<dependency org="org.nervousync" name="magi-nodeps-jdk11" rev="${version}"/>
```
仅使用目标数据库：
仅支持JDBC数据库（不包含Apache Derby）
```
<dependency org="org.nervousync" name="magi-jdbc-jdk11" rev="${version}"/>
```
仅支持Apache Derby
```
<dependency org="org.nervousync" name="magi-derby-jdk11" rev="${version}"/>
```
仅支持Apache Cassandra
```
<dependency org="org.nervousync" name="magi-cassandra-jdk11" rev="${version}"/>
```
仅支持MongoDB
```
<dependency org="org.nervousync" name="magi-mongodb-jdk11" rev="${version}"/>
```

### 2、初始化数据源
读取数据源配置信息作为参数，执行 org.nervousync.magi.entity.EntityFactory 的 initialize 静态方法来初始化实体类工厂。

**从指定位置读取配置信息：**

使用 org.nervousync.utils.StringUtils 的 stringToObject 方法转换配置信息字符串为 org.nervousync.brain.configs.BrainConfigure 对象。

参数信息：

| 参数名         | 数据类型  | 用途                           |
|-------------|-------|------------------------------|
| string      | 字符串   | 需要转换为JavaBean的字符串            |
| encoding    | 字符串   | 字符串的编码集（默认为UTF-8），此参数可以省略    |
| beanClass   | Class | JavaBean的类定义                 |
| schemaPaths | 字符串数组 | 用于验证XML字符串的XSD文件路径数组，此参数可以省略 |

使用 org.nervousync.utils.StringUtils 的 fileToObject 方法转换配置信息文件为 org.nervousync.brain.configs.BrainConfigure 对象。

参数信息：

| 参数名         | 数据类型  | 用途                           |
|-------------|-------|------------------------------|
| filePath    | 字符串   | 需要转换为JavaBean的文件存储路径         |
| beanClass   | Class | JavaBean的类定义                 |
| schemaPaths | 字符串数组 | 用于验证XML字符串的XSD文件路径数组，此参数可以省略 |

**使用配置信息管理器读取配置信息：**

首先确认 org.nervousync.configs.ConfigureManager 已初始化，调用 ConfigureManager 的 getInstance 方法获取配置信息管理器实例对象，
并调用配置信息管理器实例对象的 readConfigure 方法读取配置信息。

### 3、注册数据表实体类
在对数据表执行操作前，需要在实体类工厂中注册数据表实体类。

**手动注册数据表实体类：**
通过 EntityFactory 的 getInstance 方法获取实体类工厂的实例对象，调用实体类工厂的 registerTables 方法注册给定的数据表实体类。

参数信息：

| 参数名           | 数据类型    | 用途            |
|---------------|---------|---------------|
| entityClasses | Class数组 | 需要注册的数据表实体类数组 |

**自动注册数据表实体类：**
通过 EntityFactory 的 getInstance 方法获取实体类工厂的实例对象，调用实体类工厂的 scanPackages 方法注册给定的数据表实体类。

参数信息：

| 参数名          | 数据类型  | 用途              |
|--------------|-------|-----------------|
| scanPackages | 字符串列表 | 包名列表，可以是正则表达式列表 |

### 4、执行增删改查操作
使用 EntityFactory 的 getInstance 静态方法获取实体类工厂的实例对象。

参数信息：

| 参数名         | 数据类型 | 用途                                |
|-------------|------|-----------------------------------|
| readOnly    | 布尔值  | TRUE:只读模式，FALSE:常规模式（默认值为FALSE）   |
| restoreMode | 布尔值  | TRUE:数据恢复模式，FALSE:常规模式（默认值为FALSE） |

**清空数据表：**

使用实体类工厂实例对象的的 truncateTables 的方法来删除数据表，执行过程中出错则抛出所有异常。

参数信息：

|      参数名      |  数据类型   |     Notes     |
|:-------------:|:-------:|:-------------:|
| entityClasses | Class数组 | 需要注册的数据表实体类数组 |

**删除数据表**

使用实体类工厂实例对象的的 dropTables 的方法来删除数据表，执行过程中出错则抛出所有异常。

参数信息：

|      参数名      |  数据类型   |      备注       |
|:-------------:|:-------:|:-------------:|
| entityClasses | Class数组 | 需要注册的数据表实体类数组 |


**执行数据插入操作：**
使用实体类工厂实例对象的 saveRecord 方法执行数据插入操作，执行过程中出错则抛出所有异常。

参数信息：

| 参数名    | 数据类型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要写入的数据表实体类对象 |

**执行数据更新操作：**
使用实体类工厂实例对象的 updateRecord 方法执行数据更新操作，执行过程中出错则抛出所有异常。

参数信息：

| 参数名    | 数据类型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要更新的数据表实体类对象 |

**执行数据删除操作：**
使用实体类工厂实例对象的 deleteRecord 方法执行数据删除操作，执行过程中出错则抛出所有异常。

参数信息：

| 参数名    | 数据类型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要删除的数据表实体类对象 |

**执行数据唯一检索操作：**
使用实体类工厂实例对象的 retrieveRecord 方法执行数据唯一检索操作，执行过程中出错则抛出所有异常。

通过主键进行数据唯一检索

参数信息：

| 参数名         | 数据类型                 | 用途         |
|-------------|----------------------|------------|
| primaryKey  | java.io.Serializable | 主键对象       |
| entityClass | java.lang.Class      | 数据表实体类     |
| forUpdate   | 布尔值                  | 检索结果用于数据更新 |

通过查询条件进行数据唯一检索

参数信息：

| 参数名         | 数据类型            | 用途         |
|-------------|-----------------|------------|
| entityClass | java.lang.Class | 数据表实体类     |
| filterMap   | java.util.Map   | 筛选条件映射表    |
| forUpdate   | 布尔值             | 检索结果用于数据更新 |

**执行数据查询操作：**
使用实体类工厂实例对象的 query 方法执行数据查询操作，执行过程中出错则抛出所有异常。

参数信息：

| 参数名         | 数据类型                                 | 用途       |
|-------------|--------------------------------------|----------|
| targetClass | java.lang.Class                      | 查询结果实体类  |
| queryInfo   | org.nervousync.brain.query.QueryInfo | 查询信息实例对象 |

使用实体类工厂实例对象的 queryForUpdate 方法执行数据查询操作，执行过程中出错则抛出所有异常。

参数信息：

| 参数名         | 数据类型            | 用途      |
|-------------|-----------------|---------|
| targetClass | java.lang.Class | 查询结果实体类 |
| filterMap   | java.util.Map   | 筛选条件映射表 |

使用实体类工厂实例对象的 queryTotal 方法获取数据查询结果总数，执行过程中出错则抛出所有异常。

参数信息：

| 参数名       | 数据类型                                 | 用途       |
|-----------|--------------------------------------|----------|
| queryInfo | org.nervousync.brain.query.QueryInfo | 查询信息实例对象 |

使用实体类工厂实例对象的 queryTotal 方法获取数据查询结果总数，执行过程中出错则抛出所有异常。

参数信息：

| 参数名         | 数据类型            | 用途      |
|-------------|-----------------|---------|
| targetClass | java.lang.Class | 查询结果实体类 |
| filterMap   | java.util.Map   | 筛选条件映射表 |

## 敏感数据的处理
在数据操作过程中，或多或少都会遇到敏感数据的处理，包括但不限于身份识别代码、电话号码、电子邮箱地址、银行卡号等，工具包中提供了简单的注解用于对敏感数据的自动处理。   
在需要处理的敏感数据属性上添加 org.nervousync.magi.annotations.data.Sensitive 注解。   
注解的 encField 参数用于指定加密后的数据存储列，参数 secureName 用于指定加密使用的安全配置名称。   
配置信息如下：

|     type参数      |     数据类型     |    加密结果样例    |
|:---------------:|:------------:|:------------:|
|     NORMAL      |  用户名、地址信息等   |   w（隐藏信息）3   |
| CHN_Social_Code | 中国大陆统一信用识别代码 | 91110（隐藏信息）X |
|   CHN_ID_Code   |  中国大陆身份证号码   |  110（隐藏信息）X  |
|     E_MAIL      |    电子邮件地址    |   w（隐藏信息）m   |
|  PHONE_NUMBER   |     电话号码     |  139（隐藏信息）1  |
|      Luhn       |    银行卡号码     | 62（隐藏信息）8888 |

## 自定义查询优化器
**1、添加查询优化器实现类：***
新增查询优化器实现类，并实现 org.nervousync.magi.query.optimizer.QueryOptimizer 接口，
在查询优化器实现类上添加 org.nervousync.annotations.provider.Provider 注解，定义好注解的 name 属性。
并将实现类写入/META-INF/services/org.nervousync.magi.query.optimizer.QueryOptimizer文件，
系统通过Java的SPI进行查询优化器实现类的加载。

**2、使用自定义的查询优化器实现类：**
在初始化实体类工厂时，显示指定查询优化器，通过调用 org.nervousync.magi.entity.EntityFactory 的 initialize 静态方法来初始化实体类工厂。

参数信息：

| 参数名           | 数据类型                                        | 用途                          |
|---------------|---------------------------------------------|-----------------------------|
| configure     | org.nervousync.brain.configs.BrainConfigure | 数据源配置信息实例对象                 |
| optimizerName | 字符串                                         | 查询优化器 Provider 注解的 name 属性值 |

## 使用代码进行数据源的配置

程序开发人员可以通过使用 org.nervousync.brain.configs.builder.BrainConfigureBuilder 类进行数据源的配置，
包括新建或修改给定的配置信息实例对象。

## 数据导入导出
在数据表实体类中添加配置注解，可以让工具包自动添加数据表和Excel文件的相互转化工具。
### 数据表实体类的修改
**1、添加工作表注解：**   
在数据表实体类上添加 org.nervousync.magi.annotations.data.ExcelSheet 注解，参数 value 为工作表的名称。   
**2、添加数据列注解：**   
在数据列属性上添加 org.nervousync.magi.annotations.data.ExcelColumn 注解。参数 value 为对应Excel数据列的索引值，起始值为：0

### 导入导出工具的初始化和使用
**1、数据导入任务的添加：**   
调用 org.nervousync.brain.commons.DataUtils 的 addTask 方法添加数据导入任务。   
+ 参数 inputStream 为需要导入的Excel文件输入流实例对象。   
+ 参数 userCode 为添加任务的操作员识别代码，每个操作员仅可以查询到自己的任务信息。   
+ 参数 transactional 为是否使用事务模式进行数据导入的状态值，数据类型为：boolean。   
+ 参数 timeout 为事务模式下的超时时间。

**2、数据导出任务的添加：**   
调用 org.nervousync.brain.commons.DataUtils 的 addTask 方法添加数据导出任务。   
+ 参数 userCode 为添加任务的操作员识别代码，每个操作员仅可以查询到自己的任务信息。   
+ 参数 queryInfos 为导出数据的查询信息数组，可以在一个导出任务中导出多个数据表的数据，并将这些数据保存在同一个Excel文件中。   

**3、工具的配置信息更新：**   
调用 org.nervousync.brain.commons.DataUtils 的 config 方法更新配置信息。   
+ 参数 threadLimit 为允许同时执行的任务线程数。   
+ 参数 expireTime 为已完成的任务在此时间以后，会被删除掉任务信息。

## 贡献与反馈
欢迎各位朋友将此文档及项目中的提示信息、错误信息等翻译为更多语言，以帮助更多的使用者更好地了解与使用此工具包。   
如果在使用过程中发现问题或需要改进、添加相关功能，请提交issue到本项目或发送电子邮件到[wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=bugs_and_features)   
为了更好地沟通，请在提交issue或发送电子邮件时，写明如下信息：   
1、目的是：发现Bug/功能改进/添加新功能   
2、请粘贴以下信息（如果存在）：传入数据，预期结果，错误堆栈信息   
3、您认为可能是哪里的代码出现问题（如提供可以帮助我们尽快地找到并解决问题）   
如果您提交的是添加新功能的相关信息，请确保需要添加的功能是一般性的通用需求，即添加的新功能可以帮助到大多数使用者。

如果您需要添加的是定制化的特殊需求，我将收取一定的定制开发费用，具体费用金额根据定制化的特殊需求的工作量进行评估。   
定制化特殊需求请直接发送电子邮件到[wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=payment_features)，同时请尽量在邮件中写明您可以负担的开发费用预算金额。

## 赞助与鸣谢
<span id="JetBrains">
    <img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg" width="100px" alt="JetBrains Logo (Main) logo.">
    <span>非常感谢 <a href="https://www.jetbrains.com/">JetBrains</a> 通过许可证赞助我们的开源项目。</span>
</span>