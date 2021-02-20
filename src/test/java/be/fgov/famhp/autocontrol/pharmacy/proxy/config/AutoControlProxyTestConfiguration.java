package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AutoControlProxyTestConfiguration {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
