package com.github.walker.mybatis.daoj.utils;


import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.util.HashMap;
import java.util.Map;

/**
 * 提提对类名/属性名与数据库表/列的映射方法
 *
 */
public class MappingUtil
{
    public static final Map<String, String> BASIC_TYPE_MAP = new HashMap<String, String>();
    private static final Map<Class, String> JDBC_YTPE_MAP = new HashMap<Class, String>();


    static
    {
        BASIC_TYPE_MAP.put("Integer", "int");
        BASIC_TYPE_MAP.put("Long", "long");
        BASIC_TYPE_MAP.put("Short", "short");
        BASIC_TYPE_MAP.put("Byte", "byte");
        BASIC_TYPE_MAP.put("Boolean", "boolean");
        BASIC_TYPE_MAP.put("Character", "char");
        BASIC_TYPE_MAP.put("Float", "float");
        BASIC_TYPE_MAP.put("Double", "double");


        JDBC_YTPE_MAP.put(java.lang.String.class, "VARCHAR");
        JDBC_YTPE_MAP.put(java.math.BigDecimal.class, "NUMERIC");
        JDBC_YTPE_MAP.put(boolean.class, "BIT");
        JDBC_YTPE_MAP.put(Boolean.class, "BIT");
        JDBC_YTPE_MAP.put(byte.class, "TINYINT");
        JDBC_YTPE_MAP.put(Byte.class, "TINYINT");
        JDBC_YTPE_MAP.put(short.class, "SMALLINT");
        JDBC_YTPE_MAP.put(Short.class, "SMALLINT");
        JDBC_YTPE_MAP.put(int.class, "INTEGER");
        JDBC_YTPE_MAP.put(Integer.class, "INTEGER");
        JDBC_YTPE_MAP.put(long.class, "BIGINT");
        JDBC_YTPE_MAP.put(Long.class, "BIGINT");
        JDBC_YTPE_MAP.put(float.class, "FLOAT");
        JDBC_YTPE_MAP.put(Float.class, "FLOAT");
        JDBC_YTPE_MAP.put(double.class, "DOUBLE");
        JDBC_YTPE_MAP.put(Double.class, "DOUBLE");
        JDBC_YTPE_MAP.put(byte[].class, "VARBINARY");
        JDBC_YTPE_MAP.put(Byte[].class, "VARBINARY");
        JDBC_YTPE_MAP.put(java.sql.Date.class, "DATE");
        JDBC_YTPE_MAP.put(java.sql.Time.class, "TIME");
        JDBC_YTPE_MAP.put(java.sql.Timestamp.class, "TIMESTAMP");
        JDBC_YTPE_MAP.put(Clob.class, "CLOB");
        JDBC_YTPE_MAP.put(Blob.class, "BLOB");
        JDBC_YTPE_MAP.put(Array.class, "ARRAY");
    }


    /**
     * 根据驼峰规则将表名转为PO类名
     *
     * @param tableName
     * @return
     */
    public static String getEntityName(String tableName)
    {
        return formatName(tableName, true);
    }

    /**
     * 根据驼峰规则将列名转为PO类的属性名
     *
     * @param columnName
     * @return
     */
    public static String getFieldName(String columnName)
    {
        return formatName(columnName, false);
    }

    public static String getBasicTypeName(String wrpperName)
    {
        return BASIC_TYPE_MAP.get(wrpperName);
    }

    private static String formatName(String src, boolean upperAtFirst)
    {
        StringBuffer buff = new StringBuffer(src.toLowerCase());

        // 首字母大写
        if (upperAtFirst)
        {
            buff.replace(0, 1, String.valueOf(Character.toUpperCase(src.charAt(0))));
        }
        // delete character '_', and convert the next character to uppercase
        for (int i = 1, length = buff.length(); i < length; )
        {

            // the last character
            char lastCh = buff.charAt(i - 1);
            // the current character
            char ch = buff.charAt(i);
            // if this character is a letter, and the last character is '_'
            if (Character.isLetter(ch) && lastCh == '_')
            {
                buff.replace(i - 1, i, String.valueOf(Character.toUpperCase(ch)));
                buff.deleteCharAt(i);
                length--;
            }
            else
            {
                i++;
            }
        }
        return buff.toString();
    }

    public static String getJdbcType(Class clazz)
    {
        return JDBC_YTPE_MAP.get(clazz);
    }

    public static void main(String[] args)
    {
        String table1 = "abc_d";
        String table2 = "abc_2";
        String table3 = "abc3";
        String table4 = "a_bc";

        System.out.println(getFieldName(table1));
        System.out.println(getFieldName(table2));
        System.out.println(getFieldName(table3));
        System.out.println(getFieldName(table4));
    }
}