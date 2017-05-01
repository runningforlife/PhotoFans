package jason.github.com.photofans.ui.preference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jason.github.com.photofans.model.ImageSource;
import jason.github.com.photofans.ui.activity.ImageSourceSelectionActivity;

/**
 * a list preference only to start an activity
 */

public class EmptyListPreference extends MultiSelectListPreference{

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmptyListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EmptyListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EmptyListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyListPreference(Context context) {
        super(context);
    }

    @Override
    public void onClick(){
        CharSequence[] sourceName = getEntries();
        CharSequence[] sourceUrl = getEntryValues();
        Set<String> defaultValues = getValues();

        List<ImageSource> sources = new ArrayList<>(sourceName.length);
        for(int i = 0; i < sourceName.length; ++i){
            ImageSource src = new ImageSource(sourceName[i].toString(),sourceUrl[i].toString());
            sources.add(src);
        }

        Intent intent = new Intent(getContext(), ImageSourceSelectionActivity.class);
        intent.putParcelableArrayListExtra("image_source", (ArrayList<? extends Parcelable>) sources);
        intent.putStringArrayListExtra("default_value",new ArrayList<>(defaultValues));

        getContext().startActivity(intent);
    }
}
