package com.application.smartbiosensor;

import android.util.Pair;

import java.util.ArrayList;

public class LinearRegression {

    public static LinearRegressionResult calculateLinearRegression(ArrayList<Pair<Double, Double>> xy){

        int n;
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        double xbar, ybar;
        double a, b, r2;
        double ssr = 0.0;      // regression sum of squares

        for(n = 0; n < xy.size(); n++) {
            sumx  += xy.get(n).first;
            sumx2 += xy.get(n).first * xy.get(n).first;
            sumy  += xy.get(n).second;
        }

        xbar = sumx / n;
        ybar = sumy / n;

        for (int i = 0; i < n; i++) {
            xxbar += (xy.get(i).first - xbar) * (xy.get(i).first - xbar);
            yybar += (xy.get(i).second - ybar) * (xy.get(i).second - ybar);
            xybar += (xy.get(i).first - xbar) * (xy.get(i).second - ybar);
        }

        a = xybar / xxbar;
        b = ybar - a * xbar;

        for (int i = 0; i < n; i++) {
            double fit = a*xy.get(i).first + b;
            ssr += (fit - ybar) * (fit - ybar);
        }

        r2    = ssr / yybar;

        return new LinearRegressionResult(a, b, r2);

    }

}
