package de.maxhenkel.modupdateserver.serializers;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateSerializer extends StdSerializer<Date> {

    public static final SimpleDateFormat ISO_UTC_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    static {
        ISO_UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected DateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator g, SerializationContext context) {
        g.writeString(ISO_UTC_FORMAT.format(value));
    }

}
