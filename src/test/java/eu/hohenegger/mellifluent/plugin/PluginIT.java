/*-
 * #%L
 * mellifluent-maven-plugin
 * %%
 * Copyright (C) 2020 - 2022 Max Hohenegger <mellifluent@hohenegger.eu>
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

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.ERRORS;
import static com.soebes.itf.jupiter.extension.MavenCLIOptions.NO_TRANSFER_PROGRESS;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

@MavenJupiterExtension
public class PluginIT {

  @MavenTest
  @MavenOption(NO_TRANSFER_PROGRESS)
  @MavenOption(ERRORS)
  void spoon(MavenExecutionResult result) {
    assertThat(result).isSuccessful();
    // assertThat(result)
    //     .out()
    //     .info()
    //     .contains("--- maven-checkstyle-plugin:3.1.2:check (default) @ bar ---");
  }
}
