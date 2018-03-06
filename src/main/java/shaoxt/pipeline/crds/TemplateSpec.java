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
package shaoxt.pipeline.crds;

import java.util.List;

/**
 * @author Sheldon Shao xshao@ebay.com on 3/4/18.
 * @version 1.0
 */
public class TemplateSpec extends BaseSpec {

    private Language language = Language.java;

    private ApplicationType applicationType;

    private SourceControl sourceControl = SourceControl.Git;

    private BuildTool buildTool;

    private TestTool testTool;

    private PackagingTool packagingTool;

    private ImageRepo imageRepo = ImageRepo.DockerHub;

    private List<String> stages;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public SourceControl getSourceControl() {
        return sourceControl;
    }

    public void setSourceControl(SourceControl sourceControl) {
        this.sourceControl = sourceControl;
    }

    public BuildTool getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(BuildTool buildTool) {
        this.buildTool = buildTool;
    }

    public TestTool getTestTool() {
        return testTool;
    }

    public void setTestTool(TestTool testTool) {
        this.testTool = testTool;
    }

    public PackagingTool getPackagingTool() {
        return packagingTool;
    }

    public void setPackagingTool(PackagingTool packagingTool) {
        this.packagingTool = packagingTool;
    }

    public ImageRepo getImageRepo() {
        return imageRepo;
    }

    public void setImageRepo(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<String> getStages() {
        return stages;
    }

    public void setStages(List<String> stages) {
        this.stages = stages;
    }
}
