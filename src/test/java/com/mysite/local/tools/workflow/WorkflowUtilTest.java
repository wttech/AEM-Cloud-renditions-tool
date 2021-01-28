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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.day.cq.dam.api.Rendition;
import java.awt.Dimension;
import java.util.stream.Stream;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WorkflowUtilTest {

  private WorkItem workItem;

  private WorkflowData workflowData;

  @BeforeEach
  void setUp() {
    workItem = mock(WorkItem.class);
    workflowData = mock(WorkflowData.class);
    when(workItem.getWorkflowData()).thenReturn(workflowData);
  }

  @Test
  @DisplayName("Given valid path to Asset, When getAssetPath, Then return valid asset path")
  void testGetAssetPathWithValidPath() {
    when(workflowData.getPayload()).thenReturn("/content/dam/someAsset.with.dots.png");

    String actual = WorkflowUtil.getAssetPath(workItem);

    assertEquals("/content/dam/someAsset.with.dots.png", actual);
  }

  @Test
  @DisplayName("Given null as path to Asset, When getAssetPath, Then return empty string")
  void testGetAssetPathWithNull() {
    when(workflowData.getPayload()).thenReturn(null);

    String actual = WorkflowUtil.getAssetPath(workItem);

    assertEquals(StringUtils.EMPTY, actual);
  }

  @Test
  @DisplayName("Given path to Asset original rendition, When getAssetPath, Then return valid asset path")
  void testGetAssetPathWithOriginalRenditionPath() {
    when(workflowData.getPayload()).thenReturn("/content/dam/test/test2/test3/43.png/jcr:content/renditions/original");

    String actual = WorkflowUtil.getAssetPath(workItem);

    assertEquals("/content/dam/test/test2/test3/43.png", actual);
  }

  @Test
  @DisplayName("Given images inputStream, When getRenditionSize, Then return valid image dimensions")
  void testGetRenditionSize() {
    Rendition rendition = mock(Rendition.class);
    Binary binary = mock(Binary.class);
    when(rendition.getBinary()).thenReturn(binary);

    Stream.of(
        WorkflowUtilTest.class.getResourceAsStream("/images/testImage.bmp"),
        WorkflowUtilTest.class.getResourceAsStream("/images/testImage.png"),
        WorkflowUtilTest.class.getResourceAsStream("/images/testImage.jpg")
    )
        .map(inputStream -> {
          try {
            when(binary.getStream()).thenReturn(inputStream);
          } catch (RepositoryException e) {
            fail("Exception occurred.");
          }

          return WorkflowUtil.getRenditionSize(rendition);
        })
        .forEach(optDimension -> {
          assertTrue(optDimension.isPresent());
          Dimension dimension = optDimension.get();
          assertEquals(4, dimension.width);
          assertEquals(6, dimension.height);
        });
  }

}
