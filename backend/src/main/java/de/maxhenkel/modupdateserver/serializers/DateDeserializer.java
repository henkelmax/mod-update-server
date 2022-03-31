package de.maxhenkel.modupdateserver.serializers;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateDeserializer extends DateDeserializers.DateDeserializer {

    public static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    public static final SimpleDateFormat ISO_FORMAT_NO_MILLISECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        try {
            return super.deserialize(jsonParser, deserializationContext);
        } catch (Exception e1) {
            String date = jsonParser.getText();
            try {
                return ISO_FORMAT.parse(date);
            } catch (ParseException e2) {
                try {
                    return ISO_FORMAT_NO_MILLISECONDS.parse(date);
                } catch (ParseException e3) {
                    throw new IOException(e1);
                }
            }
        }
    }

}
