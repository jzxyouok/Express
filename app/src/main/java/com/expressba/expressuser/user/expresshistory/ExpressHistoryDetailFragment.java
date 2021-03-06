package com.expressba.expressuser.user.expresshistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expressba.expressuser.main.UIFragment;
import com.expressba.expressuser.model.ExpressInfo;
import com.expressba.expressuser.R;
import com.expressba.expressuser.myelement.MyFragmentManager;

/**
 * 快件详情
 * Created by songchao on 16/5/8.
 */
public class ExpressHistoryDetailFragment extends UIFragment implements View.OnClickListener{

    private ExpressInfo expressInfo;
    private Integer sendOrReceive = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_express_history_detail,container,false);
        view.findViewById(R.id.top_bar_left_img).setOnClickListener(this);
        return view;
    }

    @Override
    protected void onBack() {
        MyFragmentManager.popFragment(getClass(),null,null,getFragmentManager());
    }

    /**
     * 静态bundle创建方法
     * @param expressInfo
     * @param sendOrReceive
     * @return
     */
    public static Bundle newInstanceBundle(ExpressInfo expressInfo,int sendOrReceive){
        Bundle bundle = new Bundle();
        bundle.putParcelable("expressinfo",expressInfo);
        bundle.putInt("sendorreceive",sendOrReceive);
        return bundle;
    }


    @Override
    protected void onStartHandlerBundle() {
        super.onStartHandlerBundle();
        if(getBundle()!=null){
            expressInfo = getBundle().getParcelable("expressinfo");
            sendOrReceive = getBundle().getInt("sendorreceive");
        }
    }

    @Override
    protected void onStartHandlerView(View view) {
        super.onStartHandlerView(view);
        if(sendOrReceive == ExpressHistoryPresenterImpl.HISTORY_SEND){
            ((TextView)view.findViewById(R.id.top_bar_center_text)).setText("发件详情");
        }else{
            ((TextView)view.findViewById(R.id.top_bar_center_text)).setText("收件详情");
        }
        if(expressInfo!=null) {
            ((TextView) view.findViewById(R.id.express_detail_id)).setText(expressInfo.getID());
            ((TextView) view.findViewById(R.id.express_detail_send_name)).setText(expressInfo.getSname());
            ((TextView) view.findViewById(R.id.express_detail_send_tel)).setText(expressInfo.getStel());
            ((TextView) view.findViewById(R.id.express_detail_send_address)).setText(expressInfo.getSadd() + " " + expressInfo.getSaddinfo());
            ((TextView) view.findViewById(R.id.express_detail_receive_name)).setText(expressInfo.getRname());
            ((TextView) view.findViewById(R.id.express_detail_receive_tel)).setText(expressInfo.getRtel());
            ((TextView) view.findViewById(R.id.express_detail_receive_address)).setText(expressInfo.getRadd() + " " + expressInfo.getRaddinfo());
            ((TextView) view.findViewById(R.id.express_detail_get_time)).setText(expressInfo.getGetTime());
            ((TextView) view.findViewById(R.id.express_detail_out_time)).setText(expressInfo.getOutTime());
            ((TextView) view.findViewById(R.id.express_detail_weight)).setText(expressInfo.getWeight() + "");
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.top_bar_left_img:
                onBack();
                break;

        }
    }
}
