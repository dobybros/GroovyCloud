package utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Grapes2PomUtils {
    private static final String path = "/Users/huzhanjing/workspace/ACServers";
    private static final String flag = "\r\n";

    public static void main(String[] args) {
        File file = new File(path);
        File[] files = file.listFiles();
        boolean containServices = true;
        for (File file1 : files){
            if(file1.getName().equals("src")){
                containServices = false;
                break;
            }
        }
        if (!containServices){
            writeToPom(file);
        }else {
            for (File file1 : files){
                if(file1.isDirectory() && !file1.getName().equals("target")){
                    writeToPom(file1);
                }
            }
        }
    }

    private static void writeToPom(File file) {
        try {
            List list = getGrapesObject(file);
            if(list != null && !list.isEmpty()){
                File tempFile = File.createTempFile("temp", ".temp", file);
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                FileInputStream inputStream = new FileInputStream(tempFile);
                tempFile.deleteOnExit();
                File pomFile = new File(file.getAbsolutePath() + "/pom.xml");
                String content = FileUtils.readFileToString(pomFile, Charset.defaultCharset());
                int index = content.indexOf("<dependencies>");
                int position = 0;
                StringBuilder stringBuilder = new StringBuilder();
                String contents = null;
                if (index != -1) {
                    position = index + "<dependencies>".length() + 1;
                    stringBuilder = contacePom(list, stringBuilder);
                } else {
                    position = content.indexOf("</project>");
                    stringBuilder.append("<dependencies>" + flag);
                    stringBuilder = contacePom(list, stringBuilder);
                    stringBuilder.append("</dependencies>" + flag);
                }
                contents = stringBuilder.toString();
                RandomAccessFile rw = new RandomAccessFile(pomFile, "rw");
                rw.seek(position);
                int tmp = 0;
                while ((tmp = rw.read()) != -1) {
                    outputStream.write(tmp);
                }
                rw.seek(position);
                rw.write(contents.getBytes());
                while ((tmp = inputStream.read()) != -1) {
                    rw.write(tmp);
                }
                rw.close();
                outputStream.close();
                inputStream.close();
            }
        } catch (Throwable t) {
            ExceptionUtils.getFullStackTrace(t);
        }
    }

    private static List<GrpesObject> getGrapesObject(File file) {
        try {
            String configDirectPath = "/src/main/groovy/config";
            File configDirectFile = new File(file.getAbsolutePath() + configDirectPath);
            List<GrpesObject> list = null;
            if (configDirectFile.exists()) {
                File configFile = new File(configDirectFile + "/imports.groovy");
                if (configFile.exists()) {
                    String grapesConfig = FileUtils.readFileToString(configFile, Charset.defaultCharset());
                    if (grapesConfig != null) {
                        String[] grabs = grapesConfig.replaceAll(" ", "").split("@Grab");
                        list = new ArrayList<GrpesObject>();
                        for (int i = 0; i < grabs.length; i++) {
                            if (grabs[i].startsWith("(group")) {
                                GrpesObject grpesObject = new GrpesObject();
                                String grab = grabs[i];
                                String[] strings = grab.split("'");
                                grpesObject.setGroup(strings[1]);
                                grpesObject.setModule(strings[3]);
                                grpesObject.setVersion(strings[5]);
                                list.add(grpesObject);
                            }
                        }

                    }
                }
                FileUtils.deleteQuietly(configDirectFile);
                return list;
            }
        } catch (Throwable t) {
            ExceptionUtils.getFullStackTrace(t);
        }
        return null;
    }

    private static StringBuilder contacePom(List<GrpesObject> list, StringBuilder pomString) {
        pomString.append("<!--GroovyGrapesStart!!!CantDelete-->" + flag);
        for (GrpesObject grpesObject : list) {
            pomString.append("<dependency>" + flag);
            pomString.append("<groupId>").append(grpesObject.getGroup()).append("</groupId>" + flag);
            pomString.append("<artifactId>").append(grpesObject.getModule()).append("</artifactId>" + flag);
            pomString.append("<version>").append(grpesObject.getVersion()).append("</version>" + flag);
            pomString.append("</dependency>" + flag);
        }
        pomString.append("<!--GroovyGrapesEnd!!!CantDelete-->" + flag);
        return pomString;
    }

    private static class GrpesObject {
        private String group;
        private String module;
        private String version;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getModule() {
            return module;
        }

        public void setModule(String module) {
            this.module = module;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
