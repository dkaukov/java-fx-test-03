package com.example.javafxtest03.fx;

import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;

import org.controlsfx.control.StatusBar;
import org.springframework.stereotype.Component;

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.axes.spi.format.DefaultFormatter;
import de.gsi.chart.plugins.YWatchValueIndicator;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.testdata.spi.CosineFunction;
import de.gsi.dataset.testdata.spi.GaussFunction;
import de.gsi.dataset.testdata.spi.RandomWalkFunction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

@Component
@FxmlView
public class MainController {
    private static final int N_SAMPLES = 50000;

    private final FxWeaver fxWeaver;

    private final MainModel model;

    @FXML
    private Menu mMain;
    @FXML
    private MenuItem addCos;
    @FXML
    private MenuItem addRandom;
    @FXML
    private MenuItem addGauss;
    @FXML
    private MenuItem clearChart;
    @FXML
    private MenuItem mExit;
    @FXML
    private XYChart chart;
    @FXML
    private ErrorDataSetRenderer errorDataSetRenderer;
    @FXML
    private StatusBar statusBar;
    @FXML
    private DefaultNumericAxis powerAxis;

    public MainController(FxWeaver fxWeaver, MainModel model) {
        this.fxWeaver = fxWeaver;
        this.model = model;
    }

    @FXML
    public void initialize() {
        model.getDatasetRaw().get().setVisible(false);
        errorDataSetRenderer.getDatasets().add(model.getDatasetFiltered().get());
        errorDataSetRenderer.getDatasets().add(model.getDatasetRaw().get());

        final YWatchValueIndicator indicator1 = new YWatchValueIndicator(powerAxis, -100);
        indicator1.setId("avg");
        indicator1.valueProperty().bindBidirectional(model.getAvg());
        indicator1.textProperty().bindBidirectional(model.getAvgTxt());
        chart.getPlugins().add(indicator1);

        addGauss.setOnAction(evt -> errorDataSetRenderer.getDatasets().add(new GaussFunction("gauss", N_SAMPLES)));
        addCos.setOnAction(evt -> errorDataSetRenderer.getDatasets().add(new CosineFunction("cos", N_SAMPLES)));
        addRandom.setOnAction(
            evt -> errorDataSetRenderer.getDatasets().add(new RandomWalkFunction("Random", N_SAMPLES)));
        clearChart.setOnAction(evt -> errorDataSetRenderer.getDatasets().clear());
        mExit.setOnAction(evt -> Platform.exit());
        statusBar.progressProperty().bindBidirectional(model.getCpuLoad());
    }
}
