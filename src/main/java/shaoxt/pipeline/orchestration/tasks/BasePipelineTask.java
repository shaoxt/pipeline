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

import org.ebayopensource.common.config.InjectProperty;
import org.ebayopensource.winder.*;
import shaoxt.pipeline.client.KubeClient;
import shaoxt.pipeline.crds.Application;
import shaoxt.pipeline.crds.Pipeline;
import shaoxt.pipeline.crds.StageName;
import shaoxt.pipeline.crds.Template;

import java.io.File;

import static shaoxt.pipeline.orchestration.PipelineConstants.APPLICATION;
import static shaoxt.pipeline.orchestration.PipelineConstants.PIPELINE;
import static shaoxt.pipeline.orchestration.PipelineConstants.WORK_DIR;

/**
 * Base Task
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public abstract class BasePipelineTask implements Task<TaskInput, TaskResult> {

    @InjectProperty(name = "shaoxt.pipeline.client.KubeClient")
    protected KubeClient kubeClient;

    public KubeClient getKubeClient() {
        return kubeClient;
    }

    public void setKubeClient(KubeClient kubeClient) {
        this.kubeClient = kubeClient;
    }

    @Override
    public final TaskState execute(TaskContext<TaskInput, TaskResult> ctx, TaskInput input, TaskResult result) throws Exception {
        String stage = getClass().getSimpleName();
        String pipelineName = input.getString(PIPELINE);
        Pipeline pipeline = kubeClient.getPipeline(pipelineName);
        if (pipeline != null) {
            pipeline.getSpec().setCurrentStage(StageName.valueOf(stage));
        }

        TaskState state = doExecute(ctx, input, result);
        if (pipeline != null) {
            //TODO add updating messages in kube
            kubeClient.getPipelineOperation().createOrReplace(pipeline);
        }
        return state;
    }

    protected abstract TaskState doExecute(TaskContext<TaskInput, TaskResult> ctx, TaskInput input, TaskResult result) throws Exception;

    public File getWorkDir(TaskResult result) {
        return new File(result.getString(WORK_DIR));
    }

    public Application getApplication(TaskInput input) throws TaskStateException {
        String applicationName = input.getString(APPLICATION);
        try {
            Application application = kubeClient.getApplication(applicationName);
            if (application == null) {
                throw new TaskStateException(TaskState.ERROR, "Application not found:" + applicationName, null);
            }
            return application;
        } catch (Exception e) {
            //Wait to next retry
            throw new TaskStateException(TaskState.WAITING, "Kube exception", e);
        }
    }

    public Template getTemplate(String templateName) throws TaskStateException {
        try {
            Template template = kubeClient.getTemplate(templateName);
            if (template == null) {
                throw new TaskStateException(TaskState.ERROR, "Template not found:" + templateName, null);
            }
            return template;
        } catch (Exception e) {
            //Wait to next retry
            throw new TaskStateException(TaskState.WAITING, "Kube exception", e);
        }
    }
}
