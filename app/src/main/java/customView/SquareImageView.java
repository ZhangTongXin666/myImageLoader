package customView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author 张同心
 * @function 自定义方形图片
 * Created by kys_31 on 2017/6/6.
 */

public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }
    public SquareImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,widthMeasureSpec);
    }
}
