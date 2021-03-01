/*-
 * #%L
 * mellifluent-maven-plugin
 * %%
 * Copyright (C) 2021 Max Hohenegger <mellifluent@hohenegger.eu>
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.hohenegger.mellifluent.plugin;

import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.resolvers.ResolveDependenciesMojo;
import org.apache.maven.plugins.dependency.utils.DependencyStatusSets;

import eu.hohenegger.mellifluent.generator.FileWriter;
import eu.hohenegger.mellifluent.generator.GeneratorException;
import eu.hohenegger.mellifluent.generator.UnassistedFluentBuilderGenerator;

@Mojo(name = "generate-fluent", requiresDependencyCollection = ResolutionScope.COMPILE, threadSafe = true,
        defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class UnassistedGeneratorMojo extends ResolveDependenciesMojo {

    @Parameter(property = "packageName", defaultValue = "eu.hohenegger.targetpackage")
    private String packageName;

    @Parameter(property = "outputPath", defaultValue = "${project.build.directory}/generated-sources/java")
    private String outputPath;

    @Parameter(property = "targetPackage", defaultValue = "")
    private String targetPackage;

    private boolean checkPackages = true;

    @Override
    protected void doExecute() throws MojoExecutionException {
        this.classifier = "sources";

        super.doExecute();

        executeDoSomething(getDependencySets(false, false));
    }

    void executeDoSomething(DependencyStatusSets dependencyStatusSets) throws MojoExecutionException {
        Set<Artifact> resolvedArtifactSet = dependencyStatusSets.getResolvedDependencies();

        doSomething(resolvedArtifactSet);
    }

    private void doSomething(Set<Artifact> filteredArtifactSet) throws MojoExecutionException {
        List<File> jarFiles = filteredArtifactSet.stream().map(Artifact::getFile).collect(toList());
        UnassistedFluentBuilderGenerator builderGenerator = new UnassistedFluentBuilderGenerator<>();
        builderGenerator.setup(jarFiles, new Consumer<CharSequence>() {
            @Override
            public void accept(CharSequence string) {
                getLog().info(string);
            }
        });

        getLog().info("Processing... " + jarFiles.stream().map(File::getName).collect(joining(",")));
        try {
            builderGenerator.generate(packageName);
        } catch (GeneratorException exception) {
            throw new MojoExecutionException("Builder Generation Exception", exception);
        }
        Path outputFolder = Paths.get(outputPath);

        if (!exists(outputFolder) || !isDirectory(outputFolder)) {
            getLog().info("Output path " + outputPath + " does not exist. Creating...");
            try {
                Files.createDirectories(outputFolder);
            } catch (IOException e) {
                getLog().error(e);
            }

            getLog().info("Created " + outputPath);
        }
        FileWriter writer = new FileWriter(builderGenerator, outputPath, targetPackage, true);

        writer.persist();
    }

    private boolean containsPackage(Artifact artifact, String packageName) throws MojoExecutionException {
        try (JarFile jarFile = new JarFile(artifact.getFile())) {
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                getLog().debug(" - " + jarEntry.getName());
                if (jarEntry.getName().startsWith(packageName)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("IOException: Group ID: " + artifact.getGroupId()
                    + " Artifact ID: " + artifact.getArtifactId(), e);
        }

        return false;
    }

    boolean isCheckPackages() {
        return checkPackages;
    }

    void setCheckPackages(boolean checkPackages) {
        this.checkPackages = checkPackages;
    }
}
