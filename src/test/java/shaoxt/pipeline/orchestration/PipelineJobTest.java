package shaoxt.pipeline.orchestration;

import org.ebayopensource.winder.TaskInput;
import org.ebayopensource.winder.WinderEngine;
import org.ebayopensource.winder.WinderTaskInput;
import org.ebayopensource.winder.quartz.QuartzEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import shaoxt.pipeline.client.DefaultKubeClient;

import static shaoxt.pipeline.orchestration.PipelineConstants.APPLICATION;
import static shaoxt.pipeline.orchestration.PipelineConstants.PIPELINE;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class PipelineJobTest {

    protected WinderEngine engine;

    @Before
    public void start() {
        engine = new QuartzEngine();
        engine.start();
    }

    @After
    public void stop() {
        if (engine != null) {
            engine.stop();
        }
    }

    @Test
    public void test() {
    }

    @Test
    @Ignore
    public void runJob() throws Exception {
        DefaultKubeClient kubeClient = new DefaultKubeClient();
        kubeClient.setMasterUrl("https://192.168.99.101:8443/");
        kubeClient.init();

        TaskInput input = new WinderTaskInput(PipelineJob.class);
        input.setJobOwner("Sheldon");
        input.put(APPLICATION, "helloworld");
        input.put(PIPELINE, "simple");

        engine.scheduleJob(input);

        Thread.sleep(9999999);
    }

}