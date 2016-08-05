package com.farast.utuclient.main.java.util;

import javafx.scene.control.Label;

/**
 * Created by cendr_000 on 30.07.2016.
 */
public final class BullshitFactory {
    private BullshitFactory()
    {

    }

    public static Label newLabel(String text, String... klass)
    {
        Label label = new Label(text);
        label.getStyleClass().addAll(klass);
        return label;
    }
}
