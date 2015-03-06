package io.vertx.serviceregistry.handlers;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.apex.handler.sockjs.SockJSHandler;
import io.vertx.ext.apex.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.apex.handler.sockjs.SockJSSocket;

import java.util.ArrayList;
import java.util.List;

public class SockJSFactory {

    private static List<SockJSSocket> clients = new ArrayList<SockJSSocket>();

    public static SockJSHandler createSocketHandler(Vertx vertx) {
        SockJSHandlerOptions sockOptions = new SockJSHandlerOptions();
        SockJSHandler handler = SockJSHandler.create(vertx, sockOptions);

        handler.socketHandler(sockJSSocket -> {
            clients.add(sockJSSocket);
            sockJSSocket.handler(data -> {

            });
            sockJSSocket.endHandler(end -> {
                clients.remove(sockJSSocket);
            });
        });

        return handler;
    }

    public static void notifyClients(JsonObject message) {
        clients.forEach(client -> {
            client.write(Buffer.buffer(message.toString()));
        });
    }
}
