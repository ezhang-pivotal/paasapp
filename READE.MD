# PAASAPP for quickly deploy and run in cloudfoundry

## Usage
deploy paasapp to cloudfoundry and send restful request to it to tell it how to run an application.


### deploy paasapp
change application.properties
```shell
server.port=8080
cf.host=api.local.pcfdev.io
cf.username=admin
cf.password=admin
cf.organization=pcfdev-org
cf.space=pcfdev-space
security.basic.enabled=false
```

run as follow
```shell
$ mvn package
$ cf push passapp
```

### build testapp

```java
package io.pivotal;

/**
 * Created by ezhang on 16/5/30.
 */
public class TestApp {
    public static void main(String[] args){
        System.out.println("Hello TestApp ");
    }
}
```

### test
```shell
curl -H "Content-Type: application/json" -X POST --data '{"taskName":"demo1","cmd":"java io.pivotal.TestApp","appLocation":"http://192.168.11.1:8000/testapp-1.0-SNAPSHOT.jar"}' http://paasapp.local.pcfdev.io/run-task
```