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
import org.cloudfoundry.operations.routes.DeleteRouteRequest;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ComponentScan
@Controller
@SpringBootApplication
@EnableAutoConfiguration
@EnableWebSecurity
/**
 * PaaS App
 * Created by ezhang on 16/5/15.
 */
public class PaasappApplication extends SpringBootServletInitializer {

	@RequestMapping(value = "/")
	String home() {
		return "Hello PaaS App";
	}
    public static ConfigurableApplicationContext context;
    public static SpringCloudFoundryClient cloudFoundryClient;
    public static CloudFoundryOperations cloudFoundryOperations;
    public static void main(String[] args) {
        context =SpringApplication.run(PaasappApplication.class, args);
        cloudFoundryClient = context.getBean("cloudFoundryClient",SpringCloudFoundryClient.class);
        cloudFoundryOperations = context.getBean("cloudFoundryOperations",CloudFoundryOperations.class);

    }
    @RequestMapping("upload")
    public String  uploadFiles(HttpServletRequest request) throws IllegalStateException, IOException
    {
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        if(multipartResolver.isMultipart(request))
        {
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();

            while(iter.hasNext())
            {
                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    String path=file.getOriginalFilename();
                    //上传
                    file.transferTo(new File(path));
                }

            }
        }

        return "/success";
    }

    public static void setRoute(
            String name,
            String newName){
        cloudFoundryOperations.routes().create(CreateRouteRequest.builder()
                .host(newName)
                .domain("local.pcfdev.io")
                .space("pcfdev-space")
                .build());
        cloudFoundryOperations.routes().map(MapRouteRequest.builder()
                .applicationName(name)
                .host(newName)
                .domain("local.pcfdev.io")
                .build()
        );
    }

    public static void unsetRoute(
            String name,
            String newName){
        cloudFoundryOperations.routes().delete(DeleteRouteRequest.builder()
                .host(newName)
                .domain("local.pcfdev.io")
                .build());
        cloudFoundryOperations.routes().unmap(UnmapRouteRequest.builder()
                .applicationName(name)
                .host(newName)
                .domain("local.pcfdev.io")
                .build()
        );
    }

    public static void rename(
                       String name,
                       String newName){
        cloudFoundryOperations.applications().rename(RenameApplicationRequest.builder()
                .name(name)
                .newName(newName)
                .build())
                .subscribe();
    }

    public static void setEnvVars(
                                 String appName,
                                 Map<String, String> env) {

           Flux.fromIterable(env.entrySet())
                   .concatMap(entry -> cloudFoundryOperations.applications()
                           .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                                   .name(appName)
                                   .variableName(entry.getKey())
                                   .variableValue(entry.getValue())
                                   .build()))
                   .subscribe();
    }

    public static void startApplication(String appName, String uri, Map<String, String> param){
        System.out.println("start app..."+appName);

        cloudFoundryOperations.applications().start(
                StartApplicationRequest.builder()
                        .name(appName)
                        .build()
        ).doOnTerminate(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void aVoid, Throwable throwable) {
                System.out.println("run task...");
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                HttpEntity<Map> entity = new HttpEntity<Map>(param, headers);
                restTemplate.postForObject(uri, entity, Map.class,param);
            }
        }).subscribe();
    }

    public static void stopApplication(String appName){
        cloudFoundryOperations.applications().stop(StopApplicationRequest.builder().name(appName).build());
    }

    public static Flux<ApplicationSummary> findApplication(){
        return cloudFoundryOperations.applications()
                .list()
                .filter(applicationSummary -> applicationSummary.getName()
                        .startsWith("emptyapp") && applicationSummary.getRunningInstances() == 0);
    }

    public static String getApplicationName() throws IOException{
        String vcap_appication = System.getenv("VCAP_APPLICATION");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
        Map<String,String> map = mapper.readValue(vcap_appication,Map.class);
        System.out.println(map);

        return map.get("application_name");
    }
}
