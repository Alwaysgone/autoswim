package io.autoswim.swim.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperProvider {
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
		   .registerModule(new Jdk8Module())
		   .registerModule(new JavaTimeModule())
		   .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	
	public ObjectMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}
}
