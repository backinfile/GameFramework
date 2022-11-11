package com.backinfile.GameFramework.gen;

import com.backinfile.GameFramework.LogCore;
import com.backinfile.support.SysException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FreeMarkerUtils {
    /**
     * 读取模板文件生成文件
     */
    public static void genFile(String filePath, String fileName, Map<String, Object> rootMap, String outPath,
                               String outFileName) {
        try {
            Configuration config = new Configuration(Configuration.VERSION_2_3_22);
            config.setDefaultEncoding("UTF-8");
            config.setDirectoryForTemplateLoading(new File(filePath));
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            genFileWithConfig(config, fileName, rootMap, new File(outPath, outFileName));
        } catch (Exception e) {
            throw new SysException(e);
        }
    }

    /**
     * 读取工程内的模板文件生成文件
     */
    public static void genFile(ClassLoader classLoader, String templatePath, String fileName,
                               Map<String, Object> rootMap, String outPath, String outFileName) {
        try {
            Configuration config = new Configuration(Configuration.VERSION_2_3_22);
            config.setDefaultEncoding("UTF-8");
            config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            config.setClassLoaderForTemplateLoading(classLoader, templatePath);

            genFileWithConfig(config, fileName, rootMap, new File(outPath, outFileName));
        } catch (Exception e) {
            throw new SysException(e);
        }
    }

    private static void genFileWithConfig(Configuration config, String fileName, Map<String, Object> rootMap,
                                          File file) throws Exception {
        LogCore.gen.info("start gen {}", file.getPath());
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new SysException("创建文件夹失败" + file.getParentFile().getAbsolutePath());
            }
        }
        try (Writer writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            Template template = config.getTemplate(fileName, "UTF-8");
            template.process(rootMap, writer);
        }
        LogCore.gen.info("gen {} success\n", file.getAbsolutePath());
    }


    /**
     * 读取项目内资源文件
     */
    public static List<String> readResource(ClassLoader classLoader, String resourceFile) {
        List<String> result = new ArrayList<>();
        InputStream in = classLoader.getResourceAsStream(resourceFile);
        assert in != null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        while (true) {
            String line = null;
            try {
                line = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line == null) {
                break;
            }
            result.add(line);
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
