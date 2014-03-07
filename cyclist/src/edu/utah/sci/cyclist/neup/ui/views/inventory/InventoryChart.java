package edu.utah.sci.cyclist.neup.ui.views.inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import edu.utah.sci.cyclist.core.event.Pair;
import edu.utah.sci.cyclist.core.util.ColorUtil;
import edu.utah.sci.cyclist.neup.ui.views.inventory.InventoryView.AgentInfo;

public class InventoryChart extends VBox {

	public enum ChartType {
		INVENTORY, NET
	}
	
	private LineChart<Number, Number> _chart;
	private NumberAxis _xAxis;
	private NumberAxis _yAxis;
	private double _scale = 1;
	private ChartType _type = ChartType.INVENTORY;
	private int _upperBound;
	
	private Map<AgentInfo, ChartInfo> _info = new HashMap<>();
	
	public class ChartInfo {
		public Collection<Pair<Integer, Double>> values;
		public XYChart.Series<Number, Number> series;
		public double scale;
		public int last;
	}
	
	public InventoryChart() {
		super();
		build();
	}
	
	
	public void selectChartType(ChartType type) {
		_type = type;
		double s = 1;
		for (ChartInfo info : _info.values()) {
			s = Math.max(s, computeScale(info.values));
		}
		if (s != _scale) {
			_scale = s;
			updateYAxis();
		}
		updateAll();
	}
	
	public void add(AgentInfo entry) {
		if (entry.series.size() == 0) return;
		
		int last = 0;
		for (Pair<Integer, Double> p : entry.series) {
			last = Math.max(last, p.v1);
		}
		if (last > _upperBound) {
			_upperBound = last;
		}
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		String style = ColorUtil.toString(entry.color);
		series.nodeProperty().addListener(o->{
			series.getNode().setStyle("-fx-stroke:"+style);
		});

		double scale = computeScale(entry.series); // relative to the current chart type
		
		if (scale > _scale) {
			updateScale(scale);
		}
		updateSeries(series, entry.series);

		ChartInfo info = new ChartInfo();
		info.values = entry.series;
		info.series = series;
		info.scale = scale;
		info.last = last;
		_info.put(entry, info);
		
		_chart.getData().add(series);
	}
	
	public void remove(AgentInfo entry) {
		ChartInfo info = _info.remove(entry);
		if (info == null) {
			return;
		}
				
		_chart.getData().remove(info.series);
		double s = 1;
		_upperBound = 0;
		for (ChartInfo ci : _info.values()) {
			s = Math.max(ci.scale, s);
			_upperBound = Math.max(_upperBound, ci.last);
		}
		if (s != _scale) {
			updateScale(s);
			updateYAxis();
		}
	}
	
	private void updateAll() {
		for (ChartInfo info : _info.values()) {
			updateSeries(info.series, info.values);
		}
	}
	
	private void updateScale(double value) {
		_scale = value;
		System.out.println("scale: "+_scale);
		updateYAxis();
		updateAll();
	}

	private void updateYAxis() {
		String label = _scale == 1 ? "kg"
				: _scale == 1000 ? "x 1000 kg"
				: String.format("x %.0e kg", _scale);
	
		_yAxis.setLabel(label);
	}

	
	private double computeScale(Collection<Pair<Integer, Double>> values) {
		double max = 0;
		if (_type == ChartType.INVENTORY) {
			double sum = 0;
			for (Pair<Integer, Double> value : values) {
				sum += Math.abs(value.v2);
			}
			max = sum;
		} else {
			double prev = 0;
			for (Pair<Integer, Double> value : values) {
				max = Math.max(max, Math.abs(value.v2-prev));
				prev = value.v2;
			}
		}
		if (max == 0) max = 1;
		double s = Math.pow(10, 3*Math.floor(Math.log10(max)/3));
		return s;
	}
	
	private void updateSeries(XYChart.Series<Number, Number> series, Collection<Pair<Integer, Double>> values) {
		series.getData().clear();

		if (_type == ChartType.INVENTORY) {
			double sum = 0;
			for (Pair<Integer, Double> value : values) {
				sum += value.v2/_scale;
				series.getData().add(new XYChart.Data<Number, Number>(value.v1, sum));
			}
		} else {
			double prev = 0;
			boolean first = true;
			for (Pair<Integer, Double> value : values) {
				double v = value.v2/_scale;
				if (first) {
					prev = v;
					first = false;
				}
				series.getData().add(new XYChart.Data<Number, Number>(value.v1, v-prev));
				prev = v;
			}
		}
	}

			
	private void build() {
		getStyleClass().add("fchart");
	
		getChildren().add(
			buildChart()
		);
		
		VBox.setVgrow(_chart, Priority.ALWAYS);
		setFillWidth(true);
	}	

	private Node buildChart() {
		_xAxis = new NumberAxis();
		_xAxis.setLabel("time");
		_xAxis.setAnimated(false);
		
		_yAxis = new NumberAxis();
		_yAxis.setLabel("Amount");
		_yAxis.setAnimated(false);
		
		_chart = new LineChart<>(_xAxis, _yAxis);
		_chart.getStyleClass().add("chart");
		_chart.setCreateSymbols(false);
		_chart.setLegendVisible(false);

		return _chart;
	}
	
}