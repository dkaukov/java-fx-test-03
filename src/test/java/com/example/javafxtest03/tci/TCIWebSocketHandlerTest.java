package com.example.javafxtest03.tci;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TCIWebSocketHandlerTest {

  @Autowired
  private TCITrxState tciTrxState;

  @Autowired
  private TCIWebSocketHandler tciWebSocketHandler;

  @Test
  void parseProtocol() {
    tciWebSocketHandler.parseMessage("protocol:ExpertSDR3,1.9;");
    assertEquals("ExpertSDR3", tciTrxState.getProtocol());
    assertEquals("1.9", tciTrxState.getProtocolVersion());
  }
}