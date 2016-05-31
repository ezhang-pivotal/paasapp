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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.*;
import org.cloudfoundry.operations.routes.CreateRouteRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Iterator;
import java.util.Map;

@RestController
/**
 * Created by ezhang on 16/5/25.
 */
public class RunTaskService {

    @RequestMapping(value= "/run-task" , method = {RequestMethod.POST})
    public void runTask(@RequestBody Map<String, String> runTaskRequest) throws  Exception{
        System.out.println("Run task request: "+runTaskRequest);
        //setTaskApp
        System.out.println(System.getenv("VCAP_APPLICATION"));
        //PaasappApplication.rename("paasapp",runTaskRequest.get("taskName"));
        PaasappApplication.setEnvVars("paasapp",runTaskRequest);
        PaasappApplication.download(runTaskRequest.get("appLocation"));
        doRunTask(runTaskRequest.get("cmd"));
    }

    public void doRunTask(String cmd) throws Exception {
        try {
            Process ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String result = sb.toString();
            System.out.println(result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


}
