package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends Activity {
  void nap() {
    try {
      Thread.sleep(10);
    } catch (Exception e) {
    }
  }

  protected void onCreate(Bundle state) {
    super.onCreate(state);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(new StartView(this));
  }
}
