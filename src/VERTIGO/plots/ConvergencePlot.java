package VERTIGO.plots;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConvergencePlot extends JComponent {

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

    private List<Double> yData;

    private File actFile, evoFile;

    public ConvergencePlot() {
        yData = new ArrayList<>();

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
        chart.getStyler().setMarkerSize(0);
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
                ArrayList<String> lines = new ArrayList<>();
                String line = brEvo.readLine();
                int tick = 0;
                while (line != null) {
                    lines.add(line);
                    String nextLine = brEvo.readLine();
                    if (nextLine == null) break;
                    line = nextLine;
                    tick++;
                }
                if (lines.size() > 0) {
                    for (int i = yData.size(); i < tick; i++) {
                        // These are new lines in the file, convergence is first number
                        String[] split = lines.get(i).split(" ");
                        yData.add(Double.parseDouble(split[0]));

                        if (yData.size() == 1) {
                            // First data point, create series
                            chart.addSeries("convergence", yData);
                        }
                    }

                    try {
                        chart.updateXYSeries("convergence", null, yData, null);
                    } catch (Exception ignored) {}
                }
                brEvo.close();
            } catch (IOException ignored) { }
        }

        // Draw plot
        super.paintComponent(g);
    }
}