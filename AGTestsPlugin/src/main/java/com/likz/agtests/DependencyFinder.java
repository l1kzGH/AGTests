package com.likz.agtests;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DependencyFinder {

    static int dialog;

    private static final String[] DEPENDENCIES = {
            "org.junit.jupiter:junit-jupiter:5.10.0",
            "org.mockito:mockito-core:5.10.0",
            "org.mockito:mockito-junit-jupiter:5.10.0",
            //"org.testng:testng:7.10.0"
    };

    public static void updateDependencies(@NotNull Project project, TerminalAG terminal) {
        terminal.addToRightPanel("Checking test dependencies");
        terminal.pauseComponentRightPanel("Checking test dependencies");
        dialog = -1;


        VirtualFile pomFile = isMavenProject(project);
        if (pomFile != null) {
            System.out.println("Maven");
            PsiFile psiFile = PsiManager.getInstance(project).findFile(pomFile);
            if (psiFile instanceof XmlFile) {
                XmlFile xmlFile = (XmlFile) psiFile;
                updatePomFile(xmlFile, project);
            }
        } else {
            boolean isKts = false;
            VirtualFile gradleFile = isGradleProject(project);
            if (gradleFile == null) {
                isKts = true;
                gradleFile = isGradleKtsProject(project);
            }
            System.out.println("Gradle, Kts=" + isKts);
            updateGradleFile(gradleFile, project, isKts);
        }

        if (dialog == Messages.OK) {
            // Maven Invoker / Gradle Tooling
            dependenciesExecute();
        }

        if (dialog == Messages.CANCEL) {
            terminal.cancelComponentRightPanel("Checking test dependencies");
        } else {
            terminal.finishComponentRightPanel("Checking test dependencies");
        }
    }

    private static void updatePomFile(XmlFile pomFile, Project project) {
        XmlTag rootTag = pomFile.getRootTag();
        if (rootTag != null && "project".equals(rootTag.getName())) {
            XmlTag[] dependenciesTag = {rootTag.findFirstSubTag("dependencies")};
            if (dependenciesTag[0] == null) {
                dependenciesTag[0] = rootTag.createChildTag("dependencies", rootTag.getNamespace(), null, false);
                rootTag.addSubTag(dependenciesTag[0], false);
            }

            for (String dependency : DEPENDENCIES) {
                String[] parts = dependency.split(":");
                if (parts.length == 3) {
                    String groupId = parts[0];
                    String artifactId = parts[1];
                    String version = parts[2];

                    if (!dependencyExists(dependenciesTag[0], parts[0], parts[1])) {
                        if (dialog == -1)
                            dialog = Messages.showOkCancelDialog("Missing dependencies found for testing in the project\n" +
                                    "Add and load required dependencies?", "Maven info", Messages.getInformationIcon());

                        if (dialog == Messages.OK) {
                            XmlTag dependencyTag = dependenciesTag[0].createChildTag("dependency", dependenciesTag[0].getNamespace(), null, false);
                            dependencyTag.addSubTag(dependencyTag.createChildTag("groupId", dependencyTag.getNamespace(), groupId, false), false);
                            dependencyTag.addSubTag(dependencyTag.createChildTag("artifactId", dependencyTag.getNamespace(), artifactId, false), false);
                            dependencyTag.addSubTag(dependencyTag.createChildTag("version", dependencyTag.getNamespace(), version, false), false);

                            WriteCommandAction.runWriteCommandAction(project, () -> {
                                dependenciesTag[0].addSubTag(dependencyTag, false);
                            });
                        }
                    }
                }
            }

        }
    }

    private static boolean dependencyExists(XmlTag dependenciesTag, String groupId, String artifactId) {
        XmlTag[] dependencyTags = dependenciesTag.findSubTags("dependency");
        for (XmlTag dependencyTag : dependencyTags) {
            XmlTag groupIdTag = dependencyTag.findFirstSubTag("groupId");
            XmlTag artifactIdTag = dependencyTag.findFirstSubTag("artifactId");
            if (groupIdTag != null && artifactIdTag != null &&
                    groupId.equals(groupIdTag.getValue().getText()) &&
                    artifactId.equals(artifactIdTag.getValue().getText())) {
                return true;
            }
        }
        return false;
    }

    private static VirtualFile isMavenProject(Project project) {
        VirtualFile pomFile = project.getBaseDir().findChild("pom.xml");
        if (pomFile != null) {
            return pomFile;
        } else {
            return null;
        }
    }

    private static VirtualFile isGradleProject(Project project) {
        VirtualFile buildGradleFile = project.getBaseDir().findChild("build.gradle");
        if (buildGradleFile != null) {
            return buildGradleFile;
        }
        return null;
    }

    private static VirtualFile isGradleKtsProject(Project project) {
        VirtualFile buildGradleKtsFile = project.getBaseDir().findChild("build.gradle.kts");
        if (buildGradleKtsFile != null) {
            return buildGradleKtsFile;
        }
        return null;
    }

    private static void updateGradleFile(VirtualFile gradleFile, Project project, boolean isKts) {
        List<String> dependencies = extractDependencies(gradleFile.getPath());
        System.out.println(dependencies);

        for (String dependency : DEPENDENCIES) {
            String[] parts = dependency.split(":");
            if (parts.length == 3) {
                String groupId = parts[0];
                String artifactId = parts[1];
                String version = parts[2];

                if (!dependencies.stream().anyMatch(s -> s.contains(parts[0] + ":" + parts[1]))) {
                    if (dialog == -1)
                        dialog = Messages.showOkCancelDialog("Missing dependencies found for testing in the project\n" +
                                "Add and load required dependencies?", "Gradle info", Messages.getInformationIcon());

                    if (dialog == Messages.OK) {
                        String[] newDependency = new String[1];
                        if (isKts) {
                            newDependency[0] = "implementation(\"" + dependency + "\")";
                        } else {
                            newDependency[0] = "implementation '" + dependency + "'";
                        }

                        try {
                            List<String> lines = Files.readAllLines(Paths.get(gradleFile.getPath()));
                            List<String> modifiedLines = lines.stream()
                                    .map(line -> {
                                        if (line.trim().equals("dependencies {")) {
                                            return line + "\n    " + newDependency[0];
                                        } else {
                                            return line;
                                        }
                                    })
                                    .collect(Collectors.toList());

                            Files.write(Paths.get(gradleFile.getPath()), modifiedLines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

    }

    private static List<String> extractDependencies(String filePath) {
        try {
            List<String> dependencies = new ArrayList<>();
            String content = new String(Files.readAllBytes(Paths.get(filePath)));

            Pattern pattern = Pattern.compile("dependencies \\{([^}]*)\\}");
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String dependenciesBlock = matcher.group(1).trim();
                Pattern dependencyPattern = Pattern.compile("\"([^\"]+)\"");
                Matcher dependencyMatcher = dependencyPattern.matcher(dependenciesBlock);

                while (dependencyMatcher.find()) {
                    dependencies.add(dependencyMatcher.group(1));
                }
            }

            return dependencies;
        } catch (Exception e) {
            System.out.println("Some exc: " + e);
        }
        return null;
    }

    private static void dependenciesExecute() {
//        if (dialog == Messages.OK) {
//            //File pomFileTemp = new File(pomFile.getPath());
//            InvocationRequest request = new DefaultInvocationRequest();
//            //request.setPomFile(pomFileTemp);
//            // Goals to maven
//            request.setGoals(Arrays.asList("clean", "install"));
//            // Maven invoker
//            Invoker invoker = new DefaultInvoker();
//
//            try {
//                // Do execute request
//                invoker.execute(request);
//                System.out.println("Maven/Gradle проект успешно обновлен!");
//            } catch (MavenInvocationException e) {
//                System.out.println(e);
//            }
//        }
    }
}
