package com.harbo.factionscore;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class HologramPlaceHolder {
    public HologramPlaceHolder(Main pl){
        HologramsAPI.registerPlaceholder(pl, "{powercore}", 1, () -> pl.getCharge() + "/" + pl.getChargeTotal());
    }
}
