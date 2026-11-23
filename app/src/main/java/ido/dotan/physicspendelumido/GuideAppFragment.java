package ido.dotan.physicspendelumido;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class GuideAppFragment extends Fragment {

    public GuideAppFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide_app, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAccordion(view, R.id.btn_section1, R.id.tv_content1);
        setupAccordion(view, R.id.btn_section2, R.id.tv_content2);
        setupAccordion(view, R.id.btn_section3, R.id.tv_content3);
        setupAccordion(view, R.id.btn_section4, R.id.tv_content4);
        setupAccordion(view, R.id.btn_section5, R.id.tv_content5);
        setupAccordion(view, R.id.btn_section6, R.id.tv_content6);
        setupAccordion(view, R.id.btn_section7, R.id.tv_content7);
    }

    private void setupAccordion(View root, int buttonId, int contentId) {
        Button button = root.findViewById(buttonId);
        final TextView content = root.findViewById(contentId);

        button.setOnClickListener(v -> {
            if (content.getVisibility() == View.GONE) {
                content.setVisibility(View.VISIBLE);
            } else {
                content.setVisibility(View.GONE);
            }
        });
    }
}
