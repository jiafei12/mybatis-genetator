package com.github.walker.mybatis.daoj.core;

import lombok.Data;

/**
 * 本自定义元数据的描述类
 */
@Data
class MetaDataDescription {
    /**
     * 列名
     */
    private String colName;
    /**
     * 列类型, 参考java.sql.Types
     */
    private int colType;

    /**
     * 是主键吗?
     */
    private boolean isPk;

    /**
     * 精确度
     */
    private int precision;

    /**
     * 小数点后长度
     */
    private int scale;

    /**
     * 映射的属性名
     */
    private String fieldName;

    /**
     * 映射的属性名的类型, 如：String
     */
    private Class fieldType;

    /**
     * 注释
     */
    private String comment;

}
