package com.example.javafxtest03.tci;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TCIWebSocketHandler extends AbstractWebSocketHandler {

  @Autowired
  private TCITrxState tciTrxState;

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    super.afterConnectionEstablished(session);
    log.info("TCI: established connection - " + session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    log.trace("TCI: {}", message.getPayload());
    parseMessage(message.getPayload());
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
    log.trace("TCI: {}", message.getPayload());
    TCIIQData payload = TCIIQData.deserialize(message.getPayload());
    applicationEventPublisher.publishEvent(payload);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    log.info("TCI: closed connection - " + session);
  }

  void parseMessage(String message) {
    String[] payload = message.split(":");
    if (payload.length == 2) {
      if (this.parseCommand(payload[0],
          payload[1].split(";")[0].split(","))) {
        log.info("<<: \"{}\"", message);
      }
    }
  }

  boolean parseCommand(String cmd, String[] params) {
    return tciTrxState.parseCommand(cmd, params);
  }

}
