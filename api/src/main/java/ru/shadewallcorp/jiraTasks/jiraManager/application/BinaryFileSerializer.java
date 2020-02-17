package ru.shadewallcorp.jiraTasks.jiraManager.application;

import akka.util.ByteString;
import com.lightbend.lagom.javadsl.api.deser.DeserializationException;
import com.lightbend.lagom.javadsl.api.deser.SerializationException;
import com.lightbend.lagom.javadsl.api.deser.StrictMessageSerializer;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.NotAcceptable;
import com.lightbend.lagom.javadsl.api.transport.UnsupportedMediaType;

import java.util.List;
import java.util.Optional;

public class BinaryFileSerializer implements StrictMessageSerializer<ByteString> {

    static class ByteStringSerializer implements NegotiatedSerializer<ByteString, ByteString>, NegotiatedDeserializer<ByteString, ByteString> {

        @Override
        public ByteString serialize(ByteString byteString) throws SerializationException {
            return byteString;
        }

        @Override
        public ByteString deserialize(ByteString wire) throws DeserializationException {
            return wire;
        }
    }

    @Override
    public NegotiatedSerializer<ByteString, ByteString> serializerForRequest() {
        return new ByteStringSerializer();
    }

    @Override
    public NegotiatedDeserializer<ByteString, ByteString> deserializer(MessageProtocol protocol) throws UnsupportedMediaType {
        return new ByteStringSerializer();
    }

    @Override
    public NegotiatedSerializer<ByteString, ByteString> serializerForResponse(List<MessageProtocol> acceptedMessageProtocols) throws NotAcceptable {
        return new ByteStringSerializer();
    }
}
