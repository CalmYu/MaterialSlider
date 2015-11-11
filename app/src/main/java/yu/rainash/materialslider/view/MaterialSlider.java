package yu.rainash.materialslider.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;

import yu.rainash.materialslider.R;


/**
 * setProgress, setOnProgressChangeListener, setMax, setMin
 * A material style seekbar
 * @author CalmYu
 * @date 2015-8-2
 */
public class MaterialSlider extends CustomView {

	private static final int ANIM_FRAME_DELAY = 15;

	private static final int DEFAULT_MAX = 100;

	private static final int DEFAULT_MIN = 0;

	/**
	 * Default main color
	 */
	private static final int COLOR_MAIN = Color.BLUE;

	private static final int COLOR_GREY = 0xffBEBEBE;
	
	private Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Paint mBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Paint mGreyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	private Ball mBall = new Ball();
	
	private int mViewWidth;
	
	private int mViewHeight;
	
	private Paint mBaseLinePaint = new Paint();
	
	private Rect mBaseLineRect = new Rect();
	
	private Rect mBottomLayoutRect = new Rect();
	
	private int mBaseLineHeight;
	
	private int mGap;
	
	private int mBallRadius;
	
	private static final double SQRT_2 = Math.sqrt(2);
	
	private Indicator mIndicator = new Indicator();
	
	private int mIndicatorRadius;
	
	private int mProgress = 0;
	
	private int mCurrentProgress = 0;
	
	private int mLeftX;
	
	private int mRightX;
	
	private int mMinProgress;
	
	private int mMaxProgress = DEFAULT_MAX;
	
	private OnProgressChangeListener mProgressListener;
	
	private Region mValidRegion = new Region();

	/**
	 * The color style of slider
	 */
	private int mColorMain = COLOR_MAIN;
	
	public MaterialSlider(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MaterialSlider, defStyleAttr, 0);
		mColorMain = a.getInt(R.styleable.MaterialSlider_colorMain, COLOR_MAIN);
		mMaxProgress = a.getInt(R.styleable.MaterialSlider_maxValue, DEFAULT_MAX);
		mMinProgress = a.getInt(R.styleable.MaterialSlider_minValue, DEFAULT_MIN);
		a.recycle();
		mIndicatorPaint.setColor(mColorMain);
		mBallPaint.setColor(mColorMain);
		mGreyPaint.setColor(COLOR_GREY);
		mGreyPaint.setStyle(Style.STROKE);
		mGreyPaint.setStrokeWidth(dp(2));
		mBaseLineHeight = dp(2);
		mGap = dp(4);
		mBallRadius = dp(6);
		mIndicatorRadius = dp(14);
		mIndicator.setRadius(mIndicatorRadius);
		mBall.setRadius(mBallRadius);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextAlign(Align.CENTER);
		mViewHeight = (int) (mIndicator.getHeight() + mGap + mBaseLineHeight / 2 + mBallRadius * 2);
		mLeftX = mIndicatorRadius;
		mTextPaint.setTextSize(mIndicator.getTextSize());
		mBall.setPosition(mLeftX, getBaseLineCenterY());
		mCurrentProgress = mMinProgress;
		mCurrentBallRadius = mBallRadius;
	}

	public MaterialSlider(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MaterialSlider(Context context) {
		this(context, null);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewWidth = getMeasurement(widthMeasureSpec, 0);
		setMeasuredDimension(mViewWidth, mViewHeight);
	}

	/**
	 * Writing in onMeasure has the same effect
	 * @param w
	 * @param h
	 * @param oldw
	 * @param oldh
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mRightX = mViewWidth - mIndicatorRadius;
		mBottomLayoutRect.set(mLeftX, (int) (getBaseLineCenterY() - mBaseLineHeight / 2), mRightX, (int) (getBaseLineCenterY() + mBaseLineHeight / 2));
	}

	/**
	 * Calculate the axis of ball
	 */
	private void calcBall() {
		int length = getCurrentLength(mCurrentProgress);
		mBall.setPosition(mLeftX + length, getBaseLineCenterY());
	}

	/**
	 * Calculate the line axis
	 */
	private void calcBaseLineRect() {
		if (mCurrentProgress == mMinProgress) {
			mBottomLayoutRect.left = mLeftX + mCurrentBallRadius;
		} else {
			mBottomLayoutRect.left = mLeftX;
		}
		int length = getCurrentLength(mCurrentProgress);
		mBaseLineRect.set(mLeftX, (int) getBaseLineCenterY() - mBaseLineHeight / 2, mLeftX + length, (int) (getBaseLineCenterY() + mBaseLineHeight / 2));
	}

	/**
	 * Calculate the axis, path etc of Indicator
	 */
	private void calcIndicator() {
		int length = getCurrentLength(mCurrentProgress);
		mIndicator.setRadius(mCurrentIndicatorRadius);
		mIndicator.setBottomPosition(mLeftX + length, getBaseLineCenterY() - mBaseLineHeight / 2 - mGap);
	}

	/**
	 * Get the yAxis of line center
	 * @return
	 */
	private float getBaseLineCenterY() {
		return mViewHeight - mBallRadius * 2;
	}

	/**
	 * If user press the in the calculated region, he can move the indicator
	 */
	private void calcValidRegion() {
		mValidRegion.set((int)(mBall.getCx() - mBallRadius * 2), (int)(mBall.getCy() - mBallRadius * 2),(int)( mBall.getCx() + mBallRadius * 2), (int)(mBall.getCy() + mBallRadius * 2));
	}

	/**
	 * Calculate the distance between left side and indicator
	 * @param progress
	 * @return
	 */
	private int getCurrentLength(int progress) {
		return (mRightX - mLeftX) * (progress - mMinProgress) / (mMaxProgress - mMinProgress);
	}

	/**
	 * The animator of indicator moving
	 */
	private Runnable progressAnimator = new Runnable() {
		public void run() {
			if (mCurrentProgress != mProgress) {
				if (!indicatorAnimator.isRunning()) {
					int delta = (int) Math.ceil(4f / 100 * (mMaxProgress - mMinProgress));
					int d = mProgress - mCurrentProgress > 0 ? delta : -delta;
					if (d > 0) {
						mCurrentProgress = Math.min(mCurrentProgress + d, mProgress);
					} else if (d < 0){
						mCurrentProgress = Math.max(mCurrentProgress + d, mProgress);
					}
					invalidate();
				}
				postDelayed(this, ANIM_FRAME_DELAY);
			} else {
				shrink();
			}
		}
	};
	
	private IndicatorAnimator indicatorAnimator = new IndicatorAnimator();

	/**
	 * The animator of indicator expanding
	 */
	class IndicatorAnimator implements Runnable {

		private static final int STAY_DURATION = 300;

		boolean isExpand = true;
		
		boolean isRunning = false;
		
		long runningTime = 0;
		
		public void run() {
			boolean isNeedAni = false;
			isRunning = true;
			if (isExpand) {
				if (mCurrentBallRadius != 0 || mCurrentIndicatorRadius != mIndicatorRadius) {
					isNeedAni = true;
					mCurrentBallRadius = Math.max(0, mCurrentBallRadius - dp(2));
					mCurrentIndicatorRadius = Math.min(mCurrentIndicatorRadius + dp(3), mIndicatorRadius);
				}
			} else {
				if (mCurrentBallRadius != mBallRadius || mCurrentIndicatorRadius != 0) {
					isNeedAni = true;
					if (runningTime >= STAY_DURATION) {
						mCurrentBallRadius = Math.min(mBallRadius, mCurrentBallRadius + dp(2));
						mCurrentIndicatorRadius = Math.max(mCurrentIndicatorRadius - dp(3), 0);
					}
				}
			}
			if (isNeedAni) {
				isRunning = true;
				invalidate();
				postDelayed(this, ANIM_FRAME_DELAY);
				runningTime += ANIM_FRAME_DELAY;
			} else {
				isRunning = false;
				runningTime = 0;
			}
		}
		
		public Runnable setIsExpand(boolean b) {
			isExpand = b;
			return this;
		}
		
		public boolean isRunning() {
			return isRunning;
		}
	}
	
	private int mCurrentBallRadius;
	
	private int mCurrentIndicatorRadius;

	/**
	 * Ball --> Indicator
	 */
	private void expand() {
		removeCallbacks(indicatorAnimator);
		post(indicatorAnimator.setIsExpand(true));
	}

	/**
	 * Indicator --> Ball
	 */
	private void shrink() {
		removeCallbacks(indicatorAnimator);
		post(indicatorAnimator.setIsExpand(false));
	}
	
	private boolean canMove = false;
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				calcValidRegion();
				if (mValidRegion.contains((int)event.getX(), (int)event.getY())) {
					attemptClaimDrag();
					canMove = true;
					expand();
					invalidate();
				} else {
					setProgress(calcProgress(event.getX()));
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (canMove) {
					attemptClaimDrag();
					moveToProgress(calcProgress(event.getX()), false);
				}
				break;
			case MotionEvent.ACTION_UP:
				if (canMove) {
					shrink();
				}
				canMove = false;
				invalidate();
				performClick();
				break;
		}
		return true;
	}

	/**
	 * Calculate the progress by the xAxis
	 * @param x
	 * @return
	 */
	private int calcProgress(float x) {
		int progress = (int) (((x - mLeftX) / (mRightX - mLeftX)) * (mMaxProgress - mMinProgress) + mMinProgress);
		progress = Math.max(Math.min(progress, mMaxProgress), mMinProgress);
		return progress;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawBaseLine(canvas);
		drawBall(canvas);
		drawIndicator(canvas, mCurrentProgress + "");
	}

	/**
	 * Draw the ball
	 * @param canvas
	 */
	private void drawBall(Canvas canvas) {
		calcBall();
		if (mCurrentProgress == mMinProgress) {
			canvas.drawCircle(mBall.getCx(), mBall.getCy(), mCurrentBallRadius, mGreyPaint);
		} else {
			canvas.drawCircle(mBall.getCx(), mBall.getCy(), mCurrentBallRadius, mBallPaint);
		}
	}

	/**
	 * Draw the line through center
	 * @param canvas
	 */
	private void drawBaseLine(Canvas canvas) {
		calcBaseLineRect();
		mBaseLinePaint.setColor(COLOR_GREY);
		canvas.drawRect(mBottomLayoutRect, mBaseLinePaint);
		mBaseLinePaint.setColor(mColorMain);
		canvas.drawRect(mBaseLineRect, mBaseLinePaint);
	}

	/**
	 * Draw the indicator
	 * @param canvas
	 * @param text
	 */
	private void drawIndicator(Canvas canvas, String text) {
		calcIndicator();
		if (mCurrentProgress == mMinProgress) {
			mIndicatorPaint.setColor(COLOR_GREY);
		} else {
			mIndicatorPaint.setColor(mColorMain);
		}
		canvas.drawPath(mIndicator.createPath(), mIndicatorPaint);
		if (mIndicator.getTextSize() > 0) {
			canvas.drawText(text, mIndicator.getCx(), mIndicator.getCy(mTextPaint), mTextPaint);
		}
	}
	
	public void setProgress(int progress) {
		moveToProgress(progress, true);
	}

	/**
	 * Move to specified progress with animation
	 * @param progress
	 * @param isNeedAnimation
	 */
	public void moveToProgress(int progress, boolean isNeedAnimation) {
		mProgress = Math.max(Math.min(mMaxProgress, progress), mMinProgress);
		if (isNeedAnimation) {
			expand();
			removeCallbacks(progressAnimator);
			post(progressAnimator);
		} else {
			mCurrentProgress = mProgress;
			invalidate();
		}
		if (mProgressListener != null) {
			mProgressListener.onProgressChange(mProgress);
		}
	}
	
	public int getProgress() {
		return mProgress;
	}
	
	public void setMax(int max) {
		this.mMaxProgress = max;
	}
	
	public int getMax() {
		return mMaxProgress;
	}
	
	public void setMin(int min) {
		this.mMinProgress = min;
		mCurrentProgress = min;
	}
	
	public int getMin() {
		return mMinProgress;
	}
	
	public void setOnProgressChangeListener (OnProgressChangeListener listener) {
		this.mProgressListener = listener;
	}
	
	public OnProgressChangeListener getOnProgressChangeListener() {
		return mProgressListener;
	}
	
	public interface OnProgressChangeListener {
		void onProgressChange(int progress);
	}

	/**
	 * Normal state
	 */
	class Ball {
		
		float cx;
		
		float cy;
		
		int radius;
		
		public void setPosition(float x, float y) {
			setCx(x);
			setCy(y);
		}

		public float getCx() {
			return cx;
		}

		public void setCx(float cx) {
			this.cx = cx;
		}

		public float getCy() {
			return cy;
		}

		public void setCy(float cy) {
			this.cy = cy;
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
		}
		
	}

	/**
	 * The indicator state when user press the ball
	 */
	class Indicator {

		float bottomX;

		float bottomY;

		int radius;
		
		float cx;
		
		float cy;

		Path path = new Path();

		public Indicator() {

		}

		public Indicator(float x, float y, int radius) {
			setBottomX(x);
			setBottomY(y);
			setRadius(radius);
		}
		
		public void setBottomPosition(float x, float y) {
			setBottomX(x);
			setBottomY(y);
		}

		public float getBottomX() {
			return bottomX;
		}

		public void setBottomX(float bottomX) {
			this.bottomX = bottomX;
			cx = bottomX;
		}

		public float getBottomY() {
			return bottomY;
		}

		public void setBottomY(float bottomY) {
			this.bottomY = bottomY;
			cy = (float) (bottomY - SQRT_2 * radius);
		}

		public int getRadius() {
			return radius;
		}

		public void setRadius(int radius) {
			this.radius = radius;
			cy = (float) (bottomY - SQRT_2 * radius);
		}

		public Path createPath() {
			path.reset();
			path.moveTo(bottomX, bottomY);
			float xx = (float) (bottomX - SQRT_2 * radius / 2);
			float xy = (float) (bottomY - SQRT_2 * radius / 2);
			path.lineTo(xx, xy);
			RectF r = new RectF((int) (bottomX - radius),
					(int) (bottomY - (SQRT_2 + 1) * radius),
					(int) (bottomX + radius), 
					(int) (bottomY - (SQRT_2 - 1)* radius));
			path.arcTo(r, 135, 270);
			path.close();
			return path;
		}
		
		public float getCx() {
			return cx;
		}
		
		public float getCy(Paint paint) {
			return cy + getTextHeight(paint) / 2 - paint.getFontMetrics().descent;
		}
		
		public int getTextSize() {
			if (radius != mIndicatorRadius) {
				return 0;
			}
			return radius * 3 / 4;
		}
		
		public float getWidth() {
			return 2 * radius;
		}
		
		public float getHeight() {
			return (float) ((1 + SQRT_2) * radius) + 1;
		}
	}
}
