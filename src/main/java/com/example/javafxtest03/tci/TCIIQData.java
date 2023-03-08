package com.example.javafxtest03.tci;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.complex.Complex;

import lombok.Builder;
import lombok.Data;

/*
 typedef struct
 {
     quint32 receiver; //!< номер приёмника
     quint32 sampleRate; //!< частота дискретизации
     quint32 format; //!< всегда равен 4 (float 32 bit)
     quint32 codec; //!< алгоритм сжатия (не реализовано), всегда 0
     quint32 crc; //!< контрольная сумма (не реализовано), всегда 0
     quint32 length; //!< длина поля данных
     quint32 type; //!< тип потока данных
     quint32 reserv[9]; //!< зарезервировано
     float data[4096]; //!< поле данных
 }DataStream;
 */
@Data
@Builder
public class TCIIQData {

  public enum StreamType {
    IQ(0),
    RX_AUDIO(1),
    TX_AUDIO(2),
    TX_CHRONOS(3),
    LINE_OUT(4);
    private final int id;
    StreamType(int id) {
      this.id = id;
    }
    public static StreamType fromId(int id) {
      for (StreamType type : values()) {
        if (type.id == id) {
          return type;
        }
      }
      return null;
    }
  }

  public enum SampleType {
    INT_16(0),
    INT_24(1),
    INT_32(2),
    FLOAT_32(3);
    private final int id;
    SampleType(int id) {
      this.id = id;
    }
    public static SampleType fromId(int id) {
      for (SampleType type : values()) {
        if (type.id == id) {
          return type;
        }
      }
      return null;
    }
  }

  private final int receiver;      //!< номер приёмника
  private final int sampleRate;    //!< частота дискретизации
  private final SampleType format; //!< SampleType
  private final int codec;         //!< алгоритм сжатия (не реализовано), всегда 0
  private final int crc;           //!< контрольная сумма (не реализовано), всегда 0
  private final int length;        //!< длина поля данных
  private final StreamType type;   //!< тип потока данных
  private final int channels;      //!< количество каналов
  private final int reserve;
  private final List<Complex> signal;

  public static TCIIQData deserialize(ByteBuffer b) {
    ByteBuffer buff = b.order(ByteOrder.LITTLE_ENDIAN);
    return TCIIQData.builder()
        .receiver(buff.getInt())
        .sampleRate(buff.getInt())
        .format(SampleType.fromId(buff.getInt()))
        .codec(buff.getInt())
        .crc(buff.getInt())
        .length(buff.getInt())
        .type(StreamType.fromId(buff.getInt()))
        .channels(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .reserve(buff.getInt())
        .signal(toComplexList(buff))
        .build();
  }

  private static List<Complex> toComplexList(ByteBuffer buff) {
    byte[] bytes = new byte[buff.remaining()];
    buff.get(bytes);
    FloatBuffer fb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
    return IntStream.range(0, fb.limit() / 2)
        .mapToObj(i -> readValue(fb))
        .collect(Collectors.toList());
  }

  private static Complex readValue(FloatBuffer fb) {
    float real = fb.get();
    float imaginary = fb.get();
    return new Complex(real, imaginary);
  }

  private static float[] toFloatArray(byte[] bytes) {
    FloatBuffer fb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
    float[] floatArray = new float[fb.limit()];
    fb.get(floatArray);
    return floatArray;
  }
}
