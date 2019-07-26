package com.example.followme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

    public static BeaconScanner getInstance(Activity activity) {
        if (mBeaconScanner == null)
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
                        if (!throwable.getMessage().contains("Monitoring stopped"))
                            //Toast.makeText(ac, "proximity observer error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            Snackbar.make(ac.findViewById(R.id.layout_switch), "앱 또는 기기 재시작 필요 -  error: " + throwable.getMessage(), Snackbar.LENGTH_INDEFINITE)

                                    .setAction("확인", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    }).show();

                        return null;
                    }
                })
                .withLowLatencyPowerMode()
                .build();


        mZone = new ProximityZoneBuilder()
                .forTag("beacon5")
                .inFarRange()
                //.inCustomRange(10)
                .onEnter(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext proximityZoneContext) {
                        if (on) {
                            //Toast.makeText(ac, "FollowMe 로봇이 10미터 안에 있습니다.", Toast.LENGTH_SHORT).show();
                            Snackbar.make(ac.findViewById(R.id.layout_switch), "FollowMe 로봇이 10미터 안에 있습니다.",
                                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            }).show();
                            try {
                                ((SwitchActivity) ac).iconOff(R.id.alertImageView);
                                mVibrator.cancel();
                            }catch (Exception e){
                                Toast.makeText(ac, "onEnter - error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                        return null;
                    }
                })
                .onExit(new Function1<ProximityZoneContext, Unit>() {
                    @Override
                    public Unit invoke(ProximityZoneContext context) {
                        if (on) {
                            //Toast.makeText(ac, "FollowMe 로봇이 10미터 밖에 있습니다.", Toast.LENGTH_SHORT).show();
                            //Snackbar.make(ac.findViewById(R.id.layout_switch), "FollowMe 로봇이 10미터 밖에 있습니다.", Snackbar.LENGTH_INDEFINITE).show();

                            Snackbar.make(ac.findViewById(R.id.layout_switch), "FollowMe 로봇이 10미터 밖에 있습니다.",
                                    Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            }).show();
                            try {
                                ((SwitchActivity) ac).iconOn(R.id.alertImageView);

                                mVibrator.vibrate(
                                        new long[]{100, 1000, 100, 500, 100, 500, 100, 1000}
                                        , 0);
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(ac, "onExit - error: " + e.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                        return null;
                    }
                })

                .build();


    }

    ProximityObserver.Handler mHandler;

    void start(final Activity ac) {
        if (on) return;
        on = true;
        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(ac,
                        // onRequirementsFulfilled
                        new Function0<Unit>() {
                            @Override
                            public Unit invoke() {
                                if (mProximityObserver != null && mZone != null) {
                                    mHandler = mProximityObserver.startObserving(mZone);
                                    //Toast.makeText(ac, "도난 방지 시스템 on", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(ac.findViewById(R.id.layout_switch), "도난 방지 시스템 on", Snackbar.LENGTH_LONG).show();

                                }
                                //Toast.makeText(m, "requirements fulfilled", Toast.LENGTH_SHORT).show();
                                return null;
                            }
                        },
                        // onRequirementsMissing
                        new Function1<List<? extends Requirement>, Unit>() {
                            @Override
                            public Unit invoke(List<? extends Requirement> requirements) {
                                //Toast.makeText(ac, "requirements missing: " + requirements, Toast.LENGTH_SHORT).show();

                                Snackbar.make(ac.findViewById(R.id.layout_switch), "권한 부족: " + requirements,
                                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        ac.finish();
                                    }
                                }).show();
                                return null;
                            }
                        },
                        // onError
                        new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                //Toast.makeText(ac, "requirements error: " + throwable, Toast.LENGTH_SHORT).show();
                                Snackbar.make(ac.findViewById(R.id.layout_switch), "권한 에러: " + throwable,
                                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                                    @Override
                                    public void onClick(View view) {
                                        ac.finish();
                                    }
                                }).show();
                                return null;
                            }
                        });
    }

    void stop(Activity ac) {
        if (!on) return;
        if (mVibrator != null)
            mVibrator.cancel();
        if (mHandler != null) {
            mHandler.stop();
            if (on)
                //Toast.makeText(activity, "도난 방지 시스템 off", Toast.LENGTH_SHORT).show();
                try {
                    Snackbar.make(ac.findViewById(R.id.layout_switch), "도난 방지 시스템 off", Snackbar.LENGTH_LONG).show();
                }catch (Exception e)
                {
                    Toast.makeText(ac, "도난 방지 시스템 off", Toast.LENGTH_SHORT).show();
                }

        }
        on = false;
    }

    boolean isOn() {
        return on;
    }
}
