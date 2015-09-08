package yu.rainash.materialslider.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;


/**
 * @author CalmYu
 * @date 2015-8-2
 */
public class CustomView extends View{
	
	private ViewParent mParent;

	public CustomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomView(Context context) {
		super(context);
	}

	protected int dp(int dipValue) {
		return DensityUtil.dip2px(getContext(), dipValue);
	}
	
	protected int sp(int spValue) {
		return DensityUtil.dip2px(getContext(), spValue);
	}
	
	protected void attemptClaimDrag() {
		if (mParent == null) {
			mParent = getParent();
		}
		if (mParent != null) {
			mParent.requestDisallowInterceptTouchEvent(true);
		}
    }
	
	public static int getTextHeight(Paint paint) {
		FontMetrics fontMetrics = paint.getFontMetrics(); 
		double fontHeight = Math.ceil(Math.abs(fontMetrics.descent - fontMetrics.ascent));
		return (int) fontHeight;
	}
	
	public static int getTextWidth(Paint paint, String text) {
		return (int) paint.measureText(text);
	}
	
	public static float getTextDecent(Paint paint) {
		FontMetrics fm = paint.getFontMetrics();
		return fm.descent;
	}
	
	public static int getMeasurement(int measureSpec, int preferred) {
		int specSize = MeasureSpec.getSize(measureSpec);
		int measurement;
		switch (MeasureSpec.getMode(measureSpec)) {
		case MeasureSpec.EXACTLY:
			measurement = specSize;
			break;
		case MeasureSpec.AT_MOST:
			measurement = Math.min(preferred, specSize);
			break;
		default:
			measurement = preferred;
			break;
		}
		return measurement;
	}
}
