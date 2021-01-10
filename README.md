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
			<version>1.0.0-SNAPSHOT</version>
			<executions>
				<execution>
					<goals>
						<goal>generate-fluent</goal>
					</goals>
					<configuration>
						<skip>false</skip>
						<sourcePackage>eu.hohenegger.mellifluent.generator.model</sourcePackage>
						<targetPackage>eu.hohenegger.mellifluent.generator.todo</targetPackage>
					</configuration>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>

```
