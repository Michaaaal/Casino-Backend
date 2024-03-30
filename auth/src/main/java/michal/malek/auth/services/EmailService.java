package michal.malek.auth.services;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.RequiredArgsConstructor;
import michal.malek.auth.configuration.EmailConfiguration;
import michal.malek.auth.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailConfiguration emailConfiguration;
    @Value("${front.url")
    private String thFrontUrl;

    @Value("${front2.url}")
    private String frontUrl;

    @Value("classpath:/templates/confirmEmail.html")
    Resource activeTemplate;


    @Value("classpath:/templates/retrievePasswordEmail.html")
    Resource retrieveTemplate;

    public void sendActivation(User user) throws IOException {
        try{
            String html = Files.toString(activeTemplate.getFile() , Charsets.UTF_8);
            html = html.replace("https://google.com","http://localhost:8888" + "/auth/activate?uid=" + user.getUid());
            emailConfiguration.sendMail(user.getEmail(), html,"CasinoFox Account Activation",true);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void sendPasswordRecovery(User user, String uid) {
        try{
            String html = Files.toString(retrieveTemplate.getFile() , Charsets.UTF_8);
            html = html.replace("https://google.com",frontUrl + "/auth/reset-password?uid=" +uid);
            emailConfiguration.sendMail(user.getEmail(),html,"CasinoFox Retrieve Password",true);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
