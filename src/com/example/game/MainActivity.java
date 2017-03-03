package com.example.game;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity {
	private Timer timer;

	// 开始按钮
	private Button start;

	// View
	private GameView gameView;
	private OverView overView;
	private boolean isRestart = false;

	private static int tableWidth;
	private static int tableHeight;
	// 被追球的位置
	private static float FingerX;
	private static float FingerY;
	// 被追球的大小
	private final float FINGER_SIZE = 30;
	// 小球的大小
	private final float BALL_SIZE = 35;
	// 小球纵向的运行速度
	private double ySpeed = 15;
	Random rand = new Random();
	// 返回一个-0.5~0.5的比率，用于控制小球的运行方向。
	private double x = rand.nextDouble() - 0.5;
	private double xyRate = rand.nextDouble() * 2 + 1.0;
	// 小球横向的运行速度
	private double xSpeed = x > 0 ? ySpeed * xyRate : -ySpeed * xyRate;
	// ballX和ballY代表小球的座标
	private float ballX;
	private float ballY = 60;
	private float BeginX, BeginY;
	private int color = Color.BLUE;

	// 开始计时
	private boolean TimeStart = false;
	// 时间
	private String status = "StartGame";

	private int lastTime=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 获取窗口管理器
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		// 获得屏幕宽和高
		tableWidth = metrics.widthPixels;
		tableHeight = metrics.heightPixels;
		FingerX = BeginX = tableWidth / 2;
		FingerY = BeginY = tableHeight - 50;
		ballX = tableWidth / 2;
		// 去掉窗口标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏显示
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 创建GameView组件
		gameView = new GameView(this);
		overView = new OverView(this);
		//
		setContentView(R.layout.activity_main);
		start = (Button) this.findViewById(R.id.start);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGame();
			}
		});
	}

	private void reStart() {
		isRestart = false;
		timer.cancel();
		setContentView(overView);
	}

	private void startGame() {
		setContentView(gameView);
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 0x123) {
					gameView.invalidate();
				}
			}
		};
		
		timer = new Timer();
		timer.schedule(new TimerTask() //
				{
					@Override
					public void run() {
						if (color == Color.RED) {
							
							TimeStart = true;
							double distance = Math.sqrt((ballX - FingerX)
									* (ballX - FingerX) + (ballY - FingerY)
									* (ballY - FingerY));
							// 如果小球碰到左右边框
							if (ballX <= BALL_SIZE
									|| ballX >= tableWidth - BALL_SIZE) {
								xSpeed = -xSpeed;
							}
							// 如果小球碰到上下边框
							else if (ballY <= BALL_SIZE
									|| ballY >= tableHeight - BALL_SIZE) {
								ySpeed = -ySpeed;
							}
							// 判断是否发生碰撞
							else if (distance <= BALL_SIZE + FINGER_SIZE) {
								color = Color.BLUE;
								lastTime=0;
								isRestart = true;
							}
							// 判断是否到达警戒距离
							else if (distance <= 400) {
								if ((ballY <= (FingerY + 80))
										&& (ballY >= (FingerY - 80))) {
									double r = (ballY - FingerY)
											/ (ballX - FingerX);
									double a = Math.atan(r) * (180 / Math.PI);
									if (a < 0) {
										a += 180;
									}
									if (FingerY >= ballY) {
										ySpeed = 40 * Math
												.sin((a * (Math.PI / 180)));
										xSpeed = 40 * Math
												.cos((a * (Math.PI / 180)));
									} else {
										ySpeed = -40
												* Math.sin((a * (Math.PI / 180)));
										xSpeed = -40
												* Math.cos((a * (Math.PI / 180)));
									}

								} else {
									double rate1 = (ballY - FingerY)
											/ (ballX - FingerX);
									double rate2 = ySpeed / xSpeed;
									double angle1 = Math.atan(rate1)
											* (180 / Math.PI);
									double angle2 = Math.atan(rate2)
											* (180 / Math.PI);
									if (angle1 < 0) {
										angle1 += 180;
									}
									if (angle2 < 0) {
										angle2 += 180;
									}
									double hd = (angle2 - angle1) / 4;
									;
									if (ballY < FingerY - 80) {
										ySpeed = 40 * Math.sin((angle2 - hd)
												* (Math.PI / 180));
										xSpeed = 40 * Math.cos((angle2 - hd)
												* (Math.PI / 180));
									} else {
										ySpeed = -40
												* Math.sin((angle2 - hd)
														* (Math.PI / 180));
										xSpeed = -40
												* Math.cos((angle2 - hd)
														* (Math.PI / 180));
									}
								}
							}
							// 小球座标增加
							ballY += ySpeed;
							ballX += xSpeed;
						}
						// 发送消息，通知系统重绘组件
						handler.sendEmptyMessage(0x123);
					}
				}, 0, 100);
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (color == Color.RED) {
					
					lastTime+=1;
					update(lastTime);
				}
			}

			

		}, 0, 1000);
	}

	class OverView extends View {
		Paint paint = new Paint();

		public OverView(Context context) {
			super(context);
			setFocusable(true);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int X = (int) event.getX();
			int Y = (int) event.getY();
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if ((Math.abs(X - tableWidth / 2) <= 50)
						&& (Math.abs(Y - tableHeight / 2) <= 50)) {
					isRestart = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isRestart) {
					startGame();
					isRestart = false;
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			}
			invalidate();
			return true;
		}

		public void onDraw(Canvas canvas) {
			paint.setStyle(Paint.Style.FILL);
			// 设置去锯齿
			paint.setAntiAlias(true);
			// 设置画笔文本大小
			paint.setTextSize(50);
			paint.setColor(Color.MAGENTA);
			canvas.drawCircle(tableWidth / 2, tableHeight / 2, 50, paint);
		}
	}

	class GameView extends View {
		Paint paint = new Paint();

		public GameView(Context context) {
			super(context);
			setFocusable(true);
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			final int X = (int) event.getX();
			final int Y = (int) event.getY();
			if (isRestart) {
				FingerX = BeginX;
				FingerY = BeginY;
				ballX = tableWidth / 2;
				ballY = 60;
				color = Color.BLUE;
				lastTime=0;
				status = "StartGame";
				invalidate();
				reStart();
			}
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if ((Math.abs(X - BeginX) <= 30)
						&& (Math.abs(Y - BeginY) <= 30)) {
					color = Color.RED;
					status="00:00";
				}
				break;
			case MotionEvent.ACTION_UP:
				if (color == Color.RED) {
					FingerX = BeginX;
					FingerY = BeginY;
					ballX = tableWidth / 2;
					ballY = 60;
					color = Color.BLUE;
					lastTime=0;
					status = "GameOver";
					reStart();
				} else {
					FingerX = BeginX;
					FingerY = BeginY;
					ballX = tableWidth / 2;
					ballY = 60;
					color = Color.BLUE;
					lastTime=0;
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				if (color == Color.RED) {
					FingerX = X;
					FingerY = Y;
				}
				break;
			}
			invalidate();
			return true;
		}

		public void onDraw(Canvas canvas) {
			paint.setStyle(Paint.Style.FILL);
			// 设置去锯齿
			paint.setAntiAlias(true);
			// 设置画笔文本大小
			paint.setTextSize(50);
			// 记录时间
			paint.setColor(Color.WHITE);
			canvas.drawText(status, tableWidth / 2 - 100, 80, paint);

			paint.setColor(color);
			canvas.drawCircle(FingerX, FingerY, 30, paint);
			paint.setColor(Color.GREEN);
			canvas.drawCircle(ballX, ballY, 30, paint);
		}

	}

	private void update(int lastTime) {
		// TODO Auto-generated method stub
		if(lastTime<10){
			status="00:0"+lastTime;
		}else if(lastTime>=10&&lastTime<60){
			status="00:"+lastTime;
		}else if(lastTime>=60&&lastTime<600){
			int minute=lastTime/60;
			int remainder=lastTime%60;
			if(remainder<10){
				status="0"+minute+":0"+remainder;
			}else{
				status="0"+minute+":"+remainder;
			}
		}else if(lastTime>=600&&lastTime<6000){
			int minute=lastTime/60;
			int remainder=lastTime%60;
			if(remainder<10){
				status=minute+":0"+remainder;
			}else{
				status=minute+":"+remainder;
			}
		}else{
			status="Unbelievable!!!";
		}
	}
}
