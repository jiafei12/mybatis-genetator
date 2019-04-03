package com.github.walker.mybatis.daoj.core;

import com.github.walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 元数据映射类
 */
class MetaMapping
{
    private Logger log = LoggerFactory.getLogger(this.getClass());

    //表名
    private String tableName;

    //表的各列及元数据
    private Map<String, MetaDataDescription> fieldsMetaMap = new LinkedHashMap<String, MetaDataDescription>();
    private String sequenceName;


    protected MetaMapping(String tableName)
    {
        try
        {
            this.tableName = tableName;
            this.parseMetaData();
            this.createTableNameSequence();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 获取字段元数据映射
     *
     * @return
     */
    protected Map<String, MetaDataDescription> getFieldsMetaMap()
    {
        return this.fieldsMetaMap;
    }

    /**
     * 获取以表名命名的序列，通常用于主键自增
     *
     * @return
     */
    protected String getTableNameSequence()
    {
        return sequenceName;
    }


    /**
     * 创建以表名命名的序列，通常用于主键自增
     *
     * @return
     */
    private String createTableNameSequence()
    {
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try
        {
            sequenceName = tableName.toUpperCase() + "_SEQ";
            conn = DBResource.getConnection();
            String sql = "SELECT COUNT(1) FROM  user_sequences WHERE SEQUENCE_NAME = '" + sequenceName + "'";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            int count = 0;
            while (rs.next())
            {
                count = rs.getInt(1);
            }

            sql = "CREATE SEQUENCE " + sequenceName + " start WITH 1 increment BY 1 nomaxvalue nominvalue nocycle nocache";
            log.debug("准备创建序列: " + sql);

            if (count > 0)
            {
                log.debug("序列 " + sequenceName + "已存在");
            }
            else
            {
                log.debug("创建序列 " + sequenceName);
                if (sequenceName.length() > 30)
                {
                    sequenceName = "序列名过长";
                    log.debug("序列名过长, 跳过" + sequenceName);
                }
                else
                {
                    stmt = conn.prepareStatement(sql);
                    stmt.executeQuery();
                    conn.commit();
                }
            }

            return sequenceName;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                DBResource.freeConnection(conn);
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 取得表的元数据，即取得各列名及类型
     *
     * @return 列名及其列类型：LinkedHashMap<String, MyMetaData> map
     * @throws Exception
     */
    protected void parseMetaData()
            throws Exception
    {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try
        {
            conn = DBResource.getConnection();

            //定位主键字段
            Set<String> keySet = new HashSet<String>();

            rs = conn.getMetaData().getPrimaryKeys(conn.getCatalog(), null, tableName);
            for (; rs.next(); )
            {
                String pk = rs.getString("COLUMN_NAME")/*.toLowerCase()*/;
                keySet.add(pk);
            }
            rs.close();

            //获取列元数据
            String sql = "SELECT * FROM " + tableName + " WHERE 1=2";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++)
            {
                String colName = rsmd.getColumnName(i)/*.toLowerCase()*/;
//                log.debug(colName + ":------ " + rsmd.getColumnType(i) + "("
//                        + rsmd.getColumnTypeName(i) + "), " + rsmd.getPrecision(i) + "(精确度), " + rsmd.getScale(i) + "(小数点后位数)");

                MetaDataDescription md = new MetaDataDescription();
                md.setColName(colName);

                // 对于Oracle的DATE数据类型，ResultSetMetaData.getColumnType返回值为93（即 TIMESTAMP）, 可能是一个BUG？
                if (rsmd.getColumnTypeName(i).equals("DATE"))
                {
                    md.setColType(Types.DATE);
                }
                else
                {
                    md.setColType(rsmd.getColumnType(i));
                }
                md.setPrecision(rsmd.getPrecision(i));
                md.setScale(rsmd.getScale(i));


                if (keySet.contains(colName))
                {
                    md.setPk(true);
                }
                else
                {
                    md.setPk(false);
                }

                // 转为驼峰命名
                String fileldName = MappingUtil.getFieldName(colName);

                // 表中字段名直接对应属性名
                //String fileldName = colName;

                md.setFieldName(fileldName);

                //把列类型映射为类属性类型
                md.setFieldType(reflectToFieldType(md.getColType(), md.getScale(), md.getPrecision()));

                fieldsMetaMap.put(colName, md);
            }

            // 设置各个字段的注释
            setFieldsComment(fieldsMetaMap, conn);

        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                DBResource.freeConnection(conn);
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 设置各个字段的注释
     *
     * @param colNameMetaMap
     * @param conn
     * @throws SQLException
     */
    private void setFieldsComment(Map<String, MetaDataDescription> colNameMetaMap, Connection conn) throws SQLException
    {
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet ret = dbmd.getColumns(null, "%", tableName, "%");
        while (ret.next())
        {
            String columnName = ret.getString("COLUMN_NAME");
            String comment = ret.getString("REMARKS");

            if (comment != null && !comment.equals("null"))
            {
                if (colNameMetaMap.containsKey(columnName))
                {
                    colNameMetaMap.get(columnName).setComment(comment);
                }
            }
        }
    }

    /**
     * 把列类型映射为类属性类型
     *
     * @param colType
     * @return
     * @throws Exception
     */
    private Class reflectToFieldType(int colType, int scale, int precision) throws Exception
    {
        switch (colType)
        {
            case Types.BIT:
                return Boolean.class;

            case Types.TINYINT:
                return Byte.class;
            case Types.SMALLINT:
                return Short.class;
            case Types.INTEGER:
                return Integer.class;
            case Types.BIGINT:
                return Long.class;

            case Types.FLOAT:
                return Float.class;
            case Types.REAL:
                return Double.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.NUMERIC:
                if (scale == 0)
                {
                    /*if (precision == 1)
                    {
                        return Boolean.class;
                    }
                    else */
                    if (precision == 38)
                    {
                        return Long.class;
                    }
                    else if (precision == 1)
                    {
                        return Boolean.class;
                    }
                    else
                    {
                        return Integer.class;
                    }
                }
                else
                {
                    if (scale < 16)
                    {
                        return Double.class;
                    }
                    else
                    {
                        return java.math.BigDecimal.class;
                    }
                }
            case Types.DECIMAL:
                if (scale == 0)
                {
                    return Long.class;
                }
                else
                {
                    return java.math.BigDecimal.class;
                }
            case Types.CHAR:
                return String.class;
            case Types.VARCHAR:
                return String.class;
            case Types.LONGVARCHAR:
                return String.class;

            case Types.DATE:
                return java.sql.Date.class;
            case Types.TIME:
                return java.sql.Time.class;
            case Types.TIMESTAMP:
                return java.sql.Timestamp.class;

            case Types.BINARY:
                return byte[].class;
            case Types.VARBINARY:
                return byte[].class;
            case Types.LONGVARBINARY:
                return byte[].class;

            case Types.BLOB:
                return String.class;
            case Types.CLOB:
                return String.class;
        }

        throw new Exception("不能识别的列类型:" + colType);
    }

    public static void main(String[] args)
    {
        System.out.println(Byte[].class.getName());
        System.out.println(Byte[].class.getSimpleName());

        System.out.println(byte[].class.getName());
        System.out.println(byte[].class.getSimpleName());
    }
}
