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

import com.adobe.cq.gfx.Gfx;
import com.adobe.cq.gfx.Instructions;
import com.adobe.cq.gfx.Plan;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.Rendition;
import com.day.cq.dam.api.renditions.RenditionMaker;
import com.day.cq.dam.api.renditions.RenditionTemplate;
import com.day.crx.JcrConstants;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.mime.MimeTypeService;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Create renditions as AEM as a Cloud Asset microservices would create.
 */
@Slf4j
@Component(property = {
    Constants.SERVICE_DESCRIPTION + "=Generate renditions as in Cloud",
    Constants.SERVICE_VENDOR + "=Cognifide",
    "process.label" + "=Generate Cloud renditions"})
public class LocalRenditionMakerProcess implements WorkflowProcess {

  @Reference
  private Gfx gfx;

  @Reference
  private RenditionMaker renditionMaker;

  @Reference
  private MimeTypeService mimeTypeService;

  @Override
  public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) {
    String assetPath = WorkflowUtil.getAssetPath(workItem);

    if (StringUtils.isNotBlank(assetPath)) {
      log.debug("Starting rendition processing for {}", assetPath);
      ResourceResolver resourceResolver = Objects.requireNonNull(workflowSession.adaptTo(ResourceResolver.class));
      Resource assetResource = resourceResolver.getResource(assetPath);

      if (assetResource != null) {
        Set<String> processingProfilePaths = ProcessingProfilesUtil.getProcessingProfilePathsToApply(assetResource);
        processingProfilePaths.stream()
            .map(resourceResolver::getResource)
            .filter(Objects::nonNull)
            .map(ProcessingProfile::fromResource)
            .forEach(processingProfile -> processProfile(assetResource, processingProfile, resourceResolver));
      } else {
        log.error("Resource {} does not exist.", assetPath);
      }
    } else {
      log.error("Could not obtain path of the asset to process");
    }
  }

  private void processProfile(Resource assetResource, ProcessingProfile processingProfile,
      ResourceResolver resourceResolver) {
    Asset asset = assetResource.adaptTo(Asset.class);

    processingProfile.getRenditions()
        .forEach(rendition -> {
          log.debug("Profile ({}) Starting.", processingProfile.getName());
          processRendition(processingProfile.getName(), asset, rendition, resourceResolver);
          log.debug("Profile ({}) Done.", processingProfile.getName());
        });
  }

  private void processRendition(String profileName, Asset asset, ProcessingProfile.Rendition rendition,
      ResourceResolver resourceResolver) {
    log.debug("Profile ({}) Rendition ({}) Obtaining config.", profileName, rendition.getName());
    NamedRenditionTemplate renditionTemplate = createRenditionTemplate(asset,
        rendition.getName(),
        rendition.getWid().intValue(),
        rendition.getHei().intValue(),
        rendition.getQlt().intValue()
    );
    log.debug("Profile ({}) Rendition ({}) Starting.", profileName, rendition.getName());
    renditionMaker.generateRenditions(asset, renditionTemplate);
    log.debug("Profile ({}) Rendition ({}) Rendition created.", profileName, rendition.getName());
    String renditionPath = asset.getPath() + "/jcr:content/renditions/" + renditionTemplate.renditionName;
    updateRenditionMetadata(renditionPath, resourceResolver);
    log.debug("Profile ({}) Rendition ({}) Done.", profileName, rendition.getName());
  }

  private void updateRenditionMetadata(String renditionPath, ResourceResolver resourceResolver) {
    Resource renditionResource = resourceResolver.getResource(renditionPath);
    if (renditionResource != null) {
      Optional<Dimension> optDimension = WorkflowUtil.getRenditionSize(renditionResource.adaptTo(Rendition.class));
      if (optDimension.isPresent()) {
        Dimension dimension = optDimension.get();
        try {
          Node renditionNode = Objects.requireNonNull(renditionResource.adaptTo(Node.class));
          Node jcrContent = renditionNode.getNode(JcrConstants.JCR_CONTENT);
          jcrContent.addMixin("dam:Metadata");

          Node metadata = jcrContent.addNode("metadata", JcrConstants.NT_UNSTRUCTURED);
          metadata.setProperty("tiff:ImageWidth", dimension.width);
          metadata.setProperty("tiff:ImageLength", dimension.height);
          resourceResolver.commit();
        } catch (RepositoryException | PersistenceException e) {
          log.error("Error while updating metadata for rendition.", e);
        }
      } else {
        log.error("Could not obtain dimensions for created rendition {}", renditionPath);
      }
    } else {
      log.error("Could not obtain resource for created rendition {}", renditionPath);
    }
  }

  private NamedRenditionTemplate createRenditionTemplate(Asset asset, String renditionName, int width,
      int height, int quality) {
    Plan plan = gfx.createPlan();
    plan.layer(0).set("src", asset.getPath());
    NamedRenditionTemplate template = NamedRenditionTemplate.builder()
        .gfx(gfx)
        .mimeType(mimeTypeService.getMimeType(renditionName))
        .renditionName(renditionName)
        .plan(plan)
        .build();

    Instructions instructions = plan.view();
    instructions.set("wid", width);
    instructions.set("hei", height);
    instructions.set("fit", "constrain,0");
    instructions.set("rszfast", quality <= 90);
    if (StringUtils.equalsAny(template.getMimeType(), "image/jpg", "image/jpeg")) {
      instructions.set("qlt", quality);
    } else if ("image/gif".equals(template.getMimeType())) {
      instructions.set("quantize", "adaptive,diffuse," + quality);
    }
    String fmt = StringUtils.substringAfter(template.getMimeType(), "/");
    if (StringUtils.equalsAny(fmt, "png", "gif", "tif")) {
      fmt += "-alpha";
    }
    instructions.set("fmt", fmt);

    return template;
  }

  @Getter
  @Builder
  private static class NamedRenditionTemplate implements RenditionTemplate {

    private Plan plan;

    private String renditionName;

    private String mimeType;

    private Gfx gfx;

    @Override
    public Rendition apply(Asset asset) {
      return Optional.ofNullable(asset.adaptTo(Resource.class))
          .map(Resource::getResourceResolver)
          .map(resourceResolver -> {
            Rendition rendition = null;
            try (InputStream stream = gfx.render(this.plan, resourceResolver)) {
              if (stream != null) {
                rendition = asset.addRendition(this.renditionName, stream, this.mimeType);
              }
            } catch (IOException e) {
              log.error("Exception occurred while generating the renditon.", e);
            }
            return rendition;
          })
          .orElse(null);
    }
  }
}
