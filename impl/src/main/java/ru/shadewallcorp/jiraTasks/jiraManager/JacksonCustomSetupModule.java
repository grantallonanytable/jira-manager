package ru.shadewallcorp.jiraTasks.jiraManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.zalando.jackson.datatype.money.MoneyModule;

/**
 * Сериализация даты и времени для ES.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class JacksonCustomSetupModule extends SimpleModule {
    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);
        ObjectMapper owner = context.getOwner();
        // Allows to write timezone (not TimeZoneId)
        owner.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Forces to write TimeZoneId
        owner.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        // Disables converting to UTC timezone at deserialization
        owner.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        // https://github.com/zalando/jackson-datatype-money
        owner.registerModule(new MoneyModule().withQuotedDecimalNumbers());
        // Для правильной сериализации часового пояса в дате
        owner.findAndRegisterModules();
    }
}
