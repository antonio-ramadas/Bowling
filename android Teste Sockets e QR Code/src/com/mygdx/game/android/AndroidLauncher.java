package com.mygdx.game.android;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import connection.AndroidClientTest;

public class AndroidLauncher extends AndroidApplication implements AndroidClientTest.Callback {

	AndroidClientTest testEnviroment = new AndroidClientTest();
	
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		//config.useAccelerometer = true;
		//config.useCompass = true;

		testEnviroment.setMyGameCallback(this);
		initialize(testEnviroment, config);
	}

	public void startScannerActivity() {
		IntentIntegrator scanIntegrator = new IntentIntegrator(this);
		scanIntegrator.initiateScan();
	}

	// Listens to the end of an activity
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			testEnviroment.setExternalIP(scanResult.getContents());
		}
	}

}
