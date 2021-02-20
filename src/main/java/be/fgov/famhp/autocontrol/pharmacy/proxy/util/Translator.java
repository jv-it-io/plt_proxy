package be.fgov.famhp.autocontrol.pharmacy.proxy.util;

import be.fgov.famhp.plato.backoffice.domain.enumeration.LanguageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Translator {

//    private MessageSource messageSource;
//
//    @Autowired
//    public Translator(MessageSource messageSource) {
//        this.messageSource = messageSource;
//    }
//
//    public String translate(String key, LanguageEnum language) {
//        return messageSource.getMessage(key, new Object[]{}, Locale.forLanguageTag(language.name()));
//    }
}
