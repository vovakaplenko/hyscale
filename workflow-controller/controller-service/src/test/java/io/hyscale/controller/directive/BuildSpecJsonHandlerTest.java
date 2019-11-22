/**
 * Copyright 2019 Pramati Prism, Inc.
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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.ControllerTestInitializer;
import io.hyscale.controller.directive.impl.BuildSpecJsonHandler;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringJUnitConfig(classes = ControllerTestInitializer.class)
public class BuildSpecJsonHandlerTest {

    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.image,
            HyscaleSpecFields.buildSpec);

    @Autowired
    private BuildSpecJsonHandler buildSpecHandler;

    private static ObjectNode serviceSpecNode = null;

    private static ServiceSpec oldServiceSpec = null;

    private static ServiceSpec updatedServiceSpec = null;

    @Test
    public void updateBuildSpec() {
        try {
            oldServiceSpec = ServiceSpecTestUtil.getServiceSpec("/servicespecs/buildSpec_update.hspec.yaml");
            serviceSpecNode = (ObjectNode) ServiceSpecTestUtil
                    .getServiceSpecJsonNode("/servicespecs/buildSpec_update.hspec.yaml");
            buildSpecHandler.update(serviceSpecNode);
            updatedServiceSpec = new ServiceSpec(serviceSpecNode);
        } catch (IOException | HyscaleException e) {
            fail();
        }
        BuildSpec oldBuildSpec = null;
        BuildSpec updatedBuildSpec = null;
        try {
            oldBuildSpec = oldServiceSpec.get(JSON_PATH, BuildSpec.class);
            updatedBuildSpec = updatedServiceSpec.get(JSON_PATH, BuildSpec.class);
        } catch (HyscaleException e) {
            fail();
        }

        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldBuildSpec.getConfigCommandsScript()),
                updatedBuildSpec.getConfigCommandsScript());
        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldBuildSpec.getRunCommandsScript()),
                updatedBuildSpec.getRunCommandsScript());

        for (Artifact updatedArtifact : updatedBuildSpec.getArtifacts()) {
            for (Artifact oldArtifact : oldBuildSpec.getArtifacts()) {
                if (updatedArtifact.getName().equals(oldArtifact.getName())) {
                    assertEquals(oldArtifact.getDestination(), updatedArtifact.getDestination());
                    if (oldArtifact.getSource().contains("\\")) {
                        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldArtifact.getSource()),
                                updatedArtifact.getSource());
                    } else {
                        assertEquals(oldArtifact.getSource(), updatedArtifact.getSource());
                    }
                }
            }
        }
    }

}
