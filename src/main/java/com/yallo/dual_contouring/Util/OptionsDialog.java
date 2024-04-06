package com.yallo.dual_contouring.Util;

import com.yallo.dual_contouring.Resources.SDF;
import com.yallo.dual_contouring.Resources.SDFs;
import org.joml.Vector2f;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OptionsDialog extends JDialog {
    private int resolution;
    private SDF sdf;

    public OptionsDialog() {
        super((Window)null);
        setModal(true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setLayout(new GridLayout(7, 1));

        JLabel resSliderName = new JLabel("Resolution: ");
        resSliderName.setHorizontalAlignment(SwingConstants.CENTER);
        add(resSliderName);

        JSlider resSlider = new JSlider(10, 200);
        resSlider.setValue(100);
        resSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                resolution = resSlider.getValue();
                resSliderName.setText("Resolution: " + resolution);
            }
        });
        add(resSlider);

        resolution = resSlider.getValue();
        resSliderName.setText("Resolution: " + resolution);

        add(new JPanel());

        JLabel sdfName = new JLabel("Initial Terrain Shape");
        sdfName.setHorizontalAlignment(SwingConstants.CENTER);
        add(sdfName);

        JComboBox<String> sdfBox = new JComboBox<>();
        sdfBox.addItem("Sphere");
        sdfBox.addItem("Torus");
        sdfBox.addItem("Plane");
        add(sdfBox);

        sdfBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                sdf = switch (e.getItem().toString()) {
                    case "Sphere" -> SDFs.getSphere(0.4f);
                    case "Torus" -> SDFs.getTorus(new Vector2f(0.3f, 0.1f));
                    default -> SDFs.getPlane(-0.25f);
                };
            }
        });

        sdf = SDFs.getSphere(0.4f);

        add(new JPanel());

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        add(ok);

        setBounds(50, 50, 500, 500);
    }

    public int getResolution() {
        return resolution;
    }

    public SDF getSdf() {
        return sdf;
    }
}
