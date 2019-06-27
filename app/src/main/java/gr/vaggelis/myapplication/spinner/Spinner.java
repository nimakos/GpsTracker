package gr.vaggelis.myapplication.spinner;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import gr.vaggelis.myapplication.R;

public class Spinner extends android.support.v7.widget.AppCompatSpinner {

    /**
     * Interface declaration
     */
    public interface OnItemChosenListener {
        void onItemChosen(DataEntry<String> item);
    }

    private List<DataEntry<String>> mItems;
    private DataEntry<String> mSelectedItem = new DataEntry<>(-1, "");
    private OnItemChosenListener mOnItemChosenListener;
    private boolean mOnItemChosenListenerEnabled = true;

    /**
     * Adjust new adapter
     * @param items List of items
     */
    private void setAdapter(List<DataEntry<String>> items) {
        ArrayAdapter<DataEntry<String>> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.view_spinner_item,
                items);
        adapter.setDropDownViewResource(R.layout.view_spinner_dropdown_item);
        setAdapter(adapter);
    }

    /**
     * Constructor
     * @param context context
     * @param attrs attributes
     */
    public Spinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Set dummy items
        mItems = new ArrayList<>();

        // Add a dummy first element
        mItems.add(new DataEntry<>(-1, getResources().getString(R.string.spinner_dummy_item)));

        // Set adapter
        setAdapter(mItems);

        setOnItemSelectedListener(new OnItemSelectedListener() {

            /**
             *  Callback method to be invoked when an item in this view has been selected.
             *  This callback is invoked only when the newly selected position is different
             *  from the previously selected position or if there was no selected item
             * @param parent    The AdapterView where the selection happened
             * @param view      The view within the AdapterView that was clicked
             * @param position  The position of the view in the adapter
             * @param id        The row id of the item that is selected
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    mSelectedItem = new DataEntry<>(-1, "");
                else
                    mSelectedItem = mItems.get(position);

                if (mOnItemChosenListener != null && mOnItemChosenListenerEnabled)
                    mOnItemChosenListener.onItemChosen(mSelectedItem);
                mOnItemChosenListenerEnabled = true;
            }

            /**
             *  Callback method to be invoked when the selection disappears from this view.
             *  The selection can disappear for instance when touch is activated or when the adapter becomes empty
             * @param parent    The AdapterView that now contains no selected item
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Clear the spinner
     */
    public void clear() {
        // Clear items
        mItems.clear();

        // Add a dummy first element
        mItems.add(new DataEntry<>(-1, getResources().getString(R.string.spinner_dummy_item)));

        // Set adapter
        setAdapter(mItems);

        // Set selected item the dummy
        setSelectedItem(-1);
    }

    /**
     * Set selection
     * @param value string value
     */
    public void setSelection(String value) {
        for (DataEntry<String> str : mItems) {
            if (str.data.equals(value)) {
                setSelectedItem(str.id);
            }
        }
    }

    /**
     * Set selected item
     * @param id item id
     */
    public void setSelectedItem(int id) {
        // Search for item in item list and if found set it as selected
        for (int i = 0; i < mItems.size(); i++) {
            if(mItems.get(i).id == id) {
                mSelectedItem = mItems.get(i);
                setSelection(i);
                break;
            }
        }
    }

    /**
     * Set selected item Ninja ??
     *
     * @param id item id
     */
    public void setSelectedItemNinja(int id) {
        mOnItemChosenListenerEnabled = false;
        setSelectedItem(id);
    }

    /**
     * Adjust listener
     * @param listener listener
     */
    public void setOnItemChosenListener(OnItemChosenListener listener) {
        mOnItemChosenListener = listener;
    }

    /**
     * Get selected item
     * @return DataEntry String item
     */
    public DataEntry<String> getSelectedItem() {
        return mSelectedItem;
    }

    /**
     * Set all items and select one of them.
     * @param items List of items
     */
    public void setItems(List<DataEntry<String>> items) {
        mItems = new ArrayList<>();

        // Add a dummy first element
        mItems.add(new DataEntry<>(-1, getResources().getString(R.string.spinner_dummy_item)));

        mItems.addAll(items);
        setAdapter(mItems);

        /* Try to ninja set the selected item

             ___   /
           ~/_  \ /
            |/* *|
            /   C.)  NINJA
           (_/\   \
             // /^\|
            /(_/   V

           Many times throughout the application the contents of a spinner are restored and reset
           because of orientation changes or/and spinner prepopulation (eg, highways-directions).
           To fix state problems we're trying here to keep the selected id even if items are changed.
           Worst case is that the items we're trying to set have different IDs than the selected
           item. Oh well, in that case nothing happens.
        */
        // Search for item in item list and if found set it as selected
        for (int i = 0; i < mItems.size(); ++i) {
            if(mItems.get(i).id == mSelectedItem.id) {
                setSelectedItem(mSelectedItem.id);
                break;
            }
        }
    }

    /**
     * If selected item position is 0 then return true otherwise return false.
     * @return true/false
     */
    public boolean isSelectionNull() {
        return getSelectedItemPosition() == 0;
    }
}
