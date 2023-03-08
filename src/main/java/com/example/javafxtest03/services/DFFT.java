package com.example.javafxtest03.services;

import com.example.javafxtest03.tci.TCIIQData;
import com.example.javafxtest03.tci.TCITrxState;
import com.github.psambit9791.jdsp.transform.FastFourier;
import com.github.psambit9791.jdsp.transform._Fourier;

import org.apache.commons.math3.complex.Complex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class DFFT {

  @Autowired
  private TCITrxState trxState;

  @EventListener
  public void OnIQ(TCIIQData data) {
    FastFourier ft = new FastFourier(data.getSignal().stream()
        .mapToDouble(Complex::getReal)
        .toArray());
    ft.transform();
    double[] power = ft.getMagnitude(true);
    double[] freq = ft.getFFTFreq(data.getSampleRate(), true);

    ft = new FastFourier(data.getSignal().stream()
        .mapToDouble(Complex::getImaginary)
        .toArray());
    ft.transform();
    double[] powerIm = ft.getMagnitude(true);
    double[] freqIm = ft.getFFTFreq(data.getSampleRate(), true);
  }

}
