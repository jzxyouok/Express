package com.expressba.expressuser.user.search;

import android.app.Activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.expressba.expressuser.model.ExpressSearchInfo;
import com.expressba.expressuser.net.VolleyHelper;
import com.expressba.expressuser.R;

/**
 * Created by songchao on 16/5/1.
 */
public class SearchExpressPresenterImpl extends VolleyHelper implements SearchExpressPresenter {
    private SearchExpressView searchExpressView;
    private String searchUrl;

    public SearchExpressPresenterImpl(Activity context, SearchExpressView searchExpressView) {
        super(context);
        this.searchExpressView = searchExpressView;
        String baseUrl = context.getResources().getString(R.string.base_url);
        searchUrl = baseUrl + context.getResources().getString(R.string.express_route_search);
    }

    @Override
    public void startGetExpressInfo(String expressID) {
        searchUrl = searchUrl.replace("{expressId}", expressID);
        doJsonArray(searchUrl, VolleyHelper.GET, null);
    }

    @Override
    public void startGetExpressImage(String id, int whichImage) {
        if(whichImage == SearchExpressFragment.LAN_SHOW){

        }else{

        }
    }

    @Override
    public void onDataReceive(Object jsonOrArray) {
        JSONArray jsonArray = (JSONArray) jsonOrArray;
        if(jsonArray.length() == 0){
            onError("您查的快递不存在");
            return;
        }
        ArrayList<ExpressSearchInfo> expressSearchInfos = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                ExpressSearchInfo expressSearchInfo = new ExpressSearchInfo();
                expressSearchInfo.setInfo(jsonObject.getString("info"));
                expressSearchInfo.setState(jsonObject.getInt("state"));
                expressSearchInfo.setTime(jsonObject.getString("time"));
                expressSearchInfos.add(expressSearchInfo);
            }
            searchExpressView.onRequestSuccess(expressSearchInfos);
        } catch (JSONException e) {
            e.printStackTrace();
            searchExpressView.onError("快递状态出错");
        }


    }

    @Override
    public void onError(String errorMessage) {
        searchExpressView.onError(errorMessage);
    }
}
