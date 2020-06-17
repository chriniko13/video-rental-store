package com.chriniko.common.infra;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

import java.io.ByteArrayOutputStream;

public class GenericCodec<T> implements MessageCodec<T, T> {

	private final Class<T> cls;

	public GenericCodec(Class<T> cls) {
		super();
		this.cls = cls;
	}

	@Override
	public void encodeToWire(Buffer buffer, T s) {
		KryoPool.use(k -> {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			Output output = new Output(bos);
			k.writeClassAndObject(output, s);
			output.close();

			byte[] yourBytes = output.toBytes();
			buffer.appendInt(yourBytes.length);
			buffer.appendBytes(yourBytes);
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public T decodeFromWire(int pos, Buffer buffer) {

		return KryoPool.use(k -> {
			// My custom message starting from this *position* of buffer
			int p = pos;

			// Length of JSON
			int length = buffer.getInt(p);

			// Jump 4 because getInt() == 4 bytes
			int start = p += 4;
			int end = p + length;

			byte[] yourBytes = buffer.getBytes(start, end);

			Input input = new Input(yourBytes);
			Object result = k.readClassAndObject(input);
			input.close();

			return (T) result;
		});
	}

	@Override
	public T transform(T customMessage) {
		// If a message is sent *locally* across the event bus.
		// This example sends message just as is
		return customMessage;
	}

	@Override
	public String name() {
		// Each codec must have a unique name.
		// This is used to identify a codec when sending a message and for unregistering
		// codecs.
		return cls.getSimpleName() + "Codec";
	}

	@Override
	public byte systemCodecID() {
		// Always -1
		return -1;
	}
}
