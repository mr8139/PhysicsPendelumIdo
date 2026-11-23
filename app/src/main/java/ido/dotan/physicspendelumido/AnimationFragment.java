package ido.dotan.physicspendelumido;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

public class AnimationFragment extends Fragment {

    private PendulumView pendulumView;
    private TextView tvPeriod;
    private SeekBar sbLength;

    public AnimationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_animation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        pendulumView = view.findViewById(R.id.pendulumView);
        tvPeriod = view.findViewById(R.id.tvPeriod);
        sbLength = view.findViewById(R.id.sbLength);

        // We use post to wait for the view to be measured
        pendulumView.post(() -> {
            int maxHeight = pendulumView.getHeight();
            // Subtract padding for the bob (radius 30) and pivot (50)
            int effectiveMax = maxHeight - 100; 
            int minLen = 100;

            sbLength.setMax(effectiveMax - minLen);
            sbLength.setProgress((effectiveMax - minLen) / 2); // Start at middle

            updatePendulum(sbLength.getProgress(), minLen);
        });

        sbLength.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int minLen = 100;
                updatePendulum(progress, minLen);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updatePendulum(int progress, int minLen) {
        int currentLen = progress + minLen;
        pendulumView.setLength(currentLen);
        
        // Period calculation
        // Scaling for physics: 1000 pixels = 1 meter
        double lengthInMeters = currentLen / 1000.0;
        double period = 2 * Math.PI * Math.sqrt(lengthInMeters / 9.8);
        tvPeriod.setText(String.format(Locale.US, "Period (T): %.2fs", period));
    }
}
