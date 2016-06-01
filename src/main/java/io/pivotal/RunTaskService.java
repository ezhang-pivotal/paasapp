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

import org.springframework.web.bind.annotation.*;
import java.io.*;
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
        String applicationName = PaasappApplication.getApplicationName();
        PaasappApplication.setEnvVars(applicationName,runTaskRequest);
        PaasappApplication.changeRoute(applicationName,runTaskRequest.get("taskName"));
        String fileName = PaasappApplication.download(runTaskRequest.get("appLocation"));
        doRunTask(runTaskRequest.get("cmd"),fileName);
    }

    public void doRunTask(String cmd, String appFileName) throws Exception {
        try {

            String[] env = new String[2];
            env[0]= ("PATH=/home/vcap/app/.java-buildpack/open_jdk_jre/bin");
            env[1]=("CLASSPATH="+appFileName);
            Process ps = Runtime.getRuntime().exec("/home/vcap/app/.java-buildpack/open_jdk_jre/bin/"+cmd,env);
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
