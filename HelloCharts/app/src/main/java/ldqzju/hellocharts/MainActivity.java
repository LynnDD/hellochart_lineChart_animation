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
