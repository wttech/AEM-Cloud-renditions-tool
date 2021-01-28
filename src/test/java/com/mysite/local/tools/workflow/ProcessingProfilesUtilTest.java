/*
 * Copyright 2020 Wunderman Thompson Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysite.local.tools.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.Set;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(value = Lifecycle.PER_CLASS)
@ExtendWith(AemContextExtension.class)
class ProcessingProfilesUtilTest {

  private final AemContext context = new AemContext();

  @BeforeAll
  void setUp() {
    context.load().json("/contentSamples/dam.json", "/content");
  }

  @Test
  @DisplayName("Given valid DAM tree, When getProcessingProfilesToApply, Then return valid processingProfile paths")
  void testGetProcessingProfilePathsToApply() {
    Resource resource = context.resourceResolver().getResource("/content/dam/test/test2/test3/43.png");
    Set<String> actual = ProcessingProfilesUtil.getProcessingProfilePathsToApply(resource);

    assertEquals(Set.of("/conf/global/settings/dam/processing/profile-from-repo",
        "/conf/global/settings/dam/processing/profile-from-repo2"), actual);
  }
}
