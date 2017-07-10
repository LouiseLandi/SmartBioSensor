package com.application.smartbiosensor;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.application.smartbiosensor.database.CalibrationDAO;
import com.application.smartbiosensor.vo.Calibration;

import java.text.DecimalFormat;
import java.util.Arrays;

public class CalibrationResult extends AppCompatActivity {

    private XYPlot plot;
    private Button discardCalibrationButton;
    private Button saveCalibrationButton;
    private TextView linearRegressionEquation;
    private TextView linearRegressionCoefficient;
    private LinearRegressionResult linearRegressionResult;

    public static final String LINEAR_REGRESSION_RESULT = "Linear Regression Result";
    public static final String X_NUMBERS = "X Numbers";
    public static final String Y_NUMBERS = "Y Numbers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resultado_calibracao);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.toolbar_calibration_result);

        discardCalibrationButton = (Button) findViewById(R.id.buttonDiscardCalibration);
        discardCalibrationButton.setOnClickListener(discardCalibrationListener);

        saveCalibrationButton = (Button) findViewById(R.id.buttonSaveCalibration);
        saveCalibrationButton.setOnClickListener(saveCalibrationListener);

        plot = (XYPlot) findViewById(R.id.plotCalibration);

        plot.setRangeLabel("Fator Intensidade Normalizado");
        plot.setDomainLabel("Índice de Refração");

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new DecimalFormat("#.##"));
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#.##"));

        plot.getLayoutManager().remove(plot.getLegend());

        plot.getGraph().getBackgroundPaint().setColor(Color.WHITE);
        plot.getGraph().getGridBackgroundPaint().setColor(Color.WHITE);
        plot.getBackgroundPaint().setColor(Color.WHITE);
        plot.getBorderPaint().setColor(Color.TRANSPARENT);

        linearRegressionEquation = (TextView) findViewById((R.id.linearRegressionEquationCalibration));
        linearRegressionCoefficient = (TextView) findViewById((R.id.linearRegressionCoefficientCalibration));

        linearRegressionResult = (LinearRegressionResult) getIntent().getSerializableExtra(LINEAR_REGRESSION_RESULT);
        Number[] xnum = (Number[])getIntent().getSerializableExtra(X_NUMBERS);
        Number[] ynum = (Number[])getIntent().getSerializableExtra(Y_NUMBERS);

        showGraphResults(xnum, ynum);

    }

    protected View.OnClickListener discardCalibrationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            onBackPressed();
        }
    };


    protected View.OnClickListener saveCalibrationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            CalibrationDAO calibrationDAO = new CalibrationDAO(getApplicationContext());

            Calibration calibration = new Calibration();
            calibration.setLinearRegressionResult(linearRegressionResult);

            calibrationDAO.addCalibration(calibration);

        }
    };

    private void showGraphResults(Number[] xnum, Number[] ynum){

        linearRegressionEquation.setText("y = " + String.valueOf(linearRegressionResult.getARounded()) + "x + " + String.valueOf(linearRegressionResult.getBRounded()));
        linearRegressionCoefficient.setText("R² = " + String.valueOf(linearRegressionResult.getR2Rounded()));

        XYSeries series = new SimpleXYSeries(Arrays.asList(xnum), Arrays.asList(ynum), "Series1");

        LineAndPointFormatter seriesFormat = new LineAndPointFormatter(getApplicationContext(), R.xml.line_point_formatter_with_labels);
        seriesFormat.getLinePaint().setColor(Color.TRANSPARENT);
        seriesFormat.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat("#.####");

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });

        double step = 0.01;
        Arrays.sort(xnum);
        Arrays.sort(ynum);

        double minX = (double)xnum[0];
        double maxX = (double)xnum[xnum.length - 1];
        double minY = (double)ynum[0];
        double maxY = (double)ynum[ynum.length - 1];

        plot.setRangeBoundaries(minY - step, maxY + step, BoundaryMode.FIXED);
        plot.setDomainBoundaries(minX - step, maxX + step, BoundaryMode.FIXED);

        int numberPointsRegression = 6;
        int countPointsRegression = 0;
        double stepRegression = (maxX - minX)/(numberPointsRegression - 1);

        Number[] xnumRegression = new Number[numberPointsRegression];
        Number[] ynumRegression = new Number[numberPointsRegression];

        for(double i = minX; countPointsRegression < numberPointsRegression; i = i + stepRegression ){
            xnumRegression[countPointsRegression] = (Number) i;
            ynumRegression[countPointsRegression] = (Number) linearRegressionResult.getYGivenX(i);
            countPointsRegression++;
        }

        XYSeries seriesRegression = new SimpleXYSeries(Arrays.asList(xnumRegression), Arrays.asList(ynumRegression), "SeriesRegression");
        LineAndPointFormatter seriesFormatRegression = new LineAndPointFormatter(getApplicationContext(), R.xml.line_point_formatter_with_labels_regression);
        seriesFormatRegression.setPointLabelFormatter(null);
        seriesFormatRegression.setPointLabeler(new PointLabeler() {
            DecimalFormat df = new DecimalFormat("#.####");

            @Override
            public String getLabel(XYSeries series, int index) {
                return df.format(series.getY(index));
            }
        });

        plot.addSeries(series, seriesFormat);
        plot.addSeries(seriesRegression, seriesFormatRegression);

        plot.redraw();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



}