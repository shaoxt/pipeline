package shaoxt.pipeline.tools;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.AuthResponse;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import com.github.dockerjava.core.command.WaitContainerResultCallback;
import org.eclipse.jgit.util.Base64;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class DockerUtilTest {

    @Test
    @Ignore
    public void buildImage() throws Exception {
        DockerClientBuilder clientBuilder = DockerClientBuilder.getInstance();
        try (DockerClient client = clientBuilder.build()) {
            BuildImageCmd cmd = client.buildImageCmd(
                    new File("/Users/xshao/helloworld"));

            BuildImageResultCallback waitContainerResultCallback = new BuildImageResultCallback();
            cmd.exec(waitContainerResultCallback);
            //TODO
            String imageId = waitContainerResultCallback.awaitImageId();
            System.out.println(imageId);
        }
    }

    @Test
    @Ignore
    public void pushImage() throws Exception {
        Properties properties = new Properties();
        try {
            properties.load(DockerUtil.class.getResourceAsStream("/META-INF/docker-java.properties"));
            if (properties.containsKey("registry.password")) {
                properties.setProperty("registry.password",
                        new String(Base64.decode(properties.getProperty("registry.password"))));
            }
        }
        catch(Exception ex) {
            //TODO Exception
            throw new IllegalStateException("Unknown exception", ex);
        }
        DefaultDockerClientConfig clientConfig
                = new DefaultDockerClientConfig.Builder().withProperties(properties).build();
        DockerClientBuilder clientBuilder = DockerClientBuilder.getInstance(clientConfig);

        DockerClient client = clientBuilder.build();
        AuthResponse response = client.authCmd().exec();
        System.out.println(response);

        PushImageCmd cmd = client.pushImageCmd("shaoxt/helloworld:latest");
        //TODO Wait until it finished for now
        PushImageResultCallback waitContainerResultCallback = new PushImageResultCallback();
        System.out.println(cmd.exec(waitContainerResultCallback));
        waitContainerResultCallback.awaitSuccess();
    }

}