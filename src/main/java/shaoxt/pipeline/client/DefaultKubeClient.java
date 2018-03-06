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
package shaoxt.pipeline.client;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import shaoxt.pipeline.crds.*;
import shaoxt.pipeline.watcher.PipelineWatcher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Shared KubeClient
 *
 * @author Sheldon Shao xshao@ebay.com on 3/5/18.
 * @version 1.0
 */
@Component
public class DefaultKubeClient implements KubeClient {

    private String namespace = "default";

    private String masterUrl;

    private Config config;

    private KubernetesClient kubernetesClient;

    private static final String APPLICATION_CRD_NAME = "applications.pipeline.shaoxt";
    private static final String PIPELINE_CRD_NAME = "pipelines.pipeline.shaoxt";
    private static final String TEMPLATE_CRD_NAME = "templates.pipeline.shaoxt";

    private static Logger log = LoggerFactory.getLogger(PipelineWatcher.class);

    private CustomResourceDefinition pipelineCRD;
    private CustomResourceDefinition applicationCRD;
    private CustomResourceDefinition templateCRD;

    private NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline,
            Resource<Pipeline, DoneablePipeline>> pipelineOperation;

    private NonNamespaceOperation<Application, ApplicationList, DoneableApplication,
            Resource<Application, DoneableApplication>> applicationOperation;

    private NonNamespaceOperation<Template, TemplateList, DoneableTemplate,
            Resource<Template, DoneableTemplate>> templateOperation;

    @PostConstruct
    public void init() {
        if (masterUrl == null || masterUrl.isEmpty()) {
            //Try
            String host = System.getenv("KUBERNETES_SERVICE_HOST");
            String port = System.getenv("KUBERNETES_SERVICE_PORT");

            if (host != null && port != null) {
                masterUrl = host + ":" + port;
                if (port.equals("443") || port.equals("6443")) {
                    masterUrl = "https://" + masterUrl;
                } else {
                    masterUrl = "http://" + masterUrl;
                }
            }
        }

        Properties props = System.getProperties();


        props.setProperty("kubernetes.auth.tryServiceAccount", "false");
        props.setProperty("kubernetes.auth.tryKubeConfig", "false");

        config = Config.autoConfigure(null);

        //TODO FIX IT. It is not a good pratice, it should be store in certificate management system
        config.setClientCertData("LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tDQpNSUlEQURDQ0FlaWdBd0lCQWdJQkFqQU5CZ2txaGtpRzl3MEJBUXNGQURBVk1STXdFUVlEVlFRREV3cHRhVzVwDQphM1ZpWlVOQk1CNFhEVEU0TURNd05qQXlNVEF3TjFvWERURTVNRE13TmpBeU1UQXdOMW93TVRFWE1CVUdBMVVFDQpDaE1PYzNsemRHVnRPbTFoYzNSbGNuTXhGakFVQmdOVkJBTVREVzFwYm1scmRXSmxMWFZ6WlhJd2dnRWlNQTBHDQpDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ1pLSmZMZWRISll6TERYM1N2ZTBGWXliQ3lRcjcrDQo5dDRXWVBWSmY3NmRVeUlKTll4WkZZbmtQVVc2R1hCNnlqaU1nWHZHM09uV2d1SHl0WldpMVdhOFlYVHpxUHovDQo4K3duc29Va2Q1QVBpWWJwcHpCRGZBbGxSN0F2bjV4R2FFdjJGRS9RNVg2QXN1QSttUkhxNnFtVW1RY3Jxc1MrDQpSRzNNWmVuRmhzLzFUMmtNclUxREpjQWQyNlM0MVFtRVA2OXVRa20xdUxMcm16VXVPdkZaSFJMUkR4OXl1MjBmDQpsRXQ3LzNxdk9Ld1g0UXVuUDVoSC9FNVh4OFVEWUxXeXc3YWFJU2hIRWtlRHVZZllVWE1wVnhZVGRCU21lc1lxDQpzM0xiTG9aTkg4ZTFHdzNjWjNHTkkyRXNvL1VDZXl5SlpFcnQrakZHZkQxNXBOb2pPRytPd0M4WEFnTUJBQUdqDQpQekE5TUE0R0ExVWREd0VCL3dRRUF3SUZvREFkQmdOVkhTVUVGakFVQmdnckJnRUZCUWNEQVFZSUt3WUJCUVVIDQpBd0l3REFZRFZSMFRBUUgvQkFJd0FEQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFGc2FJdkUvZFp5dWhGT2dlDQpoSGNkeEpldERBUGJNZFF4czZIaWFJQ21kRmhSVzJwaXZLREtSb1o3TVI5eVM4VlJSUy9MM3J0ejRvYU1zNEg3DQp0NlBNcFdZWE51aHR3aWR2V2thaDc3U0hnM1ArZmJuNjNBdGtVOVFKYjE4Z1k0ckpjZWxkQ2tpaHU1SG1Ha29KDQovWXJzbXZDbGNCMW9OYXFTTDlkK3VwZm0rSThYcmtERFZaYzRyeFV4eGVsUGgvbVdEU0JERjZuS0RTWmU4VEh5DQpOSkNHMUZvTGM3cUlhREpBaDN6MUdvN1pXNHNpaVo1U2NIbVptNkVwNEtaNHNsa0lWU0ZaTkR2aXhKNXBsamtxDQp6SkRjakVqU0ROY3dzcVdYNDg3QmRkL2ZXNmtpZnEyU3ZnZ0pKeEordGZDYnFBejdYOGNxb1lIcXBiSFpQd1Z6DQpQYlNnbHc9PQ0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQ==");
        //TODO FIX IT. It is not a good pratice, it should be store in certificate management system
        config.setClientKeyData("LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQ0KTUlJRW93SUJBQUtDQVFFQW1TaVh5M25SeVdNeXcxOTByM3RCV01td3NrSysvdmJlRm1EMVNYKytuVk1pQ1RXTQ0KV1JXSjVEMUZ1aGx3ZXNvNGpJRjd4dHpwMW9MaDhyV1ZvdFZtdkdGMDg2ajgvL1BzSjdLRkpIZVFENG1HNmFjdw0KUTN3SlpVZXdMNStjUm1oTDloUlAwT1YrZ0xMZ1Bwa1I2dXFwbEprSEs2ckV2a1J0ekdYcHhZYlA5VTlwREsxTg0KUXlYQUhkdWt1TlVKaEQrdmJrSkp0Yml5NjVzMUxqcnhXUjBTMFE4ZmNydHRINVJMZS85NnJ6aXNGK0VMcHorWQ0KUi94T1Y4ZkZBMkMxc3NPMm1pRW9SeEpIZzdtSDJGRnpLVmNXRTNRVXBuckdLck55Mnk2R1RSL0h0UnNOM0dkeA0KalNOaExLUDFBbnNzaVdSSzdmb3hSbnc5ZWFUYUl6aHZqc0F2RndJREFRQUJBb0lCQUNTenhwUjdMOXU3eXRsbw0KMFpTUHk5d1VFU1RXdStCcmxsTDBqek02eXBuMjV3d2Y2ZThiS3owbnRjTGYvOFZkR21vSlNYa2hUV1FiWHp3RA0KYWNWb3NFTHFjOVZFSk1UTlZrVTVFWkRvbWZ3dkw4WmtTenVReU11ZGYwb2FUSi9PdEkxVzFyMnZrVDRVSDRyQQ0Kc2J1QzJucUNwV1pBZVA5RGRMQnJwN2F2Rkx5ckdSQVA3QjNqb1BlSUNzUkVRMUVNSUpZMnVpbUVjcHFreWxPZA0KU00vQXNLMnlLeURicXlEb013NHpPeSs2VEVhcUx3V0IwVUVVaXYwMHg1djFOYklNeTRCQ3cxZHNCYkk3SWFQbw0KVDh3eTVuelQ0NjJYeFNWbG4ybi9iNmF3WGhWczhjUFZDem9wUmNRRHNzMTJJdDZMRW96V0hxNzJYektoN1Zpdw0KbitQNkFKa0NnWUVBeUE5alRGMWRGek43L1VsM2lnZ01kM1Z3cWdWZVJuUG5ocmxibk8ybWE0WW1QRi92emo0Sg0KY3ZLWXhLSFZNd3JrQXFDUStCdW4zRnN5OFhlTTcwTUpTNU9jZGY4MFJCYmlJcXBxZmdTaHdGZzNRc1lVdmdjYg0KSGNMb0tXbEtLMW0zaFVFcXhJR3k1Ym12bG9iOXRxZ3dUbTRhdUpFd0szdnJxLzBYTWRCS213MENnWUVBdy92cg0KaWZqUGluOEFlR2NtTnAzZXFFc2Rldm5nQVRFMlA5RnpFaWhRT2VDSTdiZWRETm4xRDQrVER3RkpJWkQ3a3NWTg0KcEM5TXhENUd1OFNhNENlS21YOVZhajdoTk5QMkg3U3RBNVBOSytCRUtjTGUrRUowcTM1dXl6NGJSRVd4UERXYg0KTTZyaUZQWitDK0tMQ0k2a1M2Z0srQnlrUlJmZURXeDNsNHpYbWJNQ2dZQTJxZFcxQVBHMXJEQ2R4Wnd2RUFIdw0KQmh2UnFRUFlka1QxeitIVFlEQVB3eWpoYUVsSzdqS0F3UDZ6QTVFUWtGSTYwWXFxOEV1T2J6RGRQNUEzcy9adg0KT3hjT09yd2FPckp4VEdVcXA3TldyZ1B6YjlJdEZoMzNwTkR3dDc4M1Z4MUpBTVJoeXhxSm9KSDByRS9Zdjkzcg0KSURTVzU3emt0TWh4UndjMmFqWm9aUUtCZ0cxdWsxeWRFN1h2cUdHU3B5SEVFUG5EeDVoTlFpa29RM1JyRFdmTQ0KSHMvU2NtTGFMZFZwTm84VHlqZU1yanNSNjRkT3FFWFBLM3hBa2RweXMxQUtoRGVsaEJvcE5qTDdUK2p3UjlOQg0KaWtTL213LytnZVg1cFkvRXJ6VEpYd3hHSmVyS3UvaWpxRTJ3UFQyQnA4U0ZjWFBWUnhxM1UzcEpFM0pIYUplMA0KeXdaRkFvR0JBSmhsU0JtVzFnekUxZVpLYlltQVZLRWVteHRrSnhwNURSUGNnZE54djFMTWpKZjV6Y3dCbzlVSg0KUENXSTdtdlAva3ljdVk4TXV6VFVmRDUrbmhFY3hMYWg1RWhSV2tSdjF2Ui9UVDdhWlBuNWVHMTFPbDlxZkVJUg0KQ3czcWpRY29vNVUrMVB3bG9DTnJsTXk1bVVMMmt3N1QwamYvNy83WEd2eit6OUIzcDVRQw0KLS0tLS1FTkQgUlNBIFBSSVZBVEUgS0VZLS0tLS0=");
        config.setTrustCerts(true);

        log.info("Master url is :" + masterUrl);

        if (masterUrl != null) {
            config.setMasterUrl(masterUrl);
        }
        else {
            log.error("No master Url was set");
            return;
        }
        config.setNoProxy(new String[]{});

        log.info("Getting master URL from kubeconfig: " + masterUrl);
        try {
            //Set default namespace
            config.setNamespace(namespace);

            kubernetesClient = new DefaultKubernetesClient(config);

            CustomResourceDefinitionList crds = kubernetesClient.customResourceDefinitions().list();
            List<CustomResourceDefinition> crdsItems = crds.getItems();
            if (log.isInfoEnabled()) {
                log.info("Found " + crdsItems.size() + " CRD(s)");
            }
            for (CustomResourceDefinition crd : crdsItems) {
                ObjectMeta metadata = crd.getMetadata();
                if (metadata != null) {
                    String name = metadata.getName();
                    if (log.isDebugEnabled()) {
                        log.debug("    " + name + " => " + metadata.getSelfLink());
                    }
                    if (PIPELINE_CRD_NAME.equals(name)) {
                        pipelineCRD = crd;
                    }
                    else if (APPLICATION_CRD_NAME.equals(name)) {
                        applicationCRD = crd;
                    }
                    else if (TEMPLATE_CRD_NAME.equals(name)) {
                        templateCRD = crd;
                    }
                }
            }

            if (pipelineCRD == null) {
                log.error("No pipeline CRD got created. Please create it.");
                System.exit(0);
            }
            pipelineOperation = kubernetesClient.customResources(pipelineCRD, Pipeline.class,
                    PipelineList.class, DoneablePipeline.class).inNamespace(namespace);

            if (applicationCRD == null) {
                log.error("No application CRD got created. Please create it.");
                System.exit(0);
            }

            applicationOperation = kubernetesClient.customResources(applicationCRD, Application.class,
                    ApplicationList.class, DoneableApplication.class).inNamespace(namespace);

            if (templateCRD == null) {
                log.error("No template CRD got created. Please create it.");
                System.exit(0);
            }

            templateOperation = kubernetesClient.customResources(templateCRD, Template.class,
                    TemplateList.class, DoneableTemplate.class).inNamespace(namespace);

        } catch (Exception e) {
            log.error("Exception occured while instantiating kube client:" + e.getMessage(), e);
        }

        //TODO change this to spring, for task injection
        System.getProperties().put(KubeClient.class.getName(), this);
    }

    @Override
    public Application getApplication(String application) throws Exception {
        return applicationOperation.withName(application).get();
    }

    @Override
    public Pipeline getPipeline(String pipeline) throws Exception {
        return pipelineOperation.withName(pipeline).get();
    }


    @Override
    public Template getTemplate(String template) throws Exception {
        return templateOperation.withName(template).get();
    }

    @Override
    public NonNamespaceOperation<Pipeline, PipelineList, DoneablePipeline, Resource<Pipeline, DoneablePipeline>> getPipelineOperation() {
        return pipelineOperation;
    }

    @Override
    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    @Override
    @PreDestroy
    public void close() throws IOException {
        if (kubernetesClient != null) {
            kubernetesClient.close();
            kubernetesClient = null;
        }
    }
}
