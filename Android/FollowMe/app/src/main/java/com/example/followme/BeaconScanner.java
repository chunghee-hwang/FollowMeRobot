package com.example.followme;

import android.app.Activity;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement;
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.util.List;
import java.util.Set;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class BeaconScanner {
    private ProximityObserver mProximityObserver;
    private ProximityZone mZone;
    private boolean on;
    private Vibrator mVibrator;
    private static BeaconScanner mBeaconScanner;
    private BeaconScanner(Activity activity) {
        init(activity);
    }
    public static BeaconScanner getInstance(Activity activity)
    {
        if(mBeaconScanner == null)
            mBeaconScanner = new BeaconScanner(activity);
        return mBeaconScanner;
    }
    void init(final Activity ac) {

        //beacon 관련 코드 넣기
        mVibrator = (Vibrator) ac.getSystemService(Context.VIBRATOR_SERVICE);
        EstimoteCloudCredentials cloudCredentials =
                new EstimoteCloudCredentials("theft-prevention-hbj", "9549db8da68aeca15f4340851bd007ea");

        // 2. Create the Proximity Observer
        mProximityObserver = new ProximityObserverBuilder(ac, cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        if(!throwable.getMessage().contains("Monitoring stopped"))
                            Toast.makeText(ac, "proximity observer error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return null;
                    }
                })
                .withLowLatencyPowerMode()
                .build();


        mZone = new ProximityZoneBuilder()
                .forTag("beacon5")
                //.inFarRange()
                .inCustomRange(10)
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext)
                    {
                        if(on) {
                            Toast.makeText(ac, "FollowMe 로봇이 10미터 안에 있습니다.", Toast.LENGTH_LONG).show();
                            mVibrator.cancel();
                        }
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext context) {
                        if(on) {
                            Toast.makeText(ac, "FollowMe 로봇이 10미터 밖에 있습니다.", Toast.LENGTH_LONG).show();
                            mVibrator.vibrate(
                                    new long[]{100, 1000, 100, 500, 100, 500, 100, 1000}
                                    , 0);
                        }
                        return null;
                    }
                })

                .build();


    }
    ProximityObserver.Handler mHandler;
    void start(final Activity ac)
    {
        on = true;
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(ac,
                        // onRequirementsFulfilled
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke()
                            {
                                if(mProximityObserver!=null && mZone !=null)
                                {
                                    mHandler = mProximityObserver.startObserving(mZone);
                                    Toast.makeText(ac, "도난 방지 시스템 on", Toast.LENGTH_SHORT).show();

                                }
                                //Toast.makeText(m, "requirements fulfilled", Toast.LENGTH_SHORT).show();
                                return null;
                            }
                        },
                        // onRequirementsMissing
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                Toast.makeText(ac, "requirements missing: " + requirements, Toast.LENGTH_SHORT).show();
                                ac.finish();
                                return null;
                            }
                        },
                        // onError
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Toast.makeText(ac, "requirements error: " + throwable, Toast.LENGTH_SHORT).show();
                                ac.finish();
                                return null;
                            }
                        });
    }
    void stop(Activity activity)
    {
        if(mVibrator!=null)
            mVibrator.cancel();
        if(mHandler!=null)
        {
            mHandler.stop();
            if(on)
                Toast.makeText(activity, "도난 방지 시스템 off", Toast.LENGTH_SHORT).show();

        }
        on = false;
    }

    boolean isOn(){
        return on;
    }
}
