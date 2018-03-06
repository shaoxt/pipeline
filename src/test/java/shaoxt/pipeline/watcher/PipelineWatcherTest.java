package shaoxt.pipeline.watcher;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/4/18.
 * @version 1.0
 */
public class PipelineWatcherTest {
    @Test
    public void eventReceived() throws Exception {

        PipelineWatcher watcher = new PipelineWatcher();
        watcher.setMasterUrl("https://192.168.99.100:8443/");
        watcher.init();

        Thread.sleep(2000000);
    }

}