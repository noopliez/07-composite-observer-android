package de.thro.inf.prg3.a07;

import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import de.thro.inf.prg3.a07.api.OpenMensaAPI;
import de.thro.inf.prg3.a07.model.Meal;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
	final Logger logger = Logger.getLogger(MainActivity.class.getName());
	AtomicBoolean filterTriggered = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this will inflate the layout from res/layout/activity_main.xml
        setContentView(R.layout.activity_main);

        // add your code here
		Button btn = findViewById(R.id.refresh_button);
		btn.setOnClickListener(v -> {
			refresh();
		});

		CheckBox checkBox = findViewById(R.id.veggie_checkBox);
		checkBox.setOnCheckedChangeListener((view, isChecked) -> {
			filterTriggered.set(isChecked);
			refresh();
		});

		refresh();
    }

    private void refresh() {
		Retrofit retrofit = new Retrofit.Builder()
			.addConverterFactory(GsonConverterFactory.create())
			.baseUrl("http://openmensa.org/api/v2/")
			.build();

		OpenMensaAPI openMensaAPI = retrofit.create(OpenMensaAPI.class);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
		String today = sdf.format(new Date());

		Call<List<Meal>> call = openMensaAPI.getMeals(today);

		call.enqueue(new Callback<List<Meal>>() {
			@Override
			public void onResponse(Call<List<Meal>> call, Response<List<Meal>> response) {
				if (response.isSuccessful()) {
					logger.info("Success");
					List<Meal> meals = response.body();

					if (meals != null) {
						String[] meals_array;

						if (!filterTriggered.get()) {
							meals_array = meals.stream().map(Meal::toString).toArray(String[]::new);
						} else {
							meals_array = meals.stream().filter(Meal::isVegetarian).map(Meal::toString).toArray(String[]::new);
						}

						ListView lv = findViewById(R.id.list_menu);

						lv.setAdapter(new ArrayAdapter<>(
							MainActivity.this,
							R.layout.meal_entry,
							meals_array
						));
					} else {
						logger.info("Request Error :: No Body");
					}
				} else {
					logger.info("Request Error :: " + response.errorBody());
				}
			}

			@Override
			public void onFailure(Call<List<Meal>> call, Throwable t) {
				logger.info("Network Error :: " + t.getLocalizedMessage());
			}
		});
	}
}
