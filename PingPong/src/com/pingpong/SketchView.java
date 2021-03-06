package com.pingpong;

import com.pingpong.Block.BLOCK_TYPE;
import com.pingpong.Game.GAME_RESULT;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.view.*;


public class SketchView extends View {
		
	private boolean beginDrawing = false;
	private final int TOTAL_MESSAGES = 2;
	private final long TIMEOUT_FOR_MESSAGES_MS = 1000;
	private final int backGroundColor =  0xffffffff;
	private final int drawColor =        0xffff0000;
	private final int textColor = 0xff808080;
	
	private long lastTimeForMessageInit;
	private static Rect textBounds = new Rect();
	private String[] messages = new String[TOTAL_MESSAGES];
	private String[] messagesWithTimeout = new String[TOTAL_MESSAGES];
	private float screenAspectRatio = -1.0f;
	private Paint paint = null;
	private Paint paintText = null;	
	private Paint paintStateText = null;	
	
	private Game game = null;
	private boolean isGameMode;
		
	private void InitSketch() {
		if (paint == null) {
			paint = new Paint();
			paint.setColor(drawColor);
			paint.setStrokeWidth(2);
			paint.setStyle(Style.STROKE);
			paint.setAntiAlias(true);
		}
		if (paintText == null) {
			paintText = new Paint();
			paintText.setColor(textColor);
			paintText.setStrokeWidth(2);
			paintText.setStyle(Style.FILL);
			paintText.setAntiAlias(true);
			paintText.setTextSize(30.0f);
		}		
		if (paintStateText == null) {
			paintStateText = new Paint();
			paintStateText.setColor(0xff000000);
			paintStateText.setStrokeWidth(2);
			paintStateText.setStyle(Style.FILL);
			paintStateText.setAntiAlias(true);
			paintStateText.setTextSize(13.0f);
		}		
	}
	
	public SketchView(Context context) {
		super(context);
		InitSketch();	
		isGameMode = false;
	}
	
	public void setGameMode(boolean mode) {
		this.isGameMode = mode;
	}
	
	public void setGame(Game game) {
		this.game = game;
	}
	
	public void setBeginDrawing(boolean draw) {
		this.beginDrawing = draw;
	}
	
	public void setMessages(String str1, String str2) {
		messages[0] = str1;
		messages[1] = str2;
	}

	public void setMessagesWithTimeout(String str1, String str2) {
		lastTimeForMessageInit = System.currentTimeMillis();
		messagesWithTimeout[0] = str1;
		messagesWithTimeout[1] = str2;
	}
	
	public void setAspectRatio(float aspectRatio) {
		this.screenAspectRatio = aspectRatio;
	}
	
	private void drawStatusBar(Canvas canvas) {
		
		// draw messages
    	final String testMessage = " !";
    	paintText.getTextBounds(testMessage, 0, testMessage.length(), textBounds);
		final float textHeightPadding = 4.0f;		
		final float drawXvert = 20.0f;
		final float viewYlimit = canvas.getHeight() / 2 ;
        
		String[] messagesForStatusBar = messagesWithTimeout;
		if (System.currentTimeMillis() - lastTimeForMessageInit > TIMEOUT_FOR_MESSAGES_MS ||
			messagesForStatusBar[0] == null || messagesForStatusBar[1] == null || 
			messagesForStatusBar[0].length() == 0 || messagesForStatusBar[1].length() == 0 )
			messagesForStatusBar = messages;
		
		for (int i = 0; i < messagesForStatusBar.length; i++) {
			if (messagesForStatusBar[i] != null && messagesForStatusBar[i].length() > 0) {
				float textY = viewYlimit + 
							  screenAspectRatio * textHeightPadding + 
							  i * (textBounds.height() + textHeightPadding);
				
	        	canvas.drawText(messagesForStatusBar[i], drawXvert, textY, paintText);				
			}
		}    	
		
	}
		
	private void drawBackground(Canvas canvas) {
        canvas.drawColor(backGroundColor);
	}
	
	private void drawGame(Canvas canvas) {
		if (game == null || !isGameMode)
			return;
		
		if (game.displaySize == null) {
			game.displaySize = new Point(canvas.getWidth(), canvas.getHeight());
		}
		
		if (Block.bonusTextBounds == null) {
			Block.bonusTextBounds = new Rect[] {new Rect(), new Rect()};
			paintText.getTextBounds(Block.STRING_MINUS, 0, Block.STRING_MINUS.length(), Block.bonusTextBounds[0]);
			paintText.getTextBounds(Block.STRING_PLUS,  0, Block.STRING_PLUS.length(),  Block.bonusTextBounds[1]);
		}
		
		if (!game.isGameStarted)
			return;
				
		for (int row=0; row < game.BLOCKS_IN_COLUMN; row++) {
			
			for (int column=0; column < game.BLOCKS_IN_ROW; column++) {				
				Block block = game.blocks[row][column];
				Bitmap blockImage = block.blockImage();
				
				if (blockImage != null) {
					canvas.drawBitmap(blockImage, block.coords[0], block.coords[1], paint);
				}
				
				if (block.blockType == BLOCK_TYPE.NONE && block.coordsText[1] > 0)
					canvas.drawText(block.blockText, block.coordsText[0], block.coordsText[1], paintStateText);
				
				if (block.blockTextBonus.length() > 0 && block.coordsTextBonus != null)
					canvas.drawText(block.blockTextBonus, block.coordsTextBonus[0], block.coordsTextBonus[1], paintText);
			}
			
		}
		
		canvas.drawRect(game.racketPosition[0], game.racketPosition[1], game.racketPosition[0] + game.racketSize.x, game.racketPosition[1] + game.racketSize.y, paintText);
		
		if (game.getGameResult() == GAME_RESULT.NONE)
			canvas.drawBitmap(game.bitmapBall, game.ballPosition[0] - game.bitmapBall.getWidth() / 2, game.ballPosition[1] - game.bitmapBall.getHeight() / 2, paint);
		
		String txtPoints = String.format("Points: %d, Blocks left: %d", game.points, game.visibleBlocks);
		String txtLives = String.format("Lives: %d", Vector2D.Clamp(game.lives, 0, 3));
		Rect txtLivesBounds = new Rect();
		paintStateText.getTextBounds(txtLives, 0, txtLives.length(), txtLivesBounds);
		
		final float dY = 10.0f;
		canvas.drawText(txtPoints, 5.0f, dY + txtLivesBounds.height(), paintStateText);
		canvas.drawText(txtLives, canvas.getWidth() - 5.0f - txtLivesBounds.width(), dY + txtLivesBounds.height(), paintStateText);
		
		canvas.drawRect(1.0f, 1.0f, canvas.getWidth() - 1, canvas.getHeight() - game.STATUS_BAR_HEIGHT - 1, paint);
	}
	
    @Override
    protected void onDraw(Canvas canvas) {
    	if (!beginDrawing)
    		return;
    	
        super.onDraw(canvas);
        
        drawBackground(canvas);
        
        drawGame(canvas);

        drawStatusBar(canvas);
        
    }

}
