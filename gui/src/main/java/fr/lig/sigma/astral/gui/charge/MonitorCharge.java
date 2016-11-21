package fr.lig.sigma.astral.gui.charge;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.query.QueryRuntime;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Loic Petit
 */
@org.apache.felix.ipojo.annotations.Component
public class MonitorCharge extends JFrame {
    private static final JLabel NO_QUERIES_LABEL = new JLabel(
            "No declared queries");
    private GridLayout layout;
    private HashMap<EventScheduler, SchedulerChargePanel> panels = new HashMap<EventScheduler, SchedulerChargePanel>();
    private boolean stop;

    @Requires
    private AstralEngine engine;
    private static final Logger log = Logger.getLogger(MonitorCharge.class);
    private ScheduledThreadPoolExecutor executor;

    @Validate
    public void ready() {
        Container panel = getContentPane();
        layout = new GridLayout();
        panel.setLayout(layout);
        addWindowStateListener(new WindowStateListener() {
            public void windowStateChanged(WindowEvent windowEvent) {
                if (windowEvent.getNewState() == WindowEvent.WINDOW_CLOSED)
                    stopMonitoring();
            }
        });
        layout.setRows(1);
        getContentPane().add(NO_QUERIES_LABEL);
        setSize(600, 50);
        startMonitoring();
    }

    public void refresh(Collection<QueryRuntime> queries) {
        Set<EventScheduler> schedulers = new HashSet<EventScheduler>(
                panels.keySet());
        boolean changed = false;
        for (QueryRuntime runtime : queries) {
            EventScheduler es = runtime.getCore().getEs();
            SchedulerChargePanel chargePanel = panels.get(es);
            if (chargePanel == null) {
                chargePanel = new SchedulerChargePanel(es);
                panels.put(es, chargePanel);
                changed = true;
            }
            chargePanel.refresh();
            schedulers.remove(es);
        }
        if (!schedulers.isEmpty()) {
            changed = true;
            for (EventScheduler es : schedulers) {
                panels.remove(es);
            }
        }
        if (changed) {
            getContentPane().removeAll();
            if (queries.size() == 0) {
                layout.setRows(1);
                getContentPane().add(NO_QUERIES_LABEL);
                setSize(getWidth(), (layout.getRows() + 1) * 25);
                panels.clear();
            } else {
                layout.setRows(queries.size());
                for (SchedulerChargePanel chargePanel : panels.values()) {
                    getContentPane().add(chargePanel.getLabel());
                    getContentPane().add(chargePanel.getProgressBar());
                }
                setSize(getWidth(), (layout.getRows() + 1) * 25);
            }
        }
    }

    public void startMonitoring() {
        stop = false;
        executor = new ScheduledThreadPoolExecutor(2);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    refresh(engine.getDeclaredQueries());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
        setVisible(true);
    }

    public void stopMonitoring() {
        stop = true;
        executor.shutdown();
    }
}
