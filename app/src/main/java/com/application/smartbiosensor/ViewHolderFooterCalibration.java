package com.application.smartbiosensor;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.ui.Anchor;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.application.smartbiosensor.database.CalibrationDAO;
import com.application.smartbiosensor.vo.Calibration;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ViewHolderFooterCalibration extends RecyclerView.ViewHolder{

    private Button calibrationButton;
    private Context context;

    public ViewHolderFooterCalibration(LayoutInflater inflater, ViewGroup parent, Context context) {
        super(inflater.inflate(R.layout.calibracao_footer, parent, false));

        this.context = context;

        calibrationButton = (Button) itemView.findViewById(R.id.buttonCalibration);
        calibrationButton.setOnClickListener(calibrationListener);

    }


    protected View.OnClickListener calibrationListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            ArrayList<Pair<Double, Double>> refractiveIndexAndFactor = CalibrationFragment.getRefractiveIndexAndIntensityFactorResults();

            /*refractiveIndexAndFactor = new ArrayList<Pair<Double, Double>>();
            refractiveIndexAndFactor.add(new Pair(1.3529,0.682879709354365));
            refractiveIndexAndFactor.add(new Pair(1.3647,0.623107266294206));
            refractiveIndexAndFactor.add(new Pair(1.3705,0.575643584949084));
            refractiveIndexAndFactor.add(new Pair(1.3793,0.510154537784423));
            refractiveIndexAndFactor.add(new Pair(1.3871,0.450325505081224));*/

            LinearRegressionResult linearRegressionResult = LinearRegression.calculateLinearRegression(refractiveIndexAndFactor);

            Number[] xnum = new Number[refractiveIndexAndFactor.size()];
            Number[] ynum = new Number[refractiveIndexAndFactor.size()];

            for(int i = 0; i < refractiveIndexAndFactor.size(); i++){
                xnum[i] = (Number)(refractiveIndexAndFactor.get(i).first);
                ynum[i] = (Number)(refractiveIndexAndFactor.get(i).second);
            }

            Context context = v.getContext();
            Intent intent = new Intent(context, CalibrationResult.class);
            intent.putExtra(CalibrationResult.LINEAR_REGRESSION_RESULT, linearRegressionResult);
            intent.putExtra(CalibrationResult.X_NUMBERS, xnum);
            intent.putExtra(CalibrationResult.Y_NUMBERS, ynum);
            context.startActivity(intent);

            /*
            linearRegressionEquation.setText("y = " + String.valueOf(linearRegressionResult.getARounded()) + "x + " + String.valueOf(linearRegressionResult.getBRounded()));
            linearRegressionEquation.setVisibility(View.VISIBLE);

            linearRegressionCoefficient.setText("R² = " + String.valueOf(linearRegressionResult.getR2Rounded()));
            linearRegressionCoefficient.setVisibility(View.VISIBLE);

            XYSeries series = new SimpleXYSeries(Arrays.asList(xnum), Arrays.asList(ynum), "Series1");

            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter seriesFormat = new LineAndPointFormatter(context, R.xml.line_point_formatter_with_labels);
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

            double stepRegression = (maxX - minX)/5;
            Number[] xnumRegression = new Number[(int)((maxX - minX)/stepRegression) + 1];
            Number[] ynumRegression = new Number[(int)((maxX - minX)/stepRegression) + 1];
            int countPointsRegression = 0;

            for(double i = minX; i <= maxX; i = i + stepRegression ){
                xnumRegression[countPointsRegression] = (Number) i;
                ynumRegression[countPointsRegression] = (Number) linearRegressionResult.getYGivenX(i);
                countPointsRegression++;
            }

            XYSeries seriesRegression = new SimpleXYSeries(Arrays.asList(xnumRegression), Arrays.asList(ynumRegression), "SeriesRegression");
            LineAndPointFormatter seriesFormatRegression = new LineAndPointFormatter(context, R.xml.line_point_formatter_with_labels_regression);
            seriesFormatRegression.setPointLabelFormatter(null);

            seriesFormatRegression.setPointLabeler(new PointLabeler() {
                DecimalFormat df = new DecimalFormat("#.####");

                @Override
                public String getLabel(XYSeries series, int index) {
                    return df.format(series.getY(index));
                }
            });


            // add a new series' to the xyplot:
            plot.addSeries(series, seriesFormat);
            plot.addSeries(seriesRegression, seriesFormatRegression);

            plot.redraw();


            //FAZER ISSO SE A PESSOA DESEJAR SALVAR A CALIBRAÇÃO
            CalibrationDAO calibrationDAO = new CalibrationDAO(context);

            Calibration calibration = new Calibration();
            calibration.setLinearRegressionResult(linearRegressionResult);

            calibrationDAO.addCalibration(calibration);
            */
        }
    };


}
