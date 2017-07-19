package com.jyt.baseapp.view.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.jyt.baseapp.R;
import com.jyt.baseapp.adapter.MessageListAdapter;
import com.jyt.baseapp.api.BeanCallback;
import com.jyt.baseapp.api.Http;
import com.jyt.baseapp.entity.BaseJson;
import com.jyt.baseapp.entity.Order;
import com.jyt.baseapp.entity.OrderMessage;
import com.jyt.baseapp.entity.SystemMessage;
import com.jyt.baseapp.util.IntentHelper;
import com.jyt.baseapp.util.T;
import com.jyt.baseapp.view.dialog.RobOrderDialog;
import com.jyt.baseapp.view.dialog.ShowTextDialog;
import com.jyt.baseapp.view.viewholder.BaseViewHolder;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;

/**
 * Created by chenweiqi on 2017/6/13.
 */

public class MessageListActivity extends BaseActivity {
    @BindView(R.id.recyclerview)
    RecyclerView recyclerview;
    @BindView(R.id.refreshLayout)
    TwinklingRefreshLayout refreshLayout;


    public static final int TYPE_SYSTEM_MESSAGE = 0;
    public static final int TYPE_ORDER_MESSAGE = 1;

    int type = -1;
    MessageListAdapter adapter;
    @Override
    protected int getLayoutId() {
        return R.layout.layout_refreshview_and_recyclerview;
    }

    @Override
    protected View getContentView() {
        return null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        type = getIntent().getIntExtra(IntentHelper.KEY_TYPE,-1);


        recyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));
        recyclerview.setAdapter(adapter = new MessageListAdapter());

        adapter.setOnViewHolderClickListener(new BaseViewHolder.OnViewHolderClickListener() {
            @Override
            public void onClick(BaseViewHolder holder, Object data, int position) {
//                if (type == TYPE_ORDER_MESSAGE){
//                    if (((OrderMessage) data).grabState.equals("2")
//                            ||((OrderMessage) data).overtime){
//                        ShowTextDialog dialog = new ShowTextDialog(getContext());
//                        dialog.setText("订单已失效");
//                        dialog.show();
//                    }else {
//                        RobOrderDialog dialog = new RobOrderDialog(getContext());
//                        dialog.setMessage((OrderMessage) data);
//                        dialog.show();
//                    }
//
//
//                }
                if (data instanceof SystemMessage){
                    IntentHelper.openSKOrderDetailActivity(getContext(),((SystemMessage) data).orderId);
                }else if (data instanceof OrderMessage){
                    IntentHelper.openPackDetailActivity(getContext(),((OrderMessage) data).packageId);
                }
            }
        });

        refreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);

                if (adapter.getDataList()!=null
                        &&adapter.getDataList().size()!=0){
                    Object lastItem = adapter.getDataList().get(0);
                    if (lastItem instanceof SystemMessage){
                        getSKSystemMessage(((SystemMessage) lastItem).messageId);
                    }else if (lastItem instanceof OrderMessage){
                        getBKOrderMessage(((OrderMessage) lastItem).messageId );
                    }
                }else {
                    refreshLayout.finishLoadmore();
                }
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);

                if (type == TYPE_SYSTEM_MESSAGE ){
                    getSKSystemMessage(null);
                }else if (type == TYPE_ORDER_MESSAGE){
                    getBKOrderMessage(null);
                }else {
                    refreshLayout.finishRefreshing();
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLayout.startLoadMore();

    }

    private void getSKSystemMessage(final String lastId){
        Http.getSKSystemMessageList(getContext(), lastId,new BeanCallback<BaseJson<List<SystemMessage>>>(getContext()) {
            @Override
            public void onResponse(BaseJson<List<SystemMessage>> response, int id) {
                super.onResponse(response,id);

                T.showShort(getContext(),response.forUser);
                if (response.ret){
                    Collections.reverse(response.data);
                    if (TextUtils.isEmpty(lastId)){
                        adapter.setDataList(response.data);
                        recyclerview.scrollToPosition(adapter.getItemCount()-1);

                    }else {
                        adapter.getDataList().addAll(0,response.data);
                    }

                    adapter.notifyDataSetChanged();

                }

                refreshLayout.finishLoadmore();
                refreshLayout.finishRefreshing();

            }
        });
    }


    private void getBKOrderMessage(final String lastId){
        Http.getBKMessageList(getContext(), lastId, new BeanCallback<BaseJson<List<OrderMessage>>>(getContext()) {
            @Override
            public void onResult(boolean success, BaseJson<List<OrderMessage>> response, Exception e) {
                super.onResult(success, response, e);
                if (success){
                    T.showShort(getContext(),response.forUser);
                    if (response.ret){
                        Collections.reverse(response.data);
                        if (TextUtils.isEmpty(lastId)){
                            adapter.setDataList(response.data);
                            recyclerview.scrollToPosition(adapter.getItemCount()-1);

                        }else {
                            adapter.getDataList().addAll(0,response.data);
                        }

                        adapter.notifyDataSetChanged();

                    }



                }else {
                    T.showShort(getContext(),e.getMessage());
                }

                refreshLayout.finishLoadmore();
                refreshLayout.finishRefreshing();

            }
        });
    }
}
