package com.astarivi.kaizoyu.licenses;

import android.graphics.Shader;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.astarivi.kaizoyu.core.theme.AppCompatActivityTheme;
import com.astarivi.kaizoyu.databinding.ActivityLicensesBinding;
import com.astarivi.kaizoyu.utils.Utils;


public class LicensesActivity extends AppCompatActivityTheme {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLicensesBinding binding = ActivityLicensesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        LinearLayout textContainer = binding.copyrightTextContainer;

        for (int x = 0; x < textContainer.getChildCount(); x++) {
            final TextView children = (TextView) textContainer.getChildAt(x);

            final Shader textShader = Utils.getBrandingTextShader(children.getTextSize());

            children.getPaint().setShader(textShader);
        }
    }

}