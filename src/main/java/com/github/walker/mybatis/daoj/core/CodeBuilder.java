package com.github.walker.mybatis.daoj.core;


import com.github.walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * 代码自动生成
 */
class CodeBuilder
{
    private static Logger logger = LoggerFactory.getLogger(CodeBuilder.class);

    protected String tableName;

    protected Map<String, MetaDataDescription> fieldsMetaMap;

    protected String sequenceName;

    protected CodeBuilder(String tableName)
    {
        this.tableName = tableName.trim().toLowerCase();
        MetaMapping metaMapping = new MetaMapping(tableName);
        this.fieldsMetaMap = metaMapping.getFieldsMetaMap();
        this.sequenceName = metaMapping.getTableNameSequence();
    }


    /**
     * 构造实体类的源码
     *
     * @param entityPackageName 实体类源码的包名
     * @param poIgnoreFields
     * @return
     */
    protected String buildEntitySource(String basicVoName, String entityPackageName, List<String> poIgnoreFields) throws Exception
    {

        StringBuffer buff = new StringBuffer();
        buff.append("package ");
        buff.append(entityPackageName + "; \n\n");

        //buff.append("import " + basicVoName + "; \n");
        //buff.append("\n");

        // public class AA {
        buff.append("public class " + MappingUtil.getEntityName(tableName));
        buff.append(" extends BasicPo {\n\n");
        //buff.append("    private static final long serialVersionUID = 1L;\n\n");

        buildClassFields(buff, poIgnoreFields);
        buildSetterAndGetter(buff, poIgnoreFields);
        buildToString(buff);


        return buff.toString();
    }

    /**
     * 生成类字段 - private int xxx
     *
     * @param buffer
     * @param poIgnoreFields
     */
    private void buildClassFields(StringBuffer buffer, List<String> poIgnoreFields)
    {
        //生成属性  private String xxx;
        for (Iterator<String> it = fieldsMetaMap.keySet().iterator(); it.hasNext(); )
        {
            String colName = it.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            String fieldName = md.getFieldName();
            if (poIgnoreFields.contains(fieldName))
            {
                continue;
            }

            // 获取基本数据类名称
            String fieldType = getBasicTypeName(md.getFieldType());

            // 字段注释信息
            if (md.getComment() != null)
            {
                buffer.append("    /** " + md.getComment().trim() + " **/\n");
            }

            buffer.append("    private " + fieldType + " " + fieldName + ";\n\n");
        }
        buffer.append("\n\n");
    }

    /**
     * 生成setter、getter方法
     *
     * @param buffer
     * @param poIgnoreFields
     */
    private void buildSetterAndGetter(StringBuffer buffer, List<String> poIgnoreFields)
    {
        //生成方法  public String getXXX();
        for (Iterator<String> it = fieldsMetaMap.keySet().iterator(); it.hasNext(); )
        {
            String colName = it.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            String fieldName = md.getFieldName();
            if (poIgnoreFields.contains(fieldName))
            {
                continue;
            }
            String fieldType = getBasicTypeName(md.getFieldType());
            String firstChar = fieldName.substring(0, 1).toUpperCase();
            if (fieldName.length() > 1 && Character.isUpperCase(fieldName.charAt(1)))
            {
                firstChar = firstChar.toLowerCase();
            }

            // 字段注释信息
            if (md.getComment() != null)
            {
                buffer.append("    /** 获取 " + md.getComment().trim() + " **/\n");
            }
            String prefix = fieldType.equals("boolean") ? " is" : " get";

            if (fieldType.equals("boolean") && fieldName.startsWith("is"))
            {
                prefix = " ";
                buffer.append("    public " + fieldType + prefix);
                buffer.append(fieldName + "() {\n");
            }
            else
            {
                buffer.append("    public " + fieldType + prefix);
                buffer.append(firstChar + fieldName.substring(1) + "() {\n");
            }
            buffer.append("        return " + fieldName + ";\n");
            buffer.append("   }\n\n");

            // 字段注释信息
            if (md.getComment() != null)
            {
                buffer.append("    /** 设置 " + md.getComment().trim() + " **/\n");
            }
            buffer.append("    public void set");
            buffer.append(firstChar + fieldName.substring(1));
            buffer.append("(" + fieldType + " " + fieldName + ") {\n");
            buffer.append("        this." + fieldName + " = " + fieldName + ";\n");
            buffer.append("   }\n\n");
        }
    }

    /**
     * 生成toString方法
     *
     * @param buffer
     */
    private void buildToString(StringBuffer buffer)
    {
        // 生成toString方法
        buffer.append("    public String toString() {");
        buffer.append("\n        return \"" + MappingUtil.getEntityName(tableName) + "{");

        int index = 0;
        for (Iterator<String> it = fieldsMetaMap.keySet().iterator(); it.hasNext(); )
        {
            String colName = it.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            String filedName = md.getFieldName();
            if (index > 0)
            {
                buffer.append("\n             + \"");
            }
            buffer.append(filedName + " = " + "\" + " + filedName + " + \", \"");
            index++;
        }
        buffer.append(" + \"}\";");
        buffer.append("\n    }\n\n");
        buffer.append("}\n\n");
    }

    /**
     * 构造DAO类的源码
     *
     * @param daoPackageName DAO类源码的包名
     * @return
     */
    protected String buildDaoSource(String basicDaoName, String daoPackageName) throws Exception
    {

        StringBuffer buff = new StringBuffer();
        buff.append("package ");
        buff.append(daoPackageName + "; \n\n");
        buff.append("import " + basicDaoName + "; \n\n");

        // public class AA {
        buff.append("import org.springframework.stereotype.Repository;\n\n");
        buff.append("@Repository\n");
        buff.append("public interface ");
        buff.append(MappingUtil.getEntityName(tableName) + "Dao");
        buff.append(" extends BasicDao {\n}\n");

        return buff.toString();
    }


    /**
     * 生成Mapper映射文件
     *
     * @param daoPackageName DAO类源码的包名
     * @param methods
     * @param createSequence
     * @param mergeFile
     * @return
     */
    protected String buildMapperSource(String daoPackageName, List<String> methods, boolean createSequence, File mergeFile) throws Exception
    {
        final String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                "<!DOCTYPE mapper PUBLIC  \"-//mybatis.org//DTD Mapper 3.0//EN\"  \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n\n";

        StringBuffer buff = new StringBuffer(head);
        buff.append("<mapper namespace=\"" + daoPackageName + "." + MappingUtil.getEntityName(tableName) + "Dao\">\n");

        //获取主键列
        Map<String, String> pkFieldMap = new HashMap<String, String>();
        //Set set = fieldsMetaMap.keySet();
        Iterator it = fieldsMetaMap.keySet().iterator();
        while (it.hasNext())
        {
            String colName = (String) it.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);

            if (md.isPk())
            {
                pkFieldMap.put(colName, md.getFieldName());
            }
        }

        // 生成ResultMap
        String resultMapId = buildResultMap(buff);

        if (methods.contains("insert"))
        {
            logger.debug("生成方法：insert");
            buildInsert(buff, pkFieldMap, createSequence);
        }

        if (methods.contains("insertBatch"))
        {
            logger.debug("生成方法：insertBatch");

            buildInsertBatch(buff);
        }

        if (methods.contains("update"))
        {
            logger.debug("生成方法：update");

            buildUpdate(buff, pkFieldMap);
        }

        if (methods.contains("updateIgnoreNull"))
        {
            logger.debug("生成方法：updateIgnoreNull");

            buildUpdateIgnoreNull(buff, pkFieldMap);
        }

        if (methods.contains("updateBatch"))
        {
            logger.debug("生成方法：updateBatch");

            buildUpdateBatch(buff, pkFieldMap);
        }

        if (methods.contains("delete"))
        {
            logger.debug("生成方法：delete");

            buildDelete(buff, pkFieldMap);
        }

        if (methods.contains("deleteBatch"))
        {
            logger.debug("生成方法：deleteBatch");

            buildDeleteBatch(buff, pkFieldMap);
        }

        if (methods.contains("deleteById"))
        {
            logger.debug("生成方法：deleteById");

            buildDeleteById(buff, pkFieldMap);
        }

        if (methods.contains("deleteAll"))
        {
            logger.debug("生成方法：deleteAll");

            buildDeleteAll(buff);
        }

        if (methods.contains("deleteBy"))
        {
            logger.debug("生成方法：deleteBy");
            buildDeleteBy(buff);
        }

        if (methods.contains("count"))
        {
            logger.debug("生成方法：count");

            buildCount(buff);
        }

        if (methods.contains("findById"))
        {
            logger.debug("生成方法：findById");

            buildFindById(buff, pkFieldMap, resultMapId);
        }

        if (methods.contains("find"))
        {
            logger.debug("生成方法：find");

            buildFind(buff, resultMapId);
        }

        buildFindByPage(buff, resultMapId);


        buildWhereSql(buff);

        buff.append("\n<!--自动生成代码结束, 请勿修改以上代码, 也不要删除此行注释-->\n\n");

        mergeFile(mergeFile, buff);

        buff.append("</mapper>\n");
        return buff.toString();
    }

    private void mergeFile(File file, StringBuffer buff)
    {
        if (file.exists())
        {
            try(BufferedReader reader = new BufferedReader(new FileReader(file)))
            {
                String line;

                boolean mergeStart = false;
                while ((line = reader.readLine()) != null)
                {
                    if (line.contains("自动生成代码结束, 请勿修改以上代码, 也不要删除此行注释"))
                    {
                        mergeStart = true;
                        continue;
                    }

                    if (mergeStart)
                    {
                        if (!line.trim().equals("</mapper>"))
                        {
                            buff.append(line + "\n");
                        }
                    }
                }
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {

            }
        }
    }

    /**
     * 生成ResultMap
     *
     * @param buff
     * @return
     */
    private String buildResultMap(StringBuffer buff)
    {
        // PO包名
        String entityPackage = ConfigLoader.getEntityPackage();
        // PO名
        String entityName = MappingUtil.getEntityName(tableName);
        // ResultMap 名
        String resultMapId = entityName + "Map";
        // PO完整类名
        String entityFullName = entityPackage + "." + entityName;

        buff.append("\n    <!-- ============================= ResultMap ============================= -->\n");
        buff.append("    <resultMap id=\"" + resultMapId + "\" type=\"" + entityFullName + "\">\n");
        Iterator iterator = fieldsMetaMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            String fieldName = md.getFieldName();

            Class javaType = md.getFieldType();
            String jdbcType = MappingUtil.getJdbcType(javaType);

            buff.append("        <result property=\"" + fieldName + "\" column=\"" + colName + "\"  javaType=\"" + javaType.getName() + "\" jdbcType=\"" + jdbcType + "\" />\n");
        }
        buff.append("    </resultMap>\n");

        return resultMapId;
    }

    /**
     * 生成Insert
     *
     * @param buff
     * @param pkFieldMap
     * @param createSequence
     */
    private void buildInsert(StringBuffer buff, Map<String, String> pkFieldMap, boolean createSequence)
    {
        String keyField = "";

        if (pkFieldMap.entrySet().iterator().hasNext())
        {
            keyField = pkFieldMap.entrySet().iterator().next().getValue();
        }
        buff.append("\n    <!-- ============================= INSERT ============================= -->\n");
        if (!pkFieldMap.isEmpty() && pkFieldMap.size() == 1)
        {
            buff.append("    <insert id=\"insert\" keyProperty=\"" + keyField + "\" >\n");
        }
        else
        {
            buff.append("    <insert id=\"insert\">\n");
        }


        // 插入序列
        if (createSequence)
        {
            buff.append("\n        <selectKey keyProperty=\"" + keyField + "\" resultType=\"java.lang.Long\" order=\"BEFORE\">" +
                    "\n                SELECT " + sequenceName + ".NEXTVAL AS " + keyField + " FROM dual\n" +
                    "        </selectKey>\n\n");
        }

        buff.append("        INSERT INTO ");
        buff.append(tableName + "( \n");

        StringBuilder valuesStr = new StringBuilder();
        Iterator iterator = fieldsMetaMap.keySet().iterator();

        //int i = 0;
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            /*if (md.isPk())
            {
                continue;
            }*/
            buff.append("                " + colName + ",\n");
            String jdbcType = MappingUtil.getJdbcType(md.getFieldType());
            valuesStr.append("                #{" + md.getFieldName() + " ,jdbcType=" + jdbcType + "},\n");
        }
        while (buff.charAt(buff.length() - 1) == ' ')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (valuesStr.charAt(valuesStr.length() - 1) == ' ')
        {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (valuesStr.charAt(valuesStr.length() - 1) == '\n')
        {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        if (valuesStr.charAt(valuesStr.length() - 1) == ',')
        {
            valuesStr.deleteCharAt(valuesStr.length() - 1);
        }
        buff.append(" )\n");

        buff.append("        VALUES ");
        buff.append("( \n" + valuesStr.toString() + ")\n");
        buff.append("    </insert>\n\n");

        valuesStr.delete(0, valuesStr.length());
    }

    /**
     * 生成Update
     *
     * @param buff
     * @param pkFieldMap
     */
    public void buildUpdate(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        buff.append("\n    <!-- ============================= UPDATE ============================= -->\n");
        buff.append("    <update id=\"update\">\n");

        buff.append("        UPDATE " + tableName + "\n");
        buff.append("        <set>\n");
        Iterator iterator = fieldsMetaMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            String jdbcType = MappingUtil.getJdbcType(md.getFieldType());
            if (md.isPk())
            {
                continue;
            }
            buff.append("            " + colName + "=#{" + md.getFieldName() + " ,jdbcType=" + jdbcType + "},\n");
        }
        buff.append("        </set>\n");

        buff.append("        WHERE ");

        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </update>\n\n");
    }


    /**
     * 生成UpdateIgnoreNull
     *
     * @param buff
     * @param pkFieldMap
     */
    public void buildUpdateIgnoreNull(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        buff.append("    <update id=\"updateIgnoreNull\">\n");
        buff.append("        UPDATE " + tableName + "\n");
        buff.append("        <set>\n");
        Iterator iterator = fieldsMetaMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            if (md.isPk())
            {
                continue;
            }
            buff.append("            <if test=\"" + md.getFieldName() + "!= null\">" + colName + "=#{" + md.getFieldName() + "},</if>\n");
        }
        buff.append("        </set>\n");

        buff.append("        WHERE ");

        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </update>\n\n");
    }

    /**
     * 生成批量插入
     *
     * @param buff
     */
    private void buildInsertBatch(StringBuffer buff)
    {
        StringBuilder valuesBuilder = new StringBuilder();
        buff.append("\n    <!-- batch insert for oracle -->\n");
        buff.append("    <insert id=\"insertBatch\">\n");
        buff.append("        INSERT INTO " + tableName + "( ");

        Iterator iterator = fieldsMetaMap.keySet().iterator();
        int i = 0;
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
//            if (md.isPk()) {
//                continue;
//            }
            buff.append(colName + ",");
            String jdbcType = MappingUtil.getJdbcType(md.getFieldType());
            valuesBuilder.append("#{" + md.getFieldName() + " ,jdbcType=" + jdbcType + "},");

            i++;
            if (i % 7 == 0)
            {
                buff.append("\n                          ");
                valuesBuilder.append("\n              ");
            }
        }
        while (buff.charAt(buff.length() - 1) == ' ')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        while (valuesBuilder.charAt(valuesBuilder.length() - 1) == ' ')
        {
            valuesBuilder.deleteCharAt(valuesBuilder.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == '\n')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (buff.charAt(buff.length() - 1) == ',')
        {
            buff.deleteCharAt(buff.length() - 1);
        }
        if (valuesBuilder.charAt(valuesBuilder.length() - 1) == '\n')
        {
            valuesBuilder.deleteCharAt(valuesBuilder.length() - 1);
        }
        if (valuesBuilder.charAt(valuesBuilder.length() - 1) == ',')
        {
            valuesBuilder.deleteCharAt(valuesBuilder.length() - 1);
        }
        buff.append(" )\n");

        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" separator=\"UNION ALL\">\n");
        buff.append("            SELECT " + valuesBuilder.toString() + " \n");
        buff.append("              FROM DUAL \n");
        buff.append("        </foreach>\n");
        buff.append("    </insert>\n\n");
        valuesBuilder.delete(0, valuesBuilder.length());
    }

    /**
     * 生成批量更新
     *
     * @param buff
     * @param pkFieldMap
     */
    private void buildUpdateBatch(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        ///////////////////// updateBatch
        buff.append("    <update id=\"updateBatch\" parameterType=\"java.util.List\">\n");
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\"  separator=\";\">\n");
        buff.append("            UPDATE " + tableName + "\n");

        buff.append("            <set>\n");
        Iterator iterator = fieldsMetaMap.keySet().iterator();
        while (iterator.hasNext())
        {
            String colName = (String) iterator.next();
            MetaDataDescription md = fieldsMetaMap.get(colName);
            if (md.isPk())
            {
                continue;
            }
            String jdbcType = MappingUtil.getJdbcType(md.getFieldType());
            buff.append("                " + colName + "=#{item." + md.getFieldName() + " ,jdbcType=" + jdbcType + "},\n");
        }
        buff.append("            </set>\n");

        buff.append("            WHERE ");

        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{item." + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");


        buff.append("        </foreach>\n");
        buff.append("    </update>\n\n");
    }

    /**
     * 生成Delete
     *
     * @param buff
     * @param pkFieldMap
     */
    private void buildDelete(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        buff.append("\n    <!-- ============================= DELETE ============================= -->\n");
        buff.append("    <delete id=\"delete\">\n");
        buff.append("        UPDATE " + tableName + " SET IS_DELETE = 1 \n");
        buff.append("        WHERE ");
        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </delete>\n\n");
    }

    /**
     * 生成批量删除
     *
     * @param buff
     * @param pkFieldMap
     */
    private void buildDeleteBatch(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        buff.append("    <delete id=\"deleteBatch\">\n");
        buff.append("        UPDATE " + tableName + " SET IS_DELETE = 1 \n");
        buff.append("        WHERE\n");
        buff.append("        <foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"OR\" close=\")\">\n");
        buff.append("            ");
        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{item." + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("        </foreach>\n");
        buff.append("    </delete>\n\n");
    }

    /**
     * 生成deleteById
     *
     * @param buff
     * @param pkFieldMap
     */
    private void buildDeleteById(StringBuffer buff, Map<String, String> pkFieldMap)
    {
        buff.append("    <delete id=\"deleteById\">\n");
        buff.append("        UPDATE " + tableName + " SET IS_DELETE = 1 \n");
        buff.append("        WHERE ");
        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </delete>\n\n");
    }

    /**
     * 生成deleteAll
     *
     * @param buff
     */
    private void buildDeleteAll(StringBuffer buff)
    {
        buff.append("    <delete id=\"deleteAll\">\n");
        buff.append("        UPDATE " + tableName + " SET IS_DELETE = 1 \n");
        buff.append("    </delete>\n\n");
    }

    private void buildDeleteBy(StringBuffer buff)
    {
        buff.append("    <delete id=\"deleteBy\">\n");
        buff.append("        UPDATE " + tableName + " SET IS_DELETE = 1 \n");
        buff.append("        <include refid=\"whereSql\"/> \n");
        buff.append("    </delete>\n\n");
    }

    /**
     * 生成count
     *
     * @param buff
     */
    private void buildCount(StringBuffer buff)
    {
        buff.append("\n    <!-- ============================= SELECT ============================= -->\n");
        buff.append("    <select id=\"count\" resultType=\"java.lang.Long\">\n");
        buff.append("        SELECT COUNT(1) FROM " + tableName);
        buff.append("\n        <include refid=\"whereSql\"/>\n");
        buff.append("    </select>\n\n");
    }

    /**
     * 生成findById
     *
     * @param buff
     * @param pkFieldMap
     */
    private void buildFindById(StringBuffer buff, Map<String, String> pkFieldMap, String resultMapId)
    {

        buff.append("    <select id=\"findById\" resultMap=\"" + resultMapId + "\">\n");
        buff.append("        SELECT * FROM " + tableName + "\n");
        buff.append("        WHERE IS_DELETE = 0 AND ");

        Iterator<String> keyIt = pkFieldMap.keySet().iterator();
        while (keyIt.hasNext())
        {
            String colName = keyIt.next();
            buff.append(colName + "=#{" + pkFieldMap.get(colName) + "} AND ");
        }
        if (buff.substring(buff.length() - 4, buff.length()).equals("AND "))
        {
            buff.delete(buff.length() - 4, buff.length());
        }
        buff.append("\n");
        buff.append("    </select>\n\n");
    }

    /**
     * 生成find
     *
     * @param buff
     */
    private void buildFind(StringBuffer buff, String resultMapId)
    {
        buff.append("    <select id=\"find\" resultMap=\"" + resultMapId + "\">\n");
        buff.append("        SELECT\n");
        buff.append("        <if test=\"entity.fields == null\">*</if>\n");
        buff.append("        <if test=\"entity.fields != null\">\n");
        buff.append("            <foreach collection=\"entity.fields\" item=\"field\" separator=\",\">\n");
        buff.append("                ${field}\n");
        buff.append("            </foreach>\n");
        buff.append("        </if>\n");
        buff.append("        FROM " + tableName + "\n");
        buff.append("        <include refid=\"whereSql\"/>\n");
        buff.append("        ORDER BY ${entity.orderBy} ${entity.order}\n");
        buff.append("    </select>\n\n");
    }


    /**
     * 生成find
     *
     * @param buff
     */
    private void buildFindByPage(StringBuffer buff, String resultMapId)
    {
        buff.append("    <select id=\"findByPage\" resultMap=\"" + resultMapId + "\">\n");
        buff.append("        SELECT * FROM (\n");
        buff.append("            SELECT ROWNUM as rn, t1.* FROM (\n");
        buff.append("                SELECT\n");
        buff.append("                    <if test=\"entity.fields == null\">*</if>\n");
        buff.append("                    <if test=\"entity.fields != null\">\n");
        buff.append("                        <foreach collection=\"entity.fields\" item=\"field\" separator=\",\">\n");
        buff.append("                            ${field}\n");
        buff.append("                        </foreach>\n");
        buff.append("                    </if>\n");
        buff.append("                FROM " + tableName + " <include refid=\"whereSql\"/>\n");
        buff.append("                ORDER BY ${entity.orderBy} ${entity.order} )t1\n");
        buff.append("            WHERE ROWNUM <![CDATA[<=]]> #{entity.offset} + #{entity.length} )t2\n");
        buff.append("        WHERE t2.rn > #{entity.offset}\n");
        buff.append("    </select>\n\n");
    }

    private void buildWhereSql(StringBuffer buff)
    {
        buff.append("  <sql id=\"whereSql\">\n");
        buff.append("       <where>\n");
        buff.append("            <if test=\"entity.eqParams != null\">\n");
        buff.append("                <foreach collection=\"entity.eqParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} = #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.neqParams != null\">\n");
        buff.append("                <foreach collection=\"entity.neqParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} != #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.gtParams != null\">\n");
        buff.append("                <foreach collection=\"entity.gtParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} > #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.ltParams != null\">\n");
        buff.append("                <foreach collection=\"entity.ltParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} <![CDATA[<]]> #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.gteParams != null\">\n");
        buff.append("                <foreach collection=\"entity.gteParams\" item=\"item\" >\n");
        buff.append("                    ${item.relation} ${item.key} >= #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.lteParams != null\">\n");
        buff.append("                <foreach collection=\"entity.lteParams\" item=\"item\" >\n");
        buff.append("                    ${item.relation} ${item.key} <![CDATA[<=]]> #{item.value}\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.likeParams != null\">\n");
        buff.append("                <foreach collection=\"entity.likeParams\" item=\"item\" >\n");
        buff.append("                    ${item.relation} LOWER(${item.key}) LIKE LOWER(concat(concat('%',#{item.value}),'%'))\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.isNullParams != null\">\n");
        buff.append("                <foreach collection=\"entity.isNullParams\" item=\"item\" >\n");
        buff.append("                    ${item.relation} ${item.key} IS NULL\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.isNotNullParams != null\">\n");
        buff.append("                <foreach collection=\"entity.isNotNullParams\" item=\"item\" >\n");
        buff.append("                    ${item.relation} ${item.key} IS NOT NULL\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.inParams != null\">\n");
        buff.append("                <foreach collection=\"entity.inParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} IN (\n");
        buff.append("                    <foreach collection=\"item.value\" item=\"inItem\" separator=\",\">\n");
        buff.append("                        ${inItem}\n");
        buff.append("                    </foreach>)\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("            <if test=\"entity.notInParams != null\">\n");
        buff.append("                <foreach collection=\"entity.notInParams\" item=\"item\">\n");
        buff.append("                    ${item.relation} ${item.key} NOT IN (\n");
        buff.append("                    <foreach collection=\"item.value\" item=\"notInItem\" separator=\",\">\n");
        buff.append("                        ${notInItem}\n");
        buff.append("                    </foreach>)\n");
        buff.append("                </foreach>\n");
        buff.append("            </if>\n\n");
        buff.append("        </where>\n");
        buff.append("    </sql>\n");
    }


    /**
     * 获取基本数据类型，如果传入的不是包装类型，则不转，直接返回原类型的字符串形式
     *
     * @param fieldType
     * @return
     */
    private String getBasicTypeName(Class fieldType)
    {
        String fieldTypeName = fieldType.getName();
        if (fieldType.getName().contains("java.lang") || fieldType.getName().startsWith("["))
        {
            fieldTypeName = fieldType.getSimpleName();
        }

        String shortFieldName = MappingUtil.getBasicTypeName(fieldTypeName);
        fieldTypeName = shortFieldName == null ? fieldTypeName : shortFieldName;
        return fieldTypeName;
    }
}
