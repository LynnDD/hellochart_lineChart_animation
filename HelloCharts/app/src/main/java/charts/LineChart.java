package charts;

import android.graphics.Color;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Lin Dingqiang on 2017/3/9.
 * 图表的工具类
 */

public class LineChart {

    /**
     * 初始化LineChart的一些设置
     */

    private int axisYTop = 0;
    private int axisYBottom = 0;
    String[] color = new String[]{"#3CCFE3", "#111111"};

    public void linePaint(LineChartView lineChart, String[] Xasis, List<int[]> Yasis, String chartName) {
        axisYTop = 0;
        axisYBottom = 0;
        List<Line> lines = new ArrayList<Line>();
        for (int i = 0; i < Yasis.size(); i++) {
            setYaxis(Yasis.get(i));
            Line line = new Line(getAxisPoints(Yasis.get(i))).setColor(Color.parseColor(color[i]));  //折线的颜色和坐标数据
            //折线图上每个数据点的形状（有三种 ：ValueShape.SQUARE正方形  ValueShape.CIRCLE圆点  ValueShape.DIAMOND菱形）
            line.setShape(ValueShape.CIRCLE);
            line.setCubic(true);//曲线是否平滑
            line.setFilled(false);//是否填充曲线的面积
            line.setHasLabelsOnlyForSelected(true);
//            line.setHasLabels(true);//曲线的数据坐标是否加上备注//  line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
            line.setHasLines(true);//是否用直线显示。如果为false 则没有曲线只有点显示
            line.setHasPoints(false);//是否显示圆点 如果为false 则没有圆点只有点显示
//            line.setPointRadius(3);//圆点的大小
            lines.add(line);//将体重折线保存到集合中
        }

        // LineChartData是宏观上面的折线数据显示，
        //因为我们已经将所有的数据都填充进折线中，
        //现在只需要将它保存到LineChartData中
        LineChartData data = new LineChartData(lines);    //设置时间坐标轴

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.parseColor("#aaaaaa"));//黑色
        axisX.setName(chartName);  //表格名称
        axisX.setTextSize(8);//设置字体大小
        axisX.setValues(getAxisXLables(Xasis));  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        axisX.setHasLines(true); //x 轴分割线
        //设置Y坐标轴
        Axis axisY = new Axis();  //Y轴
        axisY.setHasLines(true);
        data.setAxisYLeft(axisY);  //Y轴设置在左边

        //将所有的数据填充到折线控件中
        lineChart.setLineChartData(data);    //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setVisibility(View.VISIBLE);    //设置一下整体的Y轴显示的开始和结束坐标
        final Viewport v1 = new Viewport(lineChart.getMaximumViewport());
        v1.bottom = axisYBottom;
        v1.top = axisYTop;    // You have to set max and current viewports separately.
        lineChart.setMaximumViewport(v1);    //设置当前的窗口显示多少个坐标数据，必须将折线的可以缩放的开关打开
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right = Xasis.length;
        lineChart.setCurrentViewport(v);
    }


    /**
     * X 轴的显示
     */
    private List<AxisValue> getAxisXLables(String[] Xasis) {
        List<AxisValue> mAxisXValues = new ArrayList<>();//横坐标
        for (int i = 0; i < Xasis.length; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(Xasis[i]));
        }
        return mAxisXValues;
    }

    /**
     * 图表的每个点的显示
     */
    private List<PointValue> getAxisPoints(int[] Yasis) {
        List<PointValue> mPointValues = new ArrayList<>();//纵坐标
        for (int i = 0; i < Yasis.length; i++) {
            mPointValues.add(new PointValue(i, Yasis[i]));
        }
        return mPointValues;
    }

    private void setYaxis(int[] Yasis) {

        for (int t : Yasis) {
            if (t > axisYTop) {
                axisYTop = t;
            }
        }

        if (axisYTop == 0) {
            axisYTop++;
        }
        axisYTop = (int) (axisYTop *1.2);
    }
}
