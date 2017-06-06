package com.example.kys_31.voicehint;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import adapter.GridViewAdapter;

public class MainActivity extends AppCompatActivity {

    private GridView mGvPictureWall;
    private List<String> listUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControl();
        initData();
        initGridView();

    }

    /**
     * 初始化数据
     */
    private void initData() {
        listUrl=new ArrayList<>();
        listUrl.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1331638760,3271773383&fm=26&gp=0.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736924827&di=beed6537d57b89cbb9cda412856435fa&imgtype=0&src=http%3A%2F%2Fimg02.tooopen.com%2Fimages%2F20160105%2Ftooopen_sy_153366142588.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497331665&di=200bf82d961181ba6e14cb32c1f28f52&imgtype=jpg&er=1&src=http%3A%2F%2Fimg1.3lian.com%2F2015%2Fa1%2F70%2Fd%2F169.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971522&di=d8a99932630934d72a0b3923b2fe8be5&imgtype=0&src=http%3A%2F%2Fimg5.duitang.com%2Fuploads%2Fitem%2F201409%2F15%2F20140915014035_iU3nX.jpeg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971521&di=bc6ac6eb4e96cc8160036fb354c2a321&imgtype=0&src=http%3A%2F%2Fimg2.3lian.com%2F2014%2Ff4%2F6%2Fd%2F106.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971520&di=607d10a0dddb309b54127cdd5a6900b7&imgtype=0&src=http%3A%2F%2Fimg2.3lian.com%2F2014%2Ff2%2F15%2Fd%2F49.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496737070594&di=73ae0ff79e86ba4ee9a4a0936cebf9ba&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D3883313783%2C600171236%26fm%3D214%26gp%3D0.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971518&di=17b6dbb71c66a3d606b74f3442c3e885&imgtype=0&src=http%3A%2F%2Fp.chanyouji.cn%2F53478%2F1375491007180p1810qcu5c1pp764pslbjbgnrd9.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971514&di=bbd800db1d76cb1201e50bd197591422&imgtype=0&src=http%3A%2F%2Fimg0.ph.126.net%2F3R8VRFvYALGL_FP49o24Rw%3D%3D%2F4871205947054694141.jpg");
        listUrl.add("https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=1331638760,3271773383&fm=26&gp=0.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736924827&di=beed6537d57b89cbb9cda412856435fa&imgtype=0&src=http%3A%2F%2Fimg02.tooopen.com%2Fimages%2F20160105%2Ftooopen_sy_153366142588.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1497331665&di=200bf82d961181ba6e14cb32c1f28f52&imgtype=jpg&er=1&src=http%3A%2F%2Fimg1.3lian.com%2F2015%2Fa1%2F70%2Fd%2F169.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971522&di=d8a99932630934d72a0b3923b2fe8be5&imgtype=0&src=http%3A%2F%2Fimg5.duitang.com%2Fuploads%2Fitem%2F201409%2F15%2F20140915014035_iU3nX.jpeg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971521&di=bc6ac6eb4e96cc8160036fb354c2a321&imgtype=0&src=http%3A%2F%2Fimg2.3lian.com%2F2014%2Ff4%2F6%2Fd%2F106.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971520&di=607d10a0dddb309b54127cdd5a6900b7&imgtype=0&src=http%3A%2F%2Fimg2.3lian.com%2F2014%2Ff2%2F15%2Fd%2F49.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496737070594&di=73ae0ff79e86ba4ee9a4a0936cebf9ba&imgtype=jpg&src=http%3A%2F%2Fimg0.imgtn.bdimg.com%2Fit%2Fu%3D3883313783%2C600171236%26fm%3D214%26gp%3D0.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971518&di=17b6dbb71c66a3d606b74f3442c3e885&imgtype=0&src=http%3A%2F%2Fp.chanyouji.cn%2F53478%2F1375491007180p1810qcu5c1pp764pslbjbgnrd9.jpg");
        listUrl.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1496736971514&di=bbd800db1d76cb1201e50bd197591422&imgtype=0&src=http%3A%2F%2Fimg0.ph.126.net%2F3R8VRFvYALGL_FP49o24Rw%3D%3D%2F4871205947054694141.jpg");
    }

    /**
     * 初始化GridView
     */
    private void initGridView() {
        GridViewAdapter adapter=new GridViewAdapter(this,listUrl);
        mGvPictureWall.setAdapter(adapter);
        mGvPictureWall.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState==SCROLL_STATE_IDLE){
                    adapter.mIfLoadBitmap=true;
                    adapter.notifyDataSetChanged();
                }
                else {
                    adapter.mIfLoadBitmap=false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void initControl() {
        mGvPictureWall=(GridView)findViewById(R.id.gv_pictureWall);
    }

}
