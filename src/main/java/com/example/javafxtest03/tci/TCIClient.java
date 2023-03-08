package com.example.javafxtest03.tci;

import java.io.IOException;
import java.net.URI;

import com.example.javafxtest03.tci.TCIWebSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TCIClient extends StandardWebSocketClient {

  @Autowired
  private TCIWebSocketHandler tciWebSocketHandler;
  private WebSocketSession webSocketSession = null;
  @Value("${tci.address:ws://localhost:50001}")
  private String tciAddress;

  public void connect() {
    doHandshake(tciWebSocketHandler,
        new WebSocketHttpHeaders(),
        URI.create(tciAddress))
        .completable()
        .exceptionally(throwable -> {
          log.warn("TCI: {}", throwable.getMessage());
          return null;
        })
        .thenAccept(wss -> {
          this.webSocketSession = wss;
          try {
            this.webSocketSession.setBinaryMessageSizeLimit(32768);
            this.webSocketSession.sendMessage(new TextMessage("iq_samplerate:384000;"));
            //this.webSocketSession.sendMessage(new TextMessage("iq_samplerate:192000;"));
            this.webSocketSession.sendMessage(new TextMessage("iq_start:0;"));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
  }

  @Scheduled(fixedRate = 5000)
  public void tryConnect() {
    if ((webSocketSession == null) || (!webSocketSession.isOpen())) {
      connect();
    }
  }
}