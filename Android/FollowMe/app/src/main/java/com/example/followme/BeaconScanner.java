package com.example.followme;

import android.app.Activity;
import android.content.Context;
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

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class BeaconScanner {
    private ProximityObserver mProximityObserver;
    private ProximityZone mZone;
    private boolean on;
    private Vibrator mVibrator;
    BeaconScanner(Activity activity) {
        init(activity);
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
                        Toast.makeText(ac, "proximity observer error: " + throwable, Toast.LENGTH_SHORT).show();
                        return null;
                    }
                })
                .build();

        mZone = new ProximityZoneBuilder()
                .forTag("beacon5")
                .inFarRange()
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext)
                    {
                        if(on) {
                            Toast.makeText(ac, "FollowMe 로봇이 5미터 안에 있습니다.", Toast.LENGTH_LONG).show();
                            mVibrator.cancel();
                        }
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext context) {
                        if(on) {
                            Toast.makeText(ac, "FollowMe 로봇이 5미터 밖에 있습니다.", Toast.LENGTH_LONG).show();
                            mVibrator.vibrate(
                                    new long[]{100, 1000, 100, 500, 100, 500, 100, 1000}
                                    , 0);
                        }
                        return null;
                    }
                })
                .build();


    }
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
                                    mProximityObserver.startObserving(mZone);
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
    void stop()
    {
        on = false;
        if(mVibrator!=null)
            mVibrator.cancel();
    }
}
