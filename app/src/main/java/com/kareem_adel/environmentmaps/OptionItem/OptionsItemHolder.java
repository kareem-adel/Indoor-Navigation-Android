package com.kareem_adel.environmentmaps.OptionItem;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.kareem_adel.environmentmaps.R;

/**
 * Created by Kareem-Adel on 2/21/2016.
 */
public class OptionsItemHolder {
    public ImageView OptionsItem_Image;
    public TextView OptionsItem_Name;
    public CheckBox OptionsItem_checkbox;

    public OptionsItemHolder(View view) {
        OptionsItem_Image = (ImageView) view.findViewById(R.id.OptionsItem_Image);
        OptionsItem_Name = (TextView) view.findViewById(R.id.OptionsItem_Name);
        OptionsItem_checkbox = (CheckBox) view.findViewById(R.id.OptionsItem_checkbox);
    }
}