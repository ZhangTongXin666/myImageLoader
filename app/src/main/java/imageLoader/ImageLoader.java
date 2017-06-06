package imageLoader;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.kys_31.voicehint.R;
import com.example.myimageloader.ImageResizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 张同心
 * @function 图片加载
 * Created by kys_31 on 2017/6/5.
 */

public class ImageLoader {

    private static final String TAG="ImageLoader";

    public static final int MESSAGE_POST_RESULT=1;
    private static final int CPU_COUNT=Runtime.getRuntime().availableProcessors();//CPU数量
    /*配置线程池*/
    private static final int CODE_POOL_SIZE=CPU_COUNT+1;
    private static final int MAINMUM_POOL_SIZE=CPU_COUNT*2+1;
    private static final long KEEP_ALIVE=10L;

    private static final int TAG_KEY_URL= R.id.civ_picture;
    private static final long DISK_CACHE_SIZE=1024*1024*50;//50兆磁盘内存
    private static final int IO_BUFFER_SIZE=8*1024;
    private static final int DISK_CAHCE_INDEX=0;
    private boolean mIsDiskLruCacheCreated=false;

    /*创建线程工厂*/
    private static final ThreadFactory sThreadFactory=new ThreadFactory() {
        private final AtomicInteger mCount=new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,"imageLoader#"+mCount.getAndIncrement());
        }
    } ;

    /*创建线程池*/
    public static final Executor THREAD_POOL_EXECUTOR=new ThreadPoolExecutor(CODE_POOL_SIZE,MAINMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>(),sThreadFactory);

    /*切换到主线程，更新UI*/
    private Handler mMainHandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            LoaderResult result=(LoaderResult)msg.obj;
            ImageView imageView=result.imageView;
            String uri=(String)imageView.getTag(TAG_KEY_URL);
            if (uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else {
                Log.e(TAG,"set Image bitmap,but uri has changed,ignored!");
            }
        }
    };

    private Context mContext;
    private LruCache<String,Bitmap> mMemoryCache;
    private com.example.myimageloader.DiskLruCache mDiskLruCache;

    private ImageLoader(Context context){
        mContext=context.getApplicationContext();
        int maxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);//应用所在进程占用的内存
        int cacheSize=maxMemory/8;
        Log.e(TAG,"maxMemory:"+maxMemory);
        /*初始化内存缓存*/
        mMemoryCache=new LruCache<String,Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key,Bitmap bitmap){
                Log.e(TAG,"sizeOf:"+(bitmap.getRowBytes()*bitmap.getHeight()/1024));
                return bitmap.getRowBytes()*bitmap.getHeight()/1024;
            }
        };

        File diskCacheDir=getDiskCacheDir(mContext,"bitmap");
        if (!diskCacheDir.exists()){
            diskCacheDir.mkdir();
        }
        if (getUsableSpace(diskCacheDir)>DISK_CACHE_SIZE){
            try {
                mDiskLruCache= com.example.myimageloader.DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated=true;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /*通过Build建立对象*/
    public static com.example.myimageloader.ImageLoader build(Context context){
        return new com.example.myimageloader.ImageLoader(context);
    }

    /*绑定图片和URL*/
    public void bindBitmap(final String url,final ImageView imageView){
        bindBitmap(url,imageView,0,0);
    }

    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemCache(key)==null){
            mMemoryCache.put(key,bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }



    public void bindBitmap(final String uri,final ImageView imageView,final int reqWidth,final int reqHeight){
        imageView.setTag(TAG_KEY_URL,uri);
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if (bitmap!=null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadVirnapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=loadBitmap(uri,reqWidth,reqHeight);
                if (bitmap!=null){
                    LoaderResult result = new LoaderResult(imageView,uri,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };

        THREAD_POOL_EXECUTOR.execute(loadVirnapTask);
    }

    private Bitmap loadBitmap(String uri,int reqWidth,int reqHeight){
        Bitmap bitmap=loadBitmapFromMemCache(uri);
        if (bitmap!=null){
            Log.e(TAG,"loadBitmapFromMemCache,url:"+uri);
            return bitmap;
        }
        try {
            bitmap=loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
            if (bitmap!=null){
                Log.e(TAG,"loadBitmapFromDisk,uri:"+uri);
                return bitmap;
            }
            bitmap=loadBitmapFromHttp(uri,reqWidth,reqHeight);
            Log.e(TAG,"loadBitmapFromHttp,uri:"+uri);
        }catch (IOException e){
            e.printStackTrace();
        }

        if (bitmap==null && !mIsDiskLruCacheCreated){
            Log.e(TAG,"encounter error,DiskLruCache is  mot  created.");
            bitmap=downloadBitmapFromUrl(uri);
        }
        return bitmap;
    }

    private Bitmap loadBitmapFromMemCache(String url){
        final String key=hashKeyFromUrl(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    private Bitmap loadBitmapFromHttp(String url,int reqWidth,int reqHeight) throws IOException{
        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can nit visit network from UI Thread.");
        }
        if (mDiskLruCache==null){
            return null;
        }

        String key=hashKeyFromUrl(url);
        com.example.myimageloader.DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor!=null){
            OutputStream outPutStream=editor.newOutputStream(DISK_CAHCE_INDEX);
            if (downloadUrlToStream(url,outPutStream)){
                editor.commit();
            }else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url,int reqWidth,int reqHeight) throws IOException{

        if (Looper.myLooper()==Looper.getMainLooper()){
            throw new RuntimeException("can nit visit network from UI Thread.");
        }
        if (mDiskLruCache==null){
            return null;
        }

        Bitmap bitmap=null;
        String key=hashKeyFromUrl(url);
        com.example.myimageloader.DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot!=null){
            FileInputStream fileInputStream=(FileInputStream)snapshot.getInputStream(DISK_CAHCE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = ImageResizer.decodeSampleBitmapFromFile(fileDescriptor,reqWidth,reqHeight);
            if (bitmap!=null){
                addBitmapToMemoryCache(key,bitmap);
            }
        }
        return bitmap;
    }

    private boolean downloadUrlToStream(String urlString,OutputStream outputStream){

        HttpURLConnection urlConnection=null;
        BufferedInputStream in=null;
        BufferedOutputStream out=null;
        try {
            final URL url=new URL(urlString);
            urlConnection=(HttpURLConnection)url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            out=new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);
            int b;
            while ((b=in.read())!=-1){
                out.write(b);
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
            try {
                if (out!=null){
                    out.close();
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Bitmap downloadBitmapFromUrl(String urlString){
        Bitmap bitmap=null;
        HttpURLConnection urlConnection=null;
        BufferedInputStream in=null;

        try {
            final URL url=new URL(urlString);
            urlConnection=(HttpURLConnection)url.openConnection();
            in=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap= BitmapFactory.decodeStream(in);
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (urlConnection!=null){
                urlConnection.disconnect();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /*MD5加密*/
    private String hashKeyFromUrl(String url){
        String cacheKey;
        try{
            final MessageDigest mDisgest=MessageDigest.getInstance("MD5");
            mDisgest.update(url.getBytes());
            cacheKey=bytesToHexString(mDisgest.digest());
        }catch (NoSuchAlgorithmException e){
            cacheKey=String.valueOf(url.hashCode());
            e.printStackTrace();
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes){
        StringBuilder sb=new StringBuilder();
        for (int i =0;i<bytes.length;i++){
            String hex=Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public File getDiskCacheDir(Context context,String uniqueName){
        boolean externalStorageAvailable= Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !Environment.isExternalStorageEmulated();//判断SD卡是否存在或是否被移除
        final String cachePath;
        if (externalStorageAvailable){
            cachePath=context.getExternalCacheDir().getPath();//获得/sdcard/Android/data/<application package>/cache 路径
        }else {
            cachePath=context.getCacheDir().getPath();//获取/data/data/<application package>/cache
        }
        return new File(cachePath + File.separator + uniqueName);//uniqueName 文件夹的名字 唯一
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path){
        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs stats=new StatFs(path.getPath());
        return (long)stats.getBlockSize()*(long)stats.getAvailableBlocks();
    }

    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView,String uri,Bitmap bitmap){
            this.bitmap=bitmap;
            this.uri=uri;
            this.imageView=imageView;
        }
    }

}
