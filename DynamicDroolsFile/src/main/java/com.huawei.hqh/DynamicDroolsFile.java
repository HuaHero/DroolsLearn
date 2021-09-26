package com.huawei.hqh;

import com.huawei.hqh.model.Person;

import org.drools.core.event.DefaultRuleRuntimeEventListener;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.KieContainer;

import org.apache.commons.io.FileUtils;
import org.kie.api.runtime.KieSession;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 通过验证及debug,确认可以通过外部文件及KieFileSystem和KieModuleModel生成内存文件kmodule.xml和规则文件，
 * 注意的是：KieFileSystem.write的一定是在资源文件(src/resources)下
 *
 */
public class DynamicDroolsFile
{
    private static final KieServices kieServices = KieServices.Factory.get();
    private static final String RESOURCES_PATH = "src" + File.separator +
                                                  "main" + File.separator +
                                                  "resources" + File.separator +
                                                  "rules";
    private static final String RULES_BASE_PATH = "src" + File.separator +
                                                  "main" + File.separator +
                                                  "config" + File.separator +
                                                  "rules";
    public static void main( String[] args ) throws IOException {



        KieFileSystem kieFileSystem = getKieFileSystem();

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        if (kieBuilder.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kieBuilder.getResults().toString());
        }
        ReleaseId releaseIdSame = kieServices.newReleaseId("com.huawei.healthplatform",
            "rule-engine", "2.1-RELEASE");
        KieContainer resultKieContainer = kieServices.newKieContainer(releaseIdSame);
        KieSession kieSession = resultKieContainer.getKieBase("achievement_rules").newKieSession();
   /*     kieSession.addEventListener(new DefaultRuleRuntimeEventListener(){
            public void ObjectInsertedEvent(ObjectInsertedEvent event) {
                Person person = (Person) event;
                String name = person.getName();
                System.out.println("name:" + name);
            }
        });*/
        Person person1 = new Person();
        person1.setName("hqh");
        person1.setAge(33);

        kieSession.insert(person1);

        int triggerdRules = kieSession.fireAllRules();
        System.out.println("triggerdRules:" + triggerdRules);
        kieSession.dispose();
        System.out.println( "Hello World!" );
    }

    private static KieFileSystem getKieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        ReleaseId releaseId = kieServices.newReleaseId("com.huawei.healthplatform",
            "rule-engine", "2.1-RELEASE");
        kieFileSystem.generateAndWritePomXML(releaseId);
        KieModuleModel kModuleModel = kieServices.newKieModuleModel();
        kModuleModel.newKieBaseModel("childcloud_rules")
            .addPackage("rules.huawei.childcloud")
            .newKieSessionModel("ksession-childcloud");
        kModuleModel.newKieBaseModel("achievement_rules")
            .addPackage("rules.huawei.achievement")
            .newKieSessionModel("ksession-achievement");
        kieFileSystem.writeKModuleXML(kModuleModel.toXML());

        for (String filePath : getRuleFiles(RULES_BASE_PATH)) {
            String drlContent = FileUtils.readFileToString(new File(filePath), "UTF-8");
            String[] filePathList = filePath.split("\\" + File.separator);
            String tmpFilePath = RESOURCES_PATH + File.separator + filePathList[filePathList.length - 1 - 2]
                                 + File.separator + filePathList[filePathList.length - 1 - 1] + File.separator
                                 + filePathList[filePathList.length - 1];
            kieFileSystem.write(tmpFilePath, drlContent);
        }
        return kieFileSystem;
    }

    private static List<String> getRuleFiles(String filePath) throws IOException {
        List<String> files = new ArrayList<>();
        File file = new File(filePath);
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File tmpFile:fileList) {
                if (tmpFile.isDirectory()) {
                    files.addAll(getRuleFiles(tmpFile.getCanonicalPath()));
                }
                if (tmpFile.isFile() && tmpFile.getName().endsWith(".drl")) {
                    files.add(tmpFile.getCanonicalPath());
                }
            }
        }
        return files;
    }
}
