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
package io.pivotal.cfclient;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CF-Java-Client Bean
 * Created by ezhang on 16/5/22.
 */
@Configuration
public class CFClient {
    @Bean
    public SpringCloudFoundryClient cloudFoundryClient(@Value("${cf.host}") String host,
                                          @Value("${cf.username}") String username,
                                          @Value("${cf.password}") String password) {
        return SpringCloudFoundryClient.builder().skipSslValidation(true)
                .host(host)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    public DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient,
                                                                @Value("${cf.organization}") String organization,
                                                                @Value("${cf.space}") String space) {
            return DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                    .organization(organization)
                    .space(space)
                    .build();
    }

}
