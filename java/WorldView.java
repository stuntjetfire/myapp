package com.example.myapplication;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import android.graphics.Path;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.File;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Arrays;
public class WorldView extends RedrawView{
RandomAccessFile handle;
int gbytes;
static int[] sizes;
long timeoff;
int X;
int Y;
static File filesDir;
MemorySurface tilesurf;
private final Rect bounds = new Rect();
private final short[]sine = new short[256];
private final short[]sharp = new short[256];
private final int[]cacheX = new int[256];
private final int[]cacheY = new int[256];
private final Path polygonPath = new Path();
private final Paint fillPaint = new Paint();
private final Paint strokePaint = new Paint();
private final Rect dst = new Rect();
private final Rect src = new Rect();
private final byte[]tile=new byte[16];
private final int[] data=new int[12];
private final int[] bg=new int[6];
private final int[] fg=new int[6];
private final int[] ob=new int[6];
public WorldView(MainActivity a){
super(a);
int i;
if(filesDir==null)filesDir=a.getFilesDir();
if(sizes==null){
sizes=new int[65];
sizes[0]=256;
for(i=1;i<65;++i){
sizes[i]=(int)Math.sqrt(((long)i)<<26);
}
}
tilesurf = new MemorySurface();
for(i=0;i<256;++i){
double s = Math.sin(i*(Math.PI/128));
sine[i] = (short)(2048*s);
s=s*.5+.5;
s=s*s;
s=s*s;
s=s*2-1;
sharp[i] = (short)(2048*s-sine[i]);
}
    fillPaint.setStyle(Paint.Style.FILL);
    fillPaint.setAntiAlias(false);
    strokePaint.setStyle(Paint.Style.STROKE);
    strokePaint.setColor(0xFF000000);
    strokePaint.setStrokeWidth(2);
    strokePaint.setAntiAlias(true);
    strokePaint.setStrokeJoin(Paint.Join.ROUND);
    strokePaint.setStrokeCap(Paint.Cap.ROUND);
}
int getNow(){
return (int)(
System.currentTimeMillis()-timeoff);
}
void randomFill(
RandomAccessFile h, long totalBytes)
throws IOException {
        //must give different results each new time
        //cryptosecurity not required
        Random rnd=new Random(
  new SecureRandom().nextLong());
        int chunkSize = 65536;
        int i;
        byte[] buffer = new byte[chunkSize];
        while (totalBytes > 0) {
            long remaining = totalBytes;
            if(remaining > chunkSize)
                remaining = chunkSize;
            rnd.nextBytes(buffer);
            for(i=0;i<chunkSize;i+=16){
              buffer[i+8]=0;
              buffer[i+9]=0;
              buffer[i+10]=0;
              buffer[i+11]=0;
              buffer[i+12]=0;
              buffer[i+13]=0;
              buffer[i+14]=0;
              buffer[i+15]=0;
            }
            h.write(buffer, 0, (int)remaining);
            totalBytes -= remaining;
        }
}
long readLe(byte[]a,int b,int c){
long ret=0;
int i;
for(i=b+c;i!=b;){
--i;
ret=(ret<<8)|(a[i]&255);
}
return ret;
}
void readColor(int[]d,int s,int b){
d[b]=((s&0xe0)*0x49)>>6;//red
d[b+1]=((s&0x1c)*0x49)>>3;//green
d[b+2]=(s&3)*0x55;//blue
}
void readSpectrum(int[]d,byte[]s,int b){
readColor(d,s[b],0);
readColor(d,s[b+1],3);
}
int spectrumSeason(int[]rgb,int now){
int daytime=now&0x1fff;
if(daytime>0x1000)daytime=0x2000-daytime;
if(daytime==0)return 0xff000000|
((rgb[0])<<16)|
((rgb[1])<<8)|
(rgb[2])
;
return 0xff000000|
((rgb[0]+(((rgb[3]-rgb[0])*daytime)>>12))<<16)|
((rgb[1]+(((rgb[4]-rgb[1])*daytime)>>12))<<8)|
(rgb[2]+(((rgb[5]-rgb[2])*daytime)>>12))
;
}
public void open(int world,int gb){
  RandomAccessFile h;
  if(handle!=null){
    h=handle;
    handle=null;
    try{
      h.close();
    }catch(Exception e){}
    h=null;
  }
  X=0;
  Y=0;
try{
  h =
new RandomAccessFile(
new File(filesDir,world+".bin"),"rw");
}catch(Exception e){return;}
  byte[]b=new byte[12];
try{
  int c=h.read(b);
  if(c==-1){
    if(gb!=-1){
      long d=sizes[gb];
      d=d*d*16;
      h.setLength(12);
      h.seek(12);
      randomFill(h,d);//write eden
      h.seek(0);
      b = "dkworld0\0\0\0\0".getBytes();
      b[7]=(byte)(gb+'0');
      h.write(b);
      gbytes=gb;
      timeoff=System.currentTimeMillis();
      handle=h;
      return;
    }
  }else if(c==12){
    if(gb==-1&&
(new String(b,0,7).equals("dkworld"))){
      gbytes=(b[7]-'0')&255;
      if(gbytes>64)gbytes=64;
      timeoff=System.currentTimeMillis() -
readLe(b,8,4);
      handle=h;
      return;
    }
  }
}catch(IOException e){}
  try{
    h.close();
  }catch(Exception e){}
}
void drawGlyphInSquare(Canvas canvas, String glyph, float boxX, float boxY) {
    float targetSize = 96f;
    float baselineSize = 1000f;

    strokePaint.setTextSize(baselineSize);
    strokePaint.getTextBounds(glyph, 0, glyph.length(), bounds);

    float w = bounds.width();
    float h = bounds.height();
    float maxDim = Math.max(w, h);

    float scale = targetSize / maxDim;
    float finalTextSize = baselineSize * scale;
    strokePaint.setTextSize(finalTextSize);
    fillPaint.setTextSize(finalTextSize);
    strokePaint.getTextBounds(glyph, 0, glyph.length(), bounds);

    float drawX = boxX - bounds.left + (targetSize - bounds.width()) / 2;
    float drawY = boxY - bounds.top + (targetSize - bounds.height()) / 2;

    canvas.drawText(glyph, drawX, drawY, fillPaint);
    canvas.drawText(glyph, drawX, drawY, strokePaint);

}
private static final int YEAR=0x20000;
private static final int WINTER=3;
private static final int SPRING=0;
private static final int SUMMER=1;
private static final int FALL=2;
int dryFactor;
boolean isDead;
int drySine(int k){
k&=255;
if(isDead)return sine[k]+sharp[k];
return sine[k]+((sharp[k]*dryFactor)>>15);
}
protected void onDraw(Canvas c) {
  RandomAccessFile f = handle;
  if(f == null)return;
  int w=c.getWidth();
  int h=c.getHeight();
  int xscroll=X%96;
  int yscroll=Y%96;
  int x;
  int y;
  int poleX;
  int poleY;
  int curX;
  int curY;
  int p;
  int fac;
  int s=sizes[gbytes];
  int type;
  int bgc;
  int fgc;
  int obc;
  int fgd;
  int birth;
  int last_write;
  int age;
  int fresh;
  int now=getNow();
  int yeartime = now&0x1ffff;
  int seasontime = now&0x7fff;
  int season = (now&0x18000)>>15;
  dryFactor = season == WINTER ? 0x8000 : 0;
  if(season == SPRING)dryFactor = 0x8000 - seasontime;
  else if(season == FALL)dryFactor = seasontime;
  boolean isDried;
  boolean isYoung;
  int rnd;
  int tmp;
  String ts;
  for(y=0;y<h+yscroll;y+=96){
    dst.top=y-yscroll;
    src.top=dst.bottom=dst.top+96;
    src.bottom=src.top+96;
    p=(((y+Y)/96)%s)*s*16+12;
    for(x=0;x<w+xscroll;x+=96){
      try{
        f.seek(p+(((x+X)/96)%s)*16);
        f.readFully(tile);
      }catch(Exception e){continue;}
      dst.left=x-xscroll;
      src.left=dst.right=dst.left+96;
      src.right=src.left+96;
      birth=(int)readLe(tile,8,4);
      last_write=(int)readLe(tile,12,4);
      age=now-birth;
      isYoung = age >= 0 && age < YEAR;
      fresh=now-last_write;
      for(int i=0;i<6;++i){
        data[i]=tile[i+2]&255;
        data[i+6]=data[i]^(tile[(i&1)+10]&255);
      }
      readSpectrum(bg,tile,2);
      readSpectrum(fg,tile,4);
      readSpectrum(ob,tile,6);
      bgc=spectrumSeason(bg,now);
      fgc=spectrumSeason(fg,now);
      obc=spectrumSeason(ob,(tile[11]&1)*now);
      fgd=bgc-fgc;
      rnd = (int)readLe(tile,4,4);
      switch(data[0]&7){
      /* todo remove &7 -
limit count of types for now */
        case 0://grass
          for(int i=0;i<96*96;++i){
rnd*=1103515245;
rnd+=12345;
            tilesurf.data[i] =
bgc+fgd*(rnd>>31);
          }
        break;
        case 1://wide grass
        for(int i=0;i<96*96;i+=192){
          for(int j=0;j<192;){
rnd*=1103515245;
rnd+=12345;
tmp=i+j;
if(j==96||j==191){
            tilesurf.data[tmp] =
bgc+fgd*(rnd>>31);
j+=1;
}else{
            tilesurf.data[tmp] =
            tilesurf.data[tmp+1] =
bgc+fgd*(rnd>>31);
j+=2;
}
          }
        }
        break;
        case 2://diffuse tile
          tmp = spectrumSeason(data,now);
          Arrays.fill(tilesurf.data,tmp);
        break;
        case 3://emission tile
          tmp = spectrumSeason(data,0);
          Arrays.fill(tilesurf.data,tmp);
        break;
        default://disco tile
          tmp = 0xff000000|
(((data[0]+((data[3]*fresh)>>10))&255)<<16)|
(((data[1]+((data[4]*fresh)>>10))&255)<<8)|
((data[2]+((data[5]*fresh)>>10))&255);
          Arrays.fill(tilesurf.data,tmp);
        break;
      }
c.save();
c.clipRect(dst);
fillPaint.setColor(obc);
      type = data[1]&15;
      /* todo remove &7 -
limit count of types for now */
      switch(type){
        case 0://tree
        case 1://symmetrical tree
        case 2://red fruit tree
        case 3://symmetrical red fruit tree
        case 4://orange tree
        case 5://symmetrical orange tree
        case 6://yellow fruit tree
        case 7://symmetrical yellow fruit tree
        case 8://blue fruit tree
        case 9://symmetrical blue fruit tree
          tilesurf.blit(c,src,dst);
          isDead = age < 0 || age > YEAR * 100;
          isDried = isDead || season == WINTER;
          if(isDried){
            fillPaint.setColor(0xff808080);
          }
          polygonPath.reset();
          poleX=dst.left+48;
          poleY=dst.top+32;
          for(int i=0;i<256;++i){
            tmp=((type&1)==0||i<=128)?i:(256-i);
            if(i>=248||i<8)fac=256;
            else fac=
(4096*255*3+
(drySine(tmp*2+data[6])-2048)*data[7]+
(drySine(tmp*3+data[8])-2048)*data[10]+
(drySine(tmp*5+data[9])-2048)*data[11])/24576;
            curX=poleX+((sine[i]*fac*11)>>16);
            curY=poleY+((sine[(i+64)&255]*fac*7)>>16);
            if(isYoung){
              tmp=age;
              curX=poleX+(((curX-poleX)*tmp)>>17);
              curY=
dst.bottom+(((curY-dst.bottom)*tmp)>>17);
            }
            cacheX[i]=curX;
            cacheY[i]=curY;
            if(i==0)
polygonPath.moveTo(curX,curY);
            else
polygonPath.lineTo(curX,curY);
          }
          polygonPath.close();
          c.drawPath(polygonPath,fillPaint);
          c.drawPath(polygonPath,strokePaint);
          if(type>1&&!isDried&&!isYoung){
            int fruits_remain = last_write & 7;
            if((now&-YEAR)!=(last_write&-YEAR)){
              fruits_remain=7;
            }
            tmp=(type-2)>>1;
            switch(season){
              case SPRING:
                fruits_remain = 7;
                fac=(255*seasontime)>>15;
                switch(tmp){
                  case 0:
fillPaint.setARGB(255,fac,255-fac,0);
                  break;
                  case 1:
fillPaint.setARGB(255,fac,255-(fac>>1),0);
                  break;
                  case 2:
fillPaint.setARGB(255,fac,255,0);
                  break;
                  case 3:
fillPaint.setARGB(255,0,255-(fac>>1),fac);
                  break;
                }
                tmp=seasontime>>12;
              break;
              case SUMMER:
                switch(tmp){
                  case 0:
fillPaint.setARGB(255,255,0,0);
                  break;
                  case 1:
fillPaint.setARGB(255,255,128,0);
                  break;
                  case 2:
fillPaint.setARGB(255,255,255,0);
                  break;
                  case 3:
fillPaint.setARGB(255,0,128,255);
                  break;
                }
                tmp=8;
              break;
              case FALL:
                fillPaint.setColor(0xff808080);
                tmp=8;
              break;
            }
            for(int i=18;i<18+36*fruits_remain;i+=36){
              curX=(cacheX[i]+poleX)>>1;
              curY=(cacheY[i]+poleY)>>1;
              c.drawCircle(curX,curY,tmp,fillPaint);
              c.drawCircle(curX,curY,tmp,strokePaint);
            }
          }
        break;
        case 10://char
          tilesurf.blit(c,src,dst);
ts=""+(char)((data[6]<<8)|data[11]);
drawGlyphInSquare(c,ts,dst.left,dst.top);
        break;
        default://nothing
          tilesurf.blit(c,src,dst);
        break;
      }
c.restore();
    }
  }
}
}
