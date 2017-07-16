/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package red.skull;

import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author Monroe
 */
public class SpectrumListener implements AudioSpectrumListener{
    
    private SpectrumBar[] bars;
    private double startFreq = 0;
    private MediaPlayer mp;
    private double minValue;
    private int[] spectrumBucketCounts;
    private double[] norms;
    private int bandCount ;
    
    
    public SpectrumListener(double startFreq, MediaPlayer mp, SpectrumBar[] bars){
        this.bars = bars;
        this.startFreq = startFreq;
        this.mp = mp;
        
        
        minValue = mp.getAudioSpectrumThreshold();
        norms = createNormArray();
        bandCount = mp.getAudioSpectrumNumBands();
        spectrumBucketCounts = createBucketCounts(startFreq, bandCount);
        
    }
    
    
    public void spectrumDataUpdate(double timestamp,
                         double duration,
                         float[] magnitudes,
                         float[] phases){
    int index = 0;
    int bucketIndex = 0;
    int currentBucketCount = 0;
    double sum = 0.0;

    while (index < magnitudes.length) {
      sum += magnitudes[index] - minValue;
      currentBucketCount += 1;

      if (currentBucketCount >= spectrumBucketCounts[bucketIndex]) {
        bars[bucketIndex].setValue(sum / norms[bucketIndex]);
        currentBucketCount = 0;
        sum = 0.0;
        bucketIndex += 1;
      }

      index += 1;
    }
  }
    
    
    private int[] createBucketCounts(double startFreq,int bandCount){
    int[] bucketCounts = new int[bars.length];

    double bandwidth = 22050.0 / bandCount;
    double centerFreq = bandwidth / 2;
    double currentSpectrumFreq = centerFreq;
    double currentEQFreq = startFreq / 2;
    double currentCutoff = 0d;
    int currentBucketIndex = -1;

    for (int i = 0; i<bandCount; i++) {
      if (currentSpectrumFreq > currentCutoff) {
        currentEQFreq *= 2;
        currentCutoff = currentEQFreq + currentEQFreq / 2;
        currentBucketIndex += 1;
      }

      if (currentBucketIndex < bucketCounts.length) {
        bucketCounts[currentBucketIndex] += 1;
        currentSpectrumFreq += bandwidth;
      }
    }

    return bucketCounts;
  }

    
    
    private double[] createNormArray() {
    double[] normArray = new double[bars.length];
    double currentNorm = 0.05;
    for (int i = 0; i<normArray.length; i++) {
      normArray[i] = 1 + currentNorm;
      currentNorm *= 2;
    }
    return normArray;
  }
    
    
}
