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

/**
 * Created by ezhang on 16/5/27.
 */
public class RunTaskRequest {
    public static final String STARTED = "STARTED";
    public static final String ENDED = "ENDED";

    String taskName;
    String cmd;
    String appLocation;
    public RunTaskRequest(){};

    public RunTaskRequest(String taskName, String cmd, String appLocation){
        this.taskName = taskName;
        this.cmd = cmd;
        this.appLocation = appLocation;

    }

    @Override
    public  String toString(){
        return taskName + " "+cmd+" "+appLocation;
    }

}
