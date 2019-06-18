package com.bit.kalmantest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class MainActivity extends AppCompatActivity
{
    private final static String TAG = "KALMAN";
    private LineChart lineChart;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Kalmanfilter kalmanfilter = new Kalmanfilter(0);
        lineChart = (LineChart)findViewById(R.id.chart);
        final TextView qText = findViewById(R.id.q_text);
        final TextView rText = findViewById(R.id.r_text);
        SeekBar qSeekBar = findViewById(R.id.q_seekbar);
        qSeekBar.setMax(5000);
        SeekBar rSeekBar = findViewById(R.id.r_seekbar);
        rSeekBar.setMax(5000);
        final List<Entry> rssi_entries = new ArrayList<>();
        final List<Entry> filtered_rssi_entries = new ArrayList<>();
        final ChartManager chartManager = new ChartManager(lineChart);
        updateData(kalmanfilter, rssi_entries, filtered_rssi_entries, chartManager);

        qSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double Q = progress * 0.0001;
                qText.setText("Q: "+String.format("%.5f", Q));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double Q = seekBar.getProgress() * 0.0001;
                qText.setText("Q: "+String.format("%.5f", Q));
                kalmanfilter.setQ(Q);
                updateData(kalmanfilter, rssi_entries, filtered_rssi_entries, chartManager);
            }
        });
        rSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                double R = progress * 0.0001;
                rText.setText("R: "+String.format("%.5f", R));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double R = seekBar.getProgress() * 0.0001;
                rText.setText("R: "+String.format("%.5f", R));
                kalmanfilter.setR(R);
                updateData(kalmanfilter, rssi_entries, filtered_rssi_entries, chartManager);
            }
        });
        qSeekBar.setProgress(50);
        rSeekBar.setProgress(100);


    }


    private void updateData(Kalmanfilter kalmanfilter, List<Entry> rssi_entries, List<Entry> filtered_rssi_entries, ChartManager chartManager)
    {
        int x = 1;
        rssi_entries.clear();
        filtered_rssi_entries.clear();
        kalmanfilter.setInitValue(-38);
        kalmanfilter.reset();
        for(int rssi = -38; rssi > -100; rssi-=1)
        {
            double filteredRssi;
            if((x % 10) == 0) {
                filteredRssi = kalmanfilter.update(rssi - 10);
                Log.i(TAG, "[" + x + "]rssi : " + (rssi - 10) + ", filteredRssi: " + filteredRssi);
                rssi_entries.add(new Entry(x, rssi-10));
            }
            else {
                filteredRssi = kalmanfilter.update(rssi);
                Log.i(TAG, "[" + x + "]rssi : " + rssi + ", filteredRssi: " + filteredRssi);
                rssi_entries.add(new Entry(x, rssi));
            }
            filtered_rssi_entries.add(new Entry(x, (float)filteredRssi));
            x++;
        }
        chartManager.setDataSet(getApplicationContext(), new ChartManager.ChartDataSet(rssi_entries, "rssi"), new ChartManager.ChartDataSet(filtered_rssi_entries, "filtered_rssi"));

    }


}
