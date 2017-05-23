    因为做毕业设计的时候使用到了github中的源码hellochart来做图表，所以把个人修改后的代码做一个总结。多条折线同时显示在一个图里，并且带有一秒的动画过渡，这里的横坐标是定长，效果类似于谱线变化。

**1.MainActivity.java**
```
package ldqzju.hellocharts;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;
import charts.LineChart;
import lecho.lib.hellocharts.view.LineChartView;

public class MainActivity extends AppCompatActivity {

    String[] Xasis = new String[20];
    List<int[]> ListYasis = new ArrayList<>();
    LineChart lineChart = new LineChart();
    LineChartView chart;
    int[] Yasis1s = new int[20];//折现1动画开始时数据
    int[] Yasis2s = new int[20];//折现2动画开始时数据
    int[] Yasis1e = new int[20];//折现1数据更新结果
    int[] Yasis2e = new int[20];//折现2数据更新结果
    int[] Yasis1gap = new int[20];//动画每秒步进的数据
    int[] Yasis2gap = new int[20];
    final int TIMEDATACHANGE = 3000;//3秒数据率刷新一次
    final int FPS = 20;//帧数设置

    private Handler handler = new Handler() {
        int count = 0;//计算帧数
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:{//数据更新
                    count =0;
                    for(int i=0;i<Xasis.length;i++){
                        Yasis1e[i] = ((int) (Math.random()*2000));
                        Yasis1gap[i] = (Yasis1e[i] - Yasis1s[i])/FPS; //每秒20帧数

                        Yasis2e[i] = ((int) (Math.random()*2000));
                        Yasis2gap[i] = (Yasis2e[i] - Yasis2s[i])/FPS; //每秒20帧数
                    }
                    break;
                }
                case 1:{
                    count ++ ;
                    if(count<FPS){//过渡动画帧数中
                        ListYasis.clear();
                        for(int i=0;i<Xasis.length;i++){
                            Yasis1s[i] =  Yasis1s[i] + Yasis1gap[i];
                            Yasis2s[i] =  Yasis2s[i] + Yasis2gap[i];
                        }
                        ListYasis.add(Yasis1s);
                        ListYasis.add(Yasis2s);
                        lineChart.linePaint(chart,Xasis,ListYasis,"折线测试1");
                        break;
                    }
                    else if(count==FPS){//最后一帧修正数据
                        ListYasis.clear();
                        for(int i=0;i<Xasis.length;i++){
                            Yasis1s[i] =  Yasis1e[i];
                            Yasis2s[i] =  Yasis2e[i];
                        }
                        ListYasis.add(Yasis1s);
                        ListYasis.add(Yasis2s);
                        lineChart.linePaint(chart,Xasis,ListYasis,"折线测试1");
                        break;
                    }
                }
                default:break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chart = (LineChartView)findViewById(R.id.lineChartView);
        for(int i=0;i<Xasis.length;i++) {
            ListYasis.clear();
            Xasis[i] = "" + i;
        }
        new Thread(){//数据更新线程
            public void run() {
                while(true){
                    Message message = new Message();
                    message.what = 0;
                    handler.sendMessage(message);
                    try {
                        sleep(TIMEDATACHANGE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){//动画线程，如果数据更新线程时常太长，动画过渡所占时间很少，可以考虑开关该线程，减少消耗
            public void run() {
                while(true){
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
}

```
**2.工具类LineChart.java**

```
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

```
**3.布局文件activity_main.xml**

```
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context="ldqzju.hellocharts.MainActivity">

    <lecho.lib.hellocharts.view.LineChartView
        android:padding="10dp"
        android:layout_width="380dp"
        android:layout_height="200dp"
        android:id="@+id/lineChartView" />



</android.support.constraint.ConstraintLayout>
```
**4.效果图**
![效果图](http://img.blog.csdn.net/20170523152102057?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamlsaW5ncWlhbmc2ODE0/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)



**5.引用说明**

> 引用块内容
[hellocharts源码来源](https://github.com/lecho/hellocharts-android)

jar文件来自于网上下载，忘了当时是在哪个地方下的，不过，都出到处都有，我就引用了，如果需要发邮箱的可以留言我。

**6.工程代码**
[这里写链接内容](https://github.com/LynnDD/hellochart_lineChart_animation)

----------


*因为是新手，所很多代码部分并不是很高效，甚至有错误，大家如果有哥什么建议和意见希望能留言我，万分感谢。*