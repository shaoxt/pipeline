package shaoxt.pipeline.tools;

import org.junit.Ignore;
import org.junit.Test;
import shaoxt.pipeline.crds.GitRepo;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
public class GitUtilTest {
    @Test
    @Ignore
    public void gitClone() throws Exception {
        GitRepo repo = new GitRepo();
        repo.setUrl("https://github.com/shaoxt/helloworld.git");
        File dir = GitUtil.gitClone("helloworld", repo);

        boolean result = JavacUtil.javac(dir);
        System.out.println(result);

        DockerUtil.generateImage("helloworld", dir);

        DockerUtil.pushImage("helloworld");
    }

}