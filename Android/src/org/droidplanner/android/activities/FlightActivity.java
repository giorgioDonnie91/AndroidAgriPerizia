package org.droidplanner.android.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.android.R;
import org.droidplanner.android.dialogs.DialogMaterialFragment;
import org.droidplanner.android.fragments.DroneMap;
import org.droidplanner.android.fragments.FlightDataFragment;
import org.droidplanner.android.fragments.actionbar.ActionBarTelemFragment;
import org.droidplanner.android.fragments.widget.TowerWidgets;
import org.droidplanner.android.fragments.widget.video.FullWidgetSoloLinkVideo;
import org.droidplanner.android.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlightActivity extends DrawerNavigationUI implements SlidingUpPanelLayout.PanelSlideListener, DroneMap.CloseToWaypointListener {

    private static final String EXTRA_IS_ACTION_DRAWER_OPENED = "extra_is_action_drawer_opened";
    private static final boolean DEFAULT_IS_ACTION_DRAWER_OPENED = true;

    private FlightDataFragment flightData;

    private int lastSaved = -1;

    @Override
    public void onDrawerClosed() {
        super.onDrawerClosed();

        if (flightData != null)
            flightData.onDrawerClosed();
    }

    @Override
    public void onDrawerOpened() {
        super.onDrawerOpened();

        if (flightData != null)
            flightData.onDrawerOpened();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        final FragmentManager fm = getSupportFragmentManager();

        //Add the flight data fragment
        flightData = (FlightDataFragment) fm.findFragmentById(R.id.map_view);
        if(flightData == null){
            Bundle args = new Bundle();
            args.putBoolean(FlightDataFragment.EXTRA_SHOW_ACTION_DRAWER_TOGGLE, true);

            flightData = new FlightDataFragment();
            flightData.setArguments(args);
            fm.beginTransaction().add(R.id.map_view, flightData).commit();
        }

        Fragment videoFragment = fm.findFragmentById(R.id.widget_view);
        if (videoFragment == null) {
            videoFragment = TowerWidgets.SOLO_VIDEO.getMaximizedFragment();
            fm.beginTransaction()
                    .add(R.id.widget_view, videoFragment)
                    .commit();
        }


        boolean isActionDrawerOpened = DEFAULT_IS_ACTION_DRAWER_OPENED;
        if (savedInstanceState != null) {
            isActionDrawerOpened = savedInstanceState.getBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened);
        }

        if (isActionDrawerOpened)
            openActionDrawer();
    }

    @Override
    protected void onToolbarLayoutChange(int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
        if(flightData != null)
            flightData.updateActionbarShadow(bottom);
    }

    @Override
    protected void addToolbarFragment() {
        final int toolbarId = getToolbarId();
        final FragmentManager fm = getSupportFragmentManager();
        Fragment actionBarTelem = fm.findFragmentById(toolbarId);
        if (actionBarTelem == null) {
            actionBarTelem = new ActionBarTelemFragment();
            fm.beginTransaction().add(toolbarId, actionBarTelem).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_ACTION_DRAWER_OPENED, isActionDrawerOpened());
    }

    @Override
    public void onStart(){
        super.onStart();

        final Context context = getApplicationContext();
        //Show the changelog if this is the first time the app is launched since update/install
        if(Utils.getAppVersionCode(context) > mAppPrefs.getSavedAppVersionCode()) {
            DialogMaterialFragment changelog = new DialogMaterialFragment();
            changelog.show(getSupportFragmentManager(), "Changelog Dialog");

            mAppPrefs.updateSavedAppVersionCode(context);
        }
    }

    @Override
    protected int getToolbarId() {
        return R.id.actionbar_toolbar;
    }

    @Override
    protected int getNavigationDrawerMenuItemId() {
        return R.id.navigation_flight_data;
    }

    @Override
    protected boolean enableMissionMenus() {
        return true;
    }

    @Override
    public void onPanelSlide(View view, float v) {
        final int bottomMargin = (int) getResources().getDimension(R.dimen.action_drawer_margin_bottom);

        //Update the bottom margin for the action drawer
        final View flightActionBar = ((ViewGroup)view).getChildAt(0);
        final int[] viewLocs = new int[2];
        flightActionBar.getLocationInWindow(viewLocs);
        updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), Math.max((int) (view.getHeight() * v), bottomMargin));
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch(newState){
            case COLLAPSED:
            case HIDDEN:
                resetActionDrawerBottomMargin();
                break;

            case EXPANDED:
                //Update the bottom margin for the action drawer
                ViewGroup slidingPanel = (ViewGroup) ((ViewGroup)panel).getChildAt(1);
                final View flightActionBar = slidingPanel.getChildAt(0);
                final int[] viewLocs = new int[2];
                flightActionBar.getLocationInWindow(viewLocs);
                updateActionDrawerBottomMargin(viewLocs[0] + flightActionBar.getWidth(), slidingPanel.getHeight());
                break;
        }
    }

    private void updateActionDrawerBottomMargin(int rightEdge, int bottomMargin){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        final View actionDrawer = ((ViewGroup)actionDrawerParent.getChildAt(1)).getChildAt(0);

        final int[] actionDrawerLocs = new int[2];
        actionDrawer.getLocationInWindow(actionDrawerLocs);

        if(actionDrawerLocs[0] <= rightEdge) {
            updateActionDrawerBottomMargin(bottomMargin);
        }
    }

    private int getActionDrawerBottomMargin(){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        return lp.bottomMargin;
    }

    private void updateActionDrawerBottomMargin(int newBottomMargin){
        final ViewGroup actionDrawerParent = (ViewGroup) getActionDrawer();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) actionDrawerParent.getLayoutParams();
        lp.bottomMargin = newBottomMargin;
        actionDrawerParent.requestLayout();
    }

    private void resetActionDrawerBottomMargin(){
        updateActionDrawerBottomMargin((int) getResources().getDimension(R.dimen.action_drawer_margin_bottom));
    }

    @Override
    public void onCloseTo(int waypointIndex) {
        Utils.log("CLOSE: TO: " + waypointIndex);
        //Log.i("CLOSE", "TO: " + waypointIndex);
        if(waypointIndex <= lastSaved)
            return;
        Utils.log("CLOSE: SAVING: " + waypointIndex);
        //Log.i("CLOSE", "SAVING: " + waypointIndex);
        lastSaved = waypointIndex;

        FullWidgetSoloLinkVideo fullWidgetSoloLinkVideo = (FullWidgetSoloLinkVideo)getSupportFragmentManager().findFragmentById(R.id.widget_view);
        if(fullWidgetSoloLinkVideo == null || !fullWidgetSoloLinkVideo.isAdded() || fullWidgetSoloLinkVideo.getView() == null)
            return;

        TextureView textureView = (TextureView)fullWidgetSoloLinkVideo.getView().findViewById(R.id.sololink_video_view);
        saveBitmapAsynch(textureView.getBitmap(), waypointIndex);
    }

    public void saveBitmapAsynch(final Bitmap bitmap, final int wayPointIndex){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String codicePercorso = Utils.loadPreferencesData(FlightActivity.this, Utils.PREF_PERCORSO);
                String codiceSinistro = Utils.loadPreferencesData(FlightActivity.this, Utils.PREF_SINISTRO);
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/drone/"+codicePercorso+"/"+(codiceSinistro == null ? "prima_perizia" : codiceSinistro));
                if(!myDir.isDirectory())
                    myDir.mkdirs();

                Utils.log("PATH: " + myDir.getAbsolutePath() + "/" + wayPointIndex+".jpeg");
                //Log.i("PATH", myDir.getAbsolutePath() + "/" + wayPointIndex+".jpeg");
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(myDir+"/"+wayPointIndex+".jpeg");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null){
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
