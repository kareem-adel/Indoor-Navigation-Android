package com.kareem_adel.environmentmaps.OptionItem;

import java.util.ArrayList;

/**
 * Created by Kareem-Adel on 2/21/2016.
 */
public class OptionsItem {
    public String OptionName;
    public int OptionImage;
    public ArrayList<OptionsItem> optionsItems;

    public OptionsItem(String OptionName, int OptionImage, ArrayList<OptionsItem> optionsItems) {
        this.OptionName = OptionName;
        this.OptionImage = OptionImage;
        this.optionsItems = optionsItems;
    }
}