package VERTIGO.plots;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LinePlot extends JComponent {

    XYChart chart;
    XChartPanel<XYChart> chartPanel;

    Color[] colors = new Color[]{
            new Color(47, 132, 220),
            new Color(97, 220, 108),
            new Color(220, 97, 79),
            new Color(112, 220, 219),
            new Color(220, 215, 44),
            new Color(220, 79, 194),
            new Color(90, 220, 169),
            new Color(220, 134, 44),
            new Color(147, 71, 220),
            new Color(161, 220, 46),
            new Color(220, 113, 135),
            new Color(77, 70, 220),
            new Color(41, 220, 58),
            new Color(220, 29, 49),
            new Color(21, 187, 220),
    };

    private List<Double> yConv, yScorePlus, yScoreMinus, yScore, yWin, yLose;
    private List<Double> xScorePlus, xScoreMinus, xWin, xLose;
    XYSeries seriesConvergence, seriesScorePlus, seriesScoreMinus, seriesScore, seriesWin, seriesLose;

    private File actFile, evoFile;

    public LinePlot() {
        reset();

        // Create Chart
        chart = new XYChartBuilder()
                .width(600)
                .height(300)
                .title("")
                .xAxisTitle("game tick")
                .yAxisTitle("convergence")
                .build();
        chart.getStyler().setChartTitleVisible(false);
        chart.getStyler().setLegendVisible(true);
        chart.getStyler().setMarkerSize(10);
        chart.getStyler().setAxisTitleFont(new Font("Arial", Font.BOLD, 20));
        chart.getStyler().setAxisTickLabelsFont(new Font("Arial", Font.PLAIN, 16));
        chart.getStyler().setLegendFont(new Font("Arial", Font.PLAIN, 18));
        chart.getStyler().setPlotBackgroundColor(Color.white);
        chart.getStyler().setChartBackgroundColor(Color.white);

        // Chart
        chartPanel = new XChartPanel<>(chart);
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);

        // Display the window.
        setVisible(true);
    }

    public void reset() {
        yConv = new ArrayList<>();
        yScorePlus = new ArrayList<>();
        xScorePlus = new ArrayList<>();
        yScoreMinus = new ArrayList<>();
        xScoreMinus = new ArrayList<>();
        yScore = new ArrayList<>();
        yWin = new ArrayList<>();
        yLose = new ArrayList<>();
        xWin = new ArrayList<>();
        xLose = new ArrayList<>();
    }

    public void setDataFiles(String act, String evo) {
        try {
            this.actFile = new File(act);
        } catch (Exception e) {
            this.actFile = null;
        }

        try {
            this.evoFile = new File(evo);
        } catch (Exception e) {
            this.evoFile = null;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        // Update data
        if (actFile != null && evoFile != null) {
            // Files not null, read last line and add to graph
            try {
                BufferedReader brEvo = new BufferedReader(new FileReader(evoFile));
                BufferedReader brAct = new BufferedReader(new FileReader(actFile));
                ArrayList<String> evoLines = new ArrayList<>();
                ArrayList<String> actLines = new ArrayList<>();
                String line = brEvo.readLine();
                int tick = 0;
                while (line != null) {
                    evoLines.add(line);
                    line = brEvo.readLine();
                    tick++;
                }
                line = brAct.readLine();
                while (line != null) {
                    actLines.add(line);
                    line = brAct.readLine();
                }
                if (evoLines.size() > 0) {
                    for (int i = yConv.size(); i < tick; i++) {
                        // These are new lines in the file

                        // Get score data from action file
                        double score = Double.parseDouble(actLines.get(i).split(" ")[1]);
                        if (yScore.size() > 0) {
                            double diff = score - yScore.get(i-1);
                            if (diff > 0) {
                                yScorePlus.add(1.0);
                                xScorePlus.add((double) i);
                            } else if (diff < 0) {
                                yScoreMinus.add(10.0);
                                xScoreMinus.add((double) i);
                            }
                        }
                        yScore.add(score);

                        // Convergence is first number in evo files
                        String[] split = evoLines.get(i).split(" ");
                        yConv.add(Double.parseDouble(split[0]));

                        // Win is 31 in evo files
                        // Lose is 38 in evo files
                        double w = Double.parseDouble(split[31]);
                        double l = Double.parseDouble(split[38]);
                        if (w != 0) {
                            yWin.add(w);
                            xWin.add((double) i);
                        }
                        if (l != 0) {
                            yLose.add(l);
                            xLose.add((double) i);
                        }

                        // Check first data points, create series
                        if (yConv.size() == 1 && seriesConvergence == null) {
                            seriesConvergence = chart.addSeries("convergence", yConv);
                            seriesConvergence.setMarker(new None());
                            seriesConvergence.setEnabled(false);
                        }
                        if (yScore.size() == 1 && seriesScore == null) {
                            seriesScore = chart.addSeries("score", yScore);
                            seriesScore.setMarker(new None());
                            seriesScore.setEnabled(false);
                        }
                        if (yScorePlus.size() == 1 && seriesScorePlus == null) {
                            seriesScorePlus = chart.addSeries("score+", xScorePlus, yScorePlus);
                            seriesScorePlus.setMarker(new TriangleUp());
                            seriesScorePlus.setLineStyle(new BasicStroke(0));
                            seriesScorePlus.setEnabled(false);
                        }
                        if (yScoreMinus.size() == 1 && seriesScoreMinus == null) {
                            seriesScoreMinus = chart.addSeries("score-", xScoreMinus, yScoreMinus);
                            seriesScoreMinus.setMarker(new TriangleDown());
                            seriesScoreMinus.setLineStyle(new BasicStroke(0));
                            seriesScoreMinus.setEnabled(false);
                        }
                        if (yWin.size() == 1 && seriesWin == null) {
                            seriesWin = chart.addSeries("win", xWin, yWin);
                            seriesWin.setMarker(new Circle()).setMarkerColor(new Color(54, 147, 80));
                            seriesWin.setLineStyle(new BasicStroke(0));
                            seriesWin.setEnabled(false);
                        }
                        if (yLose.size() == 1 && seriesLose == null) {
                            seriesLose = chart.addSeries("lose", xLose, yLose);
                            seriesLose.setMarker(new Cross()).setMarkerColor(Color.red);
                            seriesLose.setLineStyle(new BasicStroke(0));
                            seriesLose.setEnabled(false);
                        }
                    }

                    // Update series
                    try {
                        chart.updateXYSeries("convergence", null, yConv, null);
                    } catch (Exception ignored) {}
                    try {
                        chart.updateXYSeries("score", null, yScore, null);
                    } catch (Exception ignored) {}
                    try {
                        chart.updateXYSeries("score+", xScorePlus, yScorePlus, null);
                    } catch (Exception ignored) {}
                    try {
                        chart.updateXYSeries("score-", xScoreMinus, yScoreMinus, null);
                    } catch (Exception ignored) {}
                }
                brEvo.close();
            } catch (IOException ignored) { }
        }

        // Draw plot
        super.paintComponent(g);
    }

    public void toggleConvergence() {
        if (seriesConvergence != null) {
            seriesConvergence.setEnabled(!seriesConvergence.isEnabled());
        }
    }

    public void toggleScore() {
        if (seriesScore != null) {
            seriesScore.setEnabled(!seriesScore.isEnabled());
        }
    }

    public void toggleScorePlus() {
        if (seriesScorePlus != null) {
            seriesScorePlus.setEnabled(!seriesScorePlus.isEnabled());
        }
    }

    public void toggleScoreMinus() {
        if (seriesScoreMinus != null) {
            seriesScoreMinus.setEnabled(!seriesScoreMinus.isEnabled());
        }
    }

    public void toggleWin() {
        if (seriesWin != null) {
            seriesWin.setEnabled(!seriesWin.isEnabled());
        }
    }
    public void toggleLose() {
        if (seriesLose != null) {
            seriesLose.setEnabled(!seriesLose.isEnabled());
        }
    }
}