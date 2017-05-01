package jason.github.com.photofans.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import jason.github.com.photofans.R;
import jason.github.com.photofans.ui.fragment.SettingsFragment;

/**
 * an activity to manage user settings
 */

public class SettingsActivity extends AppCompatActivity{
    private static final String TAG = "Settings";

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        android.app.FragmentManager fragmentMgr = getFragmentManager();
        fragmentMgr.beginTransaction()
                   .add(android.R.id.content, new SettingsFragment())
                   .commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            Intent intent = new Intent(this, GalleryActivity.class);
            NavUtils.navigateUpTo(this,intent);
        }

        return super.onOptionsItemSelected(item);
    }

}
