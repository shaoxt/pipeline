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
package shaoxt.pipeline.watcher;

import io.fabric8.kubernetes.client.*;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import shaoxt.pipeline.client.KubeClient;
import shaoxt.pipeline.crds.Pipeline;

import javax.annotation.PostConstruct;

/**
 * Watcher
 *
 * @author Sheldon Shao xshao@ebay.com on 3/4/18.
 * @version 1.0
 */
@Component
public class PipelineWatcher implements Watcher<Pipeline> {


    @Autowired
    private KubeClient kubeClient;

    private static Logger log = LoggerFactory.getLogger(PipelineWatcher.class);

    @PostConstruct
    public void init() {
        kubeClient.getPipelineOperation().watch(this);
    }

    @Override
    public void eventReceived(Action action, Pipeline pipeline) {
        if (Action.MODIFIED == action || Action.ADDED == action) {
            System.out.println(action + " Pipeline:" + pipeline.getMetadata().getName());
        }
    }

    @Override
    public void onClose(KubernetesClientException e) {

    }

    public KubeClient getKubeClient() {
        return kubeClient;
    }

    public void setKubeClient(KubeClient kubeClient) {
        this.kubeClient = kubeClient;
    }
}
