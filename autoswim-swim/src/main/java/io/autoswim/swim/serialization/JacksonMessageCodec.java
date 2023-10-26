package io.autoswim.swim.serialization;

import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.autoswim.swim.messages.SwimMessage;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.cluster.transport.api.MessageCodec;

public class JacksonMessageCodec implements MessageCodec {

	private final ObjectMapper objectMapper;

	public JacksonMessageCodec() {
		objectMapper = ObjectMapperProvider.OBJECT_MAPPER;
	}
	
	@Override
	public Message deserialize(InputStream is) throws Exception {
		SwimMessage swimMessage = objectMapper.readValue(is, SwimMessage.class);
	    return Message.fromData(swimMessage);
	}

	@Override
	public void serialize(Message message, OutputStream stream) throws Exception {
		objectMapper.writeValue(stream, message.data());
	}
}
