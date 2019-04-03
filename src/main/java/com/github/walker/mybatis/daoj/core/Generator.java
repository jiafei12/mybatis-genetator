package com.github.walker.mybatis.daoj.core;


import com.github.walker.mybatis.daoj.utils.MappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class Generator {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void generator() {
        //BasicEntity类名
        String basicEntity = ConfigLoader.getBasicEntity();

        //BasicDao类名
        String basicDao = ConfigLoader.getBasicDao();

        //生成的实体类所在的包
        String entityPackage = ConfigLoader.getEntityPackage();

        //生成的DAO类所在的包
        String daoPackage = ConfigLoader.getDaoPackage();

        //代码生成的输出目录
        String outputDirName = ConfigLoader.getOutputDir();

        //需要合并的mapper所在目录
        String mergeFileDir = ConfigLoader.getMergeFileDir();

        //列出要生成的表名
        String[] tables = ConfigLoader.getTables();

        // 要生成的方法
        List<String> methods = ConfigLoader.getMethods();

        List<String> poIgnoreFields = ConfigLoader.getPoIgnoreFields();

        // 是否创建自增序列
        boolean createSequence = ConfigLoader.isCreateSequence();

        // 输出文件夹
        File outputDir = new File(outputDirName);
        outputDir.deleteOnExit();
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }


        File mergeDir = new File(mergeFileDir);
        if (!mergeDir.exists()) {
            mergeDir.mkdirs();
        }

        File poPackageDir = new File(outputDir, "po");
        poPackageDir.mkdirs();

        File daoPackageDir = new File(outputDir, "dao");
        daoPackageDir.mkdirs();

        File mapperPackageDir = new File(outputDir, "mapper");
        mapperPackageDir.mkdirs();


        for (int i = 0; i < tables.length; i++) {
            try {
                CodeBuilder codeBuilder = new CodeBuilder(tables[i]);

                //生成实体类
                String codeStr = codeBuilder.buildEntitySource(basicEntity, entityPackage, poIgnoreFields);
                this.createFile(codeStr, poPackageDir.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + ".java");

                //生成DAO类
                codeStr = codeBuilder.buildDaoSource(basicDao, daoPackage);
                this.createFile(codeStr, daoPackageDir.getCanonicalPath()
                        + File.separator + MappingUtil.getEntityName(tables[i]) + "Dao.java");

                //生成MAPPER
                String mapperFileName = MappingUtil.getEntityName(tables[i]) + "Mapper.xml";
                File mergeFile = new File(mergeDir, mapperFileName);
                codeStr = codeBuilder.buildMapperSource(daoPackage, methods, createSequence, mergeFile);
                this.createFile(codeStr, mapperPackageDir.getCanonicalPath()
                        + File.separator + mapperFileName);

                //复制DAO/VO基类
                InputStream is = Generator.class.getClassLoader().getResourceAsStream("BasicDao.java");
                File basicDaoFile = new File(outputDir, "BasicDao.java");
                this.writeToFile(is, basicDaoFile);

                is = Generator.class.getClassLoader().getResourceAsStream("BasicPo.java");
                File basicVoFile = new File(outputDir, "BasicPo.java");
                this.writeToFile(is, basicVoFile);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private void writeToFile(InputStream is, File file) throws Exception {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte[] bytes = new byte[5 * 1024];
            int len = 0;
            while ((len = is.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error("", e);
            throw e;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }


    /**
     * 将文本内容写入指定的文件
     *
     * @param fileContent
     * @param fileName
     */
    private void createFile(String fileContent, String fileName)
            throws IOException {
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
            osw.write(fileContent, 0, fileContent.length());
            osw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (osw != null) {
                osw.close();
            }
        }
    }
}
