package com.example.myapplication;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
public class MemorySurface{
  int[] data;
  Bitmap bmp;
  static Paint pnt;
  public MemorySurface(){
    data = new int[96*96];
    if(pnt == null){
      pnt = new Paint();
    }
    pnt.setFilterBitmap(false);
    bmp = Bitmap.createBitmap(
4096, 4096, Bitmap.Config.ARGB_8888);
  }
  public void blit(Canvas c,Rect src,Rect dst){
    bmp.setPixels(
data, 0, 96, src.left, src.top,
96, 96);
    c.drawBitmap(bmp,src,dst,pnt);
  }
}
