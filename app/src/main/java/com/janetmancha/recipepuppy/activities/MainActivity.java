package com.janetmancha.recipepuppy.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.janetmancha.recipepuppy.R;
import com.janetmancha.recipepuppy.model.Recipe;
import com.janetmancha.recipepuppy.adapters.RecipesAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    final Activity context = this;
    private ArrayList<Recipe> recipes = new ArrayList<>();
    ListView myListView;
    TextView textViewInit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewInit = findViewById(R.id.textViewInit);
        myListView = findViewById(R.id.listViewRecipes);
        myListView.setAdapter(new RecipesAdapter(context, R.layout.list_item, recipes));
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
                intent.putExtra("image",recipes.get(position).image);
                intent.putExtra("title",recipes.get(position).title);
                intent.putExtra("ingredients",recipes.get(position).ingredients);
                intent.putExtra("web",recipes.get(position).web);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.setQuery("", false);
                searchView.setIconified(true);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String searchText) {
                if (!searchText.equals("")){
                    recipes.clear();
                    makeRequest(searchText);
                }
                return true;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    void makeRequest(String searchText) {
        textViewInit.setVisibility(View.INVISIBLE);
        String url = "http://www.recipepuppy.com/api/?q=" + searchText;
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    receiveResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show();
                }

            });
        queue.add(stringRequest);
    }

    void receiveResponse (String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String tit, ing, ima, w;
            for (int i=0; i<jsonObject.getJSONArray("results").length(); i++) {
                tit = jsonObject.getJSONArray("results").getJSONObject(i).getString("title");
                ing = jsonObject.getJSONArray("results").getJSONObject(i).getString("ingredients");
                ima = jsonObject.getJSONArray("results").getJSONObject(i).getString("thumbnail");
                w = jsonObject.getJSONArray("results").getJSONObject(i).getString("href");
                recipes.add(new Recipe(tit,ing,ima,w));
            }
            myListView.setAdapter(new RecipesAdapter(context, R.layout.list_item, recipes));
        }catch (Exception e){
            Toast.makeText(this, "Parse Error", Toast.LENGTH_SHORT).show();
        }
    }
}
