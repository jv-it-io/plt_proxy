package be.fgov.famhp.autocontrol.pharmacy.proxy.config;

import be.fgov.fagg.common.config.FamhpDatabaseAutoConfiguration;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import io.github.jhipster.domain.util.JSR310DateConverters.DateToZonedDateTimeConverter;
import io.github.jhipster.domain.util.JSR310DateConverters.ZonedDateTimeToDateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableMongoRepositories("be.fgov.famhp.autocontrol.pharmacy.proxy.repository")
@Import(value = {FamhpDatabaseAutoConfiguration.class})
public class DatabaseConfiguration {

    private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
        return new ValidatingMongoEventListener(validator());
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(DateToZonedDateTimeConverter.INSTANCE);
        converters.add(ZonedDateTimeToDateConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }

    @Bean
    @Primary
    public MongoClient mongo(MongoProperties properties, ObjectProvider<MongoClientOptions> options, Environment environment) {
        return (new MongoClientFactory(properties, environment)).createMongoClient((MongoClientOptions)options.getIfAvailable());
    }

    @Bean()
    @Primary
    public MongoDbFactory mongoDbFactory(MongoProperties mongoProperties,  Environment environment) throws Exception {
        log.info("----Create MongoFactory ------");
        log.info("Env = " + environment.toString());
        log.info("Host = " + mongoProperties.getHost());
        log.info("DBName = " + mongoProperties.getDatabase());
        log.info("uri = " + mongoProperties.getUri());
        return new SimpleMongoDbFactory(new MongoClientFactory(mongoProperties,environment).
            createMongoClient(MongoClientOptions.builder().build()),
            mongoProperties.getDatabase());
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MongoConverter mongoConverter) throws Exception {
        return new MongoTemplate(mongoDbFactory,mongoConverter);
    }

}
