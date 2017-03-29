package jason.github.com.photofans.ui.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.ui.GalleryPresenter;
import jason.github.com.photofans.ui.GalleryPresenterImpl;
import jason.github.com.photofans.ui.GalleryView;
import jason.github.com.photofans.ui.adapter.GalleryAdapter;

public class GalleryActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GalleryView{

    private static final String TAG = "GalleryActivity";

    private SwipeRefreshLayout mRefresher;
    private RecyclerView mRvImgList;
    private GalleryPresenter mPresenter;
    private GalleryAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                Log.v(TAG,"onDrawerOpened()");
                if(mRefresher.isRefreshing()){
                    mRefresher.setRefreshing(false);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Log.v(TAG,"onDrawerClosed()");
                if(mRefresher.isRefreshing()){
                    mRefresher.setRefreshing(false);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        initView();
    }

    @Override
    public void onResume(){
        super.onResume();

        mPresenter.loadAllDataAsync();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_favorite) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_category) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initView(){

        mPresenter = new GalleryPresenterImpl(GalleryActivity.this,this);
        mPresenter.init();

        mRefresher = (SwipeRefreshLayout)findViewById(R.id.fragment_container);
        mRefresher.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG,"onRefresh()");
                //mPresenter.loadAllDataAsync();
                mPresenter.refresh();
            }
        });

        mRvImgList = (RecyclerView) mRefresher.findViewById(R.id.rcv_gallery);

        GridLayoutManager gridLayoutMgr = new GridLayoutManager(GalleryActivity.this,2);
        mRvImgList.setLayoutManager(gridLayoutMgr);

        mAdapter = new GalleryAdapter(GalleryActivity.this);
        mRvImgList.setAdapter(mAdapter);
    }

    @Override
    public void notifyDataChanged(List<ImageRealm> result) {
        Log.v(TAG,"notifyDataChanged(): data size = " + result.size());
        mAdapter.setImageList(result);
        mAdapter.notifyDataSetChanged();

        //mRvImgList.invalidate();
        //mPresenter.loadAllDataAsync();

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }

        if(isSuccess) {
            Toast.makeText(getApplicationContext(), R.string.refresh_success, Toast.LENGTH_SHORT)
                    .show();
        }else{
            Toast.makeText(getApplicationContext(),R.string.refresh_error,Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
