package com.github.davidmoten.xuml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import xuml.tools.miuml.metamodel.jaxb.Class;
import xuml.tools.miuml.metamodel.jaxb.ModeledDomain;
import xuml.tools.model.compiler.Util;

public class StateDiagramViewer {

    private final JFrame frame = new JFrame();
    private final JPanel vvContainer = createVvContainer();
    private volatile ModeledDomain domain;
    private volatile JMenu classMenu;

    public void open(ModeledDomain domain) {
        this.domain = domain;
        classMenu.removeAll();
        Util.getClasses(domain).stream().forEach(cls -> {
            JMenuItem m = new JMenuItem(cls.getName());
            m.setEnabled(cls.getLifecycle() != null);
            m.addActionListener(evt -> show(cls));
            classMenu.add(m);
        });
    }

    public void show(Class c) {
        System.out.println("showing " + c.getName());
        Graph<String, Edge> g = new DirectedSparseGraph<String, Edge>();
        if (c.getLifecycle() != null) {
            c.getLifecycle().getState().stream().forEach(state -> g.addVertex(state.getName()));
            c.getLifecycle().getTransition().stream().forEach(transition -> {
                String fromState = transition.getState();
                String toState = transition.getDestination();
                String eventName = c.getLifecycle().getEvent().stream().map(ev -> ev.getValue())
                        .filter(event -> event.getID().equals(transition.getEventID()))
                        .map(event -> event.getName()).findAny().get();
                g.addEdge(new Edge(eventName), fromState, toState);
            });
        }
        if (!g.getVertices().isEmpty()) {
            VisualizationViewer<String, Edge> vv = createVisualizationViewer(g);
            SwingUtilities.invokeLater(() -> {
                vvContainer.removeAll();
                vvContainer.add(vv);
                createMenu(vv);
                vvContainer.repaint();
                vvContainer.revalidate();
                System.out.println("added vv");
                frame.setTitle(c.getName());
            });
        }
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            // Creates a menubar for a JFrame
            JMenuBar menuBar = new JMenuBar();
            // Add the menubar to the frame
            frame.setJMenuBar(menuBar);
            // Define and add two drop down menu to the menubar
            JMenu fileMenu = new JMenu("File");
            classMenu = new JMenu("Class");
            JMenu editMenu = new JMenu("Edit");
            menuBar.add(fileMenu);
            menuBar.add(editMenu);
            menuBar.add(classMenu);
            JMenuItem openAction = new JMenuItem("Open");
            JMenuItem exitAction = new JMenuItem("Exit");
            fileMenu.add(openAction);
            fileMenu.add(exitAction);
            openAction.addActionListener(System.out::println);
            exitAction.addActionListener(e -> System.exit(0));
            frame.getContentPane().add(vvContainer);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        });
    }

    private static JPanel createVvContainer() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    public static final class Edge {
        final String name;

        Edge(String name) {
            this.name = name;
        }

        public static Edge of(String name) {
            return new Edge(name);
        }
    }

    public static void show(Graph<String, Edge> graph) {

    }

    private static VisualizationViewer<String, Edge> createVisualizationViewer(
            Graph<String, Edge> graph) {
        FRLayout<String, Edge> layout = new FRLayout<String, Edge>(graph, new Dimension(800, 600));
        while (!layout.done())
            layout.step();

        VisualizationViewer<String, Edge> vv = new VisualizationViewer<String, Edge>(layout,
                new Dimension(800, 600));
        vv.getRenderContext().setVertexLabelTransformer(s -> s);
        vv.getRenderContext().setVertexFillPaintTransformer(vertex -> vertex.equals("Created")
                ? Color.decode("#B5D9E6") : Color.decode("#FFF1BC"));
        vv.getRenderContext().setVertexDrawPaintTransformer(vertex -> Color.black);
        vv.getRenderContext().setVertexShapeTransformer(createVertexShapeTransformer(layout));
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setEdgeLabelTransformer(edge -> edge.name);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<String, Edge>());

        // The following code adds capability for mouse picking of
        // vertices/edges. Vertices can even be moved!
        final DefaultModalGraphMouse<String, Number> graphMouse = new DefaultModalGraphMouse<String, Number>();
        vv.setGraphMouse(graphMouse);
        graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
        return vv;
    }

    private static Transformer<String, Shape> createVertexShapeTransformer(
            Layout<String, Edge> layout) {
        return vertex -> {
            int w = 100;
            int margin = 5;
            int ascent = 12;
            int descent = 5;
            Shape shape = new Rectangle(-w / 2 - margin, -ascent - margin, w + 2 * margin,
                    (ascent + descent) + 2 * margin);
            return shape;
        };
    }

    private static void createMenu(JPanel panel) {
        final JPopupMenu menu = new JPopupMenu();
        JMenuItem m = new JMenuItem("Print...");
        menu.add(m);
        m.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Panels.print(panel);
                } catch (PrinterException e1) {
                    e1.printStackTrace();
                }
            }
        });
        m = new JMenuItem("Save image as PNG...");
        menu.add(m);
        m.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.addChoosableFileFilter(new ImageFilter());
                int returnVal = fc.showSaveDialog(panel);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (!file.getName().toUpperCase().endsWith(".PNG"))
                        file = new File(file.getAbsolutePath() + ".PNG");
                    try {
                        Panels.saveImage(panel, file);
                    } catch (RuntimeException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                    menu.show((Component) e.getSource(), e.getX(), e.getY());
            }
        });
    }

    private static class ImageFilter extends FileFilter {

        // Accept all directories and all gif, jpg, tiff, or png files.
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().toUpperCase().endsWith(".PNG");
        }

        // The description of this filter
        @Override
        public String getDescription() {
            return "PNG files";
        }
    }

}