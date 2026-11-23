package ido.dotan.physicspendelumido;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class FormulasFragment extends Fragment {

    private CardView currentlyVisibleCard = null;

    public FormulasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_formulas, container, false);

        final Button buttonPeriod = view.findViewById(R.id.button_period);
        buttonPeriod.setText(getString(R.string.period_of_oscillation) + "\n" + getString(R.string.formula_period));
        final CardView cardPeriod = view.findViewById(R.id.card_period);
        setCardToggle(buttonPeriod, cardPeriod);

        final Button buttonFrequency = view.findViewById(R.id.button_frequency);
        buttonFrequency.setText(getString(R.string.frequency_of_oscillation) + "\n" + getString(R.string.formula_frequency));
        final CardView cardFrequency = view.findViewById(R.id.card_frequency);
        setCardToggle(buttonFrequency, cardFrequency);

        final Button buttonAngularFrequency = view.findViewById(R.id.button_angular_frequency);
        buttonAngularFrequency.setText(getString(R.string.angular_frequency) + "\n" + getString(R.string.formula_angular_frequency));
        final CardView cardAngularFrequency = view.findViewById(R.id.card_angular_frequency);
        setCardToggle(buttonAngularFrequency, cardAngularFrequency);

        final Button buttonMotionEquation = view.findViewById(R.id.button_motion_equation);
        buttonMotionEquation.setText(getString(R.string.equation_of_motion) + "\n" + getString(R.string.formula_equation_of_motion));
        final CardView cardMotionEquation = view.findViewById(R.id.card_motion_equation);
        setCardToggle(buttonMotionEquation, cardMotionEquation);

        final Button buttonRestoringForce = view.findViewById(R.id.button_restoring_force);
        buttonRestoringForce.setText(getString(R.string.restoring_force) + "\n" + getString(R.string.formula_restoring_force));
        final CardView cardRestoringForce = view.findViewById(R.id.card_restoring_force);
        setCardToggle(buttonRestoringForce, cardRestoringForce);

        final Button buttonPotentialEnergy = view.findViewById(R.id.button_potential_energy);
        buttonPotentialEnergy.setText(getString(R.string.potential_energy) + "\n" + getString(R.string.formula_potential_energy));
        final CardView cardPotentialEnergy = view.findViewById(R.id.card_potential_energy);
        setCardToggle(buttonPotentialEnergy, cardPotentialEnergy);

        final Button buttonKineticEnergy = view.findViewById(R.id.button_kinetic_energy);
        buttonKineticEnergy.setText(getString(R.string.kinetic_energy) + "\n" + getString(R.string.formula_kinetic_energy));
        final CardView cardKineticEnergy = view.findViewById(R.id.card_kinetic_energy);
        setCardToggle(buttonKineticEnergy, cardKineticEnergy);

        return view;
    }

    private void setCardToggle(Button button, CardView card) {
        button.setOnClickListener(v -> {
            if (card.getVisibility() == View.VISIBLE) {
                card.setVisibility(View.GONE);
                currentlyVisibleCard = null;
            } else {
                if (currentlyVisibleCard != null) {
                    currentlyVisibleCard.setVisibility(View.GONE);
                }
                card.setVisibility(View.VISIBLE);
                currentlyVisibleCard = card;
            }
        });
    }
}
