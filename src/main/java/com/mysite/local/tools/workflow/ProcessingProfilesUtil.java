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
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ProcessingProfilesUtil {

  static Set<String> getProcessingProfilePathsToApply(Resource assetResource) {
    return (new DamNodesIterator(assetResource)).toStream()
        .map(optResource ->
            optResource.map(resource -> resource.getChild(JcrConstants.JCR_CONTENT))
                .map(Resource::getValueMap)
                .map(jcrContentValueMap -> jcrContentValueMap.get("processingProfile"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
        )
        .flatMap(Optional::stream)
        .collect(Collectors.toSet());
  }

  private static class DamNodesIterator implements Iterator<Optional<Resource>> {

    private Optional<Resource> optResource;

    DamNodesIterator(Resource resource) {
      optResource = Optional.of(resource);
    }

    @Override
    public boolean hasNext() {
      return optResource.map(Resource::getPath)
          .map(path -> StringUtils.startsWith(path, "/content/dam/"))
          .orElse(false);
    }

    @Override
    public Optional<Resource> next() {
      optResource = optResource.map(Resource::getParent);
      return optResource;
    }

    Stream<Optional<Resource>> toStream() {
      return StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(this, Spliterator.ORDERED),
          false
      );
    }
  }

}
