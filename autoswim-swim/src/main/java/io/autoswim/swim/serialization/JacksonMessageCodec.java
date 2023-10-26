package io.autoswim.swim.serialization;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.autoswim.swim.messages.SwimMessage;
import io.scalecube.cluster.transport.api.Message;
import io.scalecube.cluster.transport.api.Message.Builder;
import io.scalecube.cluster.transport.api.MessageCodec;

public class JacksonMessageCodec implements MessageCodec {

	private final ObjectMapper objectMapper;

	public JacksonMessageCodec() {
		objectMapper = ObjectMapperProvider.OBJECT_MAPPER;
	}

	@Override
	public Message deserialize(InputStream is) throws Exception {
		try (ObjectInputStream ois = new ObjectInputStream(is)) {
			// headers
			int headersSize = ois.readInt();
			Map<String, String> headers = new HashMap<>(headersSize);
			for (int i = 0; i < headersSize; i++) {
				String name = ois.readUTF();
				String value = (String) ois.readObject(); // value is nullable
				headers.put(name, value);
			}
			Builder messageBuilder = Message.withHeaders(headers);
			byte[] remainingBytes = ois.readAllBytes();
			if(headers.containsKey("swim")) {
				// autoswim messages are handled separately so that its object mapping works correctly
				SwimMessage swimMessage = objectMapper.readValue(remainingBytes, SwimMessage.class);
				messageBuilder.data(swimMessage);
			} else {
				messageBuilder.data(ois.readObject());
			}
			return messageBuilder
			.build();
		}
	}

	@Override
	public void serialize(Message message, OutputStream stream) throws Exception {
		try (ObjectOutputStream oos = new ObjectOutputStream(stream)) {
			// headers
			int headerSize = message.headers().size();
			Map<String, String> headers = new HashMap<>(message.headers());
			if(message.data() instanceof SwimMessage) {
				headerSize++;
				headers.put("swim", "true");
			}
			oos.writeInt(headerSize);
			for (Entry<String, String> header : headers.entrySet()) {
				oos.writeUTF(header.getKey());
				oos.writeObject(header.getValue()); // value is nullable
			}
			// data
			if(message.data() instanceof SwimMessage) {
				byte[] data = objectMapper.writeValueAsBytes(message.data());
				oos.write(data);
			} else {
				oos.writeObject(message.data());
			}
			oos.flush();
		}
	}
}
