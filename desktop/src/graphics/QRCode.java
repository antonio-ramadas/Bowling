package graphics;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCode {

	String message;
	Sprite image;

	int width;
	int height;
	
	public QRCode(String m, int w, int h){
		message = m;
		width = w;
		height = h;
		defineImage();
	}
	public QRCode(int w, int h){
		setMessageAsHostAddress();
		width = w;
		height = h;
		defineImage();
	}
	
	public Sprite getImage(){
		return image;
	}	
	public void setDimensions(int w, int h){
		width = w;
		height = h;
		
	}
	public void setMessageAsHostAddress(){
		try {
			message = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private BitMatrix generateBitmatrix (String m){

		BitMatrix QRmatrix = null;
		try {
			QRCodeWriter writer = new QRCodeWriter();
			QRmatrix = writer.encode(m, BarcodeFormat.QR_CODE, width, height);

		} catch (WriterException e) {
			e.printStackTrace();
		}
		return QRmatrix;
	}
	public void defineImage()
	{
		BitMatrix QRmatrix = generateBitmatrix(message);
		Pixmap QRimage = new Pixmap(width,height, Pixmap.Format.RGB565);		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (QRmatrix.get(i, j))
				{
					QRimage.setColor(Color.BLACK);
					QRimage.drawPixel(i, j);
				}
				else {
					QRimage.setColor(Color.WHITE);
					QRimage.drawPixel(i, j);
				}
			}
		}
		Texture QRtext = new Texture(QRimage);
		image = new Sprite(QRtext);
	}

}
