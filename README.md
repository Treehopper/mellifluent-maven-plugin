# mellifluent
Maven Plugin for generating Builders with fluent API

![Travis CI](https://travis-ci.com/Treehopper/mellifluent-maven-plugin.svg?token=qKxxqzyP8RDjwJq45sxG&branch=develop "Build Status")

## How to use
```xml
<build>
	<plugins>
		<plugin>
			<groupId>eu.hohenegger.mellifluent</groupId>
			<artifactId>mellifluent-maven-plugin</artifactId>
			<executions>
				<execution>
					<goals>
						<goal>generate-fluent</goal>
					</goals>
					<configuration>
						<packageName>external.library.package</packageName>
						<targetPackage>eu.hohenegger.mellifluent.generator.todo</targetPackage>
					</configuration>
				</execution>
			</executions>
		</plugin>
		<plugin>
			<groupId>org.codehaus.mojo</groupId>
			<artifactId>build-helper-maven-plugin</artifactId>
			<version>3.2.0</version>
			<executions>
				<execution>
					<id>add-source</id>
					<phase>process-sources</phase>
					<goals>
						<goal>add-source</goal>
					</goals>
					<configuration>
						<sources>
							<source>${project.build.directory}/generated-sources/java</source>
						</sources>
					</configuration>
				</execution>
			</executions>
		</plugin>
		<plugin>
			<groupId>org.apache.maven.plugins</groupId>
			<artifactId>maven-compiler-plugin</artifactId>
			<version>3.8.1</version>
		</plugin>
	</plugins>
</build>

```
