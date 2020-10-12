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

import com.adobe.granite.workflow.exec.WorkItem;
import com.day.cq.dam.api.Rendition;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.jcr.RepositoryException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class WorkflowUtil {

  static String getAssetPath(WorkItem workItem) {
    return Optional.ofNullable(workItem.getWorkflowData().getPayload())
        .filter(String.class::isInstance)
        .map(String.class::cast)
        // there are cases when the path points to /jcr:content/renditions/original
        .map(path -> StringUtils.substringBefore(path, "/jcr:content"))
        .orElse(StringUtils.EMPTY);
  }

  static Optional<Dimension> getRenditionSize(Rendition rendition) {
    return Optional.ofNullable(rendition)
        .map(rend -> {
          Dimension dimension = null;
          try {
            BufferedImage image = ImageIO.read(rend.getBinary().getStream());
            dimension = new Dimension(image.getWidth(), image.getHeight());
          } catch (IOException | RepositoryException e) {
            log.error("Error occurred while reading the rendition.", e);
          }
          return dimension;
        });
  }

}
