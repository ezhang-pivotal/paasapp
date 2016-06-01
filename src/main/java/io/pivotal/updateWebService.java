package io.pivotal;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
/**
 * Created by ezhang on 16/5/30.
 */
public class updateWebService {
    @RequestMapping(value= "/update" , method = {RequestMethod.POST})
    public void updateWebFiles(@RequestBody Map<String, String> updateWebFile) throws  Exception{

        String applicationName = "paasapp";//PaasappApplication.getApplicationName();

        PaasappApplication.rename("paasapp",updateWebFile.get("appName"));
    }
}
