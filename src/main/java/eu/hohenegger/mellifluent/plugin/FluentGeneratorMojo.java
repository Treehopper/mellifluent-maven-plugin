/*-
 * #%L
 * mellifluent-maven-plugin
 * %%
 * Copyright (C) 2020 - 2021 Max Hohenegger <mellifluent@hohenegger.eu>
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
import static java.util.stream.Collectors.toList;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE_PLUS_RUNTIME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import eu.hohenegger.mellifluent.generator.FileWriter;
import eu.hohenegger.mellifluent.generator.FluentBuilderGenerator;
import eu.hohenegger.mellifluent.generator.GeneratorException;
import spoon.compiler.ModelBuildingException;

@Mojo(name = "generate-fluent", requiresDependencyCollection = COMPILE_PLUS_RUNTIME, threadSafe = true, defaultPhase = GENERATE_SOURCES)
public class FluentGeneratorMojo extends AbstractMojo {

    @Parameter(property = "sourcePackage", defaultValue = "")
    private String sourcePackage;

    @Parameter(property = "targetPackage", defaultValue = "")
    private String targetPackage;

    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    @Parameter(property = "srcRoot", defaultValue = "${project.build.sourceDirectory}")
    private String srcRoot;

    @Parameter(property = "outputPath", defaultValue = "${project.build.directory}/generated-sources/java/")
    private String outputPath;

    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    private List<String> classpath;

    @Inject
    private FluentBuilderGenerator builderGenerator;

    protected List<String> createClassPath() {
        return classpath.stream()
                .filter(cpel -> !cpel.contains("target"))
                .collect(toList());
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> classPath = createClassPath();
        if (classPath.isEmpty()) {
            getLog().error("Classpath is empty. Make sure to include the compile phase so that compile-time dependencies are available.");
            return;
        }

        if (skip) {
            getLog().debug("Execution skipped");
            return;
        }

        try {
            String sourcePackagePath = sourcePackage.replace('.', '/');

            Consumer<CharSequence> progressListener = new Consumer<>() {
                @Override
                public void accept(CharSequence string) {
                    getLog().info(string);
                }
            };
            try {
                builderGenerator.setup(Paths.get(srcRoot, sourcePackagePath),
                        Thread.currentThread().getContextClassLoader(),
                        classPath,
                        progressListener);
            } catch (ModelBuildingException e) {
                getLog().error("Error building source model with sourcePackage " + sourcePackage + " and classpath "
                        + StringUtils.join(classPath, ", "), e);
            }
            getLog().info("Processing... " + sourcePackage);
            builderGenerator.generate(sourcePackage);
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
        } catch (GeneratorException exception) {
            throw new MojoExecutionException("Builder Generation Exception", exception);
        }
    }
}
