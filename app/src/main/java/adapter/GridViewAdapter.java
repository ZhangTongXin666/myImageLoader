package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.kys_31.voicehint.R;

import java.util.List;

import customView.SquareImageView;
import com.example.myimageloader.ImageLoader;

/**
 * @author 张同心
 * @function 照片墙适配器
 * Created by kys_31 on 2017/6/6.
 */

public class GridViewAdapter extends BaseAdapter {

    private Context context;
    private List<String> listUrl;
    private ImageLoader mImageLoader;
    public boolean mIfLoadBitmap=false;

    private static final String TAG="GridViewAdapter";//标签

    public GridViewAdapter(Context context, List<String> listUrl){
        this.context=context;
        this.listUrl=listUrl;
        mImageLoader=ImageLoader.build(context);
    }

    @Override
    public int getCount() {
        return listUrl.size();
    }

    @Override
    public Object getItem(int position) {
        return listUrl.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder=null;
        if (convertView==null){
            viewHolder=new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.activitymain_gridview_item,null);
            convertView.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        viewHolder.oCivPicture=(SquareImageView)convertView.findViewById(R.id.civ_picture);
        String tag=(String) viewHolder.oCivPicture.getTag();
        String url=(String)getItem(position);
        if (!url.equals(tag)){
            viewHolder.oCivPicture.setImageDrawable(context.getResources().getDrawable(R.drawable.big_older,null));
        }
        if (mIfLoadBitmap){
            viewHolder.oCivPicture.setTag(url);
            mImageLoader.bindBitmap((String)getItem(position),viewHolder.oCivPicture,300,300);
        }
        return convertView;
    }
    private class ViewHolder{
        public SquareImageView oCivPicture;
    }

}
