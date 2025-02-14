# 三聖賢數據中心平台

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.nervousync/magi-jdk11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.nervousync/magi-jdk11/)
[![License](https://img.shields.io/github/license/wmkm0113/magi-jdk11.svg)](https://github.com/wmkm0113/magi-jdk11/blob/master/LICENSE)
![Language](https://img.shields.io/badge/language-Java-green)
[![Twitter:wmkm0113](https://img.shields.io/twitter/follow/wmkm0113?label=Follow)](https://twitter.com/wmkm0113)

[English](README.md)
[简体中文](README_zh_CN.md)
繁體中文

為資料處理平臺打造的介面工具包，提供統一資料平臺的查詢輸入輸出格式、資料導入匯出工具、懶載入資料列或關聯資料等功能，同時還可以根據注解自動對敏感性資料進行加密/解密、查詢準則拆分等操作，

## 目錄* [JDK版本](#JDK版本)
* [生命週期](#生命週期)
* [使用方法](#使用方法)
  + [在項目中添加支持](#1在專案中添加支持)
  + [初始化數據源](#2初始化數據源)
  + [註冊數據表實體類](#3註冊數據表實體類)
  + [執行增刪改查操作](#4執行增刪改查操作)
* [敏感數據的處理](#敏感性資料的處理)
* [自定義查詢優化器](#自定義查詢優化器)
* [使用代碼進行數據源的配置](#使用代碼進行數據源的配置)
* [數據導入導出](#數據導入導出)
  + [資料表實體類的修改](#資料表實體類的修改)
  + [導入導出工具的初始化和使用](#導入匯出工具的初始化和使用)
* [貢獻與反饋](#貢獻與反饋)
* [贊助與鳴謝](#贊助與鳴謝)

## JDK版本：
編譯：OpenJDK 11   
運行：OpenJDK 11+ 或相容版本

## 生命週期：
**功能凍結：** 2026年12月31日   
**安全更新：** 2029年12月31日

## 使用方法：
### 1、在專案中添加支持
**Maven:**   
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-nodeps-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
僅使用目標數據庫：
僅支持JDBC數據庫（不包含Apache Derby）
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-jdbc-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
僅支持Apache Derby
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-derby-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
僅支持Apache Cassandra
```
<dependency>
    <groupId>org.nervousync</groupId>
	<artifactId>magi-cassandra-jdk11</artifactId>
    <version>${version}</version>
</dependency>
```
僅支持MongoDB
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
僅使用目標數據庫：
僅支持JDBC數據庫（不包含Apache Derby）
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-jdbc-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-jdbc-jdk11:${version}'
```
僅支持Apache Derby
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-derby-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-derby-jdk11:${version}'
```
僅支持Apache Cassandra
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-cassandra-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-cassandra-jdk11:${version}'
```
僅支持MongoDB
```
Manual: compileOnly group: 'org.nervousync', name: 'magi-mongodb-jdk11', version: '${version}'
Short: compileOnly 'org.nervousync:magi-mongodb-jdk11:${version}'
```
**SBT:**   
```
libraryDependencies += "org.nervousync" % "magi-nodeps-jdk11" % "${version}" % "provided"
```
僅使用目標數據庫：
僅支持JDBC數據庫（不包含Apache Derby）
```
libraryDependencies += "org.nervousync" % "magi-jdbc-jdk11" % "${version}" % "provided"
```
僅支持Apache Derby
```
libraryDependencies += "org.nervousync" % "magi-derby-jdk11" % "${version}" % "provided"
```
僅支持Apache Cassandra
```
libraryDependencies += "org.nervousync" % "magi-cassandra-jdk11" % "${version}" % "provided"
```
僅支持MongoDB
```
libraryDependencies += "org.nervousync" % "magi-mongodb-jdk11" % "${version}" % "provided"
```
**Ivy:**   
```
<dependency org="org.nervousync" name="magi-nodeps-jdk11" rev="${version}"/>
```
僅使用目標數據庫：
僅支持JDBC數據庫（不包含Apache Derby）
```
<dependency org="org.nervousync" name="magi-jdbc-jdk11" rev="${version}"/>
```
僅支持Apache Derby
```
<dependency org="org.nervousync" name="magi-derby-jdk11" rev="${version}"/>
```
僅支持Apache Cassandra
```
<dependency org="org.nervousync" name="magi-cassandra-jdk11" rev="${version}"/>
```
僅支持MongoDB
```
<dependency org="org.nervousync" name="magi-mongodb-jdk11" rev="${version}"/>
```
### 2、初始化數據源
讀取數據源配置信息作為參數，執行 org.nervousync.magi.entity.EntityFactory 的 initialize 靜態方法來初始化實體類工廠。

**從指定位置讀取配置信息：**

使用 org.nervousync.utils.StringUtils 的 stringToObject 方法轉換配置信息字符串為 org.nervousync.brain.configs.BrainConfigure 對象。

參數信息：

| 參數名         | 數據類型  | 用途                           |
|-------------|-------|--------------------------- ---|
| string      | 字符串   | 需要轉換為JavaBean的字符串            |
| encoding    | 字符串   | 字符串的編碼集（默認為UTF-8），此參數可以省略    |
| beanClass   | Class | JavaBean的類定義                 |
| schemaPaths | 字符串數組 | 用於驗證XML字符串的XSD文件路徑數組，此參數可以省略 |

使用 org.nervousync.utils.StringUtils 的 fileToObject 方法轉換配置信息文件為 org.nervousync.brain.configs.BrainConfigure 對象。

參數信息：

| 參數名         | 數據類型  | 用途                           |
|-------------|-------|--------------------------- ---|
| filePath    | 字符串   | 需要轉換為JavaBean的文件存儲路徑         |
| beanClass   | Class | JavaBean的類定義                 |
| schemaPaths | 字符串數組 | 用於驗證XML字符串的XSD文件路徑數組，此參數可以省略 |

**使用配置信息管理器讀取配置信息：**

首先確認 org.nervousync.configs.ConfigureManager 已初始化，調用 ConfigureManager 的 getInstance 方法獲取配置信息管理器實例對象，
並調用配置信息管理器實例對象的 readConfigure 方法讀取配置信息。
### 3、註冊數據表實體類
在對數據表執行操作前，需要在實體類工廠中註冊數據表實體類。

**手動註冊數據表實體類：**
通過 EntityFactory 的 getInstance 方法獲取實體類工廠的實例對象，調用實體類工廠的 registerTables 方法註冊給定的數據表實體類。

參數信息：

| 參數名           | 數據類型    | 用途            |
|---------------|---------|---------------|
| entityClasses | Class數組 | 需要註冊的數據表實體類數組 |

**自動註冊數據表實體類：**
通過 EntityFactory 的 getInstance 方法獲取實體類工廠的實例對象，調用實體類工廠的 scanPackages 方法註冊給定的數據表實體類。

參數信息：

| 參數名          | 數據類型  | 用途              |
|--------------|-------|-----------------|
| scanPackages | 字符串列表 | 包名列表，可以是正則表達式列表 |
### 4、執行增刪改查操作
使用 EntityFactory 的 getInstance 靜態方法獲取實體類工廠的實例對象。

參數信息：

| 參數名         | 數據類型 | 用途                                |
|-------------|------|---------------------------- -------|
| readOnly    | 布爾值  | TRUE:只讀模式，FALSE:常規模式（默認值為FALSE）   |
| restoreMode | 布爾值  | TRUE:數據恢復模式，FALSE:常規模式（默認值為FALSE） |

**清空數據表：**

使用實體類工廠實例對象的的 truncateTables 的方法來刪除數據表，執行過程中出錯則拋出所有異常。

參數信息：

|      參數名      |  數據類型   |     Notes     |
|:-------------:|:-------:|:-------------:|
| entityClasses | Class數組 | 需要註冊的數據表實體類數組 |

**刪除數據表**

使用實體類工廠實例對象的的 dropTables 的方法來刪除數據表，執行過程中出錯則拋出所有異常。

參數信息：

|      參數名      |  數據類型   |      備註       |
|:-------------:|:-------:|:-------------:|
| entityClasses | Class數組 | 需要註冊的數據表實體類數組 |


**執行數據插入操作：**
使用實體類工廠實例對象的 saveRecord 方法執行數據插入操作，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名    | 數據類型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要寫入的數據表實體類對象 |

**執行數據更新操作：**
使用實體類工廠實例對象的 updateRecord 方法執行數據更新操作，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名    | 數據類型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要更新的數據表實體類對象 |

**執行數據刪除操作：**
使用實體類工廠實例對象的 deleteRecord 方法執行數據刪除操作，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名    | 數據類型                                  | 用途           |
|--------|---------------------------------------|--------------|
| object | org.nervousync.magi.entity.BaseObject | 要刪除的數據表實體類對象 |

**執行數據唯一檢索操作：**
使用實體類工廠實例對象的 retrieveRecord 方法執行數據唯一檢索操作，執行過程中出錯則拋出所有異常。

通過主鍵進行數據唯一檢索

參數信息：

| 參數名         | 數據類型                 | 用途         |
|-------------|----------------------|------------|
| primaryKey  | java.io.Serializable | 主鍵對象       |
| entityClass | java.lang.Class      | 數據表實體類     |
| forUpdate   | 布爾值                  | 檢索結果用於數據更新 |

通過查詢條件進行數據唯一檢索

參數信息：

| 參數名         | 數據類型            | 用途         |
|-------------|-----------------|------------|
| entityClass | java.lang.Class | 數據表實體類     |
| filterMap   | java.util.Map   | 篩選條件映射表    |
| forUpdate   | 布爾值             | 檢索結果用於數據更新 |

**執行數據查詢操作：**
使用實體類工廠實例對象的 query 方法執行數據查詢操作，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名         | 數據類型                                 | 用途       |
|-------------|----------------------------------- ---|----------|
| targetClass | java.lang.Class                      | 查詢結果實體類  |
| queryInfo   | org.nervousync.brain.query.QueryInfo | 查詢信息實例對象 |

使用實體類工廠實例對象的 queryForUpdate 方法執行數據查詢操作，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名         | 數據類型            | 用途      |
|-------------|-----------------|---------|
| targetClass | java.lang.Class | 查詢結果實體類 |
| filterMap   | java.util.Map   | 篩選條件映射表 |

使用實體類工廠實例對象的 queryTotal 方法獲取數據查詢結果總數，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名       | 數據類型                                 | 用途       |
|-----------|------------------------------------- -|----------|
| queryInfo | org.nervousync.brain.query.QueryInfo | 查詢信息實例對象 |

使用實體類工廠實例對象的 queryTotal 方法獲取數據查詢結果總數，執行過程中出錯則拋出所有異常。

參數信息：

| 參數名         | 數據類型            | 用途      |
|-------------|-----------------|---------|
| targetClass | java.lang.Class | 查詢結果實體類 |
| filterMap   | java.util.Map   | 篩選條件映射表 |

## 敏感性資料的處理
在資料操作過程中，或多或少都會遇到敏感性資料的處理，包括但不限於身份識別代碼、電話號碼、電子郵箱位址、銀行卡號等，工具包中提供了簡單的注解用於對敏感性資料的自動處理。   
在需要處理的敏感性資料屬性上添加 org.nervousync.database.annotations.data.Sensitive 注解。   
注解的 encField 參數用於指定加密後的資料存儲列，參數 secureName 用於指定加密使用的安全配置名稱。   
配置資訊如下：

|     type參數      |     資料類型     |    加密結果樣例    |
|:---------------:|:------------:|:------------:|
|     NORMAL      |  用戶名、位址資訊等   |   w（隱藏資訊）3   |
| CHN_Social_Code | 中國大陸統一信用識別代碼 | 91110（隱藏資訊）X |
|   CHN_ID_Code   |  中國大陸身份證號碼   |  110（隱藏資訊）X  |
|     E_MAIL      |    電子郵寄地址    |   w（隱藏資訊）m   |
|  PHONE_NUMBER   |     電話號碼     |  139（隱藏資訊）1  |
|      Luhn       |    銀行卡號碼     | 62（隱藏資訊）8888 |

## 自定義查詢優化器
**1、添加查詢優化器實現類：***
新增查詢優化器實現類，並實現 org.nervousync.magi.query.optimizer.QueryOptimizer 接口，
在查詢優化器實現類上添加 org.nervousync.annotations.provider.Provider 註解，定義好註解的 name 屬性。
並將實現類寫入/META-INF/services/org.nervousync.magi.query.optimizer.QueryOptimizer文件，
系統通過Java的SPI進行查詢優化器實現類的加載。

**2、使用自定義的查詢優化器實現類：**
在初始化實體類工廠時，顯示指定查詢優化器，通過調用 org.nervousync.magi.entity.EntityFactory 的 initialize 靜態方法來初始化實體類工廠。

參數信息：

| 參數名           | 數據類型                                        | 用途                          |
|---------------|--------------------------------- ------------|-----------------------------|
| configure     | org.nervousync.brain.configs.BrainConfigure | 數據源配置信息實例對象                 |
| optimizerName | 字符串                                         | 查詢優化器 Provider 註解的 name 屬性值 |

## 使用代碼進行數據源的配置

程序開發人員可以通過使用 org.nervousync.brain.configs.builder.BrainConfigureBuilder 類進行數據源的配置，
包括新建或修改給定的配置信息實例對象。

## 數據導入導出
在資料表實體類中添加配置注解，可以讓工具包自動添加資料表和Excel檔的相互轉化工具。
### 資料表實體類的修改
**1、添加工作表注解：**   
在資料表實體類上添加 org.nervousync.database.annotations.data.ExcelSheet 注解，參數 value 為工作表的名稱。   
**2、添加數據列注解：**   
在資料列屬性上添加 org.nervousync.database.annotations.data.ExcelColumn 注解。參數 value 為對應Excel資料列的索引值，起始值為：0

### 導入匯出工具的初始化和使用
**1、工具的初始化：**   
顯示調用 org.nervousync.database.commons.DataUtils 的 initialize 靜態方法，傳入對應的參數配置資訊，初始化導入匯出工具。   
其中 basePath 參數為導入匯出工具的預設工作目錄，所有導入匯出檔會臨時存儲到此目錄中。   
+ 參數 providerName 為任務存儲適配器的識別代碼，用於保存導入匯出任務，如果未指定存儲適配器，則所有任務資訊存儲在記憶體中，當工具停止工作後會丟失未完成的任務資訊。   
+ 參數 threadLimit 為允許同時執行的任務執行緒數。   
+ 參數 expireTime 為已完成的任務在此時間以後，會被刪除掉任務資訊。

**2、資料導入任務的添加：**   
調用 org.nervousync.database.commons.DataUtils 的 addTask 方法添加資料導入任務。   
+ 參數 inputStream 為需要導入的Excel檔輸入流實例物件。   
+ 參數 userCode 為添加任務的操作員識別代碼，每個操作員僅可以查詢到自己的任務資訊。   
+ 參數 transactional 為是否使用事務模式進行資料導入的狀態值，資料類型為：boolean。   
+ 參數 timeout 為事務模式下的超時時間。

**3、資料匯出任務的添加：**   
調用 org.nervousync.database.commons.DataUtils 的 addTask 方法添加資料匯出任務。   
+ 參數 userCode 為添加任務的操作員識別代碼，每個操作員僅可以查詢到自己的任務資訊。   
+ 參數 queryInfos 為匯出資料的查詢資訊陣列，可以在一個匯出任務中匯出多個資料表的資料，並將這些資料保存在同一個Excel檔中。   

**4、工具的配置資訊更新：**   
調用 org.nervousync.database.commons.DataUtils 的 config 方法更新配置資訊。   
+ 參數 threadLimit 為允許同時執行的任務執行緒數。   
+ 參數 expireTime 為已完成的任務在此時間以後，會被刪除掉任務資訊。

## 貢獻與反饋
歡迎各位朋友將此文檔及專案中的提示資訊、錯誤資訊等翻譯為更多語言，以説明更多的使用者更好地瞭解與使用此工具包。   
如果在使用過程中發現問題或需要改進、添加相關功能，請提交issue到本專案或發送電子郵件到[wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=bugs_and_features)   
為了更好地溝通，請在提交issue或發送電子郵件時，寫明如下資訊：   
1、目的是：發現Bug/功能改進/添加新功能   
2、請粘貼以下資訊（如果存在）：傳入資料，預期結果，錯誤堆疊資訊   
3、您認為可能是哪裡的代碼出現問題（如提供可以幫助我們儘快地找到並解決問題）   
如果您提交的是添加新功能的相關資訊，請確保需要添加的功能是一般性的通用需求，即添加的新功能可以幫助到大多數使用者。

如果您需要添加的是定制化的特殊需求，我將收取一定的定制開發費用，具體費用金額根據定制化的特殊需求的工作量進行評估。   
定制化特殊需求請直接發送電子郵件到[wmkm0113\@gmail.com](mailto:wmkm0113@gmail.com?subject=payment_features)，同時請儘量在郵件中寫明您可以負擔的開發費用預算金額。

## 贊助與鳴謝
<span id="JetBrains">
    <img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jetbrains.svg" width="100px" alt="JetBrains Logo (Main) logo.">
    <span>非常感謝 <a href="https://www.jetbrains.com/">JetBrains</a> 通過許可證贊助我們的開源項目。</span>
</span>

