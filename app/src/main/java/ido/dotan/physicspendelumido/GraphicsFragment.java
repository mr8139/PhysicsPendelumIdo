package ido.dotan.physicspendelumido;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class GraphicsFragment extends Fragment {

    private GraphView currentlyVisibleGraph = null;

    // Pendulum parameters - using some default values
    private double length = 1.0; // meters
    private double gravity = 9.8; // m/s^2
    private double amplitude = 0.2; // radians

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphics, container, false);

        final Button buttonDisplacement = view.findViewById(R.id.button_displacement);
        final GraphView graphDisplacement = view.findViewById(R.id.graph_displacement);
        setupGraph(graphDisplacement, "Displacement (m)");
        setGraphToggle(buttonDisplacement, graphDisplacement, this::plotDisplacementGraph);

        final Button buttonVelocity = view.findViewById(R.id.button_velocity);
        final GraphView graphVelocity = view.findViewById(R.id.graph_velocity);
        setupGraph(graphVelocity, "Velocity (m/s)");
        setGraphToggle(buttonVelocity, graphVelocity, this::plotVelocityGraph);

        final Button buttonAcceleration = view.findViewById(R.id.button_acceleration);
        final GraphView graphAcceleration = view.findViewById(R.id.graph_acceleration);
        setupGraph(graphAcceleration, "Acceleration (m/s^2)");
        setGraphToggle(buttonAcceleration, graphAcceleration, this::plotAccelerationGraph);

        final Button buttonEnergy = view.findViewById(R.id.button_energy);
        final GraphView graphEnergy = view.findViewById(R.id.graph_energy);
        setupGraph(graphEnergy, "Energy (J)");
        setGraphToggle(buttonEnergy, graphEnergy, this::plotEnergyGraph);

        return view;
    }

    private void setGraphToggle(Button button, GraphView graph, Runnable plotFunction) {
        button.setOnClickListener(v -> {
            if (graph.getVisibility() == View.VISIBLE) {
                graph.setVisibility(View.GONE);
                currentlyVisibleGraph = null;
            } else {
                if (currentlyVisibleGraph != null) {
                    currentlyVisibleGraph.setVisibility(View.GONE);
                }
                plotFunction.run();
                graph.setVisibility(View.VISIBLE);
                currentlyVisibleGraph = graph;
            }
        });
    }

    private void setupGraph(GraphView graph, String yTitle) {
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(getPeriod() * 3);
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (s)");
        graph.getGridLabelRenderer().setVerticalAxisTitle(yTitle);
    }

    private double getPeriod() {
        return 2 * Math.PI * Math.sqrt(length / gravity);
    }

    private double getAngularFrequency() {
        return Math.sqrt(gravity / length);
    }

    private void plotDisplacementGraph() {
        if (getView() == null) return;
        GraphView graph = getView().findViewById(R.id.graph_displacement);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        double omega = getAngularFrequency();
        for (double t = 0; t < getPeriod() * 3; t += 0.01) {
            double x = amplitude * Math.cos(omega * t);
            series.appendData(new DataPoint(t, x), true, 3000);
        }
        graph.addSeries(series);
    }

    private void plotVelocityGraph() {
        if (getView() == null) return;
        GraphView graph = getView().findViewById(R.id.graph_velocity);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        double omega = getAngularFrequency();
        for (double t = 0; t < getPeriod() * 3; t += 0.01) {
            double v = -amplitude * omega * Math.sin(omega * t);
            series.appendData(new DataPoint(t, v), true, 3000);
        }
        graph.addSeries(series);
    }

    private void plotAccelerationGraph() {
        if (getView() == null) return;
        GraphView graph = getView().findViewById(R.id.graph_acceleration);
        graph.removeAllSeries();
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        double omega = getAngularFrequency();
        for (double t = 0; t < getPeriod() * 3; t += 0.01) {
            double a = -amplitude * omega * omega * Math.cos(omega * t);
            series.appendData(new DataPoint(t, a), true, 3000);
        }
        graph.addSeries(series);
    }

    private void plotEnergyGraph() {
        if (getView() == null) return;
        GraphView graph = getView().findViewById(R.id.graph_energy);
        graph.removeAllSeries();
        // For simplicity, mass = 1kg
        double mass = 1.0;
        LineGraphSeries<DataPoint> kineticEnergySeries = new LineGraphSeries<>();
        kineticEnergySeries.setColor(Color.RED);
        kineticEnergySeries.setTitle("Kinetic Energy");

        LineGraphSeries<DataPoint> potentialEnergySeries = new LineGraphSeries<>();
        potentialEnergySeries.setColor(Color.BLUE);
        potentialEnergySeries.setTitle("Potential Energy");

        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        double omega = getAngularFrequency();
        for (double t = 0; t < getPeriod() * 3; t += 0.01) {
            double velocity = -amplitude * omega * Math.sin(omega * t);
            double kineticEnergy = 0.5 * mass * velocity * velocity;
            double potentialEnergy = 0.5 * mass * omega * omega * Math.pow(amplitude * Math.cos(omega * t), 2);
            kineticEnergySeries.appendData(new DataPoint(t, kineticEnergy), true, 3000);
            potentialEnergySeries.appendData(new DataPoint(t, potentialEnergy), true, 3000);
        }
        graph.addSeries(kineticEnergySeries);
        graph.addSeries(potentialEnergySeries);
    }
}
