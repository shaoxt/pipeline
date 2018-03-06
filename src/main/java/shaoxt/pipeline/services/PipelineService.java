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
package shaoxt.pipeline.services;

import org.ebayopensource.winder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shaoxt.pipeline.client.KubeClient;
import shaoxt.pipeline.crds.*;
import shaoxt.pipeline.orchestration.PipelineJob;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

import static shaoxt.pipeline.orchestration.PipelineConstants.APPLICATION;
import static shaoxt.pipeline.orchestration.PipelineConstants.PIPELINE;

/**
 * Pipeline Service
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
@Component
@Singleton
@Path("/pipeline")
public class PipelineService {

    private static Logger logger = LoggerFactory.getLogger(PipelineService.class);

    @Autowired
    private KubeClient kubeClient;

    private WinderEngine engine;

    @PostConstruct
    public void init() {
        engine = WinderUtil.getEngine();
    }

    @GET
    @Path("/listJobs/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map> listJobs(
            @PathParam("appName") String appName,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("limit") @DefaultValue("100")int limit) throws WinderScheduleException {

        WinderJobFilter jobFilter = new WinderJobFilter();
        jobFilter.setKeyField(JobKeyField.JOB_GROUP);
        jobFilter.setValue(appName);
        jobFilter.setStart(new Date(System.currentTimeMillis()-(60*24*3600L*1000L)));
        jobFilter.setEnd(new Date(System.currentTimeMillis()+(624*3600L*1000L)));
        jobFilter.setOffset(offset);
        jobFilter.setLimit(limit);
        List<WinderJobDetail> details = engine.getSchedulerManager().listJobDetails(jobFilter);
        List<Map> list = new ArrayList<>(details.size());
        details.forEach(d -> {
            list.add(d.toMap());
        });
        return list;
    }

    @GET
    @Path("/details/{jobId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map getDetail(@PathParam("jobId")String jobId) throws WinderScheduleException {
        WinderJobDetail detail =  engine.getSchedulerManager().getJobDetail(jobId);
        return detail != null ? detail.toMap() : null;
    }

    private Random random = new Random();


    @POST
    @Path("/trigger/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String trigger(@PathParam("appName") String appName,
                          @QueryParam("branch") @DefaultValue("master") String branch) throws Exception {

        Application application = kubeClient.getApplication(appName);
        if (application == null) {
            throw new IllegalArgumentException("Application not found:" + appName);
        }

        ApplicationSpec spec = application.getSpec();
        //Get the stages from Application first, it might be customized by application
        List<String> stages = spec.getStages();
        if (stages == null) {
            //Then check the predefined Template
            Template template = kubeClient.getTemplate(spec.getInitialTemplate());
            if (template == null) {
                throw new IllegalStateException("Template not found:" + appName);
            }
            stages = template.getSpec().getStages();
        }

        if (stages == null || stages.isEmpty()) {
            throw new IllegalStateException(appName + " does not have any validate stages");
        }

        String pipelineName = appName + "-" + branch + "-" + random.nextInt(10000);
        Pipeline pipeline = new Pipeline();
        pipeline.getMetadata().setName(pipelineName);
        pipeline.setKind("Pipeline");
        PipelineSpec pipelineSpec = new PipelineSpec();
        pipelineSpec.setApplication(appName);
        pipelineSpec.setStages(stages);
        pipeline.setSpec(pipelineSpec);
        pipelineSpec.setCurrentStage(StageName.valueOf(stages.get(0)));

        Pipeline old = kubeClient.getPipeline(pipelineName);
        if (old != null) {
            kubeClient.getPipelineOperation().delete(old);
        }
        kubeClient.getPipelineOperation().createOrReplace(pipeline);

        WinderTaskInput input = new WinderTaskInput(PipelineJob.class);
        input.setJobGroup(appName);
        input.setJobOwner("Sheldon");
        input.put(APPLICATION, appName);
        input.put(PIPELINE, pipelineName);

        JobId jobId = engine.scheduleJob(input);
        return jobId.toString();
    }
}
