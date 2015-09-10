package gjz.bluetooth;

//画图部分


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.PanListener;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

//import org.achartengine.chartdemo.demo.R;

public class XYChartBuilder extends Activity {


    public static final String TYPE = "type";

    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

    private XYSeries mCurrentSeries;

    private XYSeriesRenderer mCurrentRenderer;

    private String mDateFormat;

    private Button mNewSeries;

    private Button mAdd;

    private EditText mX;

    private EditText mY;

    private GraphicalView mChartView;

    private int index = 0;
    private int x = 0;
    private double y = 0;
    private int flag = 0;
    private int mflag = 0;

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
        mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
        mCurrentSeries = (XYSeries) savedState.getSerializable("current_series");
        mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
        mDateFormat = savedState.getString("date_format");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("dataset", mDataset);
        outState.putSerializable("renderer", mRenderer);
        outState.putSerializable("current_series", mCurrentSeries);
        outState.putSerializable("current_renderer", mCurrentRenderer);
        outState.putString("date_format", mDateFormat);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_xychart_builder);

        mRenderer.setApplyBackgroundColor(true);//设置是否显示背景色
        mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));//设置背景色
        mRenderer.setAxisTitleTextSize(16); //设置轴标题文字的大小
        mRenderer.setChartTitleTextSize(20);//?设置整个图表标题文字大小
        mRenderer.setLabelsTextSize(25);//设置刻度显示文字的大小(XY轴都会被设置)
        mRenderer.setLegendTextSize(15);//图例文字大小
        mRenderer.setMargins(new int[]{200, 30, 0, 30});//设置图表的外边框(上/左/下/右)  30 70 0 10
        mRenderer.setZoomButtonsVisible(false);//是否显示放大缩小按钮
        mRenderer.setPointSize(10);//设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
        if (flag == 0) {
            String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);//图例
            XYSeries series = new XYSeries(seriesTitle);//定义XYSeries
            mDataset.addSeries(series);//在XYMultipleSeriesDataset中添加XYSeries
            mCurrentSeries = series;//设置当前需要操作的XYSeries
            XYSeriesRenderer renderer = new XYSeriesRenderer();//定义XYSeriesRenderer
            mRenderer.addSeriesRenderer(renderer);//将单个XYSeriesRenderer增加到XYMultipleSeriesRenderer
            renderer.setPointStyle(PointStyle.CIRCLE);//点的类型是圆形
            renderer.setFillPoints(true);//设置点是否实心
            mCurrentRenderer = renderer;
            flag = 1;

        }
        Bundle bunde = this.getIntent().getExtras();
        double[] Times = bunde.getDoubleArray("data");
        int count = bunde.getInt("Times");
        int k = 0;
        while (count != 0) {
            mCurrentSeries.add(k, Times[k]);
            k++;
            count--;
            mflag = 0;
            if (mChartView != null) {
                mChartView.repaint();//重画图表
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChartView == null) {
            LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
            mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
            mRenderer.setClickEnabled(true);//设置图表是否允许点击
            mRenderer.setSelectableBuffer(100);//设置点的缓冲半径值(在某点附件点击时,多大范围内都算点击这个点)
            mChartView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //这段代码处理点击一个点后,获得所点击的点在哪个序列中以及点的坐标.
                    //-->start
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    double[] xy = mChartView.toRealPoint(0);
                    if (seriesSelection == null) {
                        Toast.makeText(XYChartBuilder.this, "No chart element was clicked", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(
                                XYChartBuilder.this,
                                "Chart element in series index " + seriesSelection.getSeriesIndex()
                                        + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                                        + " closest point value X=" + seriesSelection.getXValue() + ", Y=" + seriesSelection.getValue()
                                        + " clicked point value X=" + (float) xy[0] + ", Y=" + (float) xy[1], Toast.LENGTH_SHORT).show();
                    }
                    //-->end
                }
            });
            mChartView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
                    if (seriesSelection == null) {
                        Toast.makeText(XYChartBuilder.this, "No chart element was long pressed",
                                Toast.LENGTH_SHORT);
                        return false; // no chart element was long pressed, so let something
                        // else handle the event
                    } else {
                        Toast.makeText(XYChartBuilder.this, "Chart element in series index "
                                + seriesSelection.getSeriesIndex() + " data point index "
                                + seriesSelection.getPointIndex() + " was long pressed", Toast.LENGTH_SHORT);
                        return true; // the element was long pressed - the event has been
                        // handled
                    }
                }
            });
            //这段代码处理放大缩小
            //-->start
            mChartView.addZoomListener(new ZoomListener() {
                public void zoomApplied(ZoomEvent e) {
                    String type = "out";
                    if (e.isZoomIn()) {
                        type = "in";
                    }
                    System.out.println("Zoom " + type + " rate " + e.getZoomRate());
                }

                public void zoomReset() {
                    System.out.println("Reset");
                }
            }, true, true);
            //-->end
            //设置拖动图表时后台打印出图表坐标的最大最小值.
            mChartView.addPanListener(new PanListener() {
                public void panApplied() {
                    System.out.println("New X range=[" + mRenderer.getXAxisMin() + ", " + mRenderer.getXAxisMax()
                            + "], Y range=[" + mRenderer.getYAxisMax() + ", " + mRenderer.getYAxisMax() + "]");
                }
            });
            layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT));
            boolean enabled = mDataset.getSeriesCount() > 0;
//            setSeriesEnabled(enabled);
        } else {
            mChartView.repaint();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        private int count = 1;
        private int[] a = new int[1024];

        public MyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mflag = 1;
            double data = Double.valueOf(intent.getStringExtra("data"));
            System.out.print(data);
            y = data;
        }
    }

}
