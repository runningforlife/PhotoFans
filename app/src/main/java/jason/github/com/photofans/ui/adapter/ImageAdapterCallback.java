package jason.github.com.photofans.ui.adapter;

import jason.github.com.photofans.model.ImageRealm;

/**
 * image detail adapter callback
 */

public interface ImageAdapterCallback {

    /*
     * get the number of images
     */
    int getCount();

    /*
     * get item at given position
     */
    ImageRealm getItemAtPos(int pos);

    /*
     * item at pos is clicked
     */
    void onItemClicked(int pos);


    /*
     * image start loading
     */
    void onImageLoadStart(int pos);

    /*
     * image loading complete
     */
    void onImageLoadDone(int pos, boolean isSuccess);

}
