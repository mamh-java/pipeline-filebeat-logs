/**
 * Licensed to Jenkins CI under one or more contributor license
 * agreements.  See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jenkins CI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.  You may obtain a copy of the
 * License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.jenkins.plugins.pipeline_filebeat_logs.input;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Implementation for the log input of filebeat.
 */
public class FileInput implements Input {

  @NonNull
  private final String filePath;

  public FileInput(@NonNull String filePath) {
    this.filePath = filePath;
  }

  @Override
  public void write(@NonNull String value) throws IOException {
    FileUtils.writeStringToFile(Paths.get(filePath).toFile(), value, "UTF-8", true);
  }
}
