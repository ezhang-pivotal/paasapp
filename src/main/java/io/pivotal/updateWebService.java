package io.pivotal;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


@RestController
/**
 * Created by ezhang on 16/5/30.
 */
public class updateWebService {
    @RequestMapping(value= "/update" , method = {RequestMethod.POST})
    public void updateWebFiles(@RequestBody Map<String, String> updateWebFile) throws  Exception{

        String applicationName = "paasapp";//PaasappApplication.getApplicationName();

        //PaasappApplication.rename("paasapp",updateWebFile.get("appName"));
        PaasappApplication.setEnvVars(applicationName,updateWebFile);
        String fileName = PaasappApplication.download(updateWebFile.get("appLocation"));
        //check for buildpack type
        updateApplication(".",fileName);
    }

    public void updateApplication(String destinationDir, String appFileName) throws IOException{
        final int BUFFER = 2048;
        File file = new File(appFileName);
        JarFile jar = new JarFile(file);

        // fist get all directories,
        // then make those directory on the destination Path
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();
            String fileName = destinationDir + File.separator + entry.getName();

            File f = new File(fileName);
            if (fileName.endsWith("/")) {
                f.mkdirs();
            }

        }

        //now create all files
        for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
            JarEntry entry = (JarEntry) enums.nextElement();

            String fileName = destinationDir + File.separator + entry.getName();
            File f = new File(fileName);

            if (!fileName.endsWith("/")) {
                InputStream is = jar.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(f);

                // write contents of 'is' to 'fos'
                while (is.available() > 0) {
                    fos.write(is.read());
                }

                fos.close();
                is.close();
            }
        }
    }
}
