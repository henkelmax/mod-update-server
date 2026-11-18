package de.maxhenkel.modupdateserver.serializers;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.jdk.JavaUtilDateDeserializer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDeserializer extends JavaUtilDateDeserializer {

    public static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final SimpleDateFormat ISO_FORMAT_NO_MILLISECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    public static final SimpleDateFormat ISO_FORMAT_NO_SECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        try {
            return super.deserialize(jsonParser, deserializationContext);
        } catch (JacksonException e1) {
            String date = jsonParser.getString();
            try {
                return ISO_FORMAT.parse(date);
            } catch (ParseException e2) {
                try {
                    return ISO_FORMAT_NO_MILLISECONDS.parse(date);
                } catch (ParseException e3) {
                    try {
                        return ISO_FORMAT_NO_SECONDS.parse(date);
                    } catch (ParseException e4) {
                        throw e1;
                    }
                }
            }
        }
    }

}
