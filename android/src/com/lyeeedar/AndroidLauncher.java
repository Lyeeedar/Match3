package com.lyeeedar;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.disableAudio = false;

		initialize( new MainGame(), config );

		Global.applicationChanger = new AndroidApplicationChanger();
		Global.applicationChanger.updateApplication( Global.applicationChanger.prefs );
	}
}
