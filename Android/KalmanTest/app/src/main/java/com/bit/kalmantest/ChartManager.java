package com.bit.kalmantest;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChartManager
{
    private LineChart mLineChart;
    ChartManager(LineChart lineChart)
    {
        mLineChart = lineChart;
    }

    public static class ChartDataSet
    {
        private List<Entry> entries;
        private String label;
        public ChartDataSet(List<Entry> entries, String label)
        {
            this.entries = entries;
            this.label = label;
        }
    }

    public void setDataSet(Context context, ChartDataSet ...chartDataSets)
    {
        LineData lineData = new LineData();
        for(ChartDataSet chartDataSet : chartDataSets)
        {
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            LineDataSet lineDataSet = new LineDataSet(chartDataSet.entries, chartDataSet.label);
            lineDataSet.setLineWidth(2f);
            lineDataSet.setValueTextSize(10);
            lineDataSet.setCircleRadius(2);
            lineDataSet.setCircleColor(color);
            lineDataSet.setCircleHoleColor(color);
            lineDataSet.setColor(color);
            lineDataSet.setDrawCircleHole(true);
            lineDataSet.setDrawCircles(true);
            lineDataSet.setDrawHorizontalHighlightIndicator(false);
            lineDataSet.setDrawHighlightIndicators(false);
            lineDataSet.setDrawValues(true);

            lineData.addDataSet(lineDataSet);
        }

        mLineChart.setData(lineData);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(10);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = mLineChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);

        YAxis yRAxis = mLineChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
        yLAxis.setTextSize(20);
        Description description = new Description();
        description.setTextSize(10);
        description.setText("rssi - filtered rssi");
        MarkerView marker = new MarkerView(context, R.layout.graph_marker);
        marker.setChartView(mLineChart);
        mLineChart.setMarker(marker);
        mLineChart.setDoubleTapToZoomEnabled(true);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setDescription(description);
        mLineChart.animateY(2000, Easing.EaseInCubic);
        mLineChart.invalidate();

    }

}
