## mybatis-daoj-oracle
为Oracle数据库定制的mapper生动生成工具

<br>

## 使用说明

**配置文件位置：mybatis-daoj-oracle/src/main/resources/mybatis-daoj.xml**

**dataSource**：配置数据源
```
    <dataSource>
         <property name="jdbcUrl" value="jdbc:oracle:thin:@192.168.1.78:1521:db2"/>
         <property name="jdbcDriver" value="oracle.jdbc.driver.OracleDriver"/>
         <property name="username" value="test"/>
         <property name="password" value="password"/>
    </dataSource>
``` 
 
**params**：配置代码生成参数
* basicEntity：PO基类完整类名
```
    <property name="basicEntity" value="com.shls.db.dao.BasicPo"/>
```

* basicDao：DAO基类完整类名
```
    <property name="basicDao" value="com.shls.db.dao.BasicDao"/>
```

* 生成后的DAO接口所在包
```
    <property name="basicDao" value="com.shls.db.dao.BasicDao"/>
```

* 代码生成的输出目录, 支持相对路径或绝对路径
```
    <property name="outputDir" value="generated-files"/>
```

* 要生成的方法，多个方法用逗号(',')分隔
```
    <!-- 可选值有：save,saveBatch,update,updateIgnoreNull,updateBatch,delete,deleteBatch,deleteById,deleteAll,count,findById,find -->
    <property name="methods" value="save,update,updateIgnoreNull,delete,deleteById,deleteAll,count,findById,find"/>
```

* 要生成的表名, 多个表可以逗号(',')分隔。
```
    <!-- 注意：ORACLE数据库表名是【大写】 -->
    <property name="tables" value="RECEIVABLES,ABS_PROJECT" create-sequence="false"/>
```

`create-sequence`指定是否自动创建自增序列，如果设置为true, 将会创建一个以表名_SEQ命名的序列（如果不存在该序列），如：`SUPPLIER_SEQ`。
同时在Mapper映射中的`save`方法中将会插入以下xml片段：
```
    <selectKey keyProperty="id" resultType="java.lang.Long" order="BEFORE">
         SELECT SUPPLIER_SEQ.NEXTVAL AS id FROM dual
    </selectKey>
```
其中`id`为表的主键字段，该字段必须为Number类型。