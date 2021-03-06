package com.expressba.expressuser.map;

import android.app.Activity;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;
import com.expressba.expressuser.map.model.HistoryTrackData;
import com.expressba.expressuser.map.model.MyLatLng;
import com.expressba.expressuser.map.toolbox.GsonService;

import java.util.List;
import java.util.Map;

/**
 * Created by songchao on 16/5/17.
 */
public class MyHistoryTrace {

    public static LBSTraceClient client;//实例化轨迹客户端;
    private StartTraceInterface startTrace;
    private Trace trace;
    public  final long SERVICE_ID = 115498;
    private OnStartTraceListener startTraceListener = null;
    private OnEntityListener entityListener = null;
    private StopTraceInterface stopTrace;
    public QueryHistoryInterface historyInterface;
    private OnTrackListener onTrackListener;//查询纠偏轨迹回调
    //private List<LatLng> latLngs;

    public MyHistoryTrace(){
        //latLngs = new ArrayList<>();
    }

    /**
     * 位置采集周期
     */
    private final int gatherInterval = 10;

    /**
     * 打包周期
     */
    private final int packInterval = 60;

    /**
     * 设置协议类型，0为http，1为https
     */
    private final int protocoType = 0;

    /**
     * 轨迹服务类型（0 : 不上传位置数据，也不接收报警信息； 1 : 不上传位置数据，但接收报警信息；2 : 上传位置数据，且接收报警信息）
     */
    private final int traceType = 2;

    /**
     * 开启轨迹上传功能，返回值 如果正常返回“yes“，如果错误返回错误信息,以前默认entityName是entity
     * @param activity
     * @return
     */
    public  void startTraceClient(Activity activity,String entityName,StartTraceInterface startTrace) {
        this.startTrace = startTrace;

        //实例化轨迹服务
        trace = new Trace(activity.getApplicationContext(), SERVICE_ID, entityName, traceType);

        //初始化开启轨迹回调监听
        initOnStartTraceListener();

        //开启轨迹服务
        client.startTrace(trace, startTraceListener);

        //设置位置采集和打包周期
        client.setInterval(gatherInterval, packInterval);

        //设置协议
        client.setProtocolType(protocoType);
    }

    /**
     * 初始化开启轨迹回调监听
     */
    private void initOnStartTraceListener(){
        //实例化开启轨迹服务回调接口
        startTraceListener = new OnStartTraceListener() {
            //开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                startTrace.startTraceCallBack(arg0,arg1);
            }

            //轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
                startTrace.startTraceCallBack(arg0,arg1);
            }
        };
    }


    /**
     * 接口用于开启轨迹服务回调
     */
    public interface StartTraceInterface {
        void startTraceCallBack(int stateCode, String message);
        void startTracePush(byte arg0, String arg1);
    }


    /**
     * 停止轨迹追踪服务
     */
    public  void stopTraceClient(StopTraceInterface stopTracee){
        this.stopTrace = stopTracee;
        //实例化停止轨迹服务回调接口
        final OnStopTraceListener stopTraceListener = new OnStopTraceListener(){
            // 轨迹服务停止成功
            @Override
            public void onStopTraceSuccess() {
                if(stopTrace!=null) {
                    stopTrace.stopTraceSuccess();
                }
            }
            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onStopTraceFailed(int arg0, String arg1) {
                if(stopTrace!=null) {
                    stopTrace.stopTraceFail(arg0, arg1);
                }
            }
        };

        //停止轨迹服务
        client.stopTrace(trace,stopTraceListener);
    }

    /**
     * 停止轨迹追踪服务结果回调接口
     */
    interface StopTraceInterface {
        void stopTraceSuccess();
        void stopTraceFail(int arg0, String arg1);
    }


    /**
     * 查询纠偏后的轨迹
     */
    public void queryProcessedHistoryTrack(String entityName,QueryHistoryInterface historyInterface){
        this.historyInterface = historyInterface;

        int simpleReturn = 0;//是否返回简化数据
        int isProcessed = 1;
        int startTime = (int) (System.currentTimeMillis() / 1000 - 6 * 60 * 60);//查询最早6小时前的时间
        int endTime = (int) (System.currentTimeMillis() / 1000);//查询最后时间
        int pageSize = 1000;//分页大小
        int pageIndex = 1;//分页索引

        initOnTrackListener();//初始化listener

        client.setOnTrackListener(onTrackListener);
        client.queryProcessedHistoryTrack(SERVICE_ID,entityName,simpleReturn,isProcessed,startTime,endTime,pageSize,pageIndex,onTrackListener);
    }

    /**
     * 初始化track回调
     */
    private void initOnTrackListener(){
        onTrackListener = new OnTrackListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                if(historyInterface != null){
                    historyInterface.requestFail(10,s);
                }
            }

            @Override
            public Map onTrackAttrCallback() {
                //设置自定信息，比如汽车油量等等
                return super.onTrackAttrCallback();
            }

            @Override
            public void onQueryHistoryTrackCallback(String s) {
                super.onQueryHistoryTrackCallback(s);
                HistoryTrackData historyTrackData = GsonService.parseJson(s,HistoryTrackData.class);
                if(historyTrackData!=null){
                    if(historyTrackData.getStatus() == 0){
                        //请求位置成功,开始处理
                        final List<MyLatLng> myLatLngs = historyTrackData.getListPoints();
                        if(historyInterface!=null){
                            historyInterface.queryHistoryCallBack(myLatLngs);
                        }
                    }else if(historyTrackData.getStatus() == 3003){
                        //指定entity不存在
                        if(historyInterface!=null){
                            historyInterface.requestFail(3003,historyTrackData.getMessage());
                        }
                    }
                }else{
                    if(historyInterface!=null){
                        historyInterface.requestFail(10,"获取数据转换出错");
                    }
                }
            }
        };
    }

    /**
     * 查询历史记录的接口回调，返回历史数据数据列表
     */
    public interface QueryHistoryInterface{
        void queryHistoryCallBack(List<MyLatLng> latLngs);
        void requestFail(int state, String message);
    }


    /**
     * 添加entityName，在entityName不存在的时候
     * @param entityName
     */
    public void addEntity(String entityName,EntityListenerInterface entityListenerInterface){
        this.entityListenerInterface = entityListenerInterface;
        initEntityListener();
        MyHistoryTrace.client.addEntity(SERVICE_ID,entityName,null,entityListener);
    }

    private void initEntityListener(){
        entityListener = new OnEntityListener() {
            @Override
            public void onRequestFailedCallback(String s) {
                entityListenerInterface.requestFailedCallBack(s);
            }

            @Override
            public void onAddEntityCallback(String s) {
                super.onAddEntityCallback(s);
                if(entityListenerInterface!=null){
                    entityListenerInterface.addEntityCallBack(s);
                }
            }
        };
    }

    /**
     * entity操作的回调接口
     */
    EntityListenerInterface entityListenerInterface;
    public interface EntityListenerInterface{
        void addEntityCallBack(String s);
        void requestFailedCallBack(String s);
    }
}
