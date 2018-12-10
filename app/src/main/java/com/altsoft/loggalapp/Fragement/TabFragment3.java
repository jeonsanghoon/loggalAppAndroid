package com.altsoft.loggalapp.Fragement;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.altsoft.Adapter.SignageListViewAdapter;
import com.altsoft.Framework.Global;
import com.altsoft.Framework.module.BaseFragment;
import com.altsoft.loggalapp.R;
import com.altsoft.loggalapp.SignageControlActivity;
import com.altsoft.model.signage.MOBILE_SIGNAGE_COND;
import com.altsoft.model.signage.MOBILE_SIGNAGE_LIST;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class TabFragment3 extends BaseFragment {
    SignageListViewAdapter adapter;
    boolean lastitemVisibleFlag = false;
    private boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수
    ListView listview;
    boolean bLastPage = false;
    Integer nPageSize = 30;
    Integer nPage = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        adapter = new SignageListViewAdapter();
        GetSignageList();
        return inflater.inflate(R.layout.fragment_tab_fragment3, container, false);
    }
    private void GetSignageList()
    {
        this.GetSignageList(null);
    }
    private void GetSignageList(Integer page) {

        MOBILE_SIGNAGE_COND Cond = new MOBILE_SIGNAGE_COND();
        try {

            Cond.LATITUDE = Global.getMapInfo().latitude;
            Cond.LONGITUDE = Global.getMapInfo().longitude;
            Cond.PAGE_COUNT = nPageSize;
            Cond.PAGE  = page == null ? 1 : page;
            if(Cond.PAGE != 1 && bLastPage) {
                Toast.makeText(getActivity(),"데이터가 모두 검색되었습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            String sAddr = Global.getMapInfo().currentLocationAddress;
            Global.getCommon().ProgressShow(getActivity());
            Call<List<MOBILE_SIGNAGE_LIST>> call = Global.getAPIService().GetMobileSignageList(Cond);
            call.enqueue(new Callback<List<MOBILE_SIGNAGE_LIST>>() {
                @Override
                public void onResponse(Call<List<MOBILE_SIGNAGE_LIST>> call, Response<List<MOBILE_SIGNAGE_LIST>> response) {
                    Global.getCommon().ProgressHide(getActivity());
                    List<MOBILE_SIGNAGE_LIST> list = response.body();
                    if(list.size() == 0) {
                        bLastPage = true;
                        Toast.makeText(getActivity(),"데이터가 모두 검색되었습니다.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if(list.size() < nPageSize) bLastPage = true;
                    if(adapter.SetDataBind(list) == true) return;

                    listview = (ListView) getView().findViewById(R.id.listview3);
                    listview.setAdapter(adapter);
                    listview.setOnScrollListener(new ListView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                            if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && lastitemVisibleFlag && mLockListView == false) {
                                // 데이터 로드
                                if(lastitemVisibleFlag == true) {
                                   // Integer page = (listview.getCount() / nPageSize) + 1;
                                    nPage = nPage + 1;
                                    GetSignageList(nPage);
                                }
                                mLockListView = false;
                                lastitemVisibleFlag = false;
                            }
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);

                        }


                    });
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            MOBILE_SIGNAGE_LIST data = adapter.getItem(position);
                            //Toast.makeText(getActivity(),adItem.TITLE  + "가 선택되었습니다.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getContext(), SignageControlActivity.class);
                            intent.putExtra("SIGN_CODE", data.SIGN_CODE);
                            getContext().startActivity(intent);
                        }
                    });
                }
                @Override
                public void onFailure(Call<List<MOBILE_SIGNAGE_LIST>> call, Throwable t) {
                    Global.getCommon().ProgressHide(getActivity());
                }
            });

        }catch(Exception ex) {
            Log.d("로그", ex.getMessage());
        }
    }
}
