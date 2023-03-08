package com.example.javafxtest03.fx;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.example.javafxtest03.tci.TCIIQData;
import com.github.psambit9791.jdsp.windows.Triangular;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.Synchronized;

@Component
@Data
public class MainModel {

  private StringProperty text = new SimpleStringProperty("bound");
  private DoubleProperty swr = new SimpleDoubleProperty(1.01);
  private DoubleProperty cpuLoad = new SimpleDoubleProperty(0.0);

  private DoubleProperty avg = new SimpleDoubleProperty(-100.0);
  private SimpleStringProperty avgTxt = new SimpleStringProperty("-100.00");

  private ObjectProperty<DoubleDataSet> datasetFiltered = new SimpleObjectProperty<>(new DoubleDataSet("filtered"));
  private ObjectProperty<DoubleDataSet> datasetRaw = new SimpleObjectProperty<>(new DoubleDataSet("raw"));

  List<List<Complex>> boundedQueue = new LinkedList<>();
  double[] powerFiltered = new double[2048*128];
  double sum = 0;

  @Scheduled(fixedRate = 250)
  public void measureCpu() {
    cpuLoad.set(getProcessCpuLoad());
  }

  @EventListener
  @Synchronized
  public void OnIQ(TCIIQData data) {
    boundedQueue.add(data.getSignal());
    if (boundedQueue.size() <  8) {
      return;
    }
    Complex[] signal = boundedQueue.stream()
        .flatMap(Collection::stream)
        .toArray(Complex[]::new);
    double[] window = new Triangular(signal.length).getWindow();

    for (int i = 0; i < signal.length; i++) {
      //signal[i] = signal[i].multiply(new Complex(window[i], 0.0));
      signal[i] = signal[i].multiply(window[i]);
    }

    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
    double[] power = Arrays.stream(fft.transform(signal, TransformType.FORWARD))
        .mapToDouble(c -> c.getReal() * c.getReal() + c.getImaginary() * c.getImaginary())
        .map(v -> 10.0 * Math.log10(v / data.getSampleRate() / 2.0))
        .toArray();

    double[] freq = new double[power.length];
    for (int i = 0; i < power.length; i++) {
      freq[i] = -data.getSampleRate() / 2.0 + 1.0 * data.getSampleRate() / power.length * i;
    }
    for (int i = 0; i < power.length / 2; i++) {
      double swap = power[i];
      power[i] = power[power.length / 2 + i];
      power[power.length / 2 + i] = swap;
    }

    int from = power.length / 10;
    int to = power.length - from;

    power = Arrays.copyOfRange(power, from, to);
    freq = Arrays.copyOfRange(freq, from, to);

    sum = 0;
    for (int i = 0; i < power.length; i++) {
      if (!Double.isInfinite(power[i])) {
        powerFiltered[i] = powerFiltered[i] * 0.9 + power[i] * 0.1;
        if (Double.isInfinite(powerFiltered[i])) {
          powerFiltered[i] = 0;
        }
        sum = sum + power[i];
      }
    }
    sum = sum / power.length;

    Platform.runLater(() -> {
      if (avg.get() != sum) {
        avg.set(sum);
        avgTxt.set(String.format("%.2f", avg.get()));
      }
    });


    datasetFiltered.get().set(freq, powerFiltered, freq.length, false);
    datasetRaw.get().set(freq, power, freq.length, false);
    //boundedQueue.subList(0, 2048).clear();
    boundedQueue.subList(0, 2).clear();
  }

  public Double getProcessCpuLoad() {
    try {
      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
      ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
      AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

      return Optional.ofNullable(list)
          .map(l -> l.isEmpty() ? null : l)
          .map(List::iterator)
          .map(Iterator::next)
          .map(Attribute.class::cast)
          .map(Attribute::getValue)
          .map(Double.class::cast)
          .orElse(null);

    } catch (Exception ex) {
      return null;
    }
  }

}
