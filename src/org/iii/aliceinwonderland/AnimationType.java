package org.iii.aliceinwonderland;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationType {
	public AnimationType(){
		
	}
	
	public Animation inFromTopAnimation(int nDuration){
		Animation inFromBottom = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,   0.02f
			 ); 
		
		inFromBottom.setDuration(nDuration);
		inFromBottom.setInterpolator(new AccelerateInterpolator());
		return inFromBottom;
	}
	
	public Animation outToTopAnimation(int nDuration) {
		Animation outToBottom = new TranslateAnimation(
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
		   Animation.RELATIVE_TO_PARENT,  -0.0f, Animation.RELATIVE_TO_PARENT,   -1.0f
		 );
		outToBottom.setDuration(nDuration);
		outToBottom.setInterpolator(new AccelerateInterpolator());
		return outToBottom;
	}
	
	public Animation inFromDownAnimation(int nDuration){
		Animation inFromBottom = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  1.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			 ); 
		
		inFromBottom.setDuration(nDuration);
		inFromBottom.setInterpolator(new AccelerateInterpolator());
		return inFromBottom;
	}
	
	public Animation outToDownAnimation(int nDuration) {
		Animation outToBottom = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
				Animation.RELATIVE_TO_PARENT,  0.00f, Animation.RELATIVE_TO_PARENT,   1.0f
		 );
		outToBottom.setDuration(nDuration);
		outToBottom.setInterpolator(new AccelerateInterpolator());
		return outToBottom;
	}
	
	public Animation inFromRightAnimation(int nDuration){
		Animation inFromBottom = new TranslateAnimation(
			Animation.RELATIVE_TO_PARENT,  +1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
			Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
			 );
		inFromBottom.setDuration(nDuration);
		inFromBottom.setInterpolator(new AccelerateInterpolator());
		return inFromBottom;
	}
	
	public Animation outToRightAnimation(int nDuration) {
		Animation outToBottom = new TranslateAnimation(
		   Animation.RELATIVE_TO_PARENT,  -1.0f, Animation.RELATIVE_TO_PARENT,  0.0f,
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		 );
		outToBottom.setDuration(nDuration);
		outToBottom.setInterpolator(new AccelerateInterpolator());
		return outToBottom;
	}
	
	public Animation inFromLeftAnimation(int nDuration) {
		Animation outToBottom = new TranslateAnimation(
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  +1.0f,
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		 );
		outToBottom.setDuration(nDuration);
		outToBottom.setInterpolator(new AccelerateInterpolator());
		return outToBottom;
	}
	
	public Animation outToLeftAnimation(int nDuration) {
		Animation outToBottom = new TranslateAnimation(
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,  -1.0f,
		   Animation.RELATIVE_TO_PARENT,  0.0f, Animation.RELATIVE_TO_PARENT,   0.0f
		 );
		outToBottom.setDuration(nDuration);
		outToBottom.setInterpolator(new AccelerateInterpolator());
		return outToBottom;
	}
}
