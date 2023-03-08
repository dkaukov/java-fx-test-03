package com.example.javafxtest03.tci;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Component
@Slf4j
public class TCITrxState {

  @Data
  public static class ChannelState {
    private int vfo;
    private int iFace;
  }

  @Data
  public static class TrxState {
    private boolean rxEnabled = true;
    private boolean txEnabled;
    private int dds;
    private boolean isIQEnabled;
    private String modulation;
    private List<ChannelState> ch = new ArrayList<>();
  }

  private String protocol;
  private String protocolVersion;
  private String device;
  private int trxCount;
  private int channelCount;
  private int iqSampleRate;
  private int audioSampleRate;
  private List<TrxState> trx = new ArrayList<>();

  boolean parseCommand(String cmd, String[] params) {
    switch (cmd) {
      case "protocol":
        this.setProtocol(params[0]);
        this.setProtocolVersion(params[1]);
        return true;
      case "device":
        this.setDevice(params[0]);
        return true;
      case "trx_count":
        this.setTrxCount(Integer.parseInt(params[0]));
        this.getTrx().clear();
        for (int i = 0; i < getTrxCount(); i++) {
          this.getTrx().add(new TrxState());
        }
        return true;
      case "channels_count":
        this.setChannelCount(Integer.parseInt(params[0]));
        this.getTrx().forEach(trx -> {
          trx.getCh().clear();
          for (int i = 0; i < getChannelCount(); i++) {
            trx.getCh().add(new ChannelState());
          }
        });
        return true;
      case "iq_samplerate":
        this.setIqSampleRate(Integer.parseInt(params[0]));
        return true;
      case "audio_samplerate":
        this.setAudioSampleRate(Integer.parseInt(params[0]));
        return true;
      case "dds":
        this.getTrx().get(Integer.parseInt(params[0])).setDds(Integer.parseInt(params[1]));
        return true;
      case "if":
        this.getTrx()
            .get(Integer.parseInt(params[0])).getCh()
            .get(Integer.parseInt(params[1]))
            .setIFace(Integer.parseInt(params[2]));
        return true;
      case "vfo":
        this.getTrx()
            .get(Integer.parseInt(params[0])).getCh()
            .get(Integer.parseInt(params[1]))
            .setVfo(Integer.parseInt(params[2]));
        return true;
      case "modulation":
        this.getTrx().get(Integer.parseInt(params[0])).setModulation(params[1]);
        return true;
      case "iq_stop":
        this.getTrx().get(Integer.parseInt(params[0])).setIQEnabled(false);
        return true;
      case "iq_start":
        this.getTrx().get(Integer.parseInt(params[0])).setIQEnabled(true);
        return true;
      case "rx_enable":
        this.getTrx().get(Integer.parseInt(params[0])).setRxEnabled(Boolean.parseBoolean(params[1]));
        return true;
      case "tx_enable":
        this.getTrx().get(Integer.parseInt(params[0])).setTxEnabled(Boolean.parseBoolean(params[1]));
        return true;
      default:
        return false;
    }
  }

}
