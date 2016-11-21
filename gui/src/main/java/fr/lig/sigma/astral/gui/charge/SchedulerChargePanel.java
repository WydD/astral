package fr.lig.sigma.astral.gui.charge;

import fr.lig.sigma.astral.common.event.EventScheduler;

import javax.swing.*;
import java.awt.*;

/**
 * @author Loic Petit
 */

public class SchedulerChargePanel {
    private CyclicBuffer<Integer> buffer = new CyclicBuffer<Integer>(25, 10);
    private EventScheduler es;
    private JProgressBar progressBar = new JProgressBar();
    private Component label;

    public SchedulerChargePanel(EventScheduler es) {
        this.es = es;
        label = new JLabel(es.getRuntime().getDisplayName());
        progressBar.setStringPainted(true);
    }

    public void refresh() {
        int value = es.getQueueCount();
        buffer.add(value);
        progressBar.setMaximum(buffer.max());
        progressBar.setValue(value);
        progressBar.setString(String.valueOf(value));
    }

    public Component getLabel() {
        return label;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }
}
