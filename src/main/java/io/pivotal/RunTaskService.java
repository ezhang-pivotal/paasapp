/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal;

import org.cloudfoundry.client.v2.applications.ApplicationEnvironmentRequest;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

@RestController
/**
 * Created by ezhang on 16/5/25.
 */
public class RunTaskService {

    @RequestMapping(value= "/run-task" , method = {RequestMethod.POST})
    public void runTask(@RequestBody Map<String, String> runTaskRequest) throws  Exception{
        System.out.println("Run task request: "+runTaskRequest);

        ApplicationSummary entry = (PaasappApplication.cloudFoundryOperations.applications()
                .list()
                .filter(applicationSummary -> applicationSummary.getName()
                        .startsWith("emptyapp") && applicationSummary.getRunningInstances() == 0))
                .elementAt(0).block();
        PaasappApplication.cloudFoundryClient.applicationsV2().update(UpdateApplicationRequest.builder()
                .applicationId(entry.getId()).environmentJsons(runTaskRequest).build()).subscribe();
/*        Flux.fromIterable(runTaskRequest.entrySet())
                .concatMap(env -> PaasappApplication.cloudFoundryOperations.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                .name(entry.getName())
                                .variableName(env.getKey())
                                .variableValue(env.getValue())
                                .build()).subscribe());*/

        (PaasappApplication.cloudFoundryOperations.applications()
                .start(StartApplicationRequest.builder()
                        .name(entry.getName())
                        .build()))
                .doOnSuccess(new Consumer<Void>() {
                                 @Override
                                 public void accept(Void aVoid) {
                                     String uri = entry.getUrls().get(0);
                                     System.out.println("run task..."+uri);

                                     RestTemplate restTemplate = new RestTemplate();
                                     HttpHeaders headers = new HttpHeaders();
                                     headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                                     HttpEntity<Map> entity = new HttpEntity<Map>(runTaskRequest, headers);

                                     restTemplate.postForObject("http://"+uri+"/run-task", entity, Map.class,runTaskRequest);
                                 }
                             }
                ).subscribe();
    }

    @RequestMapping(value= "/finish-task" , method = {RequestMethod.POST})
    public void finishTask(@RequestBody Map<String, String> finTaskRequest) throws  Exception{
        System.out.println("finish task request: "+finTaskRequest);
        PaasappApplication.cloudFoundryOperations.applications().stop(StopApplicationRequest.builder()
                .name(finTaskRequest.get("name")).build()).subscribe();
    }

    @RequestMapping(value= "/updateApp" , method = {RequestMethod.POST})
    public void updateApp(@RequestBody Map<String, String> updateAppRequest) throws  Exception{
        System.out.println("Run update request: "+updateAppRequest);
        ApplicationSummary entry = (PaasappApplication.cloudFoundryOperations.applications()
                .list()
                .filter(applicationSummary -> applicationSummary.getName()
                        .startsWith("emptywebapp") && applicationSummary.getRunningInstances() == 0))
                .elementAt(0).block();
        String uri = entry.getUrls().get(0);
        System.out.println("update ..."+uri);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Map> entity = new HttpEntity<Map>(updateAppRequest, headers);

        restTemplate.postForObject("http://"+uri+"/update", entity, Map.class,updateAppRequest);
    }
}
