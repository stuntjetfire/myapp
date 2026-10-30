package com.example.myapplication;
public class StartView extends WorldView{
public StartView(MainActivity a){
super(a);
open(0,-1);
if(handle==null)open(0,0);
}
}
