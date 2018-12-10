package com.altsoft.loggalapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.altsoft.Adapter.LocalBoxListViewAdapter;
import com.altsoft.Adapter.SearchAdapter;
import com.altsoft.Adapter.SearchBannerAdapter;
import com.altsoft.Adapter.SignageListViewAdapter;
import com.altsoft.Framework.Global;
import com.altsoft.Framework.GsonInfo;
import com.altsoft.Framework.control.altAutoCmpleateTextView;
import com.altsoft.Framework.module.BaseActivity;
import com.altsoft.loggalapp.detail.LocalboxbannerListActivity;
import com.altsoft.model.DEVICE_LOCATION;
import com.altsoft.model.DEVICE_LOCATION_COND;
import com.altsoft.model.T_AD;
import com.altsoft.model.category.CATEGORY_COND;
import com.altsoft.model.category.CATEGORY_LIST;
import com.altsoft.model.keyword.CODE_DATA;
import com.altsoft.model.keyword.KEYWORD_COND;
import com.altsoft.model.search.MOBILE_AD_SEARCH_COND;
import com.altsoft.model.search.MOBILE_AD_SEARCH_DATA;
import com.altsoft.model.signage.MOBILE_SIGNAGE_COND;
import com.altsoft.model.signage.MOBILE_SIGNAGE_LIST;
import com.altsoft.togglegroupbutton.MultiSelectToggleGroup;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Search2Activity extends BaseActivity {
    private String TAG = Search2Activity.class.getSimpleName();
    private android.support.v7.widget.Toolbar tbMainSearch;

    Activity activity;
    SearchAutoCompleate searchAutoCompleate;
    SearchCategory searchCategory;
    SearchBanner searchBanner;
    SearchLocalBox searchLocalBox;
    SearchSignage searchSignage;

    /// 자동완성
    private  class SearchAutoCompleate{
        SearchAdapter adapter;
        List<String> list;          // 데이터를 넣은 리스트변수
        altAutoCmpleateTextView autoCompleteTextView ;
        Boolean bAutoDrop = false;
        String beforeData = "";

        private SearchAutoCompleate(){}

        private void onResume() {
            Global.getCommon().hideSoftInputWindow(activity, autoCompleteTextView, true);
        }


        private void setUpViews() {

            list = new ArrayList<String>();
            autoCompleteTextView = (altAutoCmpleateTextView) activity.findViewById(R.id.autoCompleteTextView);
            autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    autoCompleteTextView.dismissDropDown();

                    adapter.setSelectedItem(adapter.getObject(position));;

                    bAutoDrop = false;
                }
            });
            autoCompleteTextView.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // SearchAutoCompleate.beforeData = s.toString();
                    bAutoDrop = true;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String data = s.toString();
                    if(!beforeData.equals(data)) {
                        if (data.length() > 0) {
                            settingList(data);
                        }
                        if(adapter != null) {
                            if(!(adapter.getSelectedItem().NAME.equals(s.toString())))
                            {
                                adapter.setSelectedItem(new CODE_DATA());
                            }
                        }
                    }
                    beforeData = data;
                }
            });
        }

        /// 자동완성 값 셋팅
        private void settingList(String query){
            list = new ArrayList<String>();

            KEYWORD_COND Cond = new KEYWORD_COND();
            Cond.KEYWORD_NAME = query;
            try {
                Call<List<CODE_DATA>> call = Global.getAPIService().GetKeywordAutoCompleateList(Cond);

                call.enqueue(new Callback<List<CODE_DATA>>() {
                    @Override
                    public void onResponse(Call<List<CODE_DATA>> call, Response<List<CODE_DATA>> response) {
                        list = new ArrayList<String>();
                        List<CODE_DATA> rtn = response.body();
                        for(CODE_DATA data : rtn) {
                           list.add(data.NAME);
                        }
                        adapter = new SearchAdapter(activity, R.layout.autocomplate_list_item, rtn);
                        autoCompleteTextView.setAdapter(adapter);
                        if(bAutoDrop) {
                            autoCompleteTextView.showDropDown();
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CODE_DATA>> call, Throwable t) {

                    }
                });

            }catch(Exception ex) {
                Log.d("로그", ex.getMessage());
            }
        }
    }
    /// 카테고리 검색
    private  class SearchCategory {
        MultiSelectToggleGroup multiCustomCompoundButton;
        private SearchCategory(){
            multiCustomCompoundButton = activity.findViewById(R.id.group_multi_custom_compoundbutton);
            SetCategoryList();
            multiCustomCompoundButton.setOnCheckedChangeListener(new MultiSelectToggleGroup.OnCheckedStateChangeListener() {
                @Override
                public void onCheckedStateChanged(MultiSelectToggleGroup group, int checkedId, boolean isChecked) {
                    //Set<Integer> chklist = multiCustomCompoundButton.getCheckedIds();
                    for(Integer  data: multiCustomCompoundButton.getCheckedIds())
                        Log.v("dd", "onCheckedStateChanged(): " + checkedId + ", isChecked = " + isChecked);

                    searchBanner.doQuery(1);
                }
            });
        }

        /// 배너정보가져오기
        private void SetCategoryList() {

            CATEGORY_COND Cond = new CATEGORY_COND();

            try {
                Cond.HIDE = false;
                Cond.CATEGORY_TYPE = 1;

                Call<List<CATEGORY_LIST>> call = Global.getAPIService().GetCategoryList(Cond);

                call.enqueue(new Callback<List<CATEGORY_LIST>>() {
                    @Override
                    public void onResponse(Call<List<CATEGORY_LIST>> call, Response<List<CATEGORY_LIST>> response) {
                        List<CATEGORY_LIST> list = response.body();
                        for(CATEGORY_LIST data  : list)
                        {
                            multiCustomCompoundButton.addButton(data.CATEGORY_CODE, data.CATEGORY_NAME);
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CATEGORY_LIST>> call, Throwable t) {
                    }
                });

            }catch(Exception ex) {
                Log.d("로그", ex.getMessage());
            }
        }
    }
    /// 배너검색
    private  class SearchBanner {

        SearchBannerAdapter adapter;
        ListView listview ;
        boolean bLastPage = false;
        Integer nFirstPageSize = 3;
        Integer nPageSize = 20;
        Integer nPage = 1;




        private void setUpViews()
        {
            listview = (ListView) activity.findViewById(R.id.listview1);
            adapter = new SearchBannerAdapter();

        }
        private void doQuery()
        {
            this.doQuery(1);
        }
        private void doQuery(Integer page)
        {

            try {
                bLastPage = false;
                doQuery(page, nFirstPageSize);
            }catch(Exception ex){ Log.d(TAG, ex.getMessage());}
        }
        /// 모바일 조회
        private void doQuery(Integer page, Integer pagesize)
        {
            if( bLastPage ) {
                Toast.makeText(activity,"데이터가 모두 검색되었습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            nPage = page;
            MOBILE_AD_SEARCH_COND Cond = new MOBILE_AD_SEARCH_COND();

            Cond.PAGE_COUNT = pagesize;
            Cond.PAGE = page;
            Cond.LONGITUDE = Global.getMapInfo().longitude;
            Cond.LATITUDE  = Global.getMapInfo().latitude;
            Cond.USER_ID = Global.getLoginInfo().USER_ID;
            Object[] arrCategory = searchCategory.multiCustomCompoundButton.getCheckedIds().toArray();
            for(int i=0;  i < arrCategory.length; i++){
                if(i==0) Cond.CATEGORY_CODES =   arrCategory[i].toString();
                else Cond.CATEGORY_CODES = Cond.CATEGORY_CODES + "," + arrCategory[i].toString();
            }

            if(searchAutoCompleate.adapter != null
                    && searchAutoCompleate.adapter.getSelectedItem() != null
                    && searchAutoCompleate.adapter.getSelectedItem().CODE != null)
            {
                Cond.KEYWORD_CODE = searchAutoCompleate.adapter.getSelectedItem().CODE;
                Cond.KEYWORD_CODE = (Cond.KEYWORD_CODE == null || Cond.KEYWORD_CODE < 0) ? null : Cond.KEYWORD_CODE;
            }
            else
            {
                Cond.KEYWORD_NAME = searchAutoCompleate.autoCompleteTextView.getText().toString();
            }


            String sJson = new GsonInfo<MOBILE_AD_SEARCH_COND, String>(MOBILE_AD_SEARCH_COND.class).ToString(Cond);
            Global.getCommon().ProgressShow(activity);
            Call<List<MOBILE_AD_SEARCH_DATA>> call = Global.getAPIService().GetMobileAdSearchList(Cond);

            call.enqueue(new Callback<List<MOBILE_AD_SEARCH_DATA>>() {

                @Override
                public void onResponse(Call<List<MOBILE_AD_SEARCH_DATA>> call, Response<List<MOBILE_AD_SEARCH_DATA>> response) {
                    List<MOBILE_AD_SEARCH_DATA> list = response.body();
                    Global.getCommon().ProgressHide(activity);

                    if((nPage ==  1 &&  list.size() < nFirstPageSize)
                            || ( list.size() > nFirstPageSize && list.size() < nPageSize)) {
                        bLastPage = true;
                    }

                    //if(searchBannerAdapter.SetDataBind(list, (list.size() <= 4) ? true : false  ) == true) return;
                    if(adapter.SetDataBind(list, nPage == 1 ? true : false) ) return;
                    listview.setAdapter(adapter);
                    Global.getCommon().getTotalHeightofListView(listview);
                    if(listview.getCount() == 0) {
                        activity.findViewById(R.id.laybanner).setVisibility(LinearLayout.GONE);
                    }
                    else {
                        activity.findViewById(R.id.laybanner).setVisibility(LinearLayout.VISIBLE);
                    }
/*
                ListPageParam.listview.setOnScrollListener(new ListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && ListPageParam.lastitemVisibleFlag && ListPageParam.mLockListView == false) {

                            // 데이터 로드
                            if(ListPageParam.lastitemVisibleFlag == true) {
                                Integer page = ((ListPageParam.listview.getCount() - ListPageParam.nFirstPageSize)  / ListPageParam.nPageSize) + 2;
                                if( ListPageParam.nPage == 1 && ListPageParam.listview.getCount() == ListPageParam.nFirstPageSize) {
                                    page = 1;
                                }


                                doQueryMobileBanner(page, ListPageParam.nPageSize);
                            }
                            ListPageParam.mLockListView = false;
                            ListPageParam.lastitemVisibleFlag = false;
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        ListPageParam.lastitemVisibleFlag = (totalItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount);
                    }
                });
*/
                   listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            MOBILE_AD_SEARCH_DATA data = adapter.getItem(position);
                            T_AD adItem = new T_AD();
                            try {
                                adItem = new GsonInfo<MOBILE_AD_SEARCH_DATA, T_AD>(MOBILE_AD_SEARCH_DATA.class, T_AD.class).ToCopy(data);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            //Toast.makeText(getActivity(),adItem.TITLE  + "가 선택되었습니다.", Toast.LENGTH_LONG).show();
                            Toast.makeText(activity,adItem.TITLE  + "가 선택되었습니다.", Toast.LENGTH_LONG).show();

                            Intent intent = new Intent(activity, WebViewActivity.class);
                            intent.putExtra("T_AD", adItem);
                            activity.startActivity(intent);


                        }
                    });
                }
                @Override
                public void onFailure(Call<List<MOBILE_AD_SEARCH_DATA>> call, Throwable t) {
                    Global.getCommon().ProgressHide(activity);
                }
            });
        }

    }
    /// 로컬박스 조회
    private  class SearchLocalBox {
        LocalBoxListViewAdapter adapter;
        ListView listview ;
        boolean bLastPage = false;
        Integer nFirstPageSize = 3;
        Integer nPageSize = 20;
        Integer nPage = 1;
        boolean lastitemVisibleFlag = false;
        boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수

        private void setUpViews()
        {
            listview = (ListView) activity.findViewById(R.id.listview_localbox);
            adapter = new LocalBoxListViewAdapter();

        }
        private void doQuery()
        {
            doQuery(1);
        }
        private void doQuery(Integer page)
        {

            try {
                bLastPage = false;
                doQuery(page, nFirstPageSize);
            }catch(Exception ex){ Log.d(TAG, ex.getMessage());}
        }
        /// 모바일 조회
        private void doQuery(Integer page, Integer pagesize)
        {
            if( bLastPage ) {
                Toast.makeText(activity,"데이터가 모두 검색되었습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            nPage = page;
            DEVICE_LOCATION_COND Cond = new DEVICE_LOCATION_COND();

            Cond.PAGE_COUNT = pagesize;
            Cond.PAGE = page;
            Cond.LONGITUDE = Global.getMapInfo().longitude;
            Cond.LATITUDE  = Global.getMapInfo().latitude;

            Object[] arrCategory = searchCategory.multiCustomCompoundButton.getCheckedIds().toArray();

            Cond.SEARCH_TEXT = searchAutoCompleate.autoCompleteTextView.getText().toString();

            String sJson = new GsonInfo<DEVICE_LOCATION_COND, String>(DEVICE_LOCATION_COND.class).ToString(Cond);
            Global.getCommon().ProgressShow(activity);
            Call<List<DEVICE_LOCATION>> call = Global.getAPIService().GetDeviceLocation(Cond);

            call.enqueue(new Callback<List<DEVICE_LOCATION>>() {

                @Override
                public void onResponse(Call<List<DEVICE_LOCATION>> call, Response<List<DEVICE_LOCATION>> response) {
                    List<DEVICE_LOCATION> list = response.body();
                    Global.getCommon().ProgressHide(activity);

                    if((nPage ==  1 &&  list.size() < nFirstPageSize)
                            || ( list.size() > nFirstPageSize && list.size() < nPageSize)) {
                        bLastPage = true;
                    }

                    //if(searchBannerAdapter.SetDataBind(list, (list.size() <= 4) ? true : false  ) == true) return;
                    if(adapter.SetDataBind(list, (nPage == 1 ? true : false)) ) return;
                    listview.setAdapter(adapter);
                    if(listview.getCount() == 0) {
                        activity.findViewById(R.id.laylocalbox).setVisibility(LinearLayout.GONE);
                    }
                    else {
                        activity.findViewById(R.id.laylocalbox).setVisibility(LinearLayout.VISIBLE);
                    }
                    Global.getCommon().getTotalHeightofListView(listview);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            DEVICE_LOCATION data = adapter.getItem(position);
                            //Toast.makeText(getActivity(),adItem.TITLE  + "가 선택되었습니다.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(activity, LocalboxbannerListActivity.class);
                            intent.putExtra("DEVICE_CODE", Long.parseLong(data.DEVICE_CODE) );
                            activity.startActivity(intent);
                        }
                    });
                }

                @Override
                public void onFailure(Call<List<DEVICE_LOCATION>> call, Throwable t) {
                    Global.getCommon().ProgressHide(activity);
                }
            });
        }

    }

    /// 사이니지 조회
    private  class SearchSignage {
        SignageListViewAdapter adapter;
        ListView listview ;
        boolean bLastPage = false;
        Integer nFirstPageSize = 3;
        Integer nPageSize = 20;
        Integer nPage = 1;
        boolean lastitemVisibleFlag = false;
        boolean mLockListView = false;          // 데이터 불러올때 중복안되게 하기위한 변수

        private void setUpViews()
        {
            listview = (ListView) activity.findViewById(R.id.listview_signage);
            adapter = new SignageListViewAdapter();

        }
        private void doQuery()
        {
            doQuery(1);
        }
        private void doQuery(Integer page)
        {

            try {
                nPage = page;
                bLastPage = false;
                doQuery(page, nFirstPageSize);
            }catch(Exception ex){ Log.d(TAG, ex.getMessage());}
        }
        /// 모바일 조회
        private void doQuery(Integer page, Integer pagesize)
        {
            if( bLastPage ) {
                Toast.makeText(activity,"데이터가 모두 검색되었습니다.", Toast.LENGTH_LONG).show();
                return;
            }
            nPage = page;
            MOBILE_SIGNAGE_COND Cond = new MOBILE_SIGNAGE_COND();

            Cond.PAGE_COUNT = pagesize;
            Cond.PAGE = page;
            Cond.LONGITUDE = Global.getMapInfo().longitude;
            Cond.LATITUDE  = Global.getMapInfo().latitude;

            Cond.SIGN_NAME = searchAutoCompleate.autoCompleteTextView.getText().toString();

            String sJson = new GsonInfo<MOBILE_SIGNAGE_COND, String>(MOBILE_SIGNAGE_COND.class).ToString(Cond);
            Global.getCommon().ProgressShow(activity);
            Call<List<MOBILE_SIGNAGE_LIST>> call = Global.getAPIService().GetMobileSignageList(Cond);

            call.enqueue(new Callback<List<MOBILE_SIGNAGE_LIST>>() {

                @Override
                public void onResponse(Call<List<MOBILE_SIGNAGE_LIST>> call, Response<List<MOBILE_SIGNAGE_LIST>> response) {
                    List<MOBILE_SIGNAGE_LIST> list = response.body();
                    Global.getCommon().ProgressHide(activity);

                    if((nPage ==  1 &&  list.size() < nFirstPageSize)
                            || ( list.size() > nFirstPageSize && list.size() < nPageSize)) {
                        bLastPage = true;
                    }

                    //if(searchBannerAdapter.SetDataBind(list, (list.size() <= 4) ? true : false  ) == true) return;
                    adapter.SetDataBind(list, (nPage == 1? true:false) );
                    listview.setAdapter(adapter);
                    if(listview.getCount() == 0) {
                        activity.findViewById(R.id.laySignage).setVisibility(LinearLayout.GONE);
                    }
                    else {
                        activity.findViewById(R.id.laySignage).setVisibility(LinearLayout.VISIBLE);
                    }
                    Global.getCommon().getTotalHeightofListView(listview);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            MOBILE_SIGNAGE_LIST data = adapter.getItem(position);
                            //Toast.makeText(getActivity(),adItem.TITLE  + "가 선택되었습니다.", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(activity, SignageControlActivity.class);
                            intent.putExtra("SIGN_CODE", data.SIGN_CODE);
                            activity.startActivity(intent);
                        }
                    });
                }

                @Override
                public void onFailure(Call<List<MOBILE_SIGNAGE_LIST>> call, Throwable t) {
                    Global.getCommon().ProgressHide(activity);
                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search2);
        activity = this;
        searchAutoCompleate = new SearchAutoCompleate();
        searchCategory = new SearchCategory();
        searchBanner = new SearchBanner();
        searchLocalBox = new SearchLocalBox();
        this.searchSignage = new SearchSignage();
        this.setUpViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchAutoCompleate.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.searchAutoCompleate = null;
        this.searchBanner = null;
        this.searchCategory = null;
        this.searchLocalBox = null;
        this.searchSignage = null;
    }


    @SuppressLint("ResourceAsColor")
    protected void setUpViews() {

        tbMainSearch = (android.support.v7.widget.Toolbar) findViewById(R.id.tb_toolbarsearch);
        setSupportActionBar(tbMainSearch);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        searchAutoCompleate.setUpViews();
        searchBanner.setUpViews();;
        searchLocalBox.setUpViews();
        searchSignage.setUpViews();
        ImageButton search = (ImageButton)findViewById(R.id.btnSearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQuery();
            }
        });
        Global.getCommon().ProgressHide(this);
        doQuery();
    }


    /// 전체조회
    private void doQuery() {
        searchBanner.doQuery();
        searchLocalBox.doQuery();
        searchSignage.doQuery();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
