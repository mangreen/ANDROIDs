package com.iiiesti.walkmap.indoor;

public class BitMapPoint {
	private int lon;
	private int lat;
	public float x;
	public float y;
	public float tx;
	public float ty;
	
	public BitMapPoint(int lon, int lat, float x, float y){
		this.lon = lon;
		this.lat = lat;
		this.x = x;
		this.y = y;
	}
	
	public void setCoordinate(int lon, int lat, float x, float y){
		this.lon = lon;
		this.lat = lat;
		this.x = x;
		this.y = y;
	}
	
	public void setTempPlace(){
		this.tx = this.x;
		this.ty = this.y;
	}
	
	public void restorePlace(){
		this.x = this.tx;
		this.y = this.ty;
	}
	
	public void postTranslate(float dx, float dy){
		this.x = this.x + dx;
		this.y = this.y + dy;
	}
	
	public void postScale(float scaleX, float scaleY, float px, float py){
		if(px > this.x){
			this.x = px - (px - this.x)*scaleX;
		}else{
			this.x = px + (this.x-px)*scaleX;
		}
		
		if(py > this.y){
			this.y = py - (py - this.y)*scaleY;
		}else{
			this.y = py + (this.y-py)*scaleY;
		}
	}
	
	public int getLongitude(){
		return this.lon;
	}
	
	public int getLatitude(){
		return this.lat;
	}
}
