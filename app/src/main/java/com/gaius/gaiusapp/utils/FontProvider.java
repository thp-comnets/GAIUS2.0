package com.gaius.gaiusapp.utils;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * extracting Typeface from Assets is a heavy operation,
 * we want to make sure that we cache the typefaces for reuse
 */
public class FontProvider {

    private static final String DEFAULT_FONT_NAME = "Helvetica";

    private final Map<String, Typeface> typefaces;
    private final Map<String, String> fontNameToTypefaceFile;
    private final Resources resources;
    private final List<String> fontNames;

    public FontProvider(Resources resources) {
        this.resources = resources;

        typefaces = new HashMap<>();

        // populate fonts
        fontNameToTypefaceFile = new HashMap<>();
        fontNameToTypefaceFile.put("Arial", "Arial.ttf");
        fontNameToTypefaceFile.put("Eutemia", "Eutemia.ttf");
        fontNameToTypefaceFile.put("GREENPIL", "GREENPIL.ttf");
        fontNameToTypefaceFile.put("Grinched", "Grinched.ttf");
        fontNameToTypefaceFile.put("Helvetica", "Helvetica.ttf");
        fontNameToTypefaceFile.put("Libertango", "Libertango.ttf");
        fontNameToTypefaceFile.put("Metal Macabre", "MetalMacabre.ttf");
        fontNameToTypefaceFile.put("Parry Hotter", "ParryHotter.ttf");
        fontNameToTypefaceFile.put("SCRIPTIN", "SCRIPTIN.ttf");
        fontNameToTypefaceFile.put("The Godfather v2", "TheGodfather_v2.ttf");
        fontNameToTypefaceFile.put("Aka Dora", "akaDora.ttf");
        fontNameToTypefaceFile.put("Waltograph", "waltograph42.ttf");
        fontNameToTypefaceFile.put("AlexBrush", "AlexBrush-Regular.ttf");
        fontNameToTypefaceFile.put("Arizona", "Arizonia-Regular.ttf");
        fontNameToTypefaceFile.put("BlackJack", "blackjack.otf");
        fontNameToTypefaceFile.put("Caviar Bold", "Caviar_Dreams_Bold.ttf");
        fontNameToTypefaceFile.put("Caviar Bold Italic", "CaviarDreams_BoldItalic.ttf");
        fontNameToTypefaceFile.put("Caviar Italic", "CaviarDreams_Italic.ttf");
        fontNameToTypefaceFile.put("Caviar", "CaviarDreams.ttf");
        fontNameToTypefaceFile.put("FFF Tusji", "FFF_Tusj.ttf");
        fontNameToTypefaceFile.put("Grand Hotel", "GrandHotel-Regular.otf");
        fontNameToTypefaceFile.put("Great Vibes", "GreatVibes-Regular.otf");
        fontNameToTypefaceFile.put("Kaushan Script", "KaushanScript-Regular.otf");
        fontNameToTypefaceFile.put("League Gothic Condensed Italic", "LeagueGothic-CondensedItalic.otf");
        fontNameToTypefaceFile.put("League Gothic Condensed", "LeagueGothic-CondensedRegular.otf");
        fontNameToTypefaceFile.put("League Gothic Italic", "LeagueGothic-Italic.otf");
        fontNameToTypefaceFile.put("League Gothic", "LeagueGothic-Regular.otf");
        fontNameToTypefaceFile.put("Lobster", "Lobster_1.3.otf");
        fontNameToTypefaceFile.put("Quicksand Dash", "Quicksand_Dash.otf");
        fontNameToTypefaceFile.put("Quicksand Bold", "Quicksand-Bold.otf");
        fontNameToTypefaceFile.put("Quicksand Bold Italic", "Quicksand-BoldItalic.otf");
        fontNameToTypefaceFile.put("Quicksand Italic", "Quicksand-Italic.otf");
        fontNameToTypefaceFile.put("Quicksand Light", "Quicksand-Light.otf");
        fontNameToTypefaceFile.put("Quicksand Light Italic", "Quicksand-LightItalic.otf");
        fontNameToTypefaceFile.put("Quicksand", "Quicksand-Regular.otf");
        fontNameToTypefaceFile.put("SEASRN", "SEASRN__.ttf");
        fontNameToTypefaceFile.put("Sofia", "Sofia-Regular.otf");
        fontNameToTypefaceFile.put("Broker","a_BrokerAbst.ttf");
        fontNameToTypefaceFile.put("Campus Marine","a_CampusMarineUp.ttf");
        fontNameToTypefaceFile.put("Copper Black","a_CooperBlackCm.ttf");
        fontNameToTypefaceFile.put("Angel","ANGEL___.ttf");
        fontNameToTypefaceFile.put("Anglo","Anglo Text.ttf");
        fontNameToTypefaceFile.put("Arabik","ArabikDB.ttf");
        fontNameToTypefaceFile.put("Arbonnie","ARBONNIE.ttf");
        fontNameToTypefaceFile.put("Ardestine","ARDESTINE.ttf");
        fontNameToTypefaceFile.put("Assassins","Assassins Dub.ttf");
        fontNameToTypefaceFile.put("Avia","AviaSSK.ttf");
        fontNameToTypefaceFile.put("Avondale","Avondale SC Shaded.ttf");
        fontNameToTypefaceFile.put("Ayosomonika","Ayosmonika Bold.ttf");
        fontNameToTypefaceFile.put("Ayuma","Ayuma2yk.ttf");
        fontNameToTypefaceFile.put("Aztec","aztec bouffon.ttf");
        fontNameToTypefaceFile.put("Baby Eskimo","Baby Eskimo Kisses.ttf");
        fontNameToTypefaceFile.put("Bajareczka Shadow","Bajareczka Shadow.ttf");
        fontNameToTypefaceFile.put("Bajareczka","bajareczka.ttf");
        fontNameToTypefaceFile.put("Beauregard","Beauregard Hollow.ttf");
        fontNameToTypefaceFile.put("Beebop","Beebop.ttf");
        fontNameToTypefaceFile.put("belinda","belindaRGUEZ.ttf");
        fontNameToTypefaceFile.put("Bermuda","Bermuda_LP_Std_Squiggle.ttf");
        fontNameToTypefaceFile.put("Beyond Wonderland","Beyond_Wonderland.ttf");
        fontNameToTypefaceFile.put("Candy","CANDY___.ttf");
        fontNameToTypefaceFile.put("Curlyq","CURLYQ.TTF");
        fontNameToTypefaceFile.put("Fofbb Italic","fofbb_ital.ttf");
        fontNameToTypefaceFile.put("Fofbb","fofbb_reg.ttf");
        fontNameToTypefaceFile.put("Hobo Std","HoboStd.otf");
        fontNameToTypefaceFile.put("Isini Script","IsiniScript.ttf");
        fontNameToTypefaceFile.put("KG Second","KGSecondChancesSketch.ttf");
        fontNameToTypefaceFile.put("KG Second Solid","KGSecondChancesSolid.ttf");
        fontNameToTypefaceFile.put("Organo","Organo.ttf");
        fontNameToTypefaceFile.put("Pees Celtic","Pees_Celtic_Plain.ttf");
        fontNameToTypefaceFile.put("Remachine","RemachineScript_PERSONAL_USE_ONLY.ttf");
        fontNameToTypefaceFile.put("Seri Fancy","rm_serifancy.ttf");
        fontNameToTypefaceFile.put("Scriplat Tin","SCRIPALT.ttf");
        fontNameToTypefaceFile.put("Srar Jedi Rounded","Star_Jedi_Rounded.ttf");
        fontNameToTypefaceFile.put("Srar Jedi","Starjedi.ttf");
        fontNameToTypefaceFile.put("Srar Jedi hol","Starjhol.ttf");

        fontNames = new ArrayList<>(fontNameToTypefaceFile.keySet());
        Collections.sort(fontNames);
    }

    /**
     * @param typefaceName must be one of the font names provided from {@link FontProvider#getFontNames()}
     * @return the Typeface associated with {@code typefaceName}, or {@link Typeface#DEFAULT} otherwise
     */
    public Typeface getTypeface(@Nullable String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) {
            return Typeface.DEFAULT;
        } else {
            //noinspection Java8CollectionsApi
            if (typefaces.get(typefaceName) == null) {
                typefaces.put(typefaceName,
                        Typeface.createFromAsset(resources.getAssets(), "fonts/" + fontNameToTypefaceFile.get(typefaceName)));
            }
            return typefaces.get(typefaceName);
        }
    }

    /**
     * use {@link FontProvider#getTypeface(String) to get Typeface for the font name}
     *
     * @return list of available font names
     */
    public List<String> getFontNames() {
        return fontNames;
    }

    /**
     * @return Default Font Name - <b>Helvetica</b>
     */
    public String getDefaultFontName() {
        return DEFAULT_FONT_NAME;
    }
}