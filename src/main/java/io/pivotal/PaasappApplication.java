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

import io.pivotal.cfclient.CFClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.RenameApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import static org.cloudfoundry.util.test.TestObjects.fill;
import static reactor.core.publisher.Mono.when;

@ComponentScan
@Controller
@SpringBootApplication
@EnableAutoConfiguration
@EnableWebSecurity
/**
 * Created by ezhang on 16/5/15.
 */
public class PaasappApplication {

	@RequestMapping(value = "/")
	String home() {
		return "Hello PaaS App";
	}
    public static ConfigurableApplicationContext ctx;
    public static SpringCloudFoundryClient cloudFoundryClient;
    public static CloudFoundryOperations cloudFoundryOperations;

    public static void main(String[] args) {
        ctx =SpringApplication.run(PaasappApplication.class, args);
        cloudFoundryClient = ctx.getBean("cloudFoundryClient",SpringCloudFoundryClient.class);
        cloudFoundryOperations = ctx.getBean("cloudFoundryOperations",CloudFoundryOperations.class);

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

    public static void rename(
                       String name,
                       String newName){
        RenameApplicationRequest renameApplicationRequest = RenameApplicationRequest.builder()
                .name(name)
                .newName(newName)
                .build();
        cloudFoundryOperations.applications().rename(renameApplicationRequest)
                .subscribe()
                .doOnError(Throwable::printStackTrace);

        cloudFoundryOperations.applications().list().map(ApplicationSummary::getName).subscribe(System.out::println);
    }


    public static String download(String appLocation) throws Exception {
        System.out.println(appLocation);

        HttpGet httpGet = new HttpGet(appLocation);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        FileOutputStream fos;
        HttpResponse response = httpClient.execute(httpGet);
        InputStream inputStream = response.getEntity().getContent();
        String fileName=appLocation.substring(appLocation.lastIndexOf("/")+1);
        System.out.println(fileName);

        try {
/*            File path = new File("/home/vcap/app/tasks");
            if (!path.exists()) {
                path.mkdirs();
            }*/
            File file = new File(fileName);
            if (file.exists()){
                System.out.println(fileName+" exists");
                return fileName;
            }

            fos = new FileOutputStream(file);
            byte[] data = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(data)) != -1) {
                fos.write(data, 0, len);
            }
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            inputStream.close();
        }
        httpClient.close();
        return fileName;
    }

    public static void setEnvVars(
                                 String appName,
                                 Map<String, String> env) {

       for (Map.Entry<String, String> entry : env.entrySet()) {
            System.out.println("set env "+entry.getKey()+":"+entry.getValue());
            cloudFoundryOperations.applications()
                    .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                            .name(appName)
                            .variableName(entry.getKey())
                            .variableValue(entry.getValue())
                            .build()
                    ).subscribe()
                    .doOnSuccess(v -> System.out.println(String.format("Done set env for %s", appName)))
                    .doOnError(e -> System.err.println(String.format("Error set env for %s", appName)));
        }
    }

    public static String getApplicationName() throws IOException{
        String vcap_appication = System.getenv("VCAP_APPLICATION");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, Boolean.TRUE);
        Map<String,String> map = mapper.readValue(vcap_appication,Map.class);
        System.out.println(map);
        String applicationName = map.get("application_name");

        return applicationName;
    }
}
