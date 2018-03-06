/**
 * Copyright (c) 2016 eBay Software Foundation. All rights reserved.
 * <p>
 * Licensed under the MIT license.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * <p>
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package shaoxt.pipeline.orchestration.tasks;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodTemplate;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.client.dsl.*;
import org.apache.commons.io.FileUtils;
import org.ebayopensource.winder.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * TODO a simple runner for demo
 * TODO The runtime related information should be in Application or ApplicationService
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class Run extends BasePipelineTask {
    @Override
    protected TaskState doExecute(TaskContext<TaskInput, TaskResult> ctx, TaskInput input, TaskResult result) throws Exception {
        File workDir = getWorkDir(result);
        //TODO hardcode
        File runFile = new File(workDir, "run.yaml");
        if (runFile.exists()) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(runFile))) {
                ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata,Boolean> list = kubeClient.getKubernetesClient().load(bis);
                Applicable<List<HasMetadata>> objects = list.deletingExisting();
                for(HasMetadata hasMetadata : objects.createOrReplace()) {
                    if (hasMetadata instanceof Deployment) {
                        kubeClient.getKubernetesClient().extensions().deployments().createOrReplace((Deployment)hasMetadata);
                    }
                    else if (hasMetadata instanceof Service) {
                        kubeClient.getKubernetesClient().services().createOrReplace((Service)hasMetadata);
                    }
                    else if (hasMetadata instanceof Pod) {
                        kubeClient.getKubernetesClient().pods().createOrReplace((Pod)hasMetadata);
                    }
                    else if (hasMetadata instanceof ReplicaSet) {
                        kubeClient.getKubernetesClient().extensions().replicaSets().createOrReplace((ReplicaSet)hasMetadata);
                    }
                }
                ctx.getJobContext().addUpdate(StatusEnum.COMPLETED, "Run image on k8s");
            }

        }
        else {
            ctx.getJobContext().addUpdate(StatusEnum.COMPLETED, "No run.yaml");
        }
        FileUtils.cleanDirectory(workDir);
        return TaskState.COMPLETED;
    }
}
