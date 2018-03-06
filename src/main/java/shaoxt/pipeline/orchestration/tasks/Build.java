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

import org.ebayopensource.winder.*;
import shaoxt.pipeline.crds.Application;
import shaoxt.pipeline.crds.ApplicationSpec;
import shaoxt.pipeline.crds.BuildTool;
import shaoxt.pipeline.crds.Template;
import shaoxt.pipeline.tools.JavacUtil;

import java.io.File;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class Build extends BasePipelineTask {

    @Override
    public TaskState execute(TaskContext<TaskInput, TaskResult> ctx, TaskInput input, TaskResult result) throws Exception {

        Application application = getApplication(input);
        File workDir = getWorkDir(result);
        ApplicationSpec spec = application.getSpec();
        //If it is changed by user, use the configuration on Application,
        //Otherwise
        BuildTool buildTool = spec.getBuildTool();
        if (buildTool == null) {
            String templateName = spec.getInitialTemplate();
            Template template = getTemplate(templateName);
            buildTool = template.getSpec().getBuildTool();
        }

        if (buildTool == null) {
            ctx.getJobContext().addUpdate(StatusEnum.ERROR, "No build tool configured!");
            return TaskState.ERROR;
        }
        switch (buildTool) {
            case Javac:
                if (!JavacUtil.javac(workDir)) {
                    ctx.getJobContext().addUpdate(StatusEnum.EXECUTING, "Javac finished!");
                    //TODO show error log
                    //TODO unsupported yet
                    return TaskState.ERROR;
                }
                break;
            default:
                //TODO unsupported yet
                return TaskState.ERROR;
        }
        return TaskState.COMPLETED;
    }

}
