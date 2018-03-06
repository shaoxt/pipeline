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
package shaoxt.pipeline.tools;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import org.eclipse.jgit.util.Base64;

import java.io.*;
import java.util.Properties;

/**
 * TODO simple shell version, just for demo
 *
 * For real world we need to setup docker engine, and generate docker image
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class DockerUtil {


    private static Properties properties = new Properties();
    static {
        try {
            properties.load(DockerUtil.class.getResourceAsStream("/META-INF/docker-java.properties"));
            if (properties.containsKey("registry.password")) {
                properties.setProperty("registry.password",
                        new String(Base64.decode(properties.getProperty("registry.password"))));
            }
        }
        catch(Exception ex) {
            //TODO Exception
//            throw new IllegalStateException("Unknown exception", ex);
        }
    }

    public static String generateImage(File workDir) throws Exception {
        DefaultDockerClientConfig clientConfig
                = new DefaultDockerClientConfig.Builder().withProperties(properties).build();
        DockerClientBuilder clientBuilder = DockerClientBuilder.getInstance(clientConfig);
        try (DockerClient client = clientBuilder.build()) {
            BuildImageCmd cmd = client.buildImageCmd(workDir);

            BuildImageResultCallback waitContainerResultCallback = new BuildImageResultCallback();
            cmd.exec(waitContainerResultCallback);
            //TODO
            String imageId = waitContainerResultCallback.awaitImageId();
            return imageId;
        }
    }

    public static void pushImage(String appName) throws Exception {
        //TODO Just for demo, password should not store in code

        DefaultDockerClientConfig clientConfig
                = new DefaultDockerClientConfig.Builder().withProperties(properties).build();
        DockerClientBuilder clientBuilder = DockerClientBuilder.getInstance(clientConfig);
        try (DockerClient client = clientBuilder.build()) {
            client.authCmd().exec();

            PushImageCmd cmd = client.pushImageCmd("shaoxt/" + appName + ":latest");
            //TODO Wait until it finished for now
            PushImageResultCallback waitContainerResultCallback = new PushImageResultCallback();
            cmd.exec(waitContainerResultCallback);
            waitContainerResultCallback.awaitSuccess();
        }
    }
}
