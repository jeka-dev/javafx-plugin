package dev.jeka.plugins.javafx;

import dev.jeka.core.api.depmanagement.*;
import dev.jeka.core.api.depmanagement.resolution.JkResolveResult;
import dev.jeka.core.api.depmanagement.resolution.JkResolvedDependencyNode;
import dev.jeka.core.api.file.JkPathSequence;
import dev.jeka.core.api.java.JkJavaVersion;
import dev.jeka.core.api.project.JkProject;
import dev.jeka.core.api.utils.JkUtilsString;
import dev.jeka.core.tool.*;
import dev.jeka.core.tool.builtins.project.ProjectKBean;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class JavafxKBean extends KBean {

    private static final String JAVAFX_GROUP = "org.openjfx";

    @JkDoc("Version of JavaFX")
    @JkDepSuggest(hint = "org.openjfx:javafx-base:javafx-base:", versionOnly = true)
    public String version;

    @JkSuggest({"windows", "mac", "unix"})
    public String targetOs = getRunbase().getProperties().get(JkConstants.JEKA_PLATFORM_OS);

    @JkSuggest({"aarch64", "amd64", "x86", "arm", "arm64"})
    public String targetArch = System.getProperty("os.arch");

    @JkInject
    private ProjectKBean projectKBean;

    @Override
    protected void init() {
        if (JkUtilsString.isBlank(version)) {
            if (projectKBean.project.getJvmTargetVersion() != null) {
                version = projectKBean.project.getJvmTargetVersion().toString();
            } else {
                version = JkJavaVersion.ofCurrent().toString();
            }
        }
    }

    @JkPostInit
    private void postInit(ProjectKBean projectKBean) {
        JkProject project = projectKBean.project;
        JkRepoSet repos = project.dependencyResolver.getRepos();
        String osClassifier = osClassifier(targetOs);
        project.dependencyResolver.parameters.addResolveResultCustomizer(resolveResult ->
                              withJavafxClassifierDeps(repos, resolveResult, osClassifier)
        );
        project.runJavaOptionCustomizer.append(options -> enhanceOptions(options, project));
        project.compilation.dependencies.addVersionProvider(JkVersionProvider.of("org.openjfx:javafx-*", version));
    }

    @JkDoc("Prints JVM module options needed to run the application")
    public void jvmOptions() {
        List<String> options = new ArrayList<>();
        enhanceOptions(options, projectKBean.project);
        System.out.println(String.join(" ", options));
    }

    private void enhanceOptions(List<String> options, JkProject project) {
        options.add("--module-path");
        JkPathSequence javafxModulePaths = javafxModulePath(project);
        options.add(javafxModulePaths.toPath());
        options.add("--add-modules");
        List<String> javafxModuleNames = javafxModuleNames(javafxModulePaths.getEntries());
        options.add(String.join(",", javafxModuleNames));
        options.add("--enable-native-access=javafx.graphics");
    }

    private JkResolveResult withJavafxClassifierDeps(JkRepoSet repos, JkResolveResult resolveResult, String classifier) {
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

    private static String osClassifier(String os) {
        return "Windows".equalsIgnoreCase(os) ? "win" : os;
    }

    private String fullClassifier() {
        return osClassifier(targetOs) + "-" + targetArch;
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




}
