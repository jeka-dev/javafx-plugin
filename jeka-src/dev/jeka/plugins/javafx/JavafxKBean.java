package dev.jeka.plugins.javafx;

import dev.jeka.core.api.depmanagement.*;
import dev.jeka.core.api.depmanagement.resolution.JkResolveResult;
import dev.jeka.core.api.depmanagement.resolution.JkResolvedDependencyNode;
import dev.jeka.core.api.file.JkPathSequence;
import dev.jeka.core.api.java.JkJavaVersion;
import dev.jeka.core.api.project.JkProject;
import dev.jeka.core.api.system.JkAnsi;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.*;
import dev.jeka.core.tool.builtins.project.ProjectKBean;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@JkDoc("Provides JavaFX support to Jeka projects.")
public class JavafxKBean extends KBean {

    private static final String JAVAFX_GROUP = "org.openjfx";

    private static final String JAVAFX_CLASSIFIER_TESTER_LIB = "org.openjfx:javafx-base:%s:25";

    @JkDoc("Version of JavaFX")
    @JkDepSuggest(hint = "org.openjfx:javafx-base:javafx-base:", versionOnly = true)
    public String version;

    @JkSuggest({"windows", "mac", "unix"})
    public String targetOs = getRunbase().getProperties().get(JkConstants.JEKA_PLATFORM_OS);

    @JkSuggest({"aarch64", "amd64", "x86", "arm", "arm64"})
    public String targetArch = System.getProperty("os.arch");

    @JkInject
    private ProjectKBean projectKBean;

    @JkPostInit
    private void postInit(ProjectKBean projectKBean) {
        JkProject project = projectKBean.project;
        JkRepoSet repos = project.dependencyResolver.getRepos();
        project.dependencyResolver.parameters.addResolveResultCustomizer(resolveResult ->
                              withJavafxClassifierDeps(repos, resolveResult)
        );

        // set java-modules
        JkPathSequence javafxModulePaths = javafxModulePath(project);
        project.jpmsModules.modulePathCustomizer.append(
                modulePaths -> modulePaths.addAll(javafxModulePaths.getEntries()));

        project.jpmsModules.addModulesCustomizer.append(
                moduleNames -> {
                    List<String> javafxModuleNames = javafxModuleNames(javafxModulePaths.getEntries());
                    moduleNames.addAll(javafxModuleNames);
                });

        project.runJavaOptionCustomizer.append(options -> enhanceOptions(options, project));
        project.compilation.dependencies.addVersionProvider(JkVersionProvider.of("org.openjfx:javafx-*", effectiveVersion()));
    }

    @JkDoc("Prints JVM module options needed to run the application")
    public void jvmOptions() {
        JkProject project = projectKBean.project;
        List<String> options = new ArrayList<>();
        String modulePath = project.jpmsModules.getModulePaths().toPath();
        if (!modulePath.isEmpty()) {
            options.add("--module-path");
            options.add(modulePath);
        }
        String addModules = String.join(",", project.jpmsModules.getAddModules());
        if (!addModules.isEmpty()) {
            options.add("--add-modules");
            options.add(addModules);
        }
        enhanceOptions(options, projectKBean.project);
        System.out.println(String.join(" ", options));
    }

    @JkDoc("Prints information about the JavaFX configuration")
    public void info() {
        System.out.println("JavaFX version       : " + effectiveVersion());
        System.out.println("JavaFX lib classifier: " + fullClassifier());
        System.out.printf("Execute %s to get the JVM module options.%n", JkAnsi.yellow("jeka javafx: jvmOptions"));
    }


    private String effectiveVersion() {
        if (JkUtilsString.isBlank(version)) {
            if (projectKBean.project.getJvmTargetVersion() != null) {
                return projectKBean.project.getJvmTargetVersion().toString();
            } else {
                return JkJavaVersion.ofCurrent().toString();
            }
        }
        return version;
    }

    private void enhanceOptions(List<String> options, JkProject project) {
        options.add("--enable-native-access=javafx.graphics");
    }

    private JkResolveResult withJavafxClassifierDeps(JkRepoSet repos, JkResolveResult resolveResult) {
        String classifier = fullClassifier();
        List<JkResolvedDependencyNode> addedNodes = resolveResult.getDependencyTree().toFlattenList().stream()
                .filter(JkResolvedDependencyNode::isModuleNode)
                .filter(depNode -> JAVAFX_GROUP.equals(depNode.getModuleNodeInfo().getModuleId().getGroup()))
                .map(depNode -> {
                    JkCoordinate platformCoordinate =
                            depNode.getModuleNodeInfo().getResolvedCoordinate().withClassifier(classifier);
                    Path file = JkCoordinateFileProxy.of(repos, platformCoordinate).get();
                    JkFileDependency fileDependency = JkFileSystemDependency.of(file);
                    Set<String> configurations = depNode.getModuleNodeInfo().getRootConfigurations();
                    JkResolvedDependencyNode.JkFileNodeInfo nodeInfo = JkResolvedDependencyNode.JkFileNodeInfo.of(
                            configurations, fileDependency);
                    return JkResolvedDependencyNode.ofFileDep(nodeInfo);

                })
                .collect(Collectors.toList());
        JkResolvedDependencyNode root = JkResolvedDependencyNode.ofRoot(addedNodes);
        JkResolvedDependencyNode mergedNode = resolveResult.getDependencyTree().withMerging(root);
        return JkResolveResult.of(mergedNode, resolveResult.getErrorReport());
    }

    private String fullClassifier() {
        String osClassifier = "Windows".equalsIgnoreCase(targetOs) ? "win" : targetOs;
        String osArchClassifier = osClassifier + "-" + targetArch;
        JkRepoSet repos = projectKBean.project.dependencyResolver.getRepos();
        JkCoordinateFileProxy fileProxy = JkCoordinateFileProxy.of(repos,
                        String.format(JAVAFX_CLASSIFIER_TESTER_LIB, osArchClassifier));
        return fileProxy.exists() ? osArchClassifier : osClassifier;
    }

    private JkPathSequence javafxModulePath(JkProject project) {
        JkResolveResult resolveResult = project.dependencyResolver.resolve(project.compilation.dependencies.get());
        JkRepoSet repos = project.dependencyResolver.getRepos();
        List<Path> paths = resolveResult.getInvolvedCoordinates().stream()
                .filter(coordinate -> JAVAFX_GROUP.equals(coordinate.getModuleId().getGroup()))
                .map(coordinate -> {
                    JkCoordinate platformCoordinate = coordinate.withClassifier(fullClassifier());
                    return JkCoordinateFileProxy.of(repos, platformCoordinate).get();
                })
                .collect(Collectors.toList());

        return JkPathSequence.of(paths);
    }

    private static List<String> javafxModuleNames(List<Path> javaFxJars) {
        return  javaFxJars.stream()
                .map(path -> path.getFileName().toString())
                .map(filename -> filename.split("-"))
                .filter(filenames -> "javafx".equals(filenames[0]))
                .map(filename -> filename[0] + "." + filename[1])
                .collect(Collectors.toList());
    }

    private static boolean needAarch(String os, String arch) {
        if ("windows".equalsIgnoreCase(os)) {
            return false;
        }
        return "aarch64".equals(arch);
    }

}
