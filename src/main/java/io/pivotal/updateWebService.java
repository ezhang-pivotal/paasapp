package io.pivotal;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController

/**
 * Created by ezhang on 16/5/30.
 */
public class updateWebService {
    @RequestMapping(value= "/update" , method = {RequestMethod.POST})
    public void updateWebFiles(@RequestBody Map<String, String> updateWebFile) throws  Exception{
        PaasappApplication.rename("paasapp",updateWebFile.get("appName"));

    }
}
