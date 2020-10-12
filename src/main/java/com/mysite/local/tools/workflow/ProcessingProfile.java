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

import com.day.crx.JcrConstants;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Getter
@Builder
@EqualsAndHashCode
@ToString
public class ProcessingProfile {

  private final String name;

  private final List<Rendition> renditions;

  static ProcessingProfile fromResource(Resource resource) {
    return ProcessingProfile.builder()
        .name(resource.getName())
        .renditions(
            StreamSupport.stream(resource.getChildren().spliterator(), false)
                .filter(res -> !JcrConstants.JCR_CONTENT.equals(res.getName()))
                .map(res -> Optional.ofNullable(res.getChild(JcrConstants.JCR_CONTENT))
                    .map(jcrContent -> jcrContent.adaptTo(Rendition.class))
                )
                .flatMap(Optional::stream)
                .collect(Collectors.toList())
        )
        .build();
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @ToString
  @EqualsAndHashCode
  @Model(adaptables = Resource.class)
  public static class Rendition {

    @ValueMapValue
    private String fmt;

    @ValueMapValue
    private Long hei;

    @ValueMapValue
    private Long wid;

    @ValueMapValue
    private Long qlt;

    @ValueMapValue
    private String includeMimeTypes;

    @ValueMapValue
    @Named("jcr:title")
    private String title;

    @ValueMapValue
    private String name;

  }
}
