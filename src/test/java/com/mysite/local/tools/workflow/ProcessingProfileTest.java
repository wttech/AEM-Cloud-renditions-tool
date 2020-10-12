/*
 * Copyright 2020 Cognifide
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

import com.mysite.local.tools.workflow.ProcessingProfile.Rendition;
import com.google.common.collect.Lists;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(value = Lifecycle.PER_CLASS)
@ExtendWith(AemContextExtension.class)
class ProcessingProfileTest {

  private final AemContext context = new AemContext();

  @BeforeAll
  void setUp() {
    context.load().json(
        "/contentSamples/processingProfiles.json",
        "/conf/global/settings/dam/processing"
    );
  }

  @Test
  @DisplayName("Given ProcessingProfile resource, When fromResource, Then return valid ProcessingProfile")
  void testFromResource() {
    Stream<Resource> input = Stream.of(
        "/conf/global/settings/dam/processing/profile-from-repo2",
        "/conf/global/settings/dam/processing/profile-from-repo"
    )
        .map(path -> context.resourceResolver().getResource(path));

    List<ProcessingProfile> actual = input.map(ProcessingProfile::fromResource)
        .collect(Collectors.toList());

    assertEquals(List.of(
        ProcessingProfile.builder()
            .name("profile-from-repo2")
            .renditions(
                List.of(
                    Rendition.builder()
                        .name("sample.with.dots2.jpeg")
                        .title("sample.with.dots")
                        .includeMimeTypes("image/.*")
                        .fmt("jpeg")
                        .hei(16002L)
                        .qlt(85L)
                        .wid(16002L)
                        .build(),
                    Rendition.builder()
                        .name("medium2.jpeg")
                        .title("medium")
                        .includeMimeTypes("image/.*")
                        .fmt("jpeg")
                        .hei(8002L)
                        .qlt(85L)
                        .wid(8002L)
                        .build()
                )
            ).build(),
        ProcessingProfile.builder()
            .name("profile-from-repo")
            .renditions(
                List.of(
                    Rendition.builder()
                        .name("sample.with.dots.jpeg")
                        .title("sample.with.dots")
                        .includeMimeTypes("image/.*")
                        .fmt("jpeg")
                        .hei(1600L)
                        .qlt(85L)
                        .wid(1600L)
                        .build(),
                    Rendition.builder()
                        .name("medium.jpeg")
                        .title("medium")
                        .includeMimeTypes("image/.*")
                        .fmt("jpeg")
                        .hei(800L)
                        .qlt(85L)
                        .wid(800L)
                        .build()
                )
            ).build()
    ), actual);
  }

}
