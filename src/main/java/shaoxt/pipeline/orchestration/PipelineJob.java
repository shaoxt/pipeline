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
package shaoxt.pipeline.orchestration;

import org.ebayopensource.winder.*;
import org.ebayopensource.winder.anno.DoneStep;
import org.ebayopensource.winder.anno.ErrorStep;
import org.ebayopensource.winder.anno.FirstStep;
import org.ebayopensource.winder.anno.Job;
import shaoxt.pipeline.orchestration.tasks.*;

/**
 * Pipeline Job
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
@Job(type = "Pipeline")
public enum PipelineJob implements Step<TaskInput, TaskResult, TaskContext<TaskInput, TaskResult>>, PipelineConstants {


    @FirstStep
    CHECKOUT(10) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Checkout precheck = new Checkout();
            if (ctx.execute(precheck)) {
                ctx.setCurrentStep(BUILD);
            }
            else {
                ctx.setCurrentStep(ERROR);
            }
        }
    },

    BUILD(20) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Build build = new Build();
            TaskState state = ctx.doExecute(build);
            switch (state) {
                case COMPLETED:
                    ctx.setCurrentStep(TEST);
                    break;
                case SKIP:
                    ctx.setCurrentStep(PACKAGING);
                    break;
                case ERROR:
                case TIMEOUT:
                default:
                    ctx.setCurrentStep(ERROR);
                    break;
            }
        }
    },

    TEST(30) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Test test = new Test();
            if (ctx.execute(test)) {
                ctx.setCurrentStep(PACKAGING);
            }
            else {
                ctx.setCurrentStep(ERROR);
            }
        }
    },
    PACKAGING(40) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Packaging packaging = new Packaging();
            if (ctx.execute(packaging)) {
                ctx.setCurrentStep(SHIP);
            }
            else {
                ctx.setCurrentStep(ERROR);
            }
        }
    },
    SHIP(50) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Ship ship = new Ship();
            if (ctx.execute(ship)) {
                ctx.setCurrentStep(RUN);
            }
            else {
                ctx.setCurrentStep(ERROR);
            }
        }
    },
    RUN(40) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            Run run = new Run();
            if (ctx.execute(run)) {
                ctx.setCurrentStep(DONE);
            }
            else {
                ctx.setCurrentStep(ERROR);
            }
        }
    },
    @DoneStep
    DONE(200) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> ctx) throws Exception {
            System.out.println("All done!");
        }
    },

    @ErrorStep
    ERROR(400) {
        @Override
        public void execute(TaskContext<TaskInput, TaskResult> context) throws Exception {
            System.out.println("ERROR");
        }
    };

    private final int code;

    public int code() {
        return code;
    }

    PipelineJob(final int code) {
        this.code = code;
    }
}
